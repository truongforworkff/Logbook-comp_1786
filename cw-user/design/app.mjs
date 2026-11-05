import { events } from "./data/events.mjs";
import { findEvents, readSaved } from "./logic/search.mjs";

const paths = {
  calendar:'<rect x="3" y="5" width="18" height="16" rx="3"/><path d="M7 3v4m10-4v4M3 11h18"/>',
  heart:'<path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0l-1 1-1-1a5.5 5.5 0 0 0-7.8 7.8L12 21l8.8-8.6a5.5 5.5 0 0 0 0-7.8Z"/>',
  search:'<circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/>',
  pin:'<path d="M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="2.5"/>',
  clock:'<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
  arrow:'<path d="M5 12h14m-5-5 5 5-5 5"/>',
  back:'<path d="M19 12H5m5-5-5 5 5 5"/>',
  chevron:'<path d="m9 5 7 7-7 7"/>',
  people:'<circle cx="9" cy="8" r="3"/><path d="M3 20v-2a6 6 0 0 1 12 0v2m2-15a3 3 0 0 1 0 6m1 4a5 5 0 0 1 3 5"/>',
  sparkle:'<path d="m12 2 2.6 7.4L22 12l-7.4 2.6L12 22l-2.6-7.4L2 12l7.4-2.6Z"/>'
};
const icon = name => '<svg class="icon" viewBox="0 0 24 24" aria-hidden="true">'+paths[name]+'</svg>';
const escapeHtml = value => String(value).replace(/[&<>"']/g, char =>
  ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char]));
const storageKey = "eventa.user.design.favourites.v1";
function loadSaved() {
  try { return readSaved(localStorage.getItem(storageKey), events); }
  catch { return new Set(); }
}
const categories = [
  ["", "Tất cả chủ đề"], ["AI", "AI & Dữ liệu"], ["Technology", "Công nghệ"],
  ["Design", "Thiết kế"], ["Business", "Kinh doanh"], ["Education", "Giáo dục"],
  ["Community", "Cộng đồng"], ["Other", "Khác"]
];
const categoryDialog = document.querySelector("#category-dialog");
const categoryLabel = code => categories.find(([value]) => value === code)?.[1] ?? "Tất cả chủ đề";
const state = { route:"explore", origin:"explore", eventId:events[0].id, sessionId:"",
  query:"", date:"", month:"", category:"", saved:loadSaved() };
const screen = document.querySelector("#screen");
const dateDialog = document.querySelector("#date-dialog");
let toastTimer;

function formatDate(date, long = false) {
  return new Intl.DateTimeFormat("vi-VN", long
    ? {weekday:"long",day:"numeric",month:"long",year:"numeric"}
    : {day:"2-digit",month:"2-digit"}).format(new Date(date+"T12:00:00"));
}
function currentEvent() { return events.find(event => event.id === state.eventId) ?? events[0]; }

function favoriteButton(event) {
  const saved = state.saved.has(event.id);
  return '<button class="favorite-button '+(saved?"saved":"")+'" data-action="favorite" data-id="'+event.id+
    '" aria-pressed="'+saved+'" aria-label="'+(saved?"Bỏ yêu thích: ":"Lưu yêu thích: ")+escapeHtml(event.name)+'">'+icon("heart")+'</button>';
}
function topbar(event) {
  return '<header class="topbar">'+(event
    ? '<button class="back-button" data-action="back">'+icon("back")+'<span>'+ (state.route==="session"?"Chi tiết sự kiện":"Quay lại")+'</span></button>'+favoriteButton(event)
    : '<div class="logo-lockup"><span class="brand-icon">'+icon("sparkle")+'</span><strong>Explore Agenda</strong></div><span class="status-pill"><i class="status-dot"></i>Bản mẫu</span><span class="avatar" aria-hidden="true">EA</span>')+'</header>';
}

