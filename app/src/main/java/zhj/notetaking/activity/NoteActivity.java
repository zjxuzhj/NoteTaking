package zhj.notetaking.activity;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import zhj.notetaking.R;
import zhj.notetaking.adapter.AdapterType;
import zhj.notetaking.adapter.ColorsListAdapter;
import zhj.notetaking.adapter.NoteAdapter;
import zhj.notetaking.data.NoteInfo;
import zhj.notetaking.db_helper.DataBaseHelper;
import zhj.notetaking.db_helper.Operate;
import zhj.notetaking.listener.ISearchAdapter;
import zhj.notetaking.listener.ItemClickListener;
import zhj.notetaking.listener.ItemLongClickListener;
import zhj.notetaking.utils.DialogUtils;
import zhj.notetaking.utils.FileUtils;
import zhj.notetaking.utils.PrefUtils;
import zhj.notetaking.utils.ThemeUtils;


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
    @BindView(R.id.contentSetting)
    LinearLayout mContentSetting;
    @BindView(R.id.giveLove)
    TextView mGiveLove;
    @BindView(R.id.feedback)
    TextView mFeedback;
    @BindView(R.id.ll_isRight)
    RelativeLayout mLlIsRight;
    @BindView(R.id.checkBox)
    CheckBox mCheckBox;
    @BindView(R.id.change_Theme)
    TextView mChangeTheme;
    @BindView(R.id.cl_coor)
    CoordinatorLayout mClCoor;
    @BindView(R.id.tv_about)
    TextView mTvAbout;
    @BindView(R.id.rel_copy_note)
    RelativeLayout mRelCopyNote;

    private SearchView mSearchView;
    private ActionBarDrawerToggle mDrawerToggle;
    // RV的LayoutManager
    private StaggeredGridLayoutManager layoutManager;
    private NoteAdapter noteAdapter;
    private DataBaseHelper helper = null;
    private Operate operate = null;
    private Operate delete_operate = null;
    private boolean isRight = false;
    //判断一列显示还是两列
    private boolean isSingle = false;
    //侧滑栏在左边还是右边
    private int gravity = Gravity.LEFT;
    private  FileUtils mFileUtils;

    private List<NoteInfo> data_list = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:         //删除某一行记录
                    delete_operate = new Operate(helper.getWritableDatabase());
                    delete_operate.delete((String) msg.obj);

                    data_list = new Operate(helper.getReadableDatabase()).getAll();
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
        data_list = operate.getAll();
        setupNoteAdapter();
        Reflesh();
    }


    //设置列数
    private void setSingle() {
        isSingle = !isSingle;
        PrefUtils.putBoolean(this, "isSingle", isSingle);
        layoutManager.setSpanCount(isSingle ? 1 : 2);
    }

    //设置搜索View 的监听
    private void setSearchViewListener() {
        // 关闭按钮监听
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                noteAdapter.setDataAndType(AdapterType.NOTE_TYPE, null);
                return false;
            }
        });
        // 搜索文字监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final List<NoteInfo> infos = new Operate(helper.getReadableDatabase()).getAll();

                int size = infos.size();
                for (int i = size - 1; i >= 0; i--) {
                    String content = infos.get(i).getNote();
                    if (!content.contains(newText)) {
                        infos.remove(i);
                    }
                }

                noteAdapter.setDataAndType(AdapterType.SEARCH_TYPE, new ISearchAdapter() {
                    @Override
                    public List<NoteInfo> get() {
                        return infos;
                    }
                });
                notifyDataChanged();
                return false;
            }
        });
    }


    //数据改变
    private void notifyDataChanged() {
        noteAdapter.getNoteInfos(data_list);
        noteAdapter.notifyDataSetChanged();
    }

    //初始化适配器
    private void setupNoteAdapter() {
        layoutManager =
                new StaggeredGridLayoutManager(isSingle ? 1 : 2, StaggeredGridLayoutManager.VERTICAL);
        noteRecycle.setLayoutManager(layoutManager);
        noteAdapter = new NoteAdapter(this, data_list);

        noteAdapter.setItemClickListener(new ItemClickListener() {


            @Override
            public void onItemClick(NoteInfo info) {
                Intent intent = new Intent(NoteActivity.this, AddActivity.class);
                String text1 = info.getNote();
                intent.putExtra("text", text1);
                intent.putExtra("which", "2");
                startActivity(intent);
                finish();

            }

        });

        noteAdapter.setItemLongClickListener(new ItemLongClickListener() {
            @Override
            public void onItemLongClick(NoteInfo info) {
                final String content = info.getNote();

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


        });

        noteRecycle.setAdapter(noteAdapter);

    }

    //实例化组件
    private void InitView() {
        mFeedback.setOnClickListener(this);
        mGiveLove.setOnClickListener(this);
        mLlIsRight.setOnClickListener(this);
        mChangeTheme.setOnClickListener(this);
        mTvAbout.setOnClickListener(this);
        mRelCopyNote.setOnClickListener(this);
        but.setOnClickListener(this);
        helper = new DataBaseHelper(NoteActivity.this);
        operate = new Operate(helper.getReadableDatabase());

        //从配置文件判断是单列还是双列展示
        isSingle = PrefUtils.getBoolean(this, "isSingle", false);

        //从配置文件判断是左边侧滑还是右边侧滑
        if (PrefUtils.getBoolean(this, "isRight", false)) {
            gravity = Gravity.RIGHT;
            setMenuGravity(Gravity.RIGHT);
        } else {
            gravity = Gravity.LEFT;
        }


        mToolBar.setTitle("记笔记");
        setSupportActionBar(mToolBar);

        mToolBar.setOnMenuItemClickListener(onMenuItemClick);

        getSupportActionBar().setHomeButtonEnabled(true);  //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //创建返回键，并实现打开关/闭监听
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolBar, R.string.app_name, R.string.app_name);

        mDrawerToggle.syncState();  //初始化状态
        mDrawerLayout.addDrawerListener(mDrawerToggle); //将DrawerLayout与DrawerToggle绑定

        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gravity == Gravity.RIGHT) {
                    if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                        mDrawerLayout.closeDrawer(Gravity.RIGHT);
                    } else {
                        mDrawerLayout.openDrawer(Gravity.RIGHT);
                    }
                } else if (gravity == Gravity.LEFT) {
                    if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                    } else {
                        mDrawerLayout.openDrawer(Gravity.LEFT);
                    }
                }
            }
        });

        nv_menu_left.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_item1:
                        mToolBar.setTitle("记笔记");
                        mRlAbout.setVisibility(View.INVISIBLE);
                        noteRecycle.setVisibility(View.VISIBLE);
                        mContentSetting.setVisibility(View.INVISIBLE);
                        item.setChecked(true);
                        break;
