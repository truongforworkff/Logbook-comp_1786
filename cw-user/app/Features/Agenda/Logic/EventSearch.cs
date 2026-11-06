using System.Globalization;
using System.Text;
using Eventa.User.Features.Agenda.Data;

namespace Eventa.User.Features.Agenda.Logic;

public static class EventSearch
{
    public static string Normalize(string value) =>
        string.Concat(value.Trim().ToLowerInvariant().Replace('đ', 'd')
            .Normalize(NormalizationForm.FormD)
            .Where(character => CharUnicodeInfo.GetUnicodeCategory(character) != UnicodeCategory.NonSpacingMark));

    public static IReadOnlyList<EventInfo> Filter(IEnumerable<EventInfo> events, string query = "",
        string date = "", string month = "", string category = "", IReadOnlySet<string>? saved = null)
    {
        var keyword = Normalize(query);
        return events.Where(item => Normalize(item.Name).Contains(keyword)
                && (date.Length == 0 || item.Date.ToString("yyyy-MM-dd") == date)
                && (month.Length == 0 || item.Date.ToString("yyyy-MM").Equals(month))
                && (category.Length == 0 || item.CategoryCode == category)
                && (saved is null || saved.Contains(item.Id)))
            .OrderBy(item => item.Status == "Completed")
            .ThenBy(item => item.Date).ThenBy(item => item.Id, StringComparer.Ordinal).ToArray();
    }
}
