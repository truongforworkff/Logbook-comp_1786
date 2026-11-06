namespace Eventa.User.Features.Agenda.Data;

public sealed record EventInfo
{
    public string Id { get; init; } = "";
    public string Name { get; init; } = "";
    public DateOnly Date { get; init; }
    public string Location { get; init; } = "";
    public string Organiser { get; init; } = "";
    public string Type { get; init; } = "";
    public string Status { get; init; } = "";
    public string CategoryCode { get; init; } = "Other";
    public string Image { get; init; } = "";
    public string Description { get; init; } = "";
    public string Source { get; init; } = "";
    public List<SessionInfo> Sessions { get; init; } = [];
}

public sealed record SessionInfo
{
    public string Id { get; init; } = "";
    public string Name { get; init; } = "";
    public string Time { get; init; } = "";
    public string Speaker { get; init; } = "";
    public string Room { get; init; } = "";
    public string Type { get; init; } = "";
    public string Description { get; init; } = "";
}
