package zhj.notetaking.domain;

import cn.bmob.v3.BmobUser;

/**
 * Created by HongJay on 2017/3/28.
 */

public class User extends BmobUser {


    private String password_t;

    public String getPassword_t() {
        return password_t;
    }

    public void setPassword_t(String password_t) {
        this.password_t = password_t;
    }
}
