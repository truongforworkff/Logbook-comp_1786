package com.comp1786.room.features.events.ui;
import com.comp1786.room.R;
import com.comp1786.room.features.events.ui.list.MainActivity;
import com.comp1786.room.features.events.data.model.Event;
import com.comp1786.room.features.events.data.local.EventDao;
import com.comp1786.room.features.events.data.local.EventDatabase;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class DesignScreenshotTest {
    @Test public void homeAndCreateScreensRenderWithRealDatabase() throws Exception {
        Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
        Context context=instrumentation.getTargetContext();
        EventDao dao=EventDatabase.get(context).events();
        for(Event event:dao.getAll()) dao.delete(event);
        String[] names={"Một ngày sáng tạo","Chạm vào thiết kế","Kết nối và sẻ chia"};
        String[] dates={"2026-11-08","2026-11-15","2026-11-22"};
        String[] places={"Greenwich · Cơ sở Hà Nội","Studio 02 · Tầng 3","The Workshop Space"};
        for(int i=0;i<names.length;i++) dao.insert(new Event(0,names[i],dates[i],places[i],"Greenwich Community","Workshop","Upcoming"));
        Intent intent=new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainActivity activity=(MainActivity)instrumentation.startActivitySync(intent);
        try {
            long deadline=android.os.SystemClock.uptimeMillis()+15000;
            boolean[] ready={false};
            while(!ready[0] && android.os.SystemClock.uptimeMillis()<deadline) {
                instrumentation.waitForIdleSync();
                instrumentation.runOnMainSync(()->{
                    RecyclerView list=activity.findViewById(R.id.list);
                    ready[0]=list.getAdapter()!=null && list.getAdapter().getItemCount()==3 && list.getChildCount()>0;
                });
                if(!ready[0]) Thread.sleep(100);
            }
            assertTrue("Three event cards must render",ready[0]);
            screenshot(instrumentation,context,"01-home.png");
            instrumentation.runOnMainSync(()->((RecyclerView)activity.findViewById(R.id.list)).getChildAt(0).performClick());
            instrumentation.waitForIdleSync();
            screenshot(instrumentation,context,"02-details.png");
            instrumentation.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);
            instrumentation.waitForIdleSync();
            instrumentation.runOnMainSync(()->activity.findViewById(R.id.add).performClick());
            instrumentation.waitForIdleSync();
            screenshot(instrumentation,context,"03-create.png");
            click(instrumentation,"pickDate");
            screenshot(instrumentation,context,"04-date-popup.png");
            instrumentation.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);
            setText(instrumentation,"nameInput","Gặp gỡ cuối năm");
            setText(instrumentation,"dateInput","25/12/2026");
            setText(instrumentation,"locationInput","Greenwich Campus");
            setText(instrumentation,"organiserInput","Greenwich Community");
            click(instrumentation,"reviewAction");
            screenshot(instrumentation,context,"05-review-popup.png");
            click(instrumentation,"secondary");
            instrumentation.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);
            instrumentation.waitForIdleSync();
            instrumentation.runOnMainSync(()->activity.findViewById(R.id.filter).performClick());
            screenshot(instrumentation,context,"06-filter-popup.png");
            instrumentation.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);
            instrumentation.waitForIdleSync();
            instrumentation.runOnMainSync(()->((RecyclerView)activity.findViewById(R.id.list)).getChildAt(0).performClick());
            click(instrumentation,"editAction");
            click(instrumentation,"deleteAction");
            screenshot(instrumentation,context,"07-delete-popup.png");
            click(instrumentation,"secondary");
            instrumentation.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);
            instrumentation.waitForIdleSync();
            instrumentation.runOnMainSync(()->activity.findViewById(R.id.add).performClick());
            click(instrumentation,"reviewAction");
            screenshot(instrumentation,context,"08-validation-notice.png");
        } finally { instrumentation.runOnMainSync(activity::finish); }
    }
    private android.view.accessibility.AccessibilityNodeInfo node(Instrumentation instrumentation,String id) throws Exception {
        android.accessibilityservice.AccessibilityServiceInfo info=instrumentation.getUiAutomation().getServiceInfo();
        info.flags|=android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        instrumentation.getUiAutomation().setServiceInfo(info);
        instrumentation.waitForIdleSync();
        instrumentation.getUiAutomation().waitForIdle(300,5000);
        android.view.accessibility.AccessibilityNodeInfo root=instrumentation.getUiAutomation().getRootInActiveWindow();
        assertNotNull(root);
        java.util.List<android.view.accessibility.AccessibilityNodeInfo> matches=root.findAccessibilityNodeInfosByViewId("com.comp1786.eventa.admin:id/"+id);
        assertFalse("Missing visible control: "+id,matches.isEmpty());
        return matches.get(0);
    }
    private void click(Instrumentation instrumentation,String id) throws Exception {
        assertTrue("Control must be clickable: "+id,node(instrumentation,id).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK));
        instrumentation.waitForIdleSync();
    }
    private void setText(Instrumentation instrumentation,String id,String value) throws Exception {
        android.os.Bundle args=new android.os.Bundle();
        args.putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,value);
        assertTrue(node(instrumentation,id).performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT,args));
    }
    private void screenshot(Instrumentation instrumentation,Context context,String name) throws Exception {
        instrumentation.getUiAutomation().waitForIdle(500,5000);
        for(String command:new String[]{"mkdir -p /data/local/tmp/eventa-screenshots",
                "screencap -p /data/local/tmp/eventa-screenshots/"+name}) {
            try(java.io.InputStream output=new android.os.ParcelFileDescriptor.AutoCloseInputStream(
                    instrumentation.getUiAutomation().executeShellCommand(command))) {
                byte[] buffer=new byte[1024];
                while(output.read(buffer)!=-1) { }
            }
        }
    }
}
