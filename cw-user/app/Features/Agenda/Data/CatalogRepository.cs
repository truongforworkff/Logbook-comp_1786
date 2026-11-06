using System.Text.Json;

namespace Eventa.User.Features.Agenda.Data;

public sealed class CatalogRepository
{
    public async Task<IReadOnlyList<EventInfo>> LoadAsync()
    {
        // shortcut: bundled admin snapshot; replace this source when cloud feature E exists.
        using var source = await FileSystem.OpenAppPackageFileAsync("events.json");
        var events = await JsonSerializer.DeserializeAsync<List<EventInfo>>(source,
            new JsonSerializerOptions { PropertyNameCaseInsensitive = true });
        return events ?? throw new InvalidDataException("Catalog is missing.");
    }
}
