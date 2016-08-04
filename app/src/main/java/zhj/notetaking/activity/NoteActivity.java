package zhj.notetaking.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import zhj.notetaking.R;
import zhj.notetaking.adapter.NoteAdapter;
import zhj.notetaking.db_helper.DataBaseHelper;
import zhj.notetaking.db_helper.Operate;
import zhj.notetaking.listener.ItemClickListener;
import zhj.notetaking.listener.ItemLongClickListener;

public class NoteActivity extends AppCompatActivity implements View.OnClickListener {

    private static long current_time = 0;      //记录系统当前时间

    //使用butterknife绑定控件
    @BindView(R.id.fab)
    FloatingActionButton but;
    @BindView(R.id.note_recycle)
    RecyclerView noteRecycle;
    @BindView(R.id.iv_view)
    ImageView ivView;


    // RV的LayoutManager
    private StaggeredGridLayoutManager layoutManager;
    private NoteAdapter noteAdapter;
    private DataBaseHelper helper = null;
    private Operate operate = null;
    private Operate delete_operate = null;

    //判断一列显示还是两列
    private boolean isSingle = false;


    private List<Map<String, String>> data_list = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
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
        ButterKnife.bind(this);

        InitView();
        data_list = operate.set();
        setupNoteAdapter();


        Reflesh();
    }

    //设置列数
    private void setSingle(){
        isSingle=!isSingle;
        if(isSingle){
            ivView.setImageResource(R.mipmap.ic_listview_white);
        }else{
            ivView.setImageResource(R.mipmap.ic_gridview_white);
        }
        layoutManager.setSpanCount(isSingle?1:2);


    }

    //初始化适配器
    private void setupNoteAdapter() {
        layoutManager =
                new StaggeredGridLayoutManager(isSingle ? 1 : 2, StaggeredGridLayoutManager.VERTICAL);
        noteRecycle.setLayoutManager(layoutManager);
        noteAdapter = new NoteAdapter(this, data_list);

        noteAdapter.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(Map<String, String> stringStringMap) {
                Intent intent = new Intent(NoteActivity.this, AddActivity.class);
                Set<String> set = stringStringMap.keySet();
                Iterator<String> it = set.iterator();
                if (it.hasNext()) {
                    String text = it.next();
                    String text1 = stringStringMap.get(it.next());
//                    holder.noteText.setText(data.get(it.next()));
//                    holder.noteTime.setText(data.get(text));
                    intent.putExtra("text", text1);
                    intent.putExtra("which", "2");
                    startActivity(intent);
                    finish();
                }


            }

        });

        noteAdapter.setItemLongClickListener(new ItemLongClickListener() {
            @Override
            public void onItemLongClick(Map<String, String> stringStringMap) {
                Set<String> set = stringStringMap.keySet();
                Iterator<String> it = set.iterator();
                if (it.hasNext()) {
                    final String text = it.next();
                    final String content = stringStringMap.get(it.next());

                    Dialog dialog = new AlertDialog.Builder(NoteActivity.this)
                            .setMessage("确定要删除吗?")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Message message = new Message();
                                    message.what = 1;
                                    message.obj = content;
                                    handler.sendMessage(message);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .create();

                    dialog.show();
                }

            }

        });

        noteRecycle.setAdapter(noteAdapter);

    }

    //实例化组件
    private void InitView() {

        but.setOnClickListener(this);

        helper = new DataBaseHelper(NoteActivity.this);
        operate = new Operate(helper.getReadableDatabase());
        ivView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                setSingle();

            }
        });
    }

    /**
     * 刷新listview数据
     */
    private void Reflesh() {
        noteAdapter.getNoteInfos(data_list);
        noteAdapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View v) {
        Intent it = new Intent(NoteActivity.this, AddActivity.class);
        it.putExtra("which", "1");
        startActivity(it);
        finish();
    }


    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - current_time) > 2000) {
            Toast.makeText(NoteActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            current_time = System.currentTimeMillis();
        } else {
            finish();
        }
    }

}
