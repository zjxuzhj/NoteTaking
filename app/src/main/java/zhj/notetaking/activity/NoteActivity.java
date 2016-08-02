package zhj.notetaking.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import zhj.notetaking.R;
import zhj.notetaking.db_helper.DataBaseHelper;
import zhj.notetaking.db_helper.Operate;

public class NoteActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener{

    private static long current_time = 0;      //记录系统当前时间

    private ListView list = null;
    private FloatingActionButton but = null;

    private DataBaseHelper helper = null;
    private Operate operate = null;
    private Operate delete_operate = null;

    private SimpleAdapter adapter = null;
    private List<Map<String, String>> data_list = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:         //删除某一行记录
                    delete_operate = new Operate(helper.getWritableDatabase());
                    delete_operate.delete((String) msg.obj);

                    data_list = new Operate(helper.getReadableDatabase()).set();
                    Reflesh();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_note);


        InitView();

        data_list = operate.set();
        Reflesh();
    }

    //实例化组件
    private void InitView(){
        list = (ListView) findViewById(R.id.note_list);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);

        but = (FloatingActionButton) findViewById(R.id.fab);
        but.setOnClickListener(this);

        helper = new DataBaseHelper(NoteActivity.this);
        operate = new Operate(helper.getReadableDatabase());
    }

    /**
     * 刷新listview数据
     * */
    private void Reflesh(){
        adapter = new SimpleAdapter(NoteActivity.this, data_list, R.layout.list_adapter,
                new String[]{"note", "time"}, new int[]{R.id.note_text, R.id.note_time});
        list.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        Intent it = new Intent(NoteActivity.this, AddActivity.class);
        it.putExtra("which","1");
        startActivity(it);
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView text = (TextView) view.findViewById(R.id.note_text);
        Intent it = new Intent(NoteActivity.this, AddActivity.class);
        it.putExtra("text", text.getText().toString());
        it.putExtra("which","2");
        startActivity(it);
        finish();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final TextView text = (TextView) view.findViewById(R.id.note_text);
        Dialog dialog = new AlertDialog.Builder(NoteActivity.this)
                .setMessage("确定要删除吗?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Message message = new Message();
                        message.what = 1;
                        message.obj = text.getText().toString();
                        handler.sendMessage(message);
                    }
                })
                .setNegativeButton("取消",null)
                .create();

        dialog.show();
        return true;
    }

    @Override
    public void onBackPressed() {
        if((System.currentTimeMillis() - current_time) > 2000){
            Toast.makeText(NoteActivity.this,"再按一次退出程序",Toast.LENGTH_SHORT).show();
            current_time = System.currentTimeMillis();
        } else{
            finish();
        }
    }
}
