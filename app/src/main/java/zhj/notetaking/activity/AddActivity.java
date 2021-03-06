package zhj.notetaking.activity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import zhj.notetaking.R;
import zhj.notetaking.broadcast.TestWidgetProvider;
import zhj.notetaking.db_helper.DataBaseHelper;
import zhj.notetaking.db_helper.Operate;


public class AddActivity extends BaseActivity implements View.OnClickListener {
    public static final int RESULT_SAVE_NOTE=3;
    @BindView(R.id.cancel)
    TextView cancel;
    @BindView(R.id.save)
    TextView save;
    @BindView(R.id.edit)
    EditText edit;

    private DataBaseHelper helper = null;
    private Operate operate = null;

    private Intent intent = null;
    private String note, which;          //分别记录  intent内容 执行操作

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        ButterKnife.bind(this);
        InitView();

        intent = getIntent();
        note = intent.getStringExtra("text");
        which = intent.getStringExtra("which");
        setResult(RESULT_SAVE_NOTE);
        if (which.equals("2")) {
            edit.setText(note);
        }
    }

    //实例化组件
    private void InitView() {

        save.setOnClickListener(this);

        cancel.setOnClickListener(this);

        helper = new DataBaseHelper(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save:
                saveNote();
                break;
            case R.id.cancel:
                finish();
                break;
        }
    }

    /**
     * 保存笔记对象到数据库
     */
    private void saveNote() {
        operate = new Operate(helper.getWritableDatabase());
        String newnote = edit.getText().toString();

        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss");
        Date d1 = new Date(time);
        String t1 = format.format(d1);

        String uuid1 = UUID.randomUUID().toString();

        if (which.equals("1")) {                //执行插入操作
            if (!newnote.equals("") || !newnote.trim().equals("")) {
                operate.insert(newnote, t1,t1, uuid1);
                updateNotification();
                finish();
            } else {
                finish();
            }
        } else {                                //执行修改操作,保存修改时间
            operate.update(note, newnote, t1);
            updateNotification();
            finish();
        }

    }

    private void updateNotification() {
        updateWidget(AddActivity.this);
        updateNoteList(AddActivity.this);

        Toasty.info(getApplicationContext(), "保存成功！").show();
    }

    @Override
    public void onBackPressed() {
        //通过回退键退回时保存数据
        saveNote();
        finish();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
    //发送广播，使appWidget更新
    private void updateWidget(Context context) {
        Intent intent = new Intent(context, TestWidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 123);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        context.sendBroadcast(intent);
    }//发送广播，使appWidget更新
    private void updateNoteList(Context context) {
        Intent intent = new Intent();
        intent.setAction("zhj.notetaking.action.update");
        context.sendBroadcast(intent);
    }
}