//                    case R.id.navigation_item2:
//                        mToolBar.setTitle("提示");
//                        item.setChecked(true);
//                        noteRecycle.setVisibility(View.INVISIBLE);
//                        break;
                    case R.id.navigation_item3:
                        //读取isRight的属性值
                        mToolBar.setTitle("设置");
                        mCheckBox.setChecked(PrefUtils.getBoolean(NoteActivity.this, "isRight", false));
                        noteRecycle.setVisibility(View.INVISIBLE);
                        mContentSetting.setVisibility(View.VISIBLE);
                        item.setChecked(true);
                        break;
                    case R.id.navigation_sub_item1:
                        mToolBar.setTitle("关于");
                        item.setChecked(true);
                        noteRecycle.setVisibility(View.INVISIBLE);
                        mContentSetting.setVisibility(View.INVISIBLE);
                        mRlAbout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.navigation_sub_item2:
                        feedback();
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

    //设置侧滑栏方向
    public void setMenuGravity(int gravity) {
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) nv_menu_left.getLayoutParams();
        params.gravity = gravity;
        nv_menu_left.setLayoutParams(params);
    }

    //对toolbar上的菜单图标的点击
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_change:
                    //设置一行展示还是两行展示
                    setSingle();
                    break;
//                case R.id.action_tip:
//                    msg += "点击提醒";
//                    break;
//                case R.id.action_menu:
//                    msg += "点击设置";
//                    break;
            }

            return true;
        }
    };


    //刷新listview数据
    private void Reflesh() {
        noteAdapter.getNoteInfos(data_list);
        noteAdapter.notifyDataSetChanged();

    }

    //点击进入笔记
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.giveLove:
                giveFavor();
                break;
            case R.id.feedback:
                feedback();
                break;
            case R.id.ll_isRight:
                if (PrefUtils.getBoolean(NoteActivity.this, "isRight", false)) {
                    mCheckBox.setChecked(false);
                    PrefUtils.putBoolean(NoteActivity.this, "isRight", false);
                    setMenuGravity(Gravity.LEFT);
                    gravity = Gravity.LEFT;
                } else {
                    mCheckBox.setChecked(true);
                    PrefUtils.putBoolean(NoteActivity.this, "isRight", true);
                    setMenuGravity(Gravity.RIGHT);
                    gravity = Gravity.RIGHT;
                }
                break;
            case R.id.change_Theme:
                //展示主题颜色选择对话框
                showThemeChooseDialog();
                break;
            case R.id.fab:
                Intent it = new Intent(NoteActivity.this, AddActivity.class);
                it.putExtra("which", "1");
                startActivity(it);
                finish();
                break;
            case R.id.tv_about:
                Snackbar.make(mClCoor, "侧滑栏有啊亲，我是占位置的。", Snackbar.LENGTH_SHORT)
                        .setAction("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();
                break;
            case R.id.rel_copy_note:
                backupLocal();
                break;
        }

    }
    //展示主题配色对话框
    public void showThemeChooseDialog(){
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilder(NoteActivity.this);
        builder.setTitle(R.string.change_theme);
        Integer[] res = new Integer[]{R.drawable.blue_mix_red_round,R.drawable.pink_mix_yellow_round,
                R.drawable.green_mix_red_round,R.drawable.brown_mix_grey_round,
                R.drawable.red_round, R.drawable.brown_round, R.drawable.blue_round,
                R.drawable.blue_grey_round, R.drawable.yellow_round, R.drawable.deep_purple_round,
                R.drawable.pink_round, R.drawable.green_round};
        List<Integer> list = Arrays.asList(res);
        ColorsListAdapter adapter = new ColorsListAdapter(NoteActivity.this, list);
        adapter.setCheckItem(ThemeUtils.getCurrentTheme(NoteActivity.this).getIntValue());
        GridView gridView = (GridView) LayoutInflater.from(NoteActivity.this).inflate(R.layout.colors_panel_layout, null);
        gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridView.setCacheColorHint(0);
        gridView.setAdapter(adapter);
        builder.setView(gridView);
        final AlertDialog dialog = builder.show();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialog.dismiss();
                onThemeChoose(i);
                Log.d("po", "onItemClick: "+i+":"+ThemeUtils.getCurrentTheme(NoteActivity.this).getIntValue()
                +":"+PrefUtils.getInt(NoteActivity.this,"change_theme_key",0x00 ));

            }
        });
    }
    public void onThemeChoose(int position) {
        int value = ThemeUtils.getCurrentTheme(NoteActivity.this).getIntValue();
        if (value != position) {
                PrefUtils.putInt(NoteActivity.this,"change_theme_key",position);
            reload(true);
        }
    }
    //通过邮件反馈建议和意见
    private void feedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:zjxuzhj@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "问题反馈");
        intent.putExtra(Intent.EXTRA_TEXT, "");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(NoteActivity.this, "您的手机无法发送邮件反馈！", Toast.LENGTH_LONG).show();
        }

    }

    //去应用市场给应用评分
    private void giveFavor() {
        try {
            Uri uri = Uri.parse("market://details?id=" + NoteActivity.this.getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            NoteActivity.this.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    //备份笔记
    private void backupLocal(){
        mFileUtils=new FileUtils();
        mFileUtils.backupNotes(this,data_list);
        Snackbar.make(mClCoor, "备份完成", Snackbar.LENGTH_SHORT)
                .setAction("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();

    }

    //对回退键的处理
    @Override
    public void onBackPressed() {
        if (!mSearchView.isIconified()) {
            mSearchView.setIconified(true);
            noteAdapter.setDataAndType(AdapterType.NOTE_TYPE, null);
            return;
        }
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

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setQueryHint("搜索笔记");
        setSearchViewListener();
        return true;
    }

    //分享相关
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
