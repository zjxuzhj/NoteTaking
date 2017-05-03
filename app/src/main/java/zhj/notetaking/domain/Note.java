package zhj.notetaking.domain;

import cn.bmob.v3.BmobObject;

/**
 * Created by HongJay on 2017/5/3.
 */

public class Note extends BmobObject {
    private String note;
    private String time;
    private String uuid;
    private String uid;
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUid() {
        return uid;
    }
}
