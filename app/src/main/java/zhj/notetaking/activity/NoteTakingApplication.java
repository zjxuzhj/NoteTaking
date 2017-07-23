package zhj.notetaking.activity;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by HongJay on 2017/6/27.
 */

public class NoteTakingApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "9c0413b88c", false);
//        Stetho.initializeWithDefaults(this);
    }
}
