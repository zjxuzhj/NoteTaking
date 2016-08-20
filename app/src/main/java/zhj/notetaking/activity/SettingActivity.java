package zhj.notetaking.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import zhj.notetaking.R;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.settingToolbar)
    Toolbar mSettingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        mSettingToolbar.setTitle("设置");
        setSupportActionBar(mSettingToolbar);
    }
}
