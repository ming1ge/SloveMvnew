package com.ming.slove.mvnew.app;

import android.app.Application;

import com.bilibili.magicasakura.utils.ThemeUtils;
import com.orhanobut.hawk.Hawk;
import com.qiniu.pili.droid.streaming.StreamingEnv;

import me.shaohui.shareutil.ShareConfig;
import me.shaohui.shareutil.ShareManager;

import static com.ming.slove.mvnew.app.APPS.BASE_URL;
import static com.ming.slove.mvnew.app.APPS.KEY_BASE_URL;

public class APP extends Application {
    /**
     * 单例模式中获取唯一的Application实例
     */
    private static APP instance;

    public static APP getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //内存泄露检测
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
        //主题切换初始化
        ThemeUtils.setSwitchColor(ThemeHelper.getSwitchColor());
        //用于存储
        Hawk.init(this).build();
        //初始为测试服务器
        BASE_URL = Hawk.get(KEY_BASE_URL, "http://product1.yibanke.com/");
        // 分享功能配置
        ShareConfig config = ShareConfig.instance()
                .qqId("1105819027")
                .wxId("wx187aeab1cec32357")
                //.weiboId(WEIBO_ID)
                // 下面两个，如果不需要登录功能，可不填写
                //.weiboRedirectUrl(REDIRECT_URL)
                //.wxSecret(WX_ID)
                ;
        ShareManager.init(config);
        //直播推流
        StreamingEnv.init(getApplicationContext());
    }
}
