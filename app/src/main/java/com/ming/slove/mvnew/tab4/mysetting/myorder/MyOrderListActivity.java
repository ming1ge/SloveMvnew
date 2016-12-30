package com.ming.slove.mvnew.tab4.mysetting.myorder;

import android.os.Bundle;

import com.ming.slove.mvnew.R;
import com.ming.slove.mvnew.common.base.TabsActivity;

/**
 * Created by Ming on 2016/11/15.
 */

public class MyOrderListActivity extends TabsActivity {


    public static String ORDER_TYPE = "order_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setToolbarTitle(R.string.title_activity_my_order);

        // 初始化mTitles、mFragments等ViewPager需要的数据
        initData();
    }

    private void initData() {
        // Tab的标题
        mTitles = new String[]{"全部", "待付款"};

        //初始化填充到ViewPager中的Fragment集合
        mFragments.add(0, new MyOrderListFragment());

        //将标记值传给fragment，0为待寄快递；1为已寄快递
        for (int i = 0; i < mFragments.size(); i++) {
            Bundle bundle = new Bundle();
            bundle.putInt(ORDER_TYPE, i);
            mFragments.get(i).setArguments(bundle);
        }

        mAdapter.setItem(mTitles, mFragments);
    }
}
