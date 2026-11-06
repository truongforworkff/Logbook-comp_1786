using Eventa.User.Features.Agenda.Data;
using Eventa.User.Features.Agenda.Logic;
using Eventa.User.Features.Favourites.Data;
using Microsoft.Extensions.DependencyInjection;

namespace Eventa.User;

public static class MauiProgram
{
    public static MauiApp CreateMauiApp()
    {
        var builder = MauiApp.CreateBuilder().UseMauiApp<App>();
        builder.Services.AddMauiBlazorWebView();
        builder.Services.AddSingleton<CatalogRepository>();
        builder.Services.AddSingleton<FavouritesStore>();
        builder.Services.AddSingleton<AgendaState>();
        builder.Services.AddSingleton<MainPage>();
#if DEBUG
        builder.Services.AddBlazorWebViewDeveloperTools();
#endif
        return builder.Build();
    }
}
