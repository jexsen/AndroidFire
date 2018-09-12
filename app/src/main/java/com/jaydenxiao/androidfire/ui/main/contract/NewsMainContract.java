package com.jaydenxiao.androidfire.ui.main.contract;

import com.jaydenxiao.androidfire.bean.NewsChannelTable;
import com.jaydenxiao.common.base.BaseModel;
import com.jaydenxiao.common.base.BasePresenter;
import com.jaydenxiao.common.base.BaseView;

import java.util.List;

import rx.Observable;

/**
 * des:
 * Created by xsf
 * on 2016.09.11:53
 */
public interface NewsMainContract {//协议

    interface Model extends BaseModel {
        Observable< List<NewsChannelTable> > lodeMineNewsChannels();//用于提供数据。
    }

    interface View extends BaseView {
        void returnMineNewsChannels(List<NewsChannelTable> newsChannelsMine);//用于显示数据。
    }
    abstract static class Presenter extends BasePresenter<View, Model> {
        public abstract void lodeMineChannelsRequest();//lodeMineChannelsRequest中调用Model 的lodeMineNewsChannels方法获取数据，然后调用View 的returnMineNewsChannels方法将数据进行显示。
    }
}
