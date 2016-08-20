package zhj.notetaking.db_helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASENAME = "Note";
    private static final int VERSION = 1;
    private static final String TABLEENAME = "book";

    public DataBaseHelper(Context context){
        super(context,DATABASENAME,null,VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "drop table if exists "+TABLEENAME;
        db.execSQL(sql);
        this.onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table "+TABLEENAME+"(" +
                "id integer primary key," +
                "note text," +
                "time DATE)";
        db.execSQL(sql);

        //2.初始化参数 ContentValues
        ContentValues cv = new ContentValues();

        cv.put("note","长按可以删除此条笔记！");
        cv.put("time", "08-19 11:11:11");
        //返回id long型  如果不成功返回-1
        //1-表名
        //2-空列的默认值
        //3-字段和值的key/value集合
        db.insert(TABLEENAME, null, cv);
        cv.clear();
        cv.put("note","记笔记是一款简洁明了的笔记app，方便你随时随地记录点滴 。喜欢可以给个五分好评哦亲！");
        cv.put("time", "08-19 11:11:12");

        db.insert(TABLEENAME, null, cv);
        cv.clear();
        cv.put("note","点击下方的悬浮按钮可以创建新的笔记。");
        cv.put("time", "08-19 11:11:13");
        db.insert(TABLEENAME, null, cv);
        cv.clear();
        cv.put("note","江城子·乙卯正月二十日夜记梦 十年生死两茫茫，不思量，自难忘。千里孤坟，无处话凄凉。纵使相逢应不识，尘满面，鬓如霜。\n" +
                "夜来幽梦忽还乡，小轩窗，正梳妆。相顾无言，惟有泪千行。料得年年肠断处，明月夜，短松冈。");
        cv.put("time", "08-19 11:11:14");
        db.insert(TABLEENAME, null, cv);

    }
}
