package zhj.notetaking.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import zhj.notetaking.R;
import zhj.notetaking.utils.ThemeUtils;

public class BaseActivity extends AppCompatActivity {
    public final static String IS_START_ANIM = "IS_START_ANIM";
    public final static String IS_CLOSE_ANIM = "IS_CLOSE_ANIM";
    protected boolean isStartAnim = true;
    protected boolean isCloseAnim = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.RedTheme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        showActivityInAnim();
        initTheme();
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
    //初始化主题切换工具
    private void initTheme(){
        ThemeUtils.Theme theme = ThemeUtils.getCurrentTheme(this);
        ThemeUtils.changeTheme(this, theme);
    }
    public int getStatusBarColor(){
        return getColorPrimary();
    }
    public int getColorPrimary(){
        TypedValue typedValue = new  TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }
    public void reload(boolean anim) {
        Intent intent = getIntent();
        if (!anim) {
            overridePendingTransition(0, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.putExtra(BaseActivity.IS_START_ANIM, false);
        }
        finish();
        if (!anim) {
            overridePendingTransition(0, 0);
        }
        startActivity(intent);
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
