package com.jaydenxiao.common.baserx;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * 用于管理单个presenter的RxBus的事件和Rxjava相关代码的生命周期处理
 * Created by xsf
 * on 2016.08.14:50
 */
public class RxManager {
    public RxBus mRxBus = RxBus.getInstance();
    //管理rxbus订阅
    private Map<String, Observable<?>> mObservables = new HashMap<>();
    /*管理Observables 和 Subscribers订阅*/
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    //管理rxbus订阅和网络访问中的订阅（常规订阅），此项目中使用到rxjava的有两类：
//    一是利用rxjava构建rxbus；
//    二是利用rxjava配合retrofit访问网络。
//    两类使用都需要订阅Observable，mCompositeSubscription 在这里是同意管理这两种订阅，方便取消之用，避免内存泄漏。

    /**
     * RxBus注入监听
     * @param eventName
     * @param action1
     */
    public <T>void on(String eventName, Action1<T> action1) {
        Observable<T> mObservable = mRxBus.register(eventName);//首先调用了mRxBus的register方法，注册了eventName事件，得到一个mObservable（Observable的子类，实际是一个subject）
        mObservables.put(eventName, mObservable);//之后存入mObservables（为了方便从上一篇中的subjectMapper中删除这个事件源
        /*订阅管理*/
        mCompositeSubscription.add(mObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(action1, new Action1<Throwable>() {//得到mObservable之后，给这个mObservable添加了订阅者action1，也就是说这个mObservable发送消息的时候，action1中的代码会被执行。
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }));
    }

    /**
     * 单纯的Observables 和 Subscribers管理
     * 用于管理网络数据数据请求//收集Subscription，用于取消订阅
     * @param m
     */
    public void add(Subscription m) {
        /*订阅管理*/
        mCompositeSubscription.add(m);//将这个Subscription 加入到mCompositeSubscription，方便取消。
    }
    /**
     * 单个presenter生命周期结束，取消订阅和所有rxbus观察
     */
    public void clear() {
        mCompositeSubscription.unsubscribe();// 取消所有订阅

        for (Map.Entry<String, Observable<?>> entry : mObservables.entrySet()) {//这个方法在一个界面销毁之时调用，for循环中的内容是取消rxbus观察（即从上一篇中的subjectMapper中删除所有事件源）
            mRxBus.unregister(entry.getKey(), entry.getValue());// 移除rxbus观察
        }
    }
    //发送rxbus
    public void post(Object tag, Object content) {
        mRxBus.post(tag, content);//使用RxBus发送事件消息，吧tag事件的事件源依次取出，发送content内容。
    }
}
