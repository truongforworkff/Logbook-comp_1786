using Eventa.User.Features.Agenda.Data;

namespace Eventa.User.Features.Agenda.Logic;

public enum AgendaRoute { Explore, Saved, Event, Session }

public sealed class AgendaState
{
    public IReadOnlyList<EventInfo> Events { get; set; } = [];
    public AgendaRoute Route { get; private set; } = AgendaRoute.Explore;
    public AgendaRoute Origin { get; private set; } = AgendaRoute.Explore;
    public string EventId { get; private set; } = "";
    public string SessionId { get; private set; } = "";
    public string Sheet { get; private set; } = "";
    public string Query { get; set; } = "";
    public string Date { get; set; } = "";
    public string Month { get; set; } = "";
    public string Category { get; set; } = "";
    public EventInfo? CurrentEvent => Events.FirstOrDefault(item => item.Id == EventId);
    public SessionInfo? CurrentSession => CurrentEvent?.Sessions.FirstOrDefault(item => item.Id == SessionId);
    public bool IsFiltered => Query.Length + Date.Length + Month.Length + Category.Length > 0;
    public event Action? Changed;
    public void Notify() => Changed?.Invoke();

    public void Navigate(AgendaRoute route)
    {
        Route = route;
        ClearFilters();
    }

    public void ClearFilters()
    {
        Query = Date = Month = Category = "";
        Notify();
    }

    public void OpenEvent(string id)
    {
        if (!Events.Any(item => item.Id == id)) return;
        if (Route is AgendaRoute.Explore or AgendaRoute.Saved) Origin = Route;
        EventId = id;
        Route = AgendaRoute.Event;
        Notify();
    }

    public void OpenSession(string id)
    {
        if (CurrentEvent?.Sessions.Any(item => item.Id == id) != true) return;
        SessionId = id;
        Route = AgendaRoute.Session;
        Notify();
    }

    public void OpenSheet(string sheet) { Sheet = sheet; Notify(); }
    public void CloseSheet() { Sheet = ""; Notify(); }

    public bool Back()
    {
        if (Sheet.Length > 0) CloseSheet();
        else if (Route == AgendaRoute.Session) { Route = AgendaRoute.Event; Notify(); }
        else if (Route == AgendaRoute.Event) { Route = Origin; Notify(); }
        else return false;
        return true;
    }
}
