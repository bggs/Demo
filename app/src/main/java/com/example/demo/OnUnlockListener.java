package com.example.demo;

/**
 * Created by liujie on 2017/9/25.
 * 解锁，手指抬起后回调的借口
 */

public interface OnUnlockListener {
    boolean isUnlockSuccess(String result);//判断解锁是否成功
    void onSuccess();// 当解锁成功时回调的方法
    void onFailure();// 当解锁失败时回调的方法
}
