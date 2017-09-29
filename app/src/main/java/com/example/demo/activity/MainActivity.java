package com.example.demo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo.bean.CircleRectData;
import com.example.demo.view.LockView;
import com.example.demo.OnUnlockListener;
import com.example.demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liujie on 2017/9/25.
 */
public class MainActivity extends AppCompatActivity {
    private LockView mLockView;
    private TextView mText;
    private int num = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvents();
    }

    private void initEvents() {
        mLockView.setOnUnlockListener(new OnUnlockListener() {
            @Override
            public boolean isUnlockSuccess(String result) {
                return result.equals("77441155336699");// 设置在什么情况下视为解锁成功
            }

            // 当解锁成功时回调的方法
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "解锁成功!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, NextActivity.class);
                startActivity(intent);
            }

            // 当解锁失败时回调的方法
            @Override
            public void onFailure() {
                if (num != 0) {
                    num--;
                    mText.setText("你还有" + num + "次输入密码的机会!");
                    Toast.makeText(MainActivity.this, "解锁失败!", Toast.LENGTH_SHORT).show();
                } else {
                    mText.setText("屏幕已锁定。请与工作人员联系。");
                    mLockView.reset();
                }
            }
        });
    }

    private void initView() {
        mLockView = (LockView) findViewById(R.id.my_view);
        mText = (TextView) findViewById(R.id.txt_password);
    }
}
