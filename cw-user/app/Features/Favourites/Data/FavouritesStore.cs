using Eventa.User.Features.Favourites.Logic;

namespace Eventa.User.Features.Favourites.Data;

public sealed class FavouritesStore
{
    private const string Key = "eventa.favourites.v1";
    private HashSet<string> ids = new(StringComparer.Ordinal);
    public IReadOnlySet<string> Ids => ids;
    public bool Contains(string id) => ids.Contains(id);

    public void Load(IEnumerable<string> validIds) =>
        ids = FavouriteCodec.Read(Preferences.Default.Get(Key, "[]"), validIds);

    public bool Toggle(string id)
    {
        var next = ids.ToHashSet(StringComparer.Ordinal);
        var added = next.Add(id);
        if (!added) next.Remove(id);
        Preferences.Default.Set(Key, FavouriteCodec.Write(next));
        ids = next;
        return added;
    }
}
