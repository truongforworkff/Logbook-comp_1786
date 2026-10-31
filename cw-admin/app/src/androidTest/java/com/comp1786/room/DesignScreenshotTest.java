package com.comp1786.room;
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
        } finally { instrumentation.runOnMainSync(activity::finish); }
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
