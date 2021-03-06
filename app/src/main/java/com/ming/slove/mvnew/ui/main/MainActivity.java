package com.ming.slove.mvnew.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bilibili.magicasakura.utils.ThemeUtils;
import com.bilibili.magicasakura.widgets.TintImageView;
import com.bilibili.magicasakura.widgets.TintTextView;
import com.google.gson.Gson;
import com.igexin.sdk.PushManager;
import com.ming.slove.mvnew.R;
import com.ming.slove.mvnew.api.other.OtherApi;
import com.ming.slove.mvnew.app.APPS;
import com.ming.slove.mvnew.common.receiver.MsgHelper;
import com.ming.slove.mvnew.common.utils.BaseTools;
import com.ming.slove.mvnew.common.utils.StringUtils;
import com.ming.slove.mvnew.model.bean.JsonMsg_NewFriend;
import com.ming.slove.mvnew.model.bean.EbankWifiConnect;
import com.ming.slove.mvnew.model.bean.FriendDetail;
import com.ming.slove.mvnew.model.bean.IpPort;
import com.ming.slove.mvnew.model.bean.MessageList;
import com.ming.slove.mvnew.model.bean.JsonMsg_Share;
import com.ming.slove.mvnew.model.database.ChatMsgModel;
import com.ming.slove.mvnew.model.database.FriendsModel;
import com.ming.slove.mvnew.model.database.InstantMsgModel;
import com.ming.slove.mvnew.model.database.MyDB;
import com.ming.slove.mvnew.model.database.NewFriendModel;
import com.ming.slove.mvnew.model.event.ChangeThemeColorEvent;
import com.ming.slove.mvnew.model.event.InstantMsgEvent;
import com.ming.slove.mvnew.model.event.NewFriendEvent;
import com.ming.slove.mvnew.model.event.RefreshTab2Event;
import com.ming.slove.mvnew.model.event.ShopApplyPassEvent;
import com.ming.slove.mvnew.model.event.ShowSideBarEvent;
import com.ming.slove.mvnew.shop.MyShopFragment;
import com.ming.slove.mvnew.shop.ShowYingShanFragment;
import com.ming.slove.mvnew.tab1.WebFragment;
import com.ming.slove.mvnew.tab2.IMFragment;
import com.ming.slove.mvnew.tab3.villagelist.VillageListFragment;
import com.ming.slove.mvnew.tab4.SettingWebFragment;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    public List<Fragment> fragments = new ArrayList<>();
    @Bind(R.id.viewPager_main)
    MyViewPager viewPager;
    @Bind(R.id.img_tab1_main_1)
    ImageView mTab11;
    @Bind(R.id.img_tab1_main_0)
    ImageView mTab10;
    @Bind(R.id.text_tab1_main)
    TextView tTab1;
    @Bind(R.id.img_tab2_main_1)
    ImageView mTab21;
    @Bind(R.id.img_tab2_main_0)
    ImageView mTab20;
    @Bind(R.id.text_tab2_main)
    TextView tTab2;
    @Bind(R.id.img_tab3_main_1)
    ImageView mTab31;
    @Bind(R.id.img_tab3_main_0)
    ImageView mTab30;
    @Bind(R.id.text_tab3_main)
    TextView tTab3;
    @Bind(R.id.img_tab4_main_1)
    ImageView mTab41;
    @Bind(R.id.img_tab4_main_0)
    ImageView mTab40;
    @Bind(R.id.text_tab4_main)
    TextView tTab4;
    @Bind(R.id.toolbar_main)
    Toolbar mToolBar;
    @Bind(R.id.tab3_guide)
    ImageView tab3Guide;
    @Bind(R.id.toolbar_title_main)
    TextView toolbarTitle;
    @Bind(R.id.badge)
    TextView badge;
    @Bind(R.id.img_tab5_main_1)
    TintImageView mTab51;
    @Bind(R.id.img_tab5_main_0)
    ImageView mTab50;
    @Bind(R.id.text_tab5_main)
    TintTextView tTab5;
    @Bind(R.id.tab5Layout)
    RelativeLayout tab5Layout;
    private MyOnPageChangeListener myOnPageChangeListener;

    private FragmentManager fragmentManager;
    private boolean isExit;//是否退出
    private int idToolbar = 1;//toolbar 功能按钮页
    private boolean isFirstRun;//是否初次运行
    private int isShopOwner;//是否是店长,1是0不是
    private int isShowYingshan;//是否是县长,1是0不是

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mToolBar.setTitle("");
        setSupportActionBar(mToolBar);
        EventBus.getDefault().register(this);

        initView();
        //登录后，向后台获取消息
        MsgHelper.getInstance().getMessageList(this,false);
        //WiFi连接到ebank网络的认证
