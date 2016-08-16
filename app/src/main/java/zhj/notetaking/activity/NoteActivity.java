package zhj.notetaking.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import zhj.notetaking.R;
import zhj.notetaking.adapter.NoteAdapter;
import zhj.notetaking.db_helper.DataBaseHelper;
import zhj.notetaking.db_helper.Operate;
import zhj.notetaking.listener.ItemClickListener;
import zhj.notetaking.listener.ItemLongClickListener;


public class NoteActivity extends BaseActivity implements View.OnClickListener {

    private static long current_time = 0;      //记录系统当前时间

    //使用butterknife绑定控件
    @BindView(R.id.fab)
    FloatingActionButton but;
    @BindView(R.id.note_recycle)
    RecyclerView noteRecycle;
    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.drawerLayout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nv_menu_left)
    NavigationView nv_menu_left;
    @BindView(R.id.rl_about)
    RelativeLayout mRlAbout;
    /**
     * 搜索
     */
    private SearchView searchView;
    private ActionBarDrawerToggle mDrawerToggle;
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

        setContentView(R.layout.activity_note);
        ButterKnife.bind(this);

        InitView();
        data_list = operate.set();
        setupNoteAdapter();


        Reflesh();
    }


    //设置列数
    private void setSingle() {
        isSingle = !isSingle;
//        if (isSingle) {
//            ivView.setImageResource(R.mipmap.ic_listview_white);
//        } else {
//            ivView.setImageResource(R.mipmap.ic_gridview_white);
//        }
        layoutManager.setSpanCount(isSingle ? 1 : 2);


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
//        ivView.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                setSingle();
//
//            }
//        });
        mToolBar.setTitle("记笔记");
        setSupportActionBar(mToolBar);

        mToolBar.setOnMenuItemClickListener(onMenuItemClick);

        getSupportActionBar().setHomeButtonEnabled(true);  //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //创建返回键，并实现打开关/闭监听
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolBar, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerToggle.syncState();  //初始化状态
        mDrawerLayout.addDrawerListener(mDrawerToggle); //将DrawerLayout与DrawerToggle绑定
//        mDrawerLayout.setScrimColor(Color.TRANSPARENT);   //去除侧边阴影
        nv_menu_left.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_item1:
                        mToolBar.setTitle("记笔记");
                        mRlAbout.setVisibility(View.INVISIBLE);
                        noteRecycle.setVisibility(View.VISIBLE);
                        item.setChecked(true);
                        break;
//                    case R.id.navigation_item2:
//                        mToolBar.setTitle("提示");
//                        item.setChecked(true);
//                        noteRecycle.setVisibility(View.INVISIBLE);
//                        break;
//                    case R.id.navigation_item3:
//                        mToolBar.setTitle("设置");
//                        item.setChecked(true);
//                        noteRecycle.setVisibility(View.INVISIBLE);

//                        break;
                    case R.id.navigation_sub_item1:
                        mToolBar.setTitle("关于");
                        item.setChecked(true);
                        noteRecycle.setVisibility(View.INVISIBLE);
                        mRlAbout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.navigation_sub_item2:
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:zjxuzhj@gmail.com"));
                        intent.putExtra(Intent.EXTRA_SUBJECT, "问题反馈");
                        intent.putExtra(Intent.EXTRA_TEXT, "");
                        startActivity(intent);
                        break;
                    case R.id.navigation_sub_item3:
                        item.setChecked(false);
                        showShare();

                        break;
                }
                //将选中设为点击状态

                //关闭抽屉
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            String msg = "";
            switch (menuItem.getItemId()) {
                case R.id.action_change:
                    msg += "点击其他1";
                    setSingle();
                    break;
//                case R.id.action_tip:
//                    msg += "点击提醒";
//                    break;
//                case R.id.action_menu:
//                    msg += "点击设置";
//                    break;
            }
//            if (!msg.equals("")) {
//                Toast.makeText(NoteActivity.this, msg, Toast.LENGTH_SHORT).show();
//            }
            return true;
        }
    };

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

    //设置foolbar的菜单栏
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
//关闭sso授权
        oks.disableSSOWhenAuthorize();

// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间等使用
        oks.setTitle("分享");
// titleUrl是标题的网络链接，QQ和QQ空间等使用
        oks.setTitleUrl("http://android.myapp.com/myapp/detail.htm?apkName=zhj.notetaking");
// text是分享文本，所有平台都需要这个字段
        oks.setText("记笔记是一款简美且好用的笔记应用，回归笔记的文字时代，方便你随时随地记录点滴 ，交互设计采用Material Design。");
// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
// url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
// comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("记笔记是一款简美且好用的笔记应用，回归笔记的文字时代，方便你随时随地记录点滴 ，交互设计采用Material Design。");
// site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
// siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://android.myapp.com/myapp/detail.htm?apkName=zhj.notetaking");

// 启动分享GUI
        oks.show(this);
    }

}
