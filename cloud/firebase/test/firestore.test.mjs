import { before, after, beforeEach, test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { initializeTestEnvironment, assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc, deleteDoc, getDocs, collection, serverTimestamp } from 'firebase/firestore';

let env;
const uid = 'approved-admin';
const sourceId = '591a501e-6b26-47c7-bfe4-3410ff637653';
const data = (revision = 1) => ({
  schemaVersion: 1, sourceId, revision, publisherUid: uid, events: [], updatedAt: serverTimestamp(),
});
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
    await setDoc(doc(context.firestore(), 'admins', uid), { enabled: true });
  });
});
test('approved publisher creates an atomic catalog that anonymous visitors can read', async () => {
  const admin = env.authenticatedContext(uid).firestore();
  await assertSucceeds(setDoc(doc(admin, 'catalogues/main'), data()));
  const visitor = env.unauthenticatedContext().firestore();
  const snapshot = await assertSucceeds(getDoc(doc(visitor, 'catalogues/main')));
  assert.equal(snapshot.data().revision, 1);
  await assertFails(setDoc(doc(visitor, 'catalogues/main'), data(2)));
  await assertFails(getDocs(collection(visitor, 'catalogues')));
});
test('sign-in alone never grants publication or permission escalation', async () => {
  const stranger = env.authenticatedContext('stranger').firestore();
  await assertFails(setDoc(doc(stranger, 'catalogues/main'), { ...data(), publisherUid: 'stranger' }));
  await assertFails(setDoc(doc(stranger, 'admins/stranger'), { enabled: true }));
  await assertFails(getDoc(doc(stranger, 'admins', uid)));
  await assertSucceeds(getDoc(doc(stranger, 'admins/stranger')));
});
test('stale or foreign snapshots cannot replace newer data', async () => {
  const admin = env.authenticatedContext(uid).firestore();
  const target = doc(admin, 'catalogues/main');
  await assertSucceeds(setDoc(target, data(5)));
  await assertFails(setDoc(target, data(4)));
  await assertFails(setDoc(target, data(5)));
  await assertFails(setDoc(target, { ...data(6), sourceId: 'different-source-that-is-36-chars-long' }));
  await assertFails(setDoc(target, { ...data(6), publisherUid: 'another-user' }));
  await assertSucceeds(setDoc(target, data(6)));
  await assertFails(deleteDoc(target));
});
test('schema, timestamp, size and unrelated paths are enforced', async () => {
  const admin = env.authenticatedContext(uid).firestore();
  for (const patch of [
    { revision: -1 }, { revision: 1.5 }, { schemaVersion: 2 }, { events: {} },
    { events: Array.from({ length: 101 }, () => ({})) }, { updatedAt: 'fake' },
    { sourceId: '' }, { privileged: true },
  ]) await assertFails(setDoc(doc(admin, 'catalogues/main'), { ...data(), ...patch }));
  await assertFails(getDoc(doc(admin, 'setup_checks/probe')));
  await assertFails(setDoc(doc(admin, 'events/anything'), { name: 'outside approved catalog' }));
});
test('revoking an admin blocks later writes', async () => {
  const admin = env.authenticatedContext(uid).firestore();
  await assertSucceeds(setDoc(doc(admin, 'catalogues/main'), data()));
  await env.withSecurityRulesDisabled(async context => {
    await setDoc(doc(context.firestore(), 'admins', uid), { enabled: false });
  });
  await assertFails(setDoc(doc(admin, 'catalogues/main'), data(2)));
});
