namespace Eventa.User;

public sealed class App(MainPage page) : Application
{
    protected override Window CreateWindow(IActivationState? activationState) =>
        new(page) { Title = "Eventa Explore" };
}
