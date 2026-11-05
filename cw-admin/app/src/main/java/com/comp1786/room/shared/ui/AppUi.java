package com.comp1786.room.shared.ui;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.comp1786.room.R;
import com.comp1786.room.databinding.SharedPopupBinding;
import com.comp1786.room.databinding.SharedNoticeBinding;
public final class AppUi {
    private final AppCompatActivity activity;
    private View notice;
    private final android.os.Handler noticeHandler=new android.os.Handler(android.os.Looper.getMainLooper());
    public AppUi(AppCompatActivity activity) { this.activity=activity; }
    public EditText filterField(android.widget.LinearLayout parent,String label,String value,int id) {
        android.widget.TextView text = new android.widget.TextView(activity);
        text.setText(label); text.setTextSize(12); text.setTextColor(activity.getColor(R.color.muted)); parent.addView(text);
        EditText input = new EditText(activity); input.setText(value); input.setSingleLine(true);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setTextSize(14); input.setMinHeight(dp(50)); input.setBackgroundResource(R.drawable.event_card); input.setPadding(dp(12),dp(12),dp(12),dp(12));
        android.widget.LinearLayout.LayoutParams size = new android.widget.LinearLayout.LayoutParams(-1,-2);
        size.topMargin=dp(7); size.bottomMargin=dp(16); input.setLayoutParams(size);
        input.setId(id); text.setLabelFor(input.getId()); parent.addView(input);
        return input;
    }
    public boolean inlineValidation(EditText field,boolean valid,int message) {
        android.widget.LinearLayout parent=(android.widget.LinearLayout)field.getParent();
        String tag="validation:"+field.getId();
        android.widget.TextView error=parent.findViewWithTag(tag);
        if(error==null) {
            error=new android.widget.TextView(activity); error.setTag(tag); error.setTextSize(12);
            error.setTextColor(activity.getColor(R.color.rose)); error.setPadding(dp(4),dp(5),dp(4),dp(3));
            error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
            parent.addView(error,parent.indexOfChild(field)+1,new android.widget.LinearLayout.LayoutParams(-1,-2));
        }
        error.setText(valid ? "" : activity.getString(message)); error.setVisibility(valid?View.GONE:View.VISIBLE);
        field.setBackgroundResource(valid?R.drawable.event_card:R.drawable.error_field);
        field.setPadding(dp(12),dp(12),dp(12),dp(12));
        return valid;
    }
    public AlertDialog openPopup(SharedPopupBinding panel) {
        AlertDialog popup=new AlertDialog.Builder(activity).setView(panel.getRoot()).create();
        panel.close.setOnClickListener(view->popup.dismiss());
        popup.show();
        android.view.Window window=popup.getWindow();
        if(window!=null) {
            int width=Math.min(activity.getResources().getDisplayMetrics().widthPixels-dp(32),dp(420));
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setDimAmount(0.28f); window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setLayout(width,android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            window.setWindowAnimations(R.style.PopupAnimation);
            panel.body.measure(View.MeasureSpec.makeMeasureSpec(width-dp(48),View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
            android.view.ViewGroup.LayoutParams size=panel.scroll.getLayoutParams();
            int available=(int)(activity.getResources().getDisplayMetrics().heightPixels*0.42f);
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
    public void reviewRow(android.widget.LinearLayout body,int label,String value) {
        android.widget.TextView heading=new android.widget.TextView(activity);
        heading.setText(label); heading.setTextSize(12); heading.setTextColor(activity.getColor(R.color.muted)); body.addView(heading);
        android.widget.TextView content=new android.widget.TextView(activity);
        content.setText(value.isEmpty()?activity.getString(R.string.not_provided):value); content.setTextSize(15);
        content.setTextColor(activity.getColor(R.color.ink)); content.setPadding(0,dp(5),0,dp(17)); body.addView(content);
    }
    public void showNotice(android.view.Window window,String message,boolean error) {
        if(notice!=null && notice.getParent() instanceof android.view.ViewGroup) ((android.view.ViewGroup)notice.getParent()).removeView(notice);
        noticeHandler.removeCallbacksAndMessages(null);
        if(window==null) window=activity.getWindow();
        if(window==null) return;
        android.widget.FrameLayout host=window.getDecorView().findViewById(android.R.id.content);
        if(host==null) return;
        SharedNoticeBinding banner=SharedNoticeBinding.inflate(activity.getLayoutInflater());
        banner.symbol.setText(error?"!":"✓"); banner.title.setText(error?R.string.notice_attention:R.string.notice_success);
        banner.message.setText(message); banner.getRoot().setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        android.widget.FrameLayout.LayoutParams position=new android.widget.FrameLayout.LayoutParams(-1,-2,android.view.Gravity.TOP);
        position.setMargins(dp(16),dp(48),dp(16),0);
        notice=banner.getRoot(); host.addView(notice,position);
        banner.close.setOnClickListener(view->{host.removeView(banner.getRoot()); if(notice==banner.getRoot()) notice=null;});
        noticeHandler.postDelayed(()->{if(banner.getRoot().getParent()==host) host.removeView(banner.getRoot()); if(notice==banner.getRoot()) notice=null;},error?10000:6000);
    }
    public void close() {
        noticeHandler.removeCallbacksAndMessages(null);
        if(notice!=null && notice.getParent() instanceof android.view.ViewGroup) ((android.view.ViewGroup)notice.getParent()).removeView(notice);
        notice=null;
    }
    public int dp(int value) { return (int)(value * activity.getResources().getDisplayMetrics().density); }
    public void styleSystemBars(android.view.Window window) {
        window.setStatusBarColor(activity.getColor(R.color.canvas));
        window.setNavigationBarColor(activity.getColor(R.color.canvas));
        androidx.core.view.WindowInsetsControllerCompat bars = new androidx.core.view.WindowInsetsControllerCompat(window,window.getDecorView());
        bars.setAppearanceLightStatusBars(true); bars.setAppearanceLightNavigationBars(true);
    }
    public void fullScreen(AlertDialog dialog,View root) {
        android.view.Window window = dialog.getWindow();
        if (window == null) return;
        styleSystemBars(window);
        window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawableResource(R.color.canvas);
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        ViewCompat.setOnApplyWindowInsetsListener(root,(view,insets)-> {
            androidx.core.graphics.Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int statusBar = activity.getResources().getIdentifier("status_bar_height","dimen","android");
            int safeTop = statusBar == 0 ? dp(24) : activity.getResources().getDimensionPixelSize(statusBar);
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

}
