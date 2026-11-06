using System.Text.Json;
using Eventa.User.Features.Agenda.Data;
using Eventa.User.Features.Agenda.Logic;
using Eventa.User.Features.Favourites.Logic;
using Eventa.User.Shared.Ui;

static void Check(bool pass, string name)
{
    if (!pass) throw new Exception("Failed: " + name);
}

var json = File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "events.json"));
var events = JsonSerializer.Deserialize<List<EventInfo>>(json,
    new JsonSerializerOptions { PropertyNameCaseInsensitive = true })!;
Check(events.Count == 5 && events.Sum(item => item.Sessions.Count) == 9, "admin snapshot");
Check(events.Select(item => item.Id).Distinct().Count() == events.Count, "stable unique event IDs");
Check(events.Any(item => item.Image.Length == 0), "optional image");
foreach (var item in events)
{
    Check(item.Name.Length > 0 && item.Date != default, "valid event");
    Check(item.Sessions.Select(session => session.Id).Distinct().Count() == item.Sessions.Count, "session IDs");
    Check(EventLabels.Categories.Any(category => category.Code == item.CategoryCode), "known category");
    Check(item.Sessions.All(session => TimeOnly.TryParseExact(session.Time, "HH:mm", out _)), "session start time");
    if (item.Image.Length > 0)
        Check(File.Exists(Path.Combine(AppContext.BaseDirectory, item.Image)), "offline cover bundled");
}
Check(EventSearch.Normalize(" ĐIỆN TOÁN ") == "dien toan", "Vietnamese normalisation");
Check(EventSearch.Filter(events, "AI VA DIEN").Count == 1, "accent and partial name");
Check(EventSearch.Filter(events, date:"2026-11-17").Single().Id == "ignite-2026", "exact date");
Check(EventSearch.Filter(events, "NVIDIA", date:"2026-11-17").Count == 0, "name AND date");
Check(EventSearch.Filter(events, category:"AI").Count == 3, "category");
Check(EventSearch.Filter(events, category:"Technology", month:"2026-11").Count == 0, "category AND month");
Check(EventSearch.Filter(events, "%_'").Count == 0, "literal special characters");
Check(EventSearch.Filter(events, "  ").Count == 5, "blank query");
Check(EventSearch.Filter(events)[0].Id == "ignite-2026", "upcoming first");
var oldCulture = System.Globalization.CultureInfo.CurrentCulture;
try
{
    System.Globalization.CultureInfo.CurrentCulture = System.Globalization.CultureInfo.GetCultureInfo("ar-SA");
    Check(EventSearch.Filter(events, date:"2026-11-17").Single().Id == "ignite-2026", "ISO dates independent of device calendar");
}
finally { System.Globalization.CultureInfo.CurrentCulture = oldCulture; }
var valid = events.Select(item => item.Id);
var saved = FavouriteCodec.Read("[\"ignite-2026\",\"unknown\",\"ignite-2026\"]", valid);
Check(saved.SetEquals(["ignite-2026"]), "deduplicate and remove unknown favourites");
Check(FavouriteCodec.Read("bad json", valid).Count == 0, "corrupt saved data");
Check(FavouriteCodec.Read("null", valid).Count == 0, "null saved data");
Check(FavouriteCodec.Read(FavouriteCodec.Write(saved), valid).SetEquals(saved), "favourites roundtrip");
Check(EventSearch.Filter(events, saved:saved).Single().Id == "ignite-2026", "saved list");
Check(EventSearch.Filter(events, saved:new HashSet<string>()).Count == 0, "empty favourites");
var state = new AgendaState { Events = events };
state.Navigate(AgendaRoute.Saved);
state.OpenEvent("ignite-2026");
state.OpenSession("ignite-2026-1");
Check(state.CurrentSession?.Name.StartsWith("Demo:") == true, "session belongs to event");
state.OpenSession("gtc-2026-1");
Check(state.SessionId == "ignite-2026-1", "reject foreign session");
state.OpenSheet("category");
Check(state.Back() && state.Sheet.Length == 0 && state.Route == AgendaRoute.Session, "back closes sheet first");
Check(state.Back() && state.Route == AgendaRoute.Event, "back to inline schedule");
Check(state.Back() && state.Route == AgendaRoute.Saved, "back retains origin");
Check(!state.Back(), "system can exit from main tabs");
Console.WriteLine("G/H checks passed: catalog, search, filters, favourites codec, navigation and offline covers.");
