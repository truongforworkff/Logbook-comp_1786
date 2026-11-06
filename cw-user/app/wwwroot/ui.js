window.eventaUi = {
  resetScroll: () => document.querySelector(".screen")?.scrollTo({top: 0}),
  focusSheet: () => document.querySelector(".sheet-close")?.focus()
};
document.addEventListener("keydown", event => {
  const sheet = document.querySelector('[role="dialog"]');
  if (!sheet || event.key !== "Tab") return;
  const controls = [...sheet.querySelectorAll("button,input")].filter(item => !item.disabled);
  const first = controls[0], last = controls.at(-1);
  if (event.shiftKey && document.activeElement === first) {
    event.preventDefault(); last?.focus();
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault(); first?.focus();
  }
});