function card(event) {
  return '<article class="event-card" data-action="event" data-id="'+event.id+'" role="button" tabindex="0" aria-label="Xem sự kiện: '+escapeHtml(event.name)+'">'+
    '<div class="cover '+(event.image?'':'no-photo')+'">'+(event.image?'<img src="'+escapeHtml(event.image)+'" alt="'+escapeHtml(event.alt)+'" loading="lazy">':'')+
    '<div class="cover-badges"><span class="category '+event.categoryClass+'">'+escapeHtml(event.category)+'</span>'+favoriteButton(event)+'</div>'+
    '<span class="date-tag">'+icon("clock")+formatDate(event.date)+' · '+event.time+' – '+event.endTime+'</span></div>'+
    '<div class="card-body"><h3 class="card-title">'+escapeHtml(event.name)+'</h3><p class="card-description">'+escapeHtml(event.description)+'</p>'+
    '<div class="venue-block"><div class="venue-line">'+icon("pin")+escapeHtml(event.location)+'</div><div class="venue-line secondary">'+icon("people")+escapeHtml(event.organiser)+'</div></div>'+
    '<div class="card-footer"><span class="sessions-label">'+event.sessions.length+' session</span><span class="small-primary">Xem lịch trình '+icon("arrow")+'</span></div></div></article>';
}
function filters() {
  const chips = [["","Tất cả"],["2026-11","Tháng 11"],["2026-12","Tháng 12"]];
  return '<div class="searchbar">'+icon("search")+'<input id="query" aria-label="Tìm sự kiện theo tên" placeholder="'+
    (state.route==="saved"?"Tìm trong sự kiện đã lưu…":"Tìm sự kiện bạn quan tâm…")+'"><button class="clear-search" data-action="clear-search" aria-label="Xóa từ khóa" '+(!state.query?'hidden':'')+'>×</button></div>'+
    '<div class="filter-row">'+chips.map(([month,label])=>'<button data-action="month" data-month="'+month+'" class="filter-chip '+(state.month===month&&!state.date?"active":"")+'" aria-pressed="'+(state.month===month&&!state.date)+'">'+label+'</button>').join("")+
    '<button class="filter-chip '+(state.date?"active":"")+'" data-action="date" aria-label="Chọn ngày diễn ra">'+icon("calendar")+(state.date?formatDate(state.date):"Chọn ngày")+'</button></div><button class="category-filter filter-chip '+(state.category?"active":"")+'" data-action="category" aria-label="Lọc theo chủ đề">'+categoryLabel(state.category)+'<span aria-hidden="true">⌄</span></button>';
}
function emptyState(savedEmpty) {
  const filtered = Boolean(state.query || state.date || state.month || state.category);
  return '<div class="empty-state"><div class="empty-icon">'+icon(savedEmpty?"heart":"search")+'</div><h2>'+
    (savedEmpty?"Dành một chỗ cho điều bạn thích":filtered?"Chưa tìm thấy sự kiện phù hợp":"Sự kiện mới đang chờ bạn")+
    '</h2><p>'+(savedEmpty?"Chạm vào trái tim ở sự kiện bạn quan tâm. Bạn sẽ tìm lại chúng thật nhanh ở đây.":"Thử một tên khác hoặc chọn ngày khác để tiếp tục khám phá.")+
    '</p><button class="primary-button secondary" data-action="'+(savedEmpty?"browse":"clear-filters")+'">'+
    (savedEmpty?"Khám phá sự kiện":"Xóa bộ lọc")+' '+icon("arrow")+'</button></div>';
}
function renderBrowse() {
  const savedOnly = state.route==="saved";
  const noSaved = savedOnly && state.saved.size===0;
  screen.innerHTML = topbar()+'<main class="content">'+(savedOnly
    ? '<div class="saved-intro"><div class="saved-emblem">'+icon("heart")+'</div><div><h1>Điều bạn quan tâm</h1><p>Những sự kiện bạn muốn dành thời gian.</p></div></div>'
    : '<h1 class="page-heading">Khám phá sự kiện</h1><p class="subheading">Những cuộc gặp mở ra ý tưởng mới.</p><div class="preview-banner">'+icon("sparkle")+'<div><strong>Một điều mới cho ngày sắp tới</strong><small>AI, công nghệ và những trải nghiệm sáng tạo.</small></div><span class="tiny-mark">↗</span></div>')+
    (noSaved?"":filters())+'<div class="section-heading"><h2 id="results-title">'+(savedOnly?"Đã lưu yêu thích":"Sự kiện nổi bật")+'</h2><span id="results-count"></span></div><div id="results" class="event-list"></div></main>';
  const query = screen.querySelector("#query");
  if (query) query.value = state.query;
  updateResults();
}
function updateResults() {
  const savedOnly = state.route==="saved";
  const results = findEvents(events, {query:state.query,date:state.date,month:state.month,category:state.category,savedOnly,saved:state.saved});
  const filtered = Boolean(state.query || state.date || state.month || state.category);
  screen.querySelector("#results-title").textContent = filtered?"Kết quả tìm kiếm":savedOnly?"Đã lưu yêu thích":"Sự kiện nổi bật";
  screen.querySelector("#results-count").textContent = results.length+" sự kiện";
  screen.querySelector("#results").innerHTML = results.length
    ? results.map(card).join("") : emptyState(savedOnly&&state.saved.size===0);
  const clear = screen.querySelector(".clear-search");
  if (clear) clear.hidden = !state.query;
}

