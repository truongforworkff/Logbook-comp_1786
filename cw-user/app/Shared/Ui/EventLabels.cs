using System.Globalization;

namespace Eventa.User.Shared.Ui;

public static class EventLabels
{
    public static readonly (string Code, string Label)[] Categories =
    [
        ("", "Tất cả chủ đề"), ("AI", "AI & Dữ liệu"), ("Technology", "Công nghệ"),
        ("Design", "Thiết kế"), ("Business", "Kinh doanh"), ("Education", "Giáo dục"),
        ("Community", "Cộng đồng"), ("Other", "Khác")
    ];
    public static string Category(string code) => Categories.FirstOrDefault(item => item.Code == code).Label ?? "Khác";
    public static string Date(DateOnly date, bool full = false) =>
        date.ToString(full ? "dddd, dd MMMM yyyy" : "dd/MM", CultureInfo.GetCultureInfo("vi-VN"));
}
