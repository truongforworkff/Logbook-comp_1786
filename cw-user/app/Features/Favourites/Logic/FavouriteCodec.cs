using System.Text.Json;

namespace Eventa.User.Features.Favourites.Logic;

public static class FavouriteCodec
{
    public static HashSet<string> Read(string raw, IEnumerable<string> validIds)
    {
        try
        {
            var ids = JsonSerializer.Deserialize<string[]>(raw) ?? [];
            var valid = validIds.ToHashSet(StringComparer.Ordinal);
            return ids.Where(valid.Contains).ToHashSet(StringComparer.Ordinal);
        }
        catch (JsonException) { return new(StringComparer.Ordinal); }
    }

    public static string Write(IEnumerable<string> ids) =>
        JsonSerializer.Serialize(ids.Order(StringComparer.Ordinal));
}
