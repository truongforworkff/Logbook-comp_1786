using Eventa.User.Features.Agenda.Data;
using Eventa.User.Features.Agenda.Logic;
using Eventa.User.Features.Favourites.Data;
using Microsoft.AspNetCore.Components;
using Microsoft.JSInterop;

namespace Eventa.User.Features.Agenda.Ui;

public partial class AgendaPage
{
    [Inject] public AgendaState State { get; set; } = default!;
    [Inject] public CatalogRepository Catalog { get; set; } = default!;
    [Inject] public FavouritesStore Favourites { get; set; } = default!;
    [Inject] public IJSRuntime JS { get; set; } = default!;
    private bool loading = true;
    private string error = "", notice = "", lastLocation = "", lastSheet = "";
    private AgendaRoute ActiveTab => State.Route is AgendaRoute.Explore or AgendaRoute.Saved ? State.Route : State.Origin;

    protected override async Task OnInitializedAsync()
    {
        State.Changed += Refresh;
        await LoadCatalog();
    }

    private async Task LoadCatalog()
    {
        loading = true;
        error = "";
        try
        {
            State.Events = await Catalog.LoadAsync();
            Favourites.Load(State.Events.Select(item => item.Id));
        }
        catch (Exception)
        {
            error = "Không đọc được dữ liệu trên thiết bị. Hãy thử mở lại danh sách.";
        }
        finally { loading = false; }
    }

    private void ToggleFavourite(string id)
    {
        if (!State.Events.Any(item => item.Id == id)) return;
        try
        {
            notice = Favourites.Toggle(id) ? "Đã lưu vào Yêu thích" : "Đã bỏ khỏi Yêu thích";
        }
        catch (Exception)
        {
            notice = "Chưa lưu được thay đổi. Hãy thử lại.";
        }
        State.Notify();
    }

    private void GoBack() { State.Back(); notice = ""; }
    private void Refresh() => _ = InvokeAsync(StateHasChanged);

    protected override async Task OnAfterRenderAsync(bool firstRender)
    {
        var location = $"{State.Route}/{State.EventId}/{State.SessionId}";
        if (location != lastLocation)
        {
            lastLocation = location;
            await JS.InvokeVoidAsync("eventaUi.resetScroll");
        }
        if (State.Sheet.Length > 0 && State.Sheet != lastSheet)
            await JS.InvokeVoidAsync("eventaUi.focusSheet");
        lastSheet = State.Sheet;
    }

    public void Dispose() => State.Changed -= Refresh;
}
