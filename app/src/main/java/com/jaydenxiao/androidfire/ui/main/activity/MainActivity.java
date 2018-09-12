package com.jaydenxiao.androidfire.ui.main.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.ViewGroup;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.jaydenxiao.androidfire.R;
import com.jaydenxiao.androidfire.app.AppConstant;
import com.jaydenxiao.androidfire.bean.TabEntity;
import com.jaydenxiao.androidfire.ui.main.fragment.CareMainFragment;
import com.jaydenxiao.androidfire.ui.main.fragment.NewsMainFragment;
import com.jaydenxiao.androidfire.ui.main.fragment.PhotosMainFragment;
import com.jaydenxiao.androidfire.ui.main.fragment.VideoMainFragment;
import com.jaydenxiao.common.base.BaseActivity;
import com.jaydenxiao.common.baseapp.AppConfig;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.jaydenxiao.common.daynightmodeutils.ChangeModeController;

import java.util.ArrayList;

import butterknife.Bind;
import cn.hugeterry.updatefun.UpdateFunGO;
import cn.hugeterry.updatefun.config.UpdateKey;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import rx.functions.Action1;

/**
 * des:主界面
 * Created by xsf
 * on 2016.09.15:32
 */
public class MainActivity extends BaseActivity {//继承自BaseActivity
    @Bind(R.id.tab_layout)
    CommonTabLayout tabLayout; //初始化MainActivisssty布局,开源项目布局com.flyco.tablayout.CommonTabLayout

    private String[] mTitles = {"首页", "美女","视频","关注"};  //底部标签名字集合
    private int[] mIconUnselectIds = {
            R.mipmap.ic_home_normal,R.mipmap.ic_girl_normal,R.mipmap.ic_video_normal,R.mipmap.ic_care_normal};//设置底部图片数组,灰色，未点击


    private int[] mIconSelectIds = {
            R.mipmap.ic_home_selected,R.mipmap.ic_girl_selected, R.mipmap.ic_video_selected,R.mipmap.ic_care_selected};//设置底部图片数组,黄色，点击


    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();  //创建CustomTabEntity集合,CustomTabEntity是一个接口

    //四个fragment
    private NewsMainFragment newsMainFragment;   //新闻
    private PhotosMainFragment photosMainFragment;  //图片
    private VideoMainFragment videoMainFragment;    //视频
    private CareMainFragment careMainFragment;  //关注
    private static int tabLayoutHeight;//tabLayout高度

    /**
     * 入口
     * @param activity
     */
    public static void startAction(Activity activity){//由起始页SplashActivity来启动这个mainactivity
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);

