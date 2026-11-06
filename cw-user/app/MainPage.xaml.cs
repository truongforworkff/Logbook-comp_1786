using Eventa.User.Features.Agenda.Logic;

namespace Eventa.User;

public partial class MainPage : ContentPage
{
    private readonly AgendaState state;

    public MainPage(AgendaState state)
    {
        InitializeComponent();
        this.state = state;
    }

    protected override bool OnBackButtonPressed() => state.Back() || base.OnBackButtonPressed();
}