function metaRow(name, title, subtitle) {
  return '<div class="meta-row"><span class="meta-icon">'+icon(name)+'</span><div><strong>'+escapeHtml(title)+'</strong>'+(subtitle?'<small>'+escapeHtml(subtitle)+'</small>':"")+'</div></div>';
}
function schedule(event) {
  return '<section class="content-section"><div class="agenda-heading"><h2>Lịch trình</h2><span class="count-pill">'+event.sessions.length+' session</span></div><div class="timeline">'+event.sessions.map(session =>
    '<article class="timeline-item" role="button" tabindex="0" data-action="session" data-id="'+session.id+'" aria-label="Xem phiên: '+escapeHtml(session.name)+'"><div><time>'+session.time+' – '+session.end+'</time><h3>'+escapeHtml(session.name)+'</h3><p>'+escapeHtml(session.type+' · '+(session.room||session.speaker))+'</p></div><span class="chevron">'+icon("chevron")+'</span></article>'
  ).join("")+'</div></section>';
}
function renderEvent() {
  const event = currentEvent();
  screen.innerHTML = topbar(event)+'<main class="content">'+(event.image?'<div class="detail-cover"><img src="'+escapeHtml(event.image)+'" alt="'+escapeHtml(event.alt)+'"></div>':'')+
    '<span class="category '+event.categoryClass+'">'+escapeHtml(event.category)+'</span><h1 class="detail-title">'+escapeHtml(event.name)+'</h1>'+
    '<div class="meta-card">'+metaRow("calendar",formatDate(event.date,true),event.time+" – "+event.endTime+" · Giờ minh họa")+
    metaRow("pin",event.location,event.organiser)+'</div>'+
    '<section class="content-section"><h2>Về sự kiện</h2><p class="body-copy">'+escapeHtml(event.description)+'</p></section>'+
    schedule(event)+'<button class="primary-button '+(state.saved.has(event.id)?"secondary":"")+'" data-action="favorite" data-id="'+event.id+'">'+
    icon("heart")+(state.saved.has(event.id)?"Đã lưu yêu thích":"Lưu sự kiện này")+'</button><p class="demo-note">'+escapeHtml(event.note)+'</p></main>';
}
function renderSession() {
  const event = currentEvent();
  const session = event.sessions.find(item=>item.id===state.sessionId) ?? event.sessions[0];
  screen.innerHTML = topbar(event)+'<main class="content"><div class="session-hero"><span class="category '+event.categoryClass+'">'+escapeHtml(session.type)+'</span><h1>'+escapeHtml(session.name)+'</h1><p class="event-ref">'+escapeHtml(event.name)+'</p><div class="time-block">'+icon("clock")+session.time+' – '+session.end+'</div></div>'+
    '<div class="meta-card">'+metaRow("calendar",formatDate(event.date,true),"Lịch minh họa")+metaRow("pin",session.room||"Chưa công bố phòng",event.location)+
    metaRow("people",session.speaker||"Chưa công bố diễn giả","")+'</div>'+
    '<section class="content-section"><h2>Bạn sẽ khám phá điều gì?</h2><p class="body-copy">'+escapeHtml(session.description)+'</p></section>'+
    '<section class="content-section"><button class="primary-button secondary" data-action="back">'+icon("back")+'Trở lại lịch trình</button></section></main>';
}

