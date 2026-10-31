package com.comp1786.room;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.comp1786.room.databinding.ActivityMainBinding;
import com.comp1786.room.databinding.DialogEventBinding;
import com.comp1786.room.databinding.DialogDetailsBinding;
import com.comp1786.room.databinding.DialogPopupBinding;
import com.comp1786.room.databinding.NoticeBannerBinding;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private EventViewModel model;
    private AlertDialog editor;
    private DialogEventBinding editorFields;
    private Event editingEvent;
    private EventAdapter adapter;
    private View notice;
    private boolean deleting;
    private final android.os.Handler noticeHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private java.util.List<Event> allEvents = java.util.Collections.emptyList();
    private String dateFilter = "", organiserFilter = "";
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        styleSystemBars(getWindow());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int padding = 0;
            view.setPadding(padding + bars.left, padding + bars.top, padding + bars.right, padding + bars.bottom);
            return insets;
        });
        model = new ViewModelProvider(this).get(EventViewModel.class);
        adapter = new EventAdapter(this::showDetails);
        if (state != null) { dateFilter = state.getString("filterDate", ""); organiserFilter = state.getString("filterOrganiser", ""); }
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(adapter);
        binding.add.setOnClickListener(view -> showEditor(null, null));
        model.getEvents().observe(this, events -> {
            allEvents = events == null ? java.util.Collections.emptyList() : events;
            renderEvents();
        });
        binding.search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) { renderEvents(); }
            public void afterTextChanged(android.text.Editable value) {}
        });
        binding.filter.setOnClickListener(view -> showFilters());

        if (state != null && state.getBoolean("editorOpen")) {
            Event draft = new Event(state.getLong("eventId"), state.getString("name", ""),
                    state.getString("date", ""), state.getString("location", ""),
                    state.getString("organiser", ""), state.getString("type", ""), state.getString("status", "Upcoming"));
            draft.description = state.getString("description", "");
            draft.contactEmail = state.getString("email", "");
            draft.contactPhone = state.getString("phone", "");
            showEditor(draft.id == 0 ? null : draft, draft);
        }
        model.getOperation().observe(this, operation -> {
            if (operation == EventViewModel.Operation.DUPLICATE) {
                showNotice(getString(R.string.duplicate_event),true);
                if (editorFields != null) { editorFields.reviewAction.setEnabled(true); editorFields.deleteAction.setEnabled(true); }
                model.consumeOperation();
            } else if (operation == EventViewModel.Operation.ERROR) {
                showNotice(getString(R.string.storage_error),true);
                if (editorFields != null) editorFields.reviewAction.setEnabled(true);
                model.consumeOperation();
            } else if (operation == EventViewModel.Operation.SUCCESS) {
                if (editor != null) editor.dismiss();
                showNotice(getString(deleting ? R.string.deleted_notice : R.string.saved_notice),false);
                deleting = false;
                model.consumeOperation();
            }
        });

    }
    private void renderEvents() {
        String query = binding.search.getText().toString().trim().toLowerCase(Locale.ROOT);
        java.util.List<Event> shown = new java.util.ArrayList<>();
        for (Event event : allEvents) {
            if (event.name.toLowerCase(Locale.ROOT).contains(query)
                && (dateFilter.isEmpty() || event.date.equals(dateFilter))
                && event.organiser.toLowerCase(Locale.ROOT).contains(organiserFilter.toLowerCase(Locale.ROOT))) shown.add(event);
        }
        adapter.submitList(shown);
        boolean filtered = !query.isEmpty() || !dateFilter.isEmpty() || !organiserFilter.isEmpty();
        binding.listTitle.setText(filtered ? R.string.filtered_section : R.string.events_section);
        binding.count.setText(getString(R.string.event_count,shown.size()));
        binding.empty.setText(filtered ? R.string.empty_search : R.string.empty);
        binding.empty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }
    private void showFilters() {
        DialogPopupBinding panel = DialogPopupBinding.inflate(getLayoutInflater());
        panel.title.setText(R.string.filter_title); panel.subtitle.setText(R.string.filter_subtitle);
        EditText date = filterField(panel.body,getString(R.string.filter_date),dateFilter.isEmpty() ? "" : EventValidation.displayDate(dateFilter));
        date.setHint(R.string.date_hint); date.setInputType(android.text.InputType.TYPE_CLASS_DATETIME | android.text.InputType.TYPE_DATETIME_VARIATION_DATE);
        EditText organiser = filterField(panel.body,getString(R.string.filter_organiser),organiserFilter);
        panel.primary.setText(R.string.apply_filter); panel.secondary.setText(R.string.cancel);
        panel.tertiary.setText(R.string.clear_filter); panel.tertiary.setVisibility(View.VISIBLE);
        AlertDialog filter = openPopup(panel);
        panel.primary.setOnClickListener(view -> {
            String entered = date.getText().toString().trim();
            if (!inlineValidation(date,entered.isEmpty() || EventValidation.validDate(entered),R.string.invalid_date)) { date.requestFocus(); return; }
            dateFilter = entered.isEmpty() ? "" : EventValidation.storageDate(entered);
            organiserFilter = organiser.getText().toString().trim(); renderEvents(); filter.dismiss();
        });
        panel.secondary.setOnClickListener(view -> filter.dismiss());
        panel.tertiary.setOnClickListener(view -> {
            dateFilter = ""; organiserFilter = ""; binding.search.setText(""); renderEvents(); filter.dismiss();
        });
    }
    private EditText filterField(android.widget.LinearLayout parent,String label,String value) {
        android.widget.TextView text = new android.widget.TextView(this);
        text.setText(label); text.setTextSize(12); text.setTextColor(getColor(R.color.muted)); parent.addView(text);
        EditText input = new EditText(this); input.setText(value); input.setSingleLine(true);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setTextSize(14); input.setMinHeight(dp(50)); input.setBackgroundResource(R.drawable.event_card); input.setPadding(dp(12),dp(12),dp(12),dp(12));
        android.widget.LinearLayout.LayoutParams size = new android.widget.LinearLayout.LayoutParams(-1,-2);
        size.topMargin=dp(7); size.bottomMargin=dp(16); input.setLayoutParams(size);
        input.setId(View.generateViewId()); text.setLabelFor(input.getId()); parent.addView(input);
        return input;
    }
    private boolean inlineValidation(EditText field,boolean valid,int message) {
        android.widget.LinearLayout parent=(android.widget.LinearLayout)field.getParent();
        String tag="validation:"+field.getId();
        android.widget.TextView error=parent.findViewWithTag(tag);
        if(error==null) {
            error=new android.widget.TextView(this); error.setTag(tag); error.setTextSize(12);
            error.setTextColor(getColor(R.color.rose)); error.setPadding(dp(4),dp(5),dp(4),dp(3));
            error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
            parent.addView(error,parent.indexOfChild(field)+1,new android.widget.LinearLayout.LayoutParams(-1,-2));
        }
        error.setText(valid ? "" : getString(message)); error.setVisibility(valid?View.GONE:View.VISIBLE);
        field.setBackgroundResource(valid?R.drawable.event_card:R.drawable.error_field);
        field.setPadding(dp(12),dp(12),dp(12),dp(12));
        return valid;
    }
    private AlertDialog openPopup(DialogPopupBinding panel) {
        AlertDialog popup=new AlertDialog.Builder(this).setView(panel.getRoot()).create();
        panel.close.setOnClickListener(view->popup.dismiss());
        popup.show();
        android.view.Window window=popup.getWindow();
        if(window!=null) {
            int width=Math.min(getResources().getDisplayMetrics().widthPixels-dp(32),dp(420));
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setDimAmount(0.28f); window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setLayout(width,android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            window.setWindowAnimations(R.style.PopupAnimation);
            panel.body.measure(View.MeasureSpec.makeMeasureSpec(width-dp(48),View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
            android.view.ViewGroup.LayoutParams size=panel.scroll.getLayoutParams();
            int available=(int)(getResources().getDisplayMetrics().heightPixels*0.42f);
            size.height=Math.min(panel.body.getMeasuredHeight(),available);
            panel.scroll.setLayoutParams(size);
            final int contentHeight=panel.body.getMeasuredHeight();
            panel.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(()->{
                android.graphics.Rect visible=new android.graphics.Rect();
                window.getDecorView().getWindowVisibleDisplayFrame(visible);
                int fixed=panel.getRoot().getPaddingTop()+panel.getRoot().getPaddingBottom();
                for(int i=0;i<panel.getRoot().getChildCount();i++) {
                    View child=panel.getRoot().getChildAt(i);
                    if(child==panel.scroll || child.getVisibility()==View.GONE) continue;
                    android.view.ViewGroup.MarginLayoutParams margins=(android.view.ViewGroup.MarginLayoutParams)child.getLayoutParams();
                    fixed+=child.getMeasuredHeight()+margins.topMargin+margins.bottomMargin;
                }
                int height=Math.min(contentHeight,Math.min(available,Math.max(dp(48),visible.height()-fixed-dp(32))));
                if(panel.scroll.getLayoutParams().height!=height) {
                    android.view.ViewGroup.LayoutParams updated=panel.scroll.getLayoutParams(); updated.height=height; panel.scroll.setLayoutParams(updated);
                }
            });
        }
        return popup;
    }
    private void reviewRow(android.widget.LinearLayout body,int label,String value) {
        android.widget.TextView heading=new android.widget.TextView(this);
        heading.setText(label); heading.setTextSize(12); heading.setTextColor(getColor(R.color.muted)); body.addView(heading);
        android.widget.TextView content=new android.widget.TextView(this);
        content.setText(value.isEmpty()?getString(R.string.not_provided):value); content.setTextSize(15);
        content.setTextColor(getColor(R.color.ink)); content.setPadding(0,dp(5),0,dp(17)); body.addView(content);
    }
    private void showNotice(String message,boolean error) {
        if(notice!=null && notice.getParent() instanceof android.view.ViewGroup) ((android.view.ViewGroup)notice.getParent()).removeView(notice);
        noticeHandler.removeCallbacksAndMessages(null);
        android.view.Window window=editor!=null && editor.isShowing()?editor.getWindow():getWindow();
        if(window==null) return;
        android.widget.FrameLayout host=window.getDecorView().findViewById(android.R.id.content);
        if(host==null) return;
        NoticeBannerBinding banner=NoticeBannerBinding.inflate(getLayoutInflater());
        banner.symbol.setText(error?"!":"✓"); banner.title.setText(error?R.string.notice_attention:R.string.notice_success);
        banner.message.setText(message); banner.getRoot().setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        android.widget.FrameLayout.LayoutParams position=new android.widget.FrameLayout.LayoutParams(-1,-2,android.view.Gravity.TOP);
        position.setMargins(dp(16),dp(48),dp(16),0);
        notice=banner.getRoot(); host.addView(notice,position);
        banner.close.setOnClickListener(view->{host.removeView(banner.getRoot()); if(notice==banner.getRoot()) notice=null;});
        noticeHandler.postDelayed(()->{if(banner.getRoot().getParent()==host) host.removeView(banner.getRoot()); if(notice==banner.getRoot()) notice=null;},error?10000:6000);
    }
    @Override protected void onDestroy() {
        noticeHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density); }
    private void styleSystemBars(android.view.Window window) {
        window.setStatusBarColor(getColor(R.color.canvas));
        window.setNavigationBarColor(getColor(R.color.canvas));
        androidx.core.view.WindowInsetsControllerCompat bars = new androidx.core.view.WindowInsetsControllerCompat(window,window.getDecorView());
        bars.setAppearanceLightStatusBars(true); bars.setAppearanceLightNavigationBars(true);
    }
    private void fullScreen(AlertDialog dialog,View root) {
        android.view.Window window = dialog.getWindow();
        if (window == null) return;
        styleSystemBars(window);
        window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawableResource(R.color.canvas);
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        ViewCompat.setOnApplyWindowInsetsListener(root,(view,insets)-> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int statusBar = getResources().getIdentifier("status_bar_height","dimen","android");
            int safeTop = statusBar == 0 ? dp(24) : getResources().getDimensionPixelSize(statusBar);
            view.setPadding(bars.left,Math.max(bars.top,safeTop),bars.right,bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
        android.widget.Button primary = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (primary == null) return;
        primary.setTextColor(android.graphics.Color.WHITE); primary.setAllCaps(false);
        primary.setBackgroundResource(R.drawable.primary_button); ViewCompat.setBackgroundTintList(primary,null);
        primary.setMinHeight(dp(52)); primary.setPadding(dp(20),dp(12),dp(20),dp(12));
    }
    private void showDetails(Event event) {
        DialogDetailsBinding page = DialogDetailsBinding.inflate(getLayoutInflater());
        page.title.setText(event.name); page.category.setText(event.type+" · "+EventAdapter.statusLabel(this,event.status));
        page.date.setText(EventValidation.displayDate(event.date)); page.location.setText(event.location);
        page.organiser.setText(getString(R.string.organised_by,event.organiser));
        page.description.setText(event.description.isEmpty()?getString(R.string.no_description):event.description);
        page.contacts.setVisibility(event.contactEmail.isEmpty() && event.contactPhone.isEmpty()?View.GONE:View.VISIBLE);
        page.email.setText(event.contactEmail); page.email.setVisibility(event.contactEmail.isEmpty()?View.GONE:View.VISIBLE);
        page.phone.setText(event.contactPhone); page.phone.setVisibility(event.contactPhone.isEmpty()?View.GONE:View.VISIBLE);
        AlertDialog details = new AlertDialog.Builder(this).setView(page.getRoot()).create();
        page.editAction.setOnClickListener(view -> { details.dismiss(); showEditor(event,event); });
        page.back.setOnClickListener(view->details.dismiss());
        details.show(); fullScreen(details,page.getRoot());
    }
    private void showEditor(Event existing, Event draft) {
        DialogEventBinding form = DialogEventBinding.inflate(getLayoutInflater());
        editorFields = form;
        editingEvent = existing;
        form.editorTitle.setText(existing == null ? R.string.create_title : R.string.edit_title);
        if (draft != null) {
            form.descriptionInput.setText(draft.description);
            form.emailInput.setText(draft.contactEmail);
            form.phoneInput.setText(draft.contactPhone);
            form.nameInput.setText(draft.name);
            form.dateInput.setText(draft.date.matches("\\d{4}-\\d{2}-\\d{2}") ? EventValidation.displayDate(draft.date) : draft.date);
            form.locationInput.setText(draft.location);
            form.organiserInput.setText(draft.organiser);
            form.typeInput.setSelection(Math.max(0, Arrays.asList(getResources().getStringArray(R.array.types)).indexOf(draft.type)));
            form.statusInput.setSelection(Math.max(0, Arrays.asList(getResources().getStringArray(R.array.statuses)).indexOf(draft.status)));
        }
        form.pickDate.setOnClickListener(view -> {
            String entered=form.dateInput.getText().toString();
            LocalDate initial=EventValidation.validDate(entered)?LocalDate.parse(EventValidation.storageDate(entered)):LocalDate.now();
            LocalDate[] selected={initial};
            DialogPopupBinding panel=DialogPopupBinding.inflate(getLayoutInflater());
            panel.title.setText(R.string.pick_date); panel.subtitle.setText(R.string.date_subtitle);
            android.widget.CalendarView calendar=new android.widget.CalendarView(this);
            java.time.ZoneId zone=java.time.ZoneId.systemDefault();
            calendar.setMinDate(LocalDate.of(1900,1,1).atStartOfDay(zone).toInstant().toEpochMilli());
            calendar.setMaxDate(LocalDate.of(2100,12,31).atStartOfDay(zone).toInstant().toEpochMilli());
            calendar.setDate(initial.atStartOfDay(zone).toInstant().toEpochMilli());
            calendar.setOnDateChangeListener((widget,year,month,day)->selected[0]=LocalDate.of(year,month+1,day));
            panel.body.addView(calendar,new android.widget.LinearLayout.LayoutParams(-1,dp(320)));
            panel.primary.setText(R.string.use_date); panel.secondary.setText(R.string.cancel);
            AlertDialog picker=openPopup(panel);
            panel.secondary.setOnClickListener(button->picker.dismiss());
            panel.primary.setOnClickListener(button->{
                form.dateInput.setText(EventValidation.displayDate(selected[0].toString()));
                inlineValidation(form.dateInput,true,R.string.invalid_date); picker.dismiss();
            });
        });
        form.moreOptions.setOnClickListener(view -> {
            boolean expand = form.optionalFields.getVisibility() != View.VISIBLE;
            form.optionalFields.setVisibility(expand ? View.VISIBLE : View.GONE);
            form.moreOptions.setText(expand ? R.string.less_options : R.string.more_options);
        });
        if (draft != null && (!draft.description.isEmpty() || !draft.contactEmail.isEmpty() || !draft.contactPhone.isEmpty())) {
            form.optionalFields.setVisibility(View.VISIBLE); form.moreOptions.setText(R.string.less_options);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(form.getRoot());
        form.deleteAction.setVisibility(existing == null ? View.GONE : View.VISIBLE);
        AlertDialog dialog = builder.create();
        editor = dialog;
        form.back.setOnClickListener(view -> dialog.dismiss());
        dialog.setOnDismissListener(ignored -> { editor = null; editorFields = null; editingEvent = null; });
        dialog.setOnShowListener(ignored -> {
            form.reviewAction.setOnClickListener(view -> {
                EditText firstInvalid=null;
                for(EditText field:new EditText[]{form.nameInput,form.locationInput,form.organiserInput}) {
                    if(!inlineValidation(field,EventValidation.validText(field.getText().toString()),R.string.required) && firstInvalid==null) firstInvalid=field;
                }
                String date=form.dateInput.getText().toString().trim();
                if(!inlineValidation(form.dateInput,EventValidation.validDate(date),R.string.invalid_date) && firstInvalid==null) firstInvalid=form.dateInput;
                String email=form.emailInput.getText().toString().trim(), phone=form.phoneInput.getText().toString().trim();
                if(!inlineValidation(form.emailInput,EventValidation.validEmail(email),R.string.invalid_email) && firstInvalid==null) firstInvalid=form.emailInput;
                if(!inlineValidation(form.phoneInput,EventValidation.validPhone(phone),R.string.invalid_phone) && firstInvalid==null) firstInvalid=form.phoneInput;
                if(firstInvalid!=null) {
                    firstInvalid.requestFocus();
                    if(firstInvalid==form.emailInput || firstInvalid==form.phoneInput) {
                        form.optionalFields.setVisibility(View.VISIBLE); form.moreOptions.setText(R.string.less_options);
                    }
                    showNotice(getString(R.string.check_fields_notice),true); return;
                }
                Event event = new Event(existing == null ? 0 : existing.id,
                        form.nameInput.getText().toString().trim(), EventValidation.storageDate(date),
                        form.locationInput.getText().toString().trim(),
                        form.organiserInput.getText().toString().trim(), form.typeInput.getSelectedItem().toString(), form.statusInput.getSelectedItem().toString());
                event.description = form.descriptionInput.getText().toString().trim();
                event.contactEmail = email;
                event.contactPhone = phone;
                DialogPopupBinding panel=DialogPopupBinding.inflate(getLayoutInflater());
                panel.title.setText(R.string.review_title); panel.subtitle.setText(R.string.review_subtitle);
                reviewRow(panel.body,R.string.name,event.name); reviewRow(panel.body,R.string.date,date);
                reviewRow(panel.body,R.string.location,event.location); reviewRow(panel.body,R.string.organiser,event.organiser);
                reviewRow(panel.body,R.string.type,event.type); reviewRow(panel.body,R.string.status,EventAdapter.statusLabel(this,event.status));
                if(!event.description.isEmpty()) reviewRow(panel.body,R.string.description,event.description);
                if(!email.isEmpty()) reviewRow(panel.body,R.string.email,email);
                if(!phone.isEmpty()) reviewRow(panel.body,R.string.phone,phone);
                panel.primary.setText(R.string.save); panel.secondary.setText(R.string.back_edit);
                AlertDialog review=openPopup(panel);
                panel.secondary.setOnClickListener(button->review.dismiss());
                panel.primary.setOnClickListener(button->{
                    panel.primary.setEnabled(false); form.reviewAction.setEnabled(false);
                    deleting=false; review.dismiss(); model.save(event);
                });
            });
            if(existing!=null) form.deleteAction.setOnClickListener(view->{
                DialogPopupBinding panel=DialogPopupBinding.inflate(getLayoutInflater());
                panel.title.setText(R.string.delete_title); panel.subtitle.setText(R.string.delete_subtitle);
                reviewRow(panel.body,R.string.name,existing.name);
                panel.primary.setText(R.string.delete); panel.secondary.setText(R.string.keep_event);
                AlertDialog confirmation=openPopup(panel);
                panel.secondary.setOnClickListener(button->confirmation.dismiss());
                panel.primary.setOnClickListener(button->{
                    deleting=true; confirmation.dismiss(); form.reviewAction.setEnabled(false); form.deleteAction.setEnabled(false);
                    model.delete(existing);
                });
            });
            form.reviewAction.setEnabled(!model.isBusy());
        });
        dialog.show();
        fullScreen(dialog,form.getRoot());
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("filterDate",dateFilter); state.putString("filterOrganiser",organiserFilter);
        if (editorFields != null) {
            state.putString("description", editorFields.descriptionInput.getText().toString());
            state.putString("email", editorFields.emailInput.getText().toString());
            state.putString("phone", editorFields.phoneInput.getText().toString());
            state.putBoolean("editorOpen", true);
            state.putLong("eventId", editingEvent == null ? 0 : editingEvent.id);
            state.putString("name", editorFields.nameInput.getText().toString());
            state.putString("date", editorFields.dateInput.getText().toString());
            state.putString("location", editorFields.locationInput.getText().toString());
            state.putString("organiser", editorFields.organiserInput.getText().toString());
            state.putString("type", editorFields.typeInput.getSelectedItem().toString());
            state.putString("status", editorFields.statusInput.getSelectedItem().toString());
        }
        super.onSaveInstanceState(state);
    }
}
