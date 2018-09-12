package com.jaydenxiao.common.base;

import android.content.Context;

import com.jaydenxiao.common.baserx.RxManager;

/**
 * des:基类presenter
 * Created by xsf
 * on 2016.07.11:55
 */
public abstract class BasePresenter<T,E>{//包含了 mModel和mView，是M和V之间沟通的桥梁。
    public Context mContext;
    public E mModel;
    public T mView;
    public RxManager mRxManage = new RxManager();

    public void setVM(T v, E m) {//初始化mModel和mView，同时调用onStart()方法
        this.mView = v;
        this.mModel = m;
        this.onStart();
    }
    public void onStart(){
    };
    public void onDestroy() {//调用了mRxManage.clear();取消掉和当前presenter中mRxManage的所有订阅。
        mRxManage.clear();
    }
}
