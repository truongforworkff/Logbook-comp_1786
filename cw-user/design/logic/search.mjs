export function normalize(value) {
  return value.trim().toLocaleLowerCase("vi").replaceAll("đ", "d")
    .normalize("NFD").replace(/\p{M}+/gu, "");
}

export function findEvents(events, { query = "", date = "", month = "", category = "", savedOnly = false, saved = new Set() } = {}) {
  const keyword = normalize(query);
  return events.filter(event =>
    normalize(event.name).includes(keyword)
    && (!date || event.date === date)
    && (!month || event.date.startsWith(month))
    && (!category || event.categoryCode === category)
    && (!savedOnly || saved.has(event.id))
  ).sort((a, b) => a.date.localeCompare(b.date));
}

export function readSaved(raw, events) {
  try {
    const value = JSON.parse(raw ?? "[]");
    const valid = new Set(events.map(event => event.id));
    return new Set(Array.isArray(value) ? value.filter(id => valid.has(id)) : []);
  } catch { return new Set(); }
}
