package zhj.notetaking.db_helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zhj.notetaking.data.NoteInfo;


public class Operate {
    private static final String TABLEENAME = "book";
    private SQLiteDatabase db = null;

    public Operate(SQLiteDatabase db) {
        this.db = db;
    }

    public void insert(String note, String time) {
        String sql = "insert into " + TABLEENAME + " (note,time) values(?,?)";
        Object obj[] = new Object[]{note, time};
        this.db.execSQL(sql, obj);
        this.db.close();
    }

    public void update(String old_note, String note, String time) {
        String sql = "update " + TABLEENAME + " set note=?,time=? where note=?";
        Object obj[] = new Object[]{note, time, old_note};
        this.db.execSQL(sql, obj);
        this.db.close();
    }

    public void delete(String note) {
        String sql = "delete from " + TABLEENAME + " where note=?";
        Object obj[] = new Object[]{note};
        this.db.execSQL(sql, obj);
        this.db.close();
    }

    public List<NoteInfo> getAll() {
        List<NoteInfo> list = new ArrayList<>();
        String sql = "select note,time from " + TABLEENAME;
        Cursor cursor = this.db.rawQuery(sql, null);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String note = cursor.getString(0);
            String time = cursor.getString(1);
            NoteInfo noteInfo = new NoteInfo();
            noteInfo.setNote(note);
            noteInfo.setTime(time);
            list.add(noteInfo);
        }
        this.db.close();
        return list;
    }
}