function render(preserveScroll = false) {
  const top = preserveScroll?screen.scrollTop:0;
  if (state.route==="explore" || state.route==="saved") renderBrowse();
  else if (state.route==="detail") renderEvent();
  else renderSession();
  const active = ["detail","session"].includes(state.route)?state.origin:state.route;
  for (const route of ["explore","saved"]) {
    const button = document.querySelector("#nav-"+route);
    button.classList.toggle("active",active===route);
    if (active===route) button.setAttribute("aria-current","page"); else button.removeAttribute("aria-current");
  }
  const count = document.querySelector("#saved-count");
  count.hidden = state.saved.size===0;
  count.textContent = state.saved.size;
  screen.scrollTop = top;
}
function navigate(route) {
  state.route = route;
  state.query = ""; state.month = ""; state.date = ""; state.category = "";
  render();
}
function toggleFavorite(id) {
  if (!events.some(event=>event.id===id)) return;
  const added = !state.saved.has(id);
  if (added) state.saved.add(id); else state.saved.delete(id);
  try { localStorage.setItem(storageKey, JSON.stringify([...state.saved])); }
  catch { showToast("Đã cập nhật trong lần xem này; trình duyệt chưa cho phép lưu lâu dài."); render(true); return; }
  render(true);
  showToast(added?"Đã lưu vào Yêu thích":"Đã bỏ khỏi Yêu thích");
}
function showToast(message) {
  clearTimeout(toastTimer);
  const toast = document.querySelector("#toast");
  toast.textContent = message; toast.hidden = false;
  toastTimer = setTimeout(()=>{toast.hidden=true;},2600);
}
function openEvent(id) {
  state.origin = ["explore","saved"].includes(state.route)?state.route:state.origin;
  state.eventId = id; state.route = "detail"; render();
}
function openDate() {
  document.querySelector("#exact-date").value = state.date;
  dateDialog.showModal();
}
function back() {
  state.route = state.route==="session"?"detail":state.origin;
  render();
}
function clearFilters() {
  state.query="";state.date="";state.month="";state.category="";render();
}

document.addEventListener("click", event => {
  const button = event.target.closest("[data-action]");
  if (!button) return;
  const action = button.dataset.action;
  event.preventDefault();
  if (action==="nav") navigate(button.dataset.view);
  else if (action==="event") openEvent(button.dataset.id);
  else if (action==="favorite") toggleFavorite(button.dataset.id);
  else if (action==="session") {state.sessionId=button.dataset.id;state.route="session";render();}
  else if (action==="back") back();
  else if (action==="browse") navigate("explore");
  else if (action==="month") {state.month=button.dataset.month;state.date="";render();}
  else if (action==="category") {
    categoryDialog.querySelector("#category-options").innerHTML = categories.map(([code,label]) =>
      '<button class="category-option '+(state.category===code?'selected':'')+'" data-action="select-category" data-category="'+code+'" aria-pressed="'+(state.category===code)+'">'+label+'<span aria-hidden="true">'+(state.category===code?'✓':'')+'</span></button>'
    ).join("");
    categoryDialog.showModal();
  }
  else if (action==="select-category") {state.category=button.dataset.category;categoryDialog.close();render();}
  else if (action==="close-category") categoryDialog.close();
  else if (action==="date") openDate();
  else if (action==="close-date") dateDialog.close();
  else if (action==="clear-date") {state.date="";state.month="";dateDialog.close();render();}
  else if (action==="clear-filters") clearFilters();
  else if (action==="clear-search") {
    state.query="";const input=screen.querySelector("#query");input.value="";updateResults();input.focus();
  } else if (action==="preview-event") openEvent(events[0].id);
  else if (action==="preview-session") {
    state.eventId=events[0].id;state.sessionId=events[0].sessions[0].id;state.route="session";render();
  }
});
document.addEventListener("input", event => {
  if (event.target.id==="query") {state.query=event.target.value;updateResults();}
});
document.addEventListener("keydown", event => {
  if (["Enter"," "].includes(event.key) && event.target.matches("[role=button]")) {
    event.preventDefault();event.target.click();
  }
});
document.querySelector("#date-form").addEventListener("submit", event => {
  event.preventDefault();state.date=document.querySelector("#exact-date").value;state.month="";dateDialog.close();render();
});
document.querySelector("#explore-icon").innerHTML=icon("calendar");
document.querySelector("#saved-icon").innerHTML=icon("heart");
render();

document.addEventListener("error", event => {
  if (!(event.target instanceof HTMLImageElement)) return;
  const cover = event.target.closest(".cover,.detail-cover");
  if (!cover) return;
  event.target.remove();
  if (cover.matches(".detail-cover")) cover.hidden = true;
  else cover.classList.add("no-photo");
}, true);
