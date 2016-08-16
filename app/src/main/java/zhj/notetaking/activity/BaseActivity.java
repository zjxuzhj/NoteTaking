package zhj.notetaking.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import zhj.notetaking.R;

public class BaseActivity extends AppCompatActivity {
    protected boolean isStartAnim = true;
    protected boolean isCloseAnim = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        showActivityInAnim();
        initWindow();

        setContentView(R.layout.activity_base);
    }

    @TargetApi(19)
    private void initWindow(){
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintColor(getStatusBarColor());
            tintManager.setStatusBarTintEnabled(true);
        }
    }
    public int getStatusBarColor(){
        return getColorPrimary();
    }
    public int getColorPrimary(){
        TypedValue typedValue = new  TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }
    protected void showActivityInAnim(){
        if (isStartAnim) {
            overridePendingTransition(R.anim.activity_down_up_anim, R.anim.activity_exit_anim);
        }
    }

    protected void showActivityExitAnim(){
        if (isCloseAnim) {
            overridePendingTransition(R.anim.activity_exit_anim, R.anim.activity_up_down_anim);
        }
    }

    @Override
    public void finish() {
        super.finish();
        showActivityExitAnim();
    }
}
