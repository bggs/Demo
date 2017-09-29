package com.example.demo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.demo.R;

/**
 * Created by liujie on 2017/9/25.
 */

public class NextActivity extends AppCompatActivity {

    private EditText mEditUser;
    private EditText mEditPassword;
    private Button mLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        initView();
        initData();
    }

    private void initData() {
        String user = mEditUser.getText().toString().trim();
        String password = mEditPassword.getText().toString().trim();
    }

    private void initView() {
        mEditUser = (EditText) findViewById(R.id.edit_user);
        mEditPassword = (EditText) findViewById(R.id.edit_password);
        mLogin = (Button) findViewById(R.id.btn_login);
    }
}
