using Android.App;
using Android.Content.PM;
using Android.OS;
using AndroidX.Activity;
using Eventa.User.Features.Agenda.Logic;
using Microsoft.Extensions.DependencyInjection;

namespace Eventa.User;

[Activity(Theme = "@style/Maui.SplashTheme", MainLauncher = true,
    ConfigurationChanges = ConfigChanges.ScreenSize | ConfigChanges.Orientation
        | ConfigChanges.UiMode | ConfigChanges.ScreenLayout
        | ConfigChanges.SmallestScreenSize | ConfigChanges.Density)]
public class MainActivity : MauiAppCompatActivity
{
    private AgendaState? state;
    private AgendaBackCallback? back;

    protected override void OnCreate(Bundle? savedInstanceState)
    {
        base.OnCreate(savedInstanceState);
        state = IPlatformApplication.Current!.Services.GetRequiredService<AgendaState>();
        back = new AgendaBackCallback(state);
        OnBackPressedDispatcher.AddCallback(this, back);
        state.Changed += UpdateBackCallback;
        UpdateBackCallback();
    }

    private void UpdateBackCallback() => RunOnUiThread(() =>
    {
        if (back is not null) back.Enabled = state?.CanGoBack == true;
    });

    protected override void OnDestroy()
    {
        if (state is not null) state.Changed -= UpdateBackCallback;
        base.OnDestroy();
    }

    private sealed class AgendaBackCallback(AgendaState state) : OnBackPressedCallback(false)
    {
        public override void HandleOnBackPressed() { state.Back(); }
    }
}
