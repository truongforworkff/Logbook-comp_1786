import assert from "node:assert/strict";
import { events } from "../data/events.mjs";
import { findEvents, readSaved, normalize } from "./search.mjs";

assert.equal(findEvents(events, {query:"MICROSOFT"}).length, 1);
assert.equal(findEvents(events, {query:"tu y tuong"})[0].id, "build");
assert.equal(findEvents(events, {date:"2026-11-17"})[0].id, "ignite");
assert.equal(findEvents(events, {date:"2026-11-17",query:"NVIDIA"}).length, 0);
assert.equal(findEvents(events, {month:"2026-12"}).length, 1);
assert.equal(findEvents(events, {savedOnly:true,saved:new Set(["gtc"])} )[0].id,"gtc");
assert.equal(findEvents(events, {savedOnly:true}).length,0);
assert.equal(findEvents(events, {query:"%_<script>"}).length,0);
assert.equal(normalize(" ĐIỆN TOÁN "), "dien toan");
assert.deepEqual([...readSaved('["ignite","unknown","ignite"]',events)],["ignite"]);
assert.equal(readSaved("invalid",events).size,0);
assert.equal(readSaved('{"ignite":true}',events).size,0);
assert.equal(new Set(events.map(event=>event.id)).size,events.length);
for (const event of events) {
  assert.match(event.date,/^\d{4}-\d{2}-\d{2}$/);
  assert.ok(event.sessions.length);
  assert.ok(event.sessions.every(session=>session.time<session.end));
}
console.log("Prototype checks passed: search, dates, favourites and catalog.");
