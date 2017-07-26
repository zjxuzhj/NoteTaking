package zhj.notetaking.activity;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListListener;
import cn.bmob.v3.listener.QueryListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import es.dmoral.toasty.Toasty;
import zhj.notetaking.R;
import zhj.notetaking.adapter.AdapterType;
import zhj.notetaking.adapter.ColorsListAdapter;
import zhj.notetaking.adapter.NoteAdapter;
import zhj.notetaking.data.NoteInfo;
import zhj.notetaking.db_helper.DataBaseHelper;
import zhj.notetaking.db_helper.Operate;
import zhj.notetaking.domain.User;
import zhj.notetaking.listener.ISearchAdapter;
import zhj.notetaking.listener.ItemClickListener;
import zhj.notetaking.listener.ItemLongClickListener;
import zhj.notetaking.utils.DialogUtils;
import zhj.notetaking.utils.FileUtils;
import zhj.notetaking.utils.NoteTakingUtils;
import zhj.notetaking.utils.PrefUtils;
import zhj.notetaking.utils.ThemeUtils;
import zhj.notetaking.utils.TimeUtils;

import static zhj.notetaking.activity.SignupActivity.SIGNUP_OK;


public class NoteActivity extends BaseActivity implements View.OnClickListener {
    public static final int LOGIN_OK = 1;
    private static long current_time = 0;      //记录系统当前时间
    public static final int REQUEST_ADD_NOTE = 1;
    public static final int REQUEST_LOGIN = 2;

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
    ScrollView mContentSetting;
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
    @BindView(R.id.tv_sync_note)
    TextView mTvSyncNote;
    @BindView(R.id.tv_sync_timeline)
    TextView mTvSyncTimeline;
    @BindView(R.id.tv_restore_note)
    TextView mTvRestoreNote;
    @BindView(R.id.textView)
    TextView mTextView;


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
    private FileUtils mFileUtils;

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
                    Collections.reverse(data_list);
                    Reflesh();
                    break;
            }
        }
    };
    private MenuItem mItemChange, mItemSearch;
    private ImageView mIv_pic_header;
    private TextView mTv_pic_header;
    private View mHeaderView;
    private View mSearchEditFrame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bmob.initialize(this, "9a694364df25c39dd2772b75b88b6935");
        setContentView(R.layout.activity_note);
        ButterKnife.bind(this);

        InitView();
        data_list = operate.getAll();
        Collections.reverse(data_list);
        setupNoteAdapter();
        Reflesh();
    }


    //设置列数
    private void setSingle() {
        isSingle = !isSingle;
        PrefUtils.putBoolean(this, "isSingle", isSingle);
        layoutManager.setSpanCount(isSingle ? 1 : 2);
    }

    private boolean mSearchCheck;


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
                startActivityForResult(intent, REQUEST_ADD_NOTE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            //正常登陆后回调
            case LOGIN_OK:
                setLogIn();
                break;
            case SIGNUP_OK:
                setLogIn();
            default:
                break;
        }
        if (requestCode == REQUEST_ADD_NOTE) {
            data_list = new Operate(helper.getReadableDatabase()).getAll();
            Collections.reverse(data_list);
            Reflesh();
        }
    }

    private void setSigupLogIn() {
        String username = PrefUtils.getString(NoteActivity.this, "username", "");
        String password = PrefUtils.getString(NoteActivity.this, "password", "");
        BmobUser.loginByAccount(username, password, new LogInListener<User>() {

            @Override
            public void done(User user, BmobException e) {
                if (user != null) {
                    Log.i("smile", "用户登陆成功");
                    UserIsLogIn();
                } else {
                    Log.i("smile", "用户登陆失败");
                    Toasty.error(getApplication(), "登录失败，请检查用户名和密码", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setLogIn() {
        User userInfo = BmobUser.getCurrentUser(User.class);
        if (userInfo != null) {
            String username = userInfo.getUsername();
            Toasty.success(NoteActivity.this, "欢迎 " + username + " 的登录！", Toast.LENGTH_SHORT, true).show();
            UserIsLogIn();
        }

    }


    private void setLogOut() {
        User userInfo = BmobUser.getCurrentUser(User.class);
        userInfo.logOut();
        Toasty.success(NoteActivity.this, "账户成功登出！", Toast.LENGTH_SHORT, true).show();
        UserIsLogOut();
    }

    private void UserIsLogIn() {
        mIv_pic_header.setImageDrawable(getResources().getDrawable(R.drawable.icon_pic));
        mTv_pic_header.setText("用记录来证明存在");
        mHeaderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this)
                        // 设置对话框标题
                        .setTitle("提示")
                        // 设置图标
                        .setMessage("确定登出账号吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setLogOut();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击按钮事件
                    }
                });
                builder.create();
                builder.show();
            }
        });
    }

    private void UserIsLogOut() {
        mIv_pic_header.setImageDrawable(getResources().getDrawable(R.drawable.icon_user));
        mTv_pic_header.setText("未登录");
        mHeaderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(intent, REQUEST_LOGIN);
            }
        });
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
        mTextView.setText("记笔记 "+ NoteTakingUtils.getAppVersion(this,"2.0.0"));
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
                        mItemChange.setVisible(true);
                        mItemSearch.setVisible(true);
                        but.setVisibility(View.VISIBLE);
                        mRlAbout.setVisibility(View.GONE);
                        noteRecycle.setVisibility(View.VISIBLE);
                        mContentSetting.setVisibility(View.GONE);
                        item.setChecked(true);
                        data_list = new Operate(helper.getReadableDatabase()).getAll();
                        Collections.reverse(data_list);
                        Reflesh();
                        break;
                    case R.id.navigation_item3:
                        //读取isRight的属性值
                        mToolBar.setTitle("设置");
                        mItemChange.setVisible(false);
                        mItemSearch.setVisible(false);
                        but.setVisibility(View.GONE);
                        mCheckBox.setChecked(PrefUtils.getBoolean(NoteActivity.this, "isRight", false));
                        noteRecycle.setVisibility(View.GONE);
                        mContentSetting.setVisibility(View.VISIBLE);
                        String sync_time = PrefUtils.getString(NoteActivity.this, "sync_time", "尚未进行备份");
                        mTvSyncTimeline.setText(sync_time);
                        item.setChecked(true);
                        break;
                    case R.id.navigation_sub_item1:
                        mToolBar.setTitle("关于");
                        mItemChange.setVisible(false);
                        mItemSearch.setVisible(false);
                        but.setVisibility(View.GONE);
                        item.setChecked(true);
                        noteRecycle.setVisibility(View.GONE);
                        mContentSetting.setVisibility(View.GONE);
                        mRlAbout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.navigation_sub_item2:
                        mItemChange.setVisible(false);
                        mItemSearch.setVisible(false);
                        but.setVisibility(View.GONE);
                        feedback();
                        break;
                    case R.id.navigation_sub_item3:
                        mItemChange.setVisible(false);
                        mItemSearch.setVisible(false);
                        but.setVisibility(View.GONE);
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
        mHeaderView = nv_menu_left.getHeaderView(0);

        mIv_pic_header = (ImageView) mHeaderView.findViewById(R.id.iv_pic_header);
        mTv_pic_header = (TextView) mHeaderView.findViewById(R.id.tv_pic_header);

        //打开app时检测是否登录，修改侧滑栏图片
        User user = BmobUser.getCurrentUser(User.class);
        if (user != null) {
            UserIsLogIn();
        } else {
            UserIsLogOut();
        }
        mTvSyncNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User bmobUser = BmobUser.getCurrentUser(User.class);
                if (bmobUser == null) {
                    Toasty.warning(NoteActivity.this, "当前未登录，登陆后可进行备份。", Toast.LENGTH_SHORT).show();
                } else {
                    final String mObjectId = bmobUser.getObjectId();
                    BmobQuery query = new BmobQuery("Note");
                    //返回50条数据，如果不加上这条语句，默认返回10条数据
                    //查询playerName叫“比目”的数据
                    query.addWhereEqualTo("uid", mObjectId);
                    query.setLimit(150);
                    //
                    asynTaskBeforeSend();
                    query.findObjectsByTable(new QueryListener<JSONArray>() {
                        @Override
                        public void done(JSONArray ary, final BmobException e) {
                            if (e == null) {
                                onTaskSucceed();
                                Log.i("bmob", "查询成功：" + ary.toString());
                                List<NoteInfo> objectList = new ArrayList<>();
                                final List<NoteInfo> addNoteList = new ArrayList<>();
                                List<NoteInfo> updateNoteList = new ArrayList<>();
                                Map<String, NoteInfo> onLineNoteListMap = new HashMap<String, NoteInfo>();
                                for (int i = 0; i < ary.length(); i++) {
                                    JSONObject temp = null;
                                    try {
                                        temp = (JSONObject) ary.get(i);

                                        String note = temp.optString("note");
                                        String time = temp.optString("time");
                                        String uuid = temp.optString("uuid");
                                        String uid = temp.optString("uid");
                                        String objectId = temp.optString("objectId");
                                        String updateTime = temp.optString("updateTime");
                                        String title = temp.optString("title");
                                        String deleted = temp.optString("deleted");

                                        NoteInfo tempNote = new NoteInfo();
                                        tempNote.setTime(time);
                                        tempNote.setNote(note);
                                        tempNote.setUid(uid);
                                        tempNote.setUuid(uuid);
                                        tempNote.setObjectId(objectId);
                                        tempNote.setUpdateTime(updateTime);
                                        tempNote.setTitle(title);
                                        tempNote.setDeleted(deleted);
                                        objectList.add(tempNote);

                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }

                                if (data_list != null) {
                                    //更新网上备份逻辑
                                    for (NoteInfo info : objectList) {
                                        for (int i = 0; i < data_list.size(); i++) {
                                            if (data_list.get(i).getUuid().contains("9cab4310-2ddc-45ca-b735-2de9a11fd11")) {
                                                continue;
                                            }
                                            if (data_list.get(i).getUuid().equals(info.getUuid()) && !data_list.get(i).getNote().equals(info.getNote())) {
                                                data_list.get(i).setObjectId(info.getObjectId());
                                                if (data_list.get(i).getUid() == null) {
                                                    NoteInfo newUidNote = data_list.get(i);
                                                    newUidNote.setUid(mObjectId);
                                                    updateNoteList.add(newUidNote);
                                                } else {
                                                    updateNoteList.add(data_list.get(i));
                                                }
                                            }
                                        }
                                        onLineNoteListMap.put(info.getUuid(), info);
                                    }
                                    //通过map判断本地note是否已经备份到网上
                                    for (NoteInfo noteInfo : data_list) {
                                        if (noteInfo.getUuid().contains("9cab4310-2ddc-45ca-b735-2de9a11fd11")) {
                                            continue;
                                        }
                                        NoteInfo getNote = onLineNoteListMap.get(noteInfo.getUuid());
                                        if (getNote == null) {
                                            if (noteInfo.getUid() == null) {
                                                NoteInfo newUidNote = noteInfo;
                                                newUidNote.setUid(mObjectId);
                                                addNoteList.add(newUidNote);
                                            } else {
                                                addNoteList.add(noteInfo);
                                            }
                                        }
                                    }

                                    //提交批量添加更新note请求
                                    BmobBatch batch = new BmobBatch();
                                    //批量添加
                                    List<BmobObject> addNote = new ArrayList<BmobObject>();
                                    addNote.addAll(addNoteList);
                                    batch.insertBatch(addNote);
                                    //批量更新
                                    final List<BmobObject> updateNote = new ArrayList<BmobObject>();
                                    updateNote.addAll(updateNoteList);
                                    batch.updateBatch(updateNote);
                                    if (addNoteList.size() + updateNoteList.size() < 1) {
                                        copySuccess(addNoteList.size(), updateNoteList.size());
                                        return;
                                    }
                                    if (addNoteList.size() + updateNoteList.size() > 50) {
                                        Toasty.error(NoteActivity.this, "可爱的用户宝宝，测试服务器承受不了超过50条的数据备份，请删减后分两次上传！", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    //执行批量操作
                                    batch.doBatch(new QueryListListener<BatchResult>() {

                                        @Override
                                        public void done(List<BatchResult> results, BmobException ex) {
                                            if (ex == null) {
                                                //返回结果的results和上面提交的顺序是一样的，请一一对应
                                                for (int i = 0; i < results.size(); i++) {
                                                    BatchResult result = results.get(i);
                                                    if (result.isSuccess()) {//只有批量添加才返回objectId
                                                        Log.i("bmob", "第" + i + "个成功：" + result.getObjectId() + "," + result.getUpdatedAt());
                                                    } else {
                                                        BmobException error = result.getError();
                                                        Log.i("bmob", "第" + i + "个失败：" + error.getErrorCode() + "," + error.getMessage());
                                                    }
                                                }
                                                copySuccess(addNoteList.size(), updateNote.size());
                                            } else {
                                                Log.i("bmob", "失败：" + e.getMessage() + "," + e.getErrorCode());
                                                Toasty.error(NoteActivity.this, "备份失败！" + e.getMessage() + "," + e.getErrorCode(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            } else {
                                onTaskFail();
                                Log.i("bmob", "失败：" + e.getMessage() + "," + e.getErrorCode());
                            }
                        }
                    });

                }
            }
        });
        //还原备份到本地逻辑
        mTvRestoreNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User bmobUser = BmobUser.getCurrentUser(User.class);
                if (bmobUser == null) {
                    Toasty.warning(NoteActivity.this, "当前未登录，登陆后可进行还原操作。", Toast.LENGTH_SHORT).show();
                } else {
                    final String mObjectId = bmobUser.getObjectId();
                    BmobQuery query = new BmobQuery("Note");
                    //返回50条数据，如果不加上这条语句，默认返回10条数据
                    //查询playerName叫“比目”的数据
                    query.addWhereEqualTo("uid", mObjectId);
                    query.setLimit(150);
                    //
                    asynTaskBeforeSend();
                    query.findObjectsByTable(new QueryListener<JSONArray>() {
                        @Override
                        public void done(JSONArray ary, final BmobException e) {
                            if (e == null) {
                                onTaskSucceed();
                                Log.i("bmob", "查询成功：" + ary.toString());
                                List<NoteInfo> objectList = new ArrayList<>();
                                List<NoteInfo> addNoteList = new ArrayList<>();
                                List<NoteInfo> updateNoteList = new ArrayList<>();
                                Map<String, NoteInfo> onLineNoteListMap = new HashMap<String, NoteInfo>();
                                for (int i = 0; i < ary.length(); i++) {
                                    JSONObject temp = null;
                                    try {
                                        temp = (JSONObject) ary.get(i);

                                        String note = temp.optString("note");
                                        String time = temp.optString("time");
                                        String uuid = temp.optString("uuid");
                                        String uid = temp.optString("uid");
                                        String objectId = temp.optString("objectId");
                                        String updateTime = temp.optString("updateTime");
                                        String title = temp.optString("title");
                                        String deleted = temp.optString("deleted");

                                        NoteInfo tempNote = new NoteInfo();
                                        tempNote.setTime(time);
                                        tempNote.setNote(note);
                                        tempNote.setUid(uid);
                                        tempNote.setUuid(uuid);
                                        tempNote.setObjectId(objectId);
                                        tempNote.setUpdateTime(updateTime);
                                        tempNote.setTitle(title);
                                        tempNote.setDeleted(deleted);
                                        objectList.add(tempNote);

                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }

                                if (data_list != null) {
                                    //还原不需要考虑本地数据数量
                                    for (NoteInfo info : objectList) {
                                        for (int i = 0; i < data_list.size(); i++) {
                                            onLineNoteListMap.put(data_list.get(i).getUuid(), data_list.get(i));
                                            if (data_list.get(i).getUuid().equals(info.getUuid()) && !data_list.get(i).getNote().equals(info.getNote())) {
                                                data_list.get(i).setObjectId(info.getObjectId());
                                                if (data_list.get(i).getUid() == null) {
                                                    NoteInfo newUidNote = data_list.get(i);
                                                    newUidNote.setUid(mObjectId);
                                                    updateNoteList.add(newUidNote);
                                                } else {
                                                    updateNoteList.add(data_list.get(i));
                                                }
                                            }
                                        }

                                    }
                                    //通过map判断不存在的note
                                    for (NoteInfo noteInfo : objectList) {
                                        NoteInfo getNote = onLineNoteListMap.get(noteInfo.getUuid());
                                        if (getNote == null) {
                                            if (noteInfo.getUid() == null) {
                                                NoteInfo newUidNote = noteInfo;
                                                newUidNote.setUid(mObjectId);
                                                addNoteList.add(newUidNote);
                                            } else {
                                                addNoteList.add(noteInfo);
                                            }
                                        }
                                    }

                                    //执行批量操作,更新本地数据库和添加
                                    for (NoteInfo noteInfo : addNoteList) {
                                        Operate operate = new Operate(helper.getWritableDatabase());
                                        operate.insert(noteInfo.getNote(), noteInfo.getTime(), noteInfo.getTime(), noteInfo.getUuid());
                                    }
                                    for (NoteInfo noteInfo : updateNoteList) {
                                        Operate operate = new Operate(helper.getWritableDatabase());
                                        operate.update(noteInfo.getNote(), noteInfo.getUpdateTime(), noteInfo.getUuid());
                                    }
                                    syncSuccess(addNoteList.size(), updateNoteList.size());
                                }

                            } else {
                                onTaskFail();
                                Log.i("bmob", "失败：" + e.getMessage() + "," + e.getErrorCode());
                            }
                        }
                    });
                }
            }
        });
    }

    private void copySuccess(int add, int update) {
        Toasty.success(NoteActivity.this, "云端备份成功！新增笔记 " + add + "条，更新笔记 " + update + "条", Toast.LENGTH_SHORT).show();
        mTvSyncTimeline.setText(TimeUtils.getCurrentTimeInString() + "");
        PrefUtils.putString(NoteActivity.this, "sync_time", TimeUtils.getCurrentTimeInString() + "");
    }

    private void syncSuccess(int add, int update) {
        Toasty.success(NoteActivity.this, "备份笔记同步成功！新增笔记 " + add + "条，更新笔记 " + update + "条", Toast.LENGTH_SHORT).show();

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
                startActivityForResult(it, REQUEST_ADD_NOTE);
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
    public void showThemeChooseDialog() {
        AlertDialog.Builder builder = DialogUtils.makeDialogBuilder(NoteActivity.this);
        builder.setTitle(R.string.change_theme);
        Integer[] res = new Integer[]{R.drawable.blue_mix_red_round, R.drawable.pink_mix_yellow_round,
                R.drawable.green_mix_red_round, R.drawable.brown_mix_grey_round,
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
                Log.d("po", "onItemClick: " + i + ":" + ThemeUtils.getCurrentTheme(NoteActivity.this).getIntValue()
                        + ":" + PrefUtils.getInt(NoteActivity.this, "change_theme_key", 0x00));

            }
        });
    }

    public void onThemeChoose(int position) {
        int value = ThemeUtils.getCurrentTheme(NoteActivity.this).getIntValue();
        if (value != position) {
            PrefUtils.putInt(NoteActivity.this, "change_theme_key", position);
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
            Toasty.warning(NoteActivity.this, "您的手机无法发送邮件反馈！", Toast.LENGTH_SHORT, true).show();
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
    private void backupLocal() {
        mFileUtils = new FileUtils();
        mFileUtils.backupNotes(this, data_list);
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
            Toasty.warning(NoteActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT, true).show();
            current_time = System.currentTimeMillis();
        } else {
            finish();
        }
    }


    //设置toolbar的菜单栏
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        mItemChange = menu.findItem(R.id.action_change);
        mItemSearch = menu.findItem(R.id.search);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setQueryHint("搜索笔记");
        setSearchViewListener();
        return true;
    }

    //设置搜索View 的监听
    private void setSearchViewListener() {

        //需要对系统版本做判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            MenuItemCompat.setOnActionExpandListener(mItemSearch,
                    new MenuItemCompat.OnActionExpandListener() {
                        @Override
                        public boolean onMenuItemActionExpand(MenuItem menuItem) {
                            return true;
                        }

                        @Override
                        public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                            //添加searchView关闭事件
                            noteAdapter.setDataAndType(AdapterType.NOTE_TYPE, null);
                            data_list = new Operate(helper.getReadableDatabase()).getAll();
                            Collections.reverse(data_list);
                            Reflesh();
                            return true;
                        }
                    });
        } else {
            mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    //添加searchView关闭事件
                    noteAdapter.setDataAndType(AdapterType.NOTE_TYPE, null);
                    data_list = new Operate(helper.getReadableDatabase()).getAll();
                    Collections.reverse(data_list);
                    Reflesh();
                    return true;
                }
            });
        }
        // 搜索文字监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //输入的同时会进行搜索
                List<NoteInfo> all = new Operate(helper.getReadableDatabase()).getAll();
                Collections.reverse(all);
                final List<NoteInfo> infos = all;
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
                Reflesh();

                return false;
            }
        });
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
