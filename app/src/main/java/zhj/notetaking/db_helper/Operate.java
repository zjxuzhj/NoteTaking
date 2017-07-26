package zhj.notetaking.db_helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zhj.notetaking.data.NoteInfo;

import static u.aly.au.S;


public class Operate {
    private static final String TABLEENAME = "book";
    private SQLiteDatabase db = null;

    public Operate(SQLiteDatabase db) {
        this.db = db;
    }

    public void insert(String note, String time,String updateTime, String uuid1) {
        String sql = "insert into " + TABLEENAME + " (note,time,updateTime,uuid) values(?,?,?,?)";
        Object obj[] = new Object[]{note, time,updateTime,uuid1};
        this.db.execSQL(sql, obj);
        this.db.close();
    }

    public void update(String old_note, String note, String updateTime) {
        String sql = "update " + TABLEENAME + " set note=?,updateTime=? where note=?";
        Object obj[] = new Object[]{note, updateTime, old_note};
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
        String sql = "select note,time,uuid,updateTime,title,deleted from " + TABLEENAME;
        Cursor cursor = this.db.rawQuery(sql, null);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String note = cursor.getString(0);
            String time = cursor.getString(1);
            String uuid = cursor.getString(2);
            String updateTime = cursor.getString(3);
            String title = cursor.getString(4);
            String deleted = cursor.getString(5);
            NoteInfo noteInfo = new NoteInfo();
            noteInfo.setNote(note);
            noteInfo.setTime(time);
            noteInfo.setUuid(uuid);
            noteInfo.setUpdateTime(updateTime);
            noteInfo.setTitle(title);
            noteInfo.setDeleted(deleted);

            list.add(noteInfo);
        }
        this.db.close();
        return list;
    }
}