        activity.overridePendingTransition(R.anim.fade_in,
                com.jaydenxiao.common.R.anim.fade_out);          //添加两个activity之间的动画切换效果
//        关于overridePendingTransition这个函数，有两点需要主意
//        1. 它必需紧挨着startActivity()或者finish()函数之后调用”
//        2. 它只在android2.0以及以上版本上适用
    }

    @Override
    public int getLayoutId() {
        return R.layout.act_main; //返回MainActivity对应的xml
    }

    @Override
    public void initPresenter() {

    }
    @Override
    public void initView() {//BaseActivity里的initView方法的重写
        //此处填上在http://fir.im/注册账号后获得的API_TOKEN以及APP的应用ID
        UpdateKey.API_TOKEN = AppConfig.API_FIRE_TOKEN;
        UpdateKey.APP_ID = AppConfig.APP_FIRE_ID;

        //如果你想通过Dialog来进行下载，可以如下设置
//        UpdateKey.DialogOrNotification=UpdateKey.WITH_DIALOG;

        UpdateFunGO.init(this);//fir.im更新框架

        //初始化菜单
        initTab();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //切换daynight模式要立即变色的页面
        ChangeModeController.getInstance().init(this,R.attr.class); //初始化夜间模式 用到了网上的一个夜间模式项目的开源，类文件在library的daynightmodeutils下面

        super.onCreate(savedInstanceState);

        //初始化frament
        initFragment(savedInstanceState);

        tabLayout.measure(0,0);//其中第四种方法，网上有很多直接传递两个0的写法，即 view.measure(0,0).


        tabLayoutHeight=tabLayout.getMeasuredHeight();//得到tabLayout控件的高度

        //监听菜单显示或隐藏 用到了RxManager框架
        mRxManager.on(AppConstant.MENU_SHOW_HIDE, new Action1<Boolean>() {//组件通信 RxManager

            @Override
            public void call(Boolean hideOrShow) {
                startAnimation(hideOrShow);//菜单显示隐藏动画
            }
        });
    }
    /**
     * 初始化tab
     */
    private void initTab() {//初始化菜单
        //遍历mTitles，加入到ArrayList<CustomTabEntity> mTabEntities集合中，其中CustomTabEntity是开源项目的接口类
        for (int i = 0; i < mTitles.length; i++) {    // private String[] mTitles = {"首页", "美女","视频","关注"};  //底部标签名字集合
            mTabEntities.add(new TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i]));
        }


        tabLayout.setTabData(mTabEntities);//设置tab数据ArrayList<CustomTabEntity> mTabEntities


        //点击监听
        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {//当tab被选中
                SwitchTo(position);//自写方法，切换到相应位置
            }
            @Override
            public void onTabReselect(int position) {//当tab重新被选择时
            }
        });
    }

    /**
     * 初始化碎片
     */
    private void initFragment(Bundle savedInstanceState) {//初始化fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();//拿到FragmentTransaction对象transaction
//        Fragment是我们经常用到的东西，常用的操作有添加(add)、移除(remove)、替换(replace)等，
//        这些操作组成一个集合transaction，
//        我们在通过调用getSupportFragmentManager().beginTransaction()来获取这个FragmentTransaction类的实例来管理这些操作，
//        将他们存进由activity管理的back stack中，这样我们就可以进行fragment变化的回退操作，也可以这样去获取FragmentTransaction类的实例

        int currentTabPosition = 0;

        if (savedInstanceState != null) {
            newsMainFragment = (NewsMainFragment) getSupportFragmentManager().findFragmentByTag("newsMainFragment");//要去指定fragment的tag
            photosMainFragment = (PhotosMainFragment) getSupportFragmentManager().findFragmentByTag("photosMainFragment");
            videoMainFragment = (VideoMainFragment) getSupportFragmentManager().findFragmentByTag("videoMainFragment");
            careMainFragment = (CareMainFragment) getSupportFragmentManager().findFragmentByTag("careMainFragment");

            currentTabPosition = savedInstanceState.getInt(AppConstant.HOME_CURRENT_TAB_POSITION);//拿到之前通过savedInstanceState存储的currentTabPosition
        } else {//如果没有找到currentTabPosition,创建新的fragment
            newsMainFragment = new NewsMainFragment();
            photosMainFragment = new PhotosMainFragment();
            videoMainFragment = new VideoMainFragment();
            careMainFragment = new CareMainFragment();

            transaction.add(R.id.fl_body, newsMainFragment, "newsMainFragment");//R.id.fl_body是FrameLayout,这里置入了tag名字
            transaction.add(R.id.fl_body, photosMainFragment, "photosMainFragment");
            transaction.add(R.id.fl_body, videoMainFragment, "videoMainFragment");
            transaction.add(R.id.fl_body, careMainFragment, "careMainFragment");
        }
        transaction.commit();//提交

        SwitchTo(currentTabPosition);//如果有savedInstanceState存储，就变为存储的fragment页面，如果没有就变为0

        tabLayout.setCurrentTab(currentTabPosition);//CommonTabLayout布局对象设置当前tab为currentTabPosition位置

    }

    /**
     * 切换
     */
    private void SwitchTo(int position) {//拿到相应postion切换页面
        LogUtils.logd("主页菜单position" + position);//library库中写的Log工具类

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (position) {
            //首页
            case 0:
                transaction.hide(photosMainFragment);
                transaction.hide(videoMainFragment);
                transaction.hide(careMainFragment);
                transaction.show(newsMainFragment);
                transaction.commitAllowingStateLoss();//commit()函数和commitAllowingStateLoss()函数的唯一区别就是当发生状态丢失的时候，后者不会抛出一个异常。通常你不应该使用这个函数，因为它意味可能发生状态丢失。
//                transaction.commit();
                break;
            //美女
            case 1:
                transaction.hide(newsMainFragment);
                transaction.hide(videoMainFragment);
                transaction.hide(careMainFragment);
                transaction.show(photosMainFragment);
                transaction.commitAllowingStateLoss();
//                为什么我们会有这种报错呢，因为我们在使用add(),remove(),replace()等方法将Fragment的变化添加进去，
//                然后在通过commit去提交这些变化（另外，在commit之前可以去调用addToBackState()方法，
//                将这些变化加入到activity管理的back stack中去，这样用户调用返回键就可以回退这些变化了），
//                提交完成之后这些变化就会应用到我们的Fragment中去。但是，这个commit()方法，你只能在avtivity存储他的状态之前调用，
//                也就是onSaveInstanceState()，我们都知道activity有一个保存状态的方法和恢复状态的方法，这个就不详细解释了，
//                在onSaveInstanceState()方法之后去调用commit()，就会抛出我们遇到的这个异常，
//                这是因为在onSaveInstanceState()之后调用commit()方法，这些变化就不会被activity存储，
//                即这些状态会被丢失，但我们可以去用commitAllowingStateLoss()这个方法去代替commit()来解决这个为题，
//                下面我们通过源码去看这两个方法的区别。
                break;
            //视频
            case 2:
                transaction.hide(newsMainFragment);
                transaction.hide(photosMainFragment);
                transaction.hide(careMainFragment);
                transaction.show(videoMainFragment);
                transaction.commitAllowingStateLoss();
                break;
            //关注
            case 3:
                transaction.hide(newsMainFragment);
                transaction.hide(photosMainFragment);
                transaction.hide(videoMainFragment);
                transaction.show(careMainFragment);
                transaction.commitAllowingStateLoss();
                break;
            default:
                break;
        }
    }

    /**
     * 菜单显示隐藏动画
     * @param showOrHide
     */
    private void startAnimation(boolean showOrHide){//showOrHide默认值是false
        final ViewGroup.LayoutParams layoutParams = tabLayout.getLayoutParams();
        ValueAnimator valueAnimator;
        ObjectAnimator alpha;

        if(!showOrHide){
             valueAnimator = ValueAnimator.ofInt(tabLayoutHeight, 0);   //定义animator

            alpha = ObjectAnimator.ofFloat(tabLayout, "alpha", 1, 0);//第一个参数：指定执行动画的控件，第二个参数：指定控件的属性，第三个参数是可变长参数
            ////alpha为透明度动画
        }else{
             valueAnimator = ValueAnimator.ofInt(0, tabLayoutHeight);

            alpha = ObjectAnimator.ofFloat(tabLayout, "alpha", 0, 1);
        }

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {//动画更新监听器
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                layoutParams.height= (int) valueAnimator.getAnimatedValue();

                tabLayout.setLayoutParams(layoutParams);
            }
        });
        AnimatorSet animatorSet=new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.playTogether(valueAnimator,alpha); //一起播放动画
        animatorSet.start();
    }

    /**
     * 监听全屏视频时返回键
     */
    @Override
    public void onBackPressed() {//当按下返回键时
        if (JCVideoPlayer.backPress()) {//视频框架类JCVideoPlayer执行backPress()方法
            return;
        }
        super.onBackPressed();
    }
    /**
     * 监听返回键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//如果点击的是后退键
            moveTaskToBack(false);//nonRoot=false→ 仅当activity为task根（即首个activity例如启动activity之类的）时才生效,效果基本等同于home键
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {//防止APP中不再栈顶的activity给回收掉,bundle有很多保存数据的方法
                                                            //onSaveInstanceState()回调方法会保证一定在activity被回收之前调用
        super.onSaveInstanceState(outState);

        //奔溃前保存位置
        LogUtils.loge("onSaveInstanceState进来了1");

        if (tabLayout != null) {//如果tabLayout是有效的
            LogUtils.loge("onSaveInstanceState进来了2");
            outState.putInt(AppConstant.HOME_CURRENT_TAB_POSITION, tabLayout.getCurrentTab());//将int值tabLayout.getCurrentTab()保存下来
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateFunGO.onResume(this);  //cn.hugeterry.updatefun自动更新框架
    }

    @Override
    protected void onStop() {
        super.onStop();
        UpdateFunGO.onStop(this);//cn.hugeterry.updatefun自动更新框架
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChangeModeController.onDestory();   //夜间模式控制器
    }
}
