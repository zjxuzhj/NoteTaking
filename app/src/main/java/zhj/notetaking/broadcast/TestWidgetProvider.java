package zhj.notetaking.broadcast;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import zhj.notetaking.R;
import zhj.notetaking.activity.AddActivity;
import zhj.notetaking.data.NoteInfo;
import zhj.notetaking.db_helper.DataBaseHelper;
import zhj.notetaking.db_helper.Operate;

import static u.aly.cv.i;

/**
 * Created by HongJay on 2017/8/9.
 */
public class TestWidgetProvider extends AppWidgetProvider {
    public static final String CLICK_ACTION = "zhj.notetaking.action.CLICK"; // 点击事件的广播ACTION
    private List<NoteInfo> data_list = new ArrayList<>();
    private List<NoteInfo> widget_data_list = new ArrayList<>();
    private DataBaseHelper helper = null;

    /**
     * 每次窗口小部件被更新都调用一次该方法
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        helper = new DataBaseHelper(context);
        data_list = new Operate(helper.getReadableDatabase()).getAll();
        Collections.reverse(data_list);
        if(data_list.size()>=3){
            for (int i = 0; i < 3; i++) {
                widget_data_list.add(data_list.get(i));
            }
        }else if(data_list.size()==2){
            widget_data_list.add(data_list.get(0));
            widget_data_list.add(data_list.get(1));
            widget_data_list.add(new NoteInfo());
        }else{
            widget_data_list.add(data_list.get(0));
            widget_data_list.add(new NoteInfo());
            widget_data_list.add(new NoteInfo());
        }

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        Intent intent = new Intent(CLICK_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, R.id.ll_widget, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.ll_widget, pendingIntent);
//        String str_item1=widget_data_list.get(0).getNote();
//        String str_item2=widget_data_list.get(0).getNote();
//        String str_item3=widget_data_list.get(0).getNote();
        remoteViews.setTextViewText(R.id.tv_item_1,widget_data_list.get(0).getNote()==null?"":widget_data_list.get(0).getNote());
        remoteViews.setTextViewText(R.id.tv_item_2,widget_data_list.get(1).getNote()==null?"":widget_data_list.get(1).getNote());
        remoteViews.setTextViewText(R.id.tv_item_3,widget_data_list.get(2).getNote()==null?"":widget_data_list.get(2).getNote());
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    /**
     * 接收窗口小部件点击时发送的广播
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (CLICK_ACTION.equals(intent.getAction())) {
            Toast.makeText(context, "hello dog!", Toast.LENGTH_SHORT).show();
            Intent it = new Intent(context, AddActivity.class);
            it.putExtra("which", "1");
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
        }
    }

    /**
     * 每删除一次窗口小部件就调用一次
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * 当最后一个该窗口小部件删除时调用该方法
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    /**
     * 当该窗口小部件第一次添加到桌面时调用该方法
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    /**
     * 当小部件大小改变时
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * 当小部件从备份恢复时调用该方法
     */
    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }
}
