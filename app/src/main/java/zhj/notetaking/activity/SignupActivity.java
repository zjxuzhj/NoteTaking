package zhj.notetaking.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import es.dmoral.toasty.Toasty;
import zhj.notetaking.R;
import zhj.notetaking.domain.User;
import zhj.notetaking.utils.PrefUtils;
import zhj.notetaking.utils.TimeUtils;


/**
 * 注册页面
 */
public class SignupActivity extends BaseActivity {
    private static final String TAG = "SignupActivity";
    public static final int SIGNUP_OK =0;
    @BindView(R.id.input_name)
    EditText _nameText;
    @BindView(R.id.input_password)
    EditText _passwordText;
    @BindView(R.id.input_reEnterPassword)
    EditText _reEnterPasswordText;
    @BindView(R.id.btn_signup)
    Button _signupButton;
    @BindView(R.id.link_login)
    TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setStartAnim(false);
        setCloseAnim(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_out,R.anim.push_left_in);
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("创建中...");
        progressDialog.show();

        final String name = _nameText.getText().toString();
        final String password = _passwordText.getText().toString();

        User user = new User();
        user.setUsername(name);
        user.setPassword(password);
        user.setPassword_t(password);
        user.signUp(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    Toasty.success(SignupActivity.this, "账号创建成功,请登录", Toast.LENGTH_SHORT).show();
                    onSignupSuccess();
                    // onSignupFailed();
                    progressDialog.dismiss();
                } else {
                    if (e.getMessage().contains("IllegalStateException")) {
                        Toasty.success(SignupActivity.this, "账号创建成功", Toast.LENGTH_SHORT).show();
                        PrefUtils.putString(getApplication(), "username", name);
                        PrefUtils.putString(getApplication(), "password", password);
                        onSignupSuccess();
                        // onSignupFailed();
                        progressDialog.dismiss();
                        return;
                    } else {
                        Log.i("bmob", "失败：" + e.getMessage() + "," + e.getErrorCode());
                        if(e.getMessage().contains("already taken")){
                            Toasty.error(SignupActivity.this, "用户名已被占用！", Toast.LENGTH_SHORT).show();
                        }else{
                            Toasty.error(SignupActivity.this, "失败：" + e.getMessage() + "," + e.getErrorCode(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                        _signupButton.setEnabled(true);
                    }
                }
            }
        });
    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(SIGNUP_OK, null);
        finish();
    }

    public void onSignupFailed() {
        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
//            _nameText.setError("至少三个字符");
            Toasty.warning(getBaseContext(),"用户名至少三个字符",Toast.LENGTH_SHORT).show();
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            Toasty.warning(getBaseContext(),"密码长度大于四，小于十",Toast.LENGTH_SHORT).show();
//            _passwordText.setError("密码长度大于四，小于十");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
//            _reEnterPasswordText.setError("两次密码不相同");
            Toasty.warning(getBaseContext(),"两次密码不相同",Toast.LENGTH_SHORT).show();
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }
}