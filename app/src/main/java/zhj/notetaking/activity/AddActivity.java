package zhj.notetaking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import zhj.notetaking.R;
import zhj.notetaking.db_helper.DataBaseHelper;
import zhj.notetaking.db_helper.Operate;


public class AddActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.delete)
    TextView delete;
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

        if (which.equals("2")) {
            edit.setText(note);
        }
    }

    //实例化组件
    private void InitView() {


        save.setOnClickListener(this);


        delete.setOnClickListener(this);

        helper = new DataBaseHelper(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save:
                operate = new Operate(helper.getWritableDatabase());
                String newnote = edit.getText().toString();

                long time = System.currentTimeMillis();
                SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss");
                Date d1 = new Date(time);
                String t1 = format.format(d1);

                Intent it = new Intent(AddActivity.this, NoteActivity.class);


                if (which.equals("1")) {                //执行插入操作
                    if (!newnote.equals("") || !newnote.trim().equals("")) {
                        operate.insert(newnote, t1);
                        startActivity(it);
                        finish();
                    } else {
                        startActivity(it);
                        finish();
                    }
                } else {                                //执行修改操作
                    operate.update(note, newnote, t1);
                    startActivity(it);
                    finish();
                }
                break;
            case R.id.delete:
                startActivity(new Intent(AddActivity.this, NoteActivity.class));
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AddActivity.this, NoteActivity.class));
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
}
