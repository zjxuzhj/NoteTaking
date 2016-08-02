package zhj.notetaking.db_helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Operate {
    private static final String TABLEENAME = "book";
    private SQLiteDatabase db = null;

    public Operate(SQLiteDatabase db){
        this.db = db;
    }

    public void insert(String note, String time){
        String sql = "insert into "+TABLEENAME+" (note,time) values(?,?)";
        Object obj[] = new Object[]{note,time};
        this.db.execSQL(sql,obj);
        this.db.close();
    }

    public void update(String old_note, String note, String time){
        String sql = "update "+TABLEENAME+" set note=?,time=? where note=?";
        Object obj[] = new Object[]{note,time,old_note};
        this.db.execSQL(sql,obj);
        this.db.close();
    }

    public void delete(String note){
        String sql = "delete from "+TABLEENAME+" where note=?";
        Object obj[] = new Object[]{note};
        this.db.execSQL(sql,obj);
        this.db.close();
    }

    public List<Map<String,String>> set(){
        List<Map<String,String>> list = new ArrayList<>();
        String sql = "select note,time from "+TABLEENAME;
        Cursor cursor = this.db.rawQuery(sql,null);
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
            Map<String,String> map = new HashMap<>();
            map.put("note",cursor.getString(0));
            map.put("time",cursor.getString(1));
            list.add(map);
        }
        this.db.close();
        return list;
    }
}
