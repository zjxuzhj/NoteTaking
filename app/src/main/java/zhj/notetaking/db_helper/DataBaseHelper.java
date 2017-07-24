package zhj.notetaking.db_helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASENAME = "Note";
    private static final int VERSION = 3;
    private static final String TABLENAME = "book";

    public DataBaseHelper(Context context) {
        super(context, DATABASENAME, null, VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            db.execSQL("ALTER TABLE " + TABLENAME + " ADD COLUMN 'uuid' TEXT DEFAULT a");
            String sql = "select * from " + TABLENAME;
            Cursor cursor = db.rawQuery(sql, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String note = cursor.getString(1);
                String time = cursor.getString(2);
                String uuid = UUID.randomUUID().toString();
                ContentValues values = new ContentValues();
                values.put("note", note);
                values.put("time", time);
                values.put("uuid", uuid);
                db.update(TABLENAME, values, "note=?", new String[]{note});
            }
        } else if (oldVersion == 2) {
            db.execSQL("ALTER TABLE " + TABLENAME + " ADD COLUMN 'updateTime'");
            db.execSQL("ALTER TABLE " + TABLENAME + " ADD COLUMN 'title' ");
            db.execSQL("ALTER TABLE " + TABLENAME + " ADD COLUMN 'deleted' ");
        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
//        String sql = "CREATE TABLE IF NOT EXISTS " + TABLENAME + "(" +
//                "id integer primary key," +
//                "note text," +
//                "time DATE)";
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLENAME + "(" +
                "id integer primary key," +
                "note text," +
                "time DATE," +
                "uuid text)";
        db.execSQL(sql);
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss");
        long time = System.currentTimeMillis();
        Date d1 = new Date(time);
        String t1 = format.format(d1);
        //2.初始化参数 ContentValues
        ContentValues cv = new ContentValues();

        cv.put("note", "长按可以删除此条笔记！");
        cv.put("time", t1);
        cv.put("uuid", "9cab4310-2ddc-45ca-b735-2de9a11fd111");
        //返回id long型  如果不成功返回-1
        //1-表名
        //2-空列的默认值
        //3-字段和值的key/value集合
        db.insert(TABLENAME, null, cv);
        cv.clear();
        cv.put("note", "记笔记是一款简洁明了的笔记app，方便你随时随地记录点滴 。喜欢可以给个五分好评哦亲！");
        cv.put("time", t1);
        cv.put("uuid", "9cab4310-2ddc-45ca-b735-2de9a11fd112");
        db.insert(TABLENAME, null, cv);
        cv.clear();
        cv.put("note", "点击下方的悬浮按钮可以创建新的笔记。");
        cv.put("time", t1);
        cv.put("uuid", "9cab4310-2ddc-45ca-b735-2de9a11fd113");
        db.insert(TABLENAME, null, cv);
        cv.clear();
        cv.put("note", "江城子·乙卯正月二十日夜记梦 十年生死两茫茫，不思量，自难忘。千里孤坟，无处话凄凉。纵使相逢应不识，尘满面，鬓如霜。\n" +
                "夜来幽梦忽还乡，小轩窗，正梳妆。相顾无言，惟有泪千行。料得年年肠断处，明月夜，短松冈。");
        cv.put("time", t1);
        cv.put("uuid", "9cab4310-2ddc-45ca-b735-2de9a11fd114");
        db.insert(TABLENAME, null, cv);

    }
}
