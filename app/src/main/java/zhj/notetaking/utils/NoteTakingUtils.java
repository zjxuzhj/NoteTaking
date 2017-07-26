package zhj.notetaking.utils;

import android.app.Activity;
import android.content.pm.PackageManager;

/**
 * Created by HongJay on 2017/7/26.
 */

public class NoteTakingUtils {
    public static String getAppVersion(Activity activity, String baseVersion) {
        try {
            baseVersion = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return baseVersion;
    }
}
