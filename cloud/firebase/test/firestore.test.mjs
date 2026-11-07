import { before, after, beforeEach, test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { initializeTestEnvironment, assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc, deleteDoc, getDocs, collection, serverTimestamp, writeBatch } from 'firebase/firestore';

let env;
const writeKey = '1'.repeat(64);
const sourceId = '591a501e-6b26-47c7-bfe4-3410ff637653';
const data = (revision = 1) => ({
  schemaVersion: 1, sourceId, revision, events: [], updatedAt: serverTimestamp(),
});
const receipt = (catalog, key = writeKey) => ({
  writeKey: key, sourceId: catalog.sourceId, revision: catalog.revision, updatedAt: serverTimestamp(),
});
const publish = (db, catalog = data(), key = writeKey) => {
  const batch = writeBatch(db);
  batch.set(doc(db, 'sync_writes/current'), receipt(catalog, key));
  batch.set(doc(db, 'catalogues/main'), catalog);
  return batch.commit();
};
before(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-eventa',
    firestore: { host: '127.0.0.1', port: 8086, rules: readFileSync('firestore.rules', 'utf8') },
  });
});
after(async () => env?.cleanup());
beforeEach(async () => {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async context => {
    await setDoc(doc(context.firestore(), 'sync_config/publisher'), { writeKey });
  });
});
test('Admin sync needs no user account; public catalog contains no credential', async () => {
  const db = env.unauthenticatedContext().firestore();
  await assertSucceeds(publish(db));
  const snapshot = await assertSucceeds(getDoc(doc(db, 'catalogues/main')));
  assert.equal(snapshot.data().revision, 1);
  assert.equal(snapshot.data().writeKey, undefined);
  await assertFails(getDocs(collection(db, 'catalogues')));
  await assertFails(getDoc(doc(db, 'sync_writes/current')));
  await assertFails(getDoc(doc(db, 'sync_config/publisher')));
});
test('missing or wrong private configuration cannot write, even with an old signed-in account', async () => {
  const visitor = env.unauthenticatedContext().firestore();
  await assertFails(setDoc(doc(visitor, 'catalogues/main'), data()));
  await assertFails(publish(visitor, data(), '2'.repeat(64)));
  const oldUser = env.authenticatedContext('former-user').firestore();
  await assertFails(setDoc(doc(oldUser, 'catalogues/main'), data()));
  await assertFails(setDoc(doc(oldUser, 'admins/former-user'), { enabled: true }));
  await assertFails(setDoc(doc(oldUser, 'sync_config/publisher'), { writeKey: '2'.repeat(64) }));
});
test('receipt must be fresh, match the catalog and commit atomically', async () => {
  const db = env.unauthenticatedContext().firestore();
  await assertFails(setDoc(doc(db, 'sync_writes/current'), receipt(data())));
  await assertSucceeds(publish(db));
  await assertFails(setDoc(doc(db, 'catalogues/main'), data(2)));
  for (const patch of [{ revision: 3 }, { sourceId: 'different-source-36-characters-long-x' }, { updatedAt: 'fake' }]) {
    const batch = writeBatch(db);
    batch.set(doc(db, 'sync_writes/current'), { ...receipt(data(2)), ...patch });
    batch.set(doc(db, 'catalogues/main'), data(2));
    await assertFails(batch.commit());
  }
  assert.equal((await getDoc(doc(db, 'catalogues/main'))).data().revision, 1);
});
test('stale snapshots, other local databases and deletion remain blocked', async () => {
  const db = env.unauthenticatedContext().firestore();
  await assertSucceeds(publish(db, data(5)));
  await assertFails(publish(db, data(4)));
  await assertFails(publish(db, data(5)));
  await assertFails(publish(db, { ...data(6), sourceId: 'different-source-36-characters-long-x' }));
  await assertSucceeds(publish(db, data(6)));
  await assertFails(deleteDoc(doc(db, 'catalogues/main')));
  await assertFails(deleteDoc(doc(db, 'sync_writes/current')));
});
test('schema, size and unrelated paths are enforced', async () => {
  const db = env.unauthenticatedContext().firestore();
  for (const patch of [
    { revision: -1 }, { revision: 1.5 }, { schemaVersion: 2 }, { events: {} },
    { events: Array.from({ length: 101 }, () => ({})) }, { updatedAt: 'fake' },
    { sourceId: '' }, { privileged: true },
  ]) await assertFails(publish(db, { ...data(), ...patch }));
  await assertFails(getDoc(doc(db, 'setup_checks/probe')));
  await assertFails(setDoc(doc(db, 'events/anything'), { name: 'outside catalog' }));
});
test('rotating the private build configuration blocks older Admin builds', async () => {
  const db = env.unauthenticatedContext().firestore();
  await assertSucceeds(publish(db));
  await env.withSecurityRulesDisabled(async context => {
    await setDoc(doc(context.firestore(), 'sync_config/publisher'), { writeKey: '2'.repeat(64) });
  });
  await assertFails(publish(db, data(2)));
  await assertSucceeds(publish(db, data(2), '2'.repeat(64)));
});