//        autoConnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void autoConnect() {
        final String auth = Hawk.get(APPS.USER_AUTH);
        OtherApi.getService().get_IpPort()
                .flatMap(new Func1<IpPort, Observable<EbankWifiConnect>>() {
                    @Override
                    public Observable<EbankWifiConnect> call(IpPort ipPort) {
                        return OtherApi.getService().get_EbankWifiConnect(ipPort.getIp(), ipPort.getPort(), ipPort.getMac(), auth);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<EbankWifiConnect>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
//                        Toast.makeText(MainActivity.this, "没有连接ebank", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(EbankWifiConnect ebankWifiConnect) {
                        if ("1".equals(ebankWifiConnect.getStatus()))
                            Toast.makeText(MainActivity.this, "恭喜你上网认证通过,获得两小时上网时间!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //状态栏主题变色
        BaseTools.colorStatusBar(this);
    }

    //主页为singletop模式，更换主题后手动刷新
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeThemeColor(ChangeThemeColorEvent event) {
        //更改主题后，改变tab4文字颜色
        int themeColor = ThemeUtils.getColorById(this, R.color.theme_color_primary);
        tTab4.setTextColor(themeColor);
    }

    //申请店长通过后，界面调整
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void ApplyPassed(ShopApplyPassEvent event) {
        isShopOwner = 1;
        tab5Layout.setVisibility(View.VISIBLE);

        fragments.add(3, new MyShopFragment());
        viewPager.setAdapter(new MyPagerAdapter());
        viewPager.setCurrentItem(4, true);

        viewPager.removeOnPageChangeListener(myOnPageChangeListener);
        viewPager.addOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void initView() {
        //数据库初始化
        MyDB.createDb(this);
        //个推,初始化SDK
        PushManager.getInstance().initialize(this.getApplicationContext());

        isShowYingshan = Hawk.get(APPS.IS_SHOW_YINGSHAN, 0);
        isShopOwner = Hawk.get(APPS.IS_SHOP_OWNER);

        fragments.add(new WebFragment());
        fragments.add(new IMFragment());
        fragments.add(new VillageListFragment());
        if (isShowYingshan == 1) {//若为营山县长
            fragments.add(new ShowYingShanFragment());
            tab5Layout.setVisibility(View.VISIBLE);
            tTab5.setText("我的县");
        } else if (isShopOwner == 1) {//若为店长
            fragments.add(new MyShopFragment());
            tab5Layout.setVisibility(View.VISIBLE);
        } else {
            tab5Layout.setVisibility(View.GONE);
        }
        fragments.add(new SettingWebFragment());

        fragmentManager = this.getSupportFragmentManager();

        viewPager.setSlipping(true);//设置ViewPager是否可以滑动
        viewPager.setOffscreenPageLimit(fragments.size());

        myOnPageChangeListener = new MyOnPageChangeListener();
        viewPager.addOnPageChangeListener(myOnPageChangeListener);
        viewPager.setAdapter(new MyPagerAdapter());

        //初次运行软件，指导添加村
        isFirstRun = Hawk.get(APPS.IS_FIRST_RUN, false);//****暂时关闭此引导
        if (isShopOwner == 0) {
            if (isFirstRun) {
                viewPager.setCurrentItem(2, true);
                toolbarTitle.setText(getResources().getText(R.string.tab3_main));
                tab3Guide.setVisibility(View.VISIBLE);
                Hawk.put(APPS.IS_FIRST_RUN, false);
            } else {
                toolbarTitle.setText(getResources().getText(R.string.tab1_main_1));
            }
        } else {
            toolbarTitle.setText(getResources().getText(R.string.tab1_main_1));
        }
    }

    /**
     * 接收到消息，更新tab2处消息徽章计数
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showCount(RefreshTab2Event event) {
        int count = event.getCount();
        if (count > 0) {
            badge.setVisibility(View.VISIBLE);
            badge.setText(String.valueOf(count));
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    //    /**
//     * 接收到新的朋友请求消息，更新tab2处消息徽章计数
//     *
//     * @param event 3
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void showCount2(NewFriendEvent event) {
//        List<NewFriendModel> nFriends = MyDB.getQueryAll(NewFriendModel.class);
//        int count = 0;
//        for (NewFriendModel nFriend : nFriends) {
//            count += nFriend.getCount();
//        }
//        if (count > 0) {
//            badge2.setVisibility(View.VISIBLE);
//            badge2.setText(String.valueOf(count));
//        } else {
//            badge2.setVisibility(View.GONE);
//        }
//    }
    @OnClick({R.id.tab3_guide, R.id.tab1Layout, R.id.tab2Layout, R.id.tab3Layout, R.id.tab4Layout, R.id.tab5Layout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tab3_guide://点击新用户引导
                tab3Guide.setVisibility(View.GONE);
                break;
            case R.id.tab1Layout:
                viewPager.setCurrentItem(0);//选中index页
                break;
            case R.id.tab2Layout:
                viewPager.setCurrentItem(1);
                break;
            case R.id.tab3Layout:
                viewPager.setCurrentItem(2);
                break;
            case R.id.tab4Layout:
                if (isShopOwner == 0 && isShowYingshan == 0) {
                    viewPager.setCurrentItem(3);
                } else {
                    viewPager.setCurrentItem(4);
                }
                break;
            case R.id.tab5Layout://我的店or我的县
                viewPager.setCurrentItem(3);
                break;
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.action_friendlist) {
//            Intent intent = new Intent(this, FriendListActivity.class);
//            startActivity(intent);
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    /*@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (idToolbar) {
            case 1://首页，刷新页面
                menu.findItem(R.id.action_refresh).setVisible(true);
                menu.findItem(R.id.action_friendlist).setVisible(false);
                menu.findItem(R.id.action_follow).setVisible(false);
                menu.findItem(R.id.action_theme).setVisible(false);
                break;
            case 2://消息页面，进入好友列表
                menu.findItem(R.id.action_refresh).setVisible(false);
                menu.findItem(R.id.action_friendlist).setVisible(true);
                menu.findItem(R.id.action_follow).setVisible(false);
                menu.findItem(R.id.action_theme).setVisible(false);
                break;
            case 3://村圈页面，进入关注村圈
                menu.findItem(R.id.action_refresh).setVisible(false);
                menu.findItem(R.id.action_friendlist).setVisible(false);
                menu.findItem(R.id.action_follow).setVisible(true);
                menu.findItem(R.id.action_theme).setVisible(false);
                break;
            case 4://设置页面，进入主题切换
                menu.findItem(R.id.action_refresh).setVisible(true);
                menu.findItem(R.id.action_friendlist).setVisible(false);
                menu.findItem(R.id.action_follow).setVisible(false);
                menu.findItem(R.id.action_theme).setVisible(true);
                break;
            case 5://我的县，刷新页面
                if (isShowYingshan == 1) {
                    menu.findItem(R.id.action_refresh).setVisible(true);
                    menu.findItem(R.id.action_friendlist).setVisible(false);
                    menu.findItem(R.id.action_follow).setVisible(false);
                    menu.findItem(R.id.action_theme).setVisible(false);
                }
                break;
            default://其他页面，无快捷按钮
                menu.findItem(R.id.action_refresh).setVisible(false);
                menu.findItem(R.id.action_friendlist).setVisible(false);
                menu.findItem(R.id.action_follow).setVisible(false);
                menu.findItem(R.id.action_theme).setVisible(false);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }*/

    @Override
    public void onBackPressed() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 页卡切换监听,点击改变图标外观
     */
    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int arg0) {
            int themeColor = ThemeUtils.getColorById(MainActivity.this, R.color.theme_color_primary);
            switch (arg0) {
                case 0://首页
                    toolbarTitle.setText(R.string.tab1_main_1);
                    idToolbar = 1;

                    tTab1.setTextColor(themeColor);   //选中时的字体颜色
                    mTab11.setVisibility(View.VISIBLE);
                    mTab10.setVisibility(View.GONE);
                    setTab2ToB();
                    setTab3ToB();
                    setTab4ToB();
                    setTab5ToB();
                    break;
                case 1://消息
                    toolbarTitle.setText(R.string.tab2_main);
                    idToolbar = 2;

                    tTab2.setTextColor(themeColor);
                    mTab21.setVisibility(View.VISIBLE);
                    mTab20.setVisibility(View.GONE);
                    setTab1ToB();
                    setTab3ToB();
                    setTab4ToB();
                    setTab5ToB();
                    break;
                case 2://我的村
                    toolbarTitle.setText(R.string.tab3_main);
                    idToolbar = 3;

                    tTab3.setTextColor(themeColor);
                    mTab31.setVisibility(View.VISIBLE);
                    mTab30.setVisibility(View.GONE);
                    setTab1ToB();
                    setTab2ToB();
                    setTab4ToB();
                    setTab5ToB();
                    break;
                case 3://县长：我的县；店长：我的店；普通：设置
                    if (isShowYingshan == 1) {//县长：我的县
                        toolbarTitle.setText(R.string.tab5_main_1);
                        idToolbar = 5;

                        tTab5.setTextColor(themeColor);
                        mTab51.setVisibility(View.VISIBLE);
                        mTab50.setVisibility(View.GONE);
                        setTab1ToB();
                        setTab2ToB();
                        setTab3ToB();
                        setTab4ToB();
                    } else if (isShopOwner == 1) {//店长：我的店
                        toolbarTitle.setText(R.string.tab5_main);
                        idToolbar = 5;

                        tTab5.setTextColor(themeColor);
                        mTab51.setVisibility(View.VISIBLE);
                        mTab50.setVisibility(View.GONE);
                        setTab1ToB();
                        setTab2ToB();
                        setTab3ToB();
                        setTab4ToB();
                    } else {//普通：设置
                        toolbarTitle.setText(R.string.tab4_main);
                        idToolbar = 4;

                        tTab4.setTextColor(themeColor);
                        mTab41.setVisibility(View.VISIBLE);
                        mTab40.setVisibility(View.GONE);
                        setTab1ToB();
                        setTab2ToB();
                        setTab3ToB();
                        setTab5ToB();
                    }
                    break;
                case 4://店长or县长：设置
                    toolbarTitle.setText(R.string.tab4_main);
                    idToolbar = 4;

                    tTab4.setTextColor(themeColor);
                    mTab41.setVisibility(View.VISIBLE);
                    mTab40.setVisibility(View.GONE);
                    setTab1ToB();
                    setTab2ToB();
                    setTab3ToB();
                    setTab5ToB();
                    break;
            }
            invalidateOptionsMenu();
        }


        private void setTab1ToB() {
            tTab1.setTextColor(getResources().getColor(R.color.tab_bnt0));
            mTab11.setVisibility(View.GONE);
            mTab10.setVisibility(View.VISIBLE);
        }

        private void setTab2ToB() {
            tTab2.setTextColor(getResources().getColor(R.color.tab_bnt0));
            mTab21.setVisibility(View.GONE);
            mTab20.setVisibility(View.VISIBLE);
        }

        private void setTab3ToB() {
            tTab3.setTextColor(getResources().getColor(R.color.tab_bnt0));
            mTab31.setVisibility(View.GONE);
            mTab30.setVisibility(View.VISIBLE);
        }

        private void setTab4ToB() {
            tTab4.setTextColor(getResources().getColor(R.color.tab_bnt0));
            mTab41.setVisibility(View.GONE);
            mTab40.setVisibility(View.VISIBLE);
        }

        private void setTab5ToB() {
            tTab5.setTextColor(getResources().getColor(R.color.tab_bnt0));
            mTab51.setVisibility(View.GONE);
            mTab50.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
            switch (arg0) {
                case 0://什么都没做
                    break;
                case 1://正在滑动
                    EventBus.getDefault().post(new ShowSideBarEvent(false));
                    break;
                case 2://滑动完毕了
                    EventBus.getDefault().post(new ShowSideBarEvent(true));
                    break;
            }
        }
    }

    /**
     * 填充ViewPager的数据适配器
     */
    private class MyPagerAdapter extends PagerAdapter {
        /**
         * 获取要滑动的控件的数量
         *
         * @return 页数
         */
        @Override
        public int getCount() {
            return fragments.size();
        }

        /**
         * 来判断显示的是否是同一页，这里我们将两个参数相比较返回即可
         *
         * @param arg0
         * @param arg1
         * @return
         */
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        /**
         * PagerAdapter只缓存四页Tab图，如果滑动的Fragment超出了缓存的范围，就会调用这个方法，将其销毁
         *
         * @param container
         * @param position
         * @param object
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(fragments.get(position).getView());
        }

        /**
         * 当要页面可以进行缓存的时候，会调用这个方法进行显示Tab的初始化，我们将要显示的Fragment加入到ViewGroup中，然后作为返回值返回即可
         *
         * @param container
         * @param position
         * @return
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = fragments.get(position);
            if (!fragment.isAdded()) { // 如果fragment还没有added
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.add(fragment, fragment.getClass().getSimpleName());
                ft.commit();
                /**
                 * 在用FragmentTransaction.commit()方法提交FragmentTransaction对象后
                 * 会在进程的主线程中,用异步的方式来执行。
                 * 如果想要立即执行这个等待中的操作,就要调用这个方法(只能在主线程中调用)。
                 * 要注意的是,所有的回调和相关的行为都会在这个调用中被执行完成,因此要仔细确认这个方法的调用位置。
                 */
                fragmentManager.executePendingTransactions();
            }

            if (fragment.getView().getParent() == null) {
                container.addView(fragment.getView()); // 为viewpager增加布局
            }
            return fragment.getView();
        }
    }
}
