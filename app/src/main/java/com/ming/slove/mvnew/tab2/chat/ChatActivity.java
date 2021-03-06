package com.ming.slove.mvnew.tab2.chat;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.ming.slove.mvnew.R;
import com.ming.slove.mvnew.api.other.OtherApi;
import com.ming.slove.mvnew.app.APPS;
import com.ming.slove.mvnew.common.base.BackActivity;
import com.ming.slove.mvnew.common.base.BaseRecyclerViewAdapter;
import com.ming.slove.mvnew.common.receiver.MsgHelper;
import com.ming.slove.mvnew.common.utils.BaseTools;
import com.ming.slove.mvnew.common.utils.MyPictureSelector;
import com.ming.slove.mvnew.common.utils.NotifyUtil;
import com.ming.slove.mvnew.common.utils.PhotoOperate;
import com.ming.slove.mvnew.common.utils.StringUtils;
import com.ming.slove.mvnew.common.widgets.dialog.MyDialog;
import com.ming.slove.mvnew.model.bean.FriendDetail;
import com.ming.slove.mvnew.model.bean.Result;
import com.ming.slove.mvnew.model.bean.JsonMsg_Share;
import com.ming.slove.mvnew.model.database.ChatMsgModel;
import com.ming.slove.mvnew.model.database.FriendsModel;
import com.ming.slove.mvnew.model.database.InstantMsgModel;
import com.ming.slove.mvnew.model.database.MyDB;
import com.ming.slove.mvnew.model.event.InstantMsgEvent;
import com.ming.slove.mvnew.model.event.NewMsgEvent;
import com.ming.slove.mvnew.model.event.SendImageEvent;
import com.ming.slove.mvnew.tab2.chat.emoji.Constants;
import com.ming.slove.mvnew.tab2.chat.emoji.SimpleCommonUtils;
import com.ming.slove.mvnew.tab2.chat.keyboard.ChatAppsGridView;
import com.ming.slove.mvnew.tab2.chat.keyboard.ChatKeyBoard;
import com.orhanobut.hawk.Hawk;
import com.sj.emoji.EmojiBean;
import com.yalantis.ucrop.entity.LocalMedia;
import com.yalantis.ucrop.util.FileUtils;
import com.yalantis.ucrop.util.PictureConfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sj.keyboard.data.EmoticonEntity;
import sj.keyboard.interfaces.EmoticonClickListener;
import sj.keyboard.widget.EmoticonsEditText;
import sj.keyboard.widget.FuncLayout;


public class ChatActivity extends BackActivity implements FuncLayout.OnFuncKeyBoardListener {

    public static String UID = "接收消息，发送消息用户id";
    public static String USER_NAME = "显示的用户名，用于标题";
    @Bind(R.id.chat_list)
    RecyclerView mXRecyclerView;
    @Bind(R.id.ek_bar)
    ChatKeyBoard ekBar;

    private ChatAdapter mAdapter;
    private ChatMsgModel chatMsg;
    private String me;//接收者uid
    private String other;//发送者uid

    private Uri cameraUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        String otherName = getIntent().getStringExtra(USER_NAME);//聊天对象的名字
        setToolbarTitle(otherName);

        configXRecyclerView();//XRecyclerView配置
        initEmoticonsKeyBoardBar();//自定义聊天键盘的配置

        initDatas();//初始化数据

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 显示数据库中消息
     */
    public void initDatas() {
        me = Hawk.get(APPS.ME_UID);
        other = getIntent().getStringExtra(UID);

        //进入聊天界面，清零当前好友的新消息计数
        FriendsModel friend = MyDB.getInstance().queryById(other, FriendsModel.class);
        if (friend != null) {
            friend.setCount(0);
            MyDB.update(friend);
        }
        //进入聊天界面，清零动态的新消息计数
        InstantMsgModel iMsg = MyDB.getInstance().queryById(other, InstantMsgModel.class);
        if (iMsg != null) {
            iMsg.setCount(0);
            MyDB.update(iMsg);
            EventBus.getDefault().post(new InstantMsgEvent());
        }
        //进入聊天界面，删除当前好友的通知
        NotificationManager nm = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        int requestCode = Integer.parseInt(other);//通知的唯一标识
        nm.cancel(requestCode);

        //查询数据库中聊天记录并显示
        QueryBuilder<ChatMsgModel> qb = new QueryBuilder<>(ChatMsgModel.class)
                .whereEquals("_from", other)
                .whereAppendAnd()
                .whereEquals("_to", me)
                .whereAppendAnd()
                .whereEquals("_type", 1)
                .whereAppendOr()
                .whereEquals("_from", me)
                .whereAppendAnd()
                .whereEquals("_to", other)
                .whereAppendAnd()
                .whereEquals("_type", 0);

        final List<ChatMsgModel> chatMsgModels = MyDB.getInstance().query(qb);
        mAdapter.addData(chatMsgModels);
        mXRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

        //长按删除消息
        mAdapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, final int position) {
                MyDialog.Builder builder = new MyDialog.Builder(ChatActivity.this);
                builder.setTitle("提示")
                        .setMessage("是否确定删除此条消息记录?")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //数据库中删除消息
                                        MyDB.delete(mAdapter.getDatas().get(position));
                                        //界面显示中删除消息
                                        mAdapter.removeItem(position);
                                        //删除最后一条，更新动态
                                        if (position == mAdapter.getItemCount()) {
                                            InstantMsgModel iMsg = MyDB.getInstance().queryById(other, InstantMsgModel.class);
                                            if (iMsg != null) {//动态存在，即更新
                                                String msg = "";
                                                if (mAdapter.getItemCount() != 0) {//删除后，列表不为空，取最后一条记录
                                                    ChatMsgModel lastItem = mAdapter.getDatas().get(mAdapter.getItemCount() - 1);
                                                    switch (lastItem.getCt()) {
                                                        case "1":
                                                            msg = "[图片]";
                                                            break;
                                                        case "2":
                                                            msg = "[语音]";
                                                            break;
                                                        case "100":
                                                            String jsonString = lastItem.getTxt();
                                                            Gson gson = new Gson();
                                                            JsonMsg_Share shareMsg = gson.fromJson(jsonString, JsonMsg_Share.class);
                                                            msg = "[分享]:\"" + shareMsg.getTitle() + "\"";
                                                            break;
                                                        default:
                                                            msg = lastItem.getTxt();//类型：文字
                                                            break;
                                                    }
                                                }
                                                iMsg.setContent(msg);
                                                MyDB.update(iMsg);
                                                EventBus.getDefault().post(new InstantMsgEvent());
                                            }
                                        }
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                if (!isFinishing()) {
                    builder.create().show();
                }
            }
        });
    }

    /**
     * 接收到推送后，更新消息
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getNewMsg(NewMsgEvent event) {
        final ChatMsgModel chatMsg = event.getChatMsg();

        if (me.equals(chatMsg.getTo()) && other.equals(chatMsg.getFrom())) {
            //当前聊天界面的好友发送来的消息直接显示
            mAdapter.addData(chatMsg);
            mXRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
        } else {
            //其他好友消息发送通知
            MsgHelper.getInstance().showNotify(chatMsg,this,true);
        }
    }

    //配置表情键盘
    private void initEmoticonsKeyBoardBar() {
        SimpleCommonUtils.initEmoticonsEditText(ekBar.getEtChat());
        ekBar.setAdapter(SimpleCommonUtils.getCommonAdapter(this, emoticonClickListener));
        ekBar.addOnFuncKeyBoardListener(this);
        ChatAppsGridView mChatAppsGridView = new ChatAppsGridView(this);
        ekBar.addFuncView(mChatAppsGridView);

        ekBar.getEtChat().setOnSizeChangedListener(new EmoticonsEditText.OnSizeChangedListener() {
            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                scrollToBottom();
            }
        });
        ekBar.getBtnSend().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnSendBtnClick(ekBar.getEtChat().getText().toString());
                ekBar.getEtChat().setText("");
            }
        });
        /*ekBar.getEmoticonsToolBarView().addFixedToolItemView(false, R.mipmap.icon_add, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "添加表情库", Toast.LENGTH_SHORT).show();
            }
        });*/
       /* ekBar.getEmoticonsToolBarView().addToolItemView(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "表情库设置", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    EmoticonClickListener emoticonClickListener = new EmoticonClickListener() {
        @Override
        public void onEmoticonClick(Object o, int actionType, boolean isDelBtn) {

            if (isDelBtn) {
                SimpleCommonUtils.delClick(ekBar.getEtChat());
            } else {
                if (o == null) {
                    return;
                }
                if (actionType == Constants.EMOTICON_CLICK_BIGIMAGE) {
                    if (o instanceof EmoticonEntity) {
                        OnSendImage(((EmoticonEntity) o).getIconUri());
                    }
                } else {
                    String content = null;
                    if (o instanceof EmojiBean) {
                        content = ((EmojiBean) o).emoji;
                    } else if (o instanceof EmoticonEntity) {
                        content = ((EmoticonEntity) o).getContent();
                    }

                    if (TextUtils.isEmpty(content)) {
                        return;
                    }
                    int index = ekBar.getEtChat().getSelectionStart();
                    Editable editable = ekBar.getEtChat().getText();
                    editable.insert(index, content);
                }
            }
        }
    };

    //配置RecyclerView
    private void configXRecyclerView() {
        //设置布局和adapter
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new ChatAdapter();
        mXRecyclerView.setLayoutManager(mLayoutManager);
        mXRecyclerView.setAdapter(mAdapter);

        mXRecyclerView.setItemAnimator(new DefaultItemAnimator());//设置Item增加、移除动画
    }

    /**
     * 发送文字消息
     *
     * @param msg 消息内容
     */
    private void OnSendBtnClick(final String msg) {
        ekBar.reset();//关闭功能键盘

        if (!TextUtils.isEmpty(msg)) {
            chatMsg = new ChatMsgModel();
            chatMsg.setType(ChatMsgModel.ITEM_TYPE_RIGHT);//发送消息
            chatMsg.setFrom(me);
            chatMsg.setTo(other);
            chatMsg.setSt(String.valueOf(System.currentTimeMillis()));//发送消息时间：当前时间
            chatMsg.setCt("0");//消息类型：文字
            chatMsg.setTxt(msg);//消息内容

            //发送消息后，更新动态
            InstantMsgModel iMsg = MyDB.getInstance().queryById(other, InstantMsgModel.class);
            if (iMsg != null) {//动态存在，即更新
                iMsg.setTime(String.valueOf(System.currentTimeMillis()).substring(0, 10));
                iMsg.setContent(msg);
                MyDB.update(iMsg);
                EventBus.getDefault().post(new InstantMsgEvent());
            } else {//动态不存在就创建
                FriendsModel friend = MyDB.getInstance().queryById(other, FriendsModel.class);
                if (friend != null) {
                    String uicon = friend.getUicon();
                    String uname = friend.getUname();
                    String time = String.valueOf(System.currentTimeMillis()).substring(0, 10);//13位时间戳，截取前10位，主要统一
                    InstantMsgModel msgModel = new InstantMsgModel(other, uicon, uname, time, msg, 0);
                    MyDB.insert(msgModel);
                    EventBus.getDefault().post(new InstantMsgEvent());
                } else {//如果发消息给非好友（新增：客户联系店长）
                    String auth = Hawk.get(APPS.USER_AUTH);
                    OtherApi.getService().get_FriendDetail(auth, other)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<FriendDetail>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onNext(FriendDetail friendDetail) {
                                    FriendDetail.DataBean.UserinfoBean userinfoBean = friendDetail.getData().getUserinfo();
                                    //存储陌生消息人信息到本地好友数据库，标识isFriend为false
                                    String save_uicon = APPS.BASE_URL + userinfoBean.getHead();
                                    String save_uname = userinfoBean.getUname();//昵称
                                    String iphone = userinfoBean.getPhone();
                                    if (StringUtils.isEmpty(save_uname)) {
                                        save_uname = iphone;//昵称为空，显示手机号
                                    }
                                    FriendsModel friendsModel = new FriendsModel(other, save_uname, save_uicon, false);
                                    MyDB.insert(friendsModel);
                                    //处理后续，新建动态
                                    String time = String.valueOf(System.currentTimeMillis()).substring(0, 10);//13位时间戳，截取前10位，主要统一
                                    InstantMsgModel msgModel = new InstantMsgModel(other, save_uicon, save_uname, time, msg, 0);
                                    MyDB.insert(msgModel);
                                    EventBus.getDefault().post(new InstantMsgEvent());
                                }
                            });
                }
            }
            //开始发送
            OtherApi.getService()
                    .post_sendMessage(me, other, "0", "yxj", msg, null, null, 1, "2")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Result>() {
                        @Override
                        public void onCompleted() {
                            mAdapter.addData(chatMsg);
                            MyDB.insert(chatMsg);
                            mXRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                        }

                        @Override
                        public void onError(Throwable e) {
                            chatMsg.setIsShowPro(2);//发送失败，显示感叹号
                            mAdapter.addData(chatMsg);
                            MyDB.insert(chatMsg);
                            mXRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                        }

                        @Override
                        public void onNext(Result result) {
                        }
                    });
        }
    }

    /**
     * 发送图片，点击事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void selectorPicture(SendImageEvent event) {
        String type = event.getType();
        if ("1".equals(type)) {//图片选择
            MyPictureSelector pictureSelector = new MyPictureSelector(this);
            pictureSelector.selectorSinglePicture();
        } else {//type=2，为拍照
            takePhoto();

        }
    }

    private void takePhoto() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "没有SD卡", Toast.LENGTH_SHORT).show();
            return;
        }
        File cameraFile = FileUtils.createCameraFile(this, 1);
        if (cameraFile != null) {
            cameraUri = Uri.fromFile(cameraFile);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            startActivityForResult(cameraIntent, PictureConfig.REQUEST_CAMERA);
        } else {
            Toast.makeText(this, "拍照失败：创建文件失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.REQUEST_IMAGE:
                    List<LocalMedia> mediaList = (List<LocalMedia>) data.getSerializableExtra(PictureConfig.REQUEST_OUTPUT);
                    if (mediaList != null) {
                        String imagPath = mediaList.get(0).getCompressPath();
                        sendImage(imagPath);
                    }
                    break;
                case PictureConfig.REQUEST_CAMERA:
                    // 拍照返回
                    if (cameraUri != null) {
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, cameraUri));//更新图库

                        File file = null;
                        try {
                            String cameraPath = cameraUri.getPath();
                            file = new PhotoOperate().scal(cameraPath);//对图片压缩处理
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (file != null) {
                            sendImage(file.getAbsolutePath());//发送图片
                        }
                    } else {
                        Toast.makeText(this, "拍照失败，请重新拍摄。", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    /**
     * 发送图片消息
     */
    public void sendImage(String imagePath) {
        ekBar.reset();//关闭功能键盘
        //处理图片，保存到指定路径
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//图片文件转为Bitmap对象
        String source = BaseTools.toBase64(bitmap) + ".jpg";

        chatMsg = new ChatMsgModel();
        chatMsg.setType(ChatMsgModel.ITEM_TYPE_RIGHT);//发送消息
        chatMsg.setFrom(me);
        chatMsg.setTo(other);
        chatMsg.setSt(String.valueOf(System.currentTimeMillis()));//发送消息时间：当前时间
        chatMsg.setCt("1");//消息类型：图片
        chatMsg.setTxt("[图片]");//消息内容
        chatMsg.setLink(imagePath);

        //发送消息后，更新动态
        InstantMsgModel iMsg = MyDB.getInstance().queryById(other, InstantMsgModel.class);
        if (iMsg != null) {
            iMsg.setTime(String.valueOf(System.currentTimeMillis()).substring(0, 10));
            iMsg.setContent("[图片]");
            MyDB.update(iMsg);
            EventBus.getDefault().post(new InstantMsgEvent());
        } else {
            FriendsModel friend = MyDB.getInstance().queryById(other, FriendsModel.class);
            if (friend != null) {
                String uicon = friend.getUicon();
                String uname = friend.getUname();
                String time = String.valueOf(System.currentTimeMillis()).substring(0, 10);//13位时间戳，截取前10位，主要统一
                InstantMsgModel msgModel = new InstantMsgModel(other, uicon, uname, time, "[图片]", 0);
                MyDB.insert(msgModel);
                EventBus.getDefault().post(new InstantMsgEvent());
            } else {//如果发消息给非好友（新增：客户联系店长）
                String auth = Hawk.get(APPS.USER_AUTH);
                OtherApi.getService().get_FriendDetail(auth, other)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<FriendDetail>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(FriendDetail friendDetail) {
                                FriendDetail.DataBean.UserinfoBean userinfoBean = friendDetail.getData().getUserinfo();
                                //存储陌生消息人信息到本地好友数据库，标识isFriend为false
                                String save_uicon = APPS.BASE_URL + userinfoBean.getHead();
                                String save_uname = userinfoBean.getUname();//昵称
                                String iphone = userinfoBean.getPhone();
                                if (StringUtils.isEmpty(save_uname)) {
                                    save_uname = iphone;//昵称为空，显示手机号
                                }
                                FriendsModel friendsModel = new FriendsModel(other, save_uname, save_uicon, false);
                                MyDB.insert(friendsModel);
                                //处理后续，新建动态
                                String time = String.valueOf(System.currentTimeMillis()).substring(0, 10);//13位时间戳，截取前10位，主要统一
                                InstantMsgModel msgModel = new InstantMsgModel(other, save_uicon, save_uname, time, "[图片]", 0);
                                MyDB.insert(msgModel);
                                EventBus.getDefault().post(new InstantMsgEvent());
                            }
                        });
            }
        }
        //发送图片
        OtherApi.getService()
                .post_sendMessage(me, other, "1", "yxj", "[图片]", source, null, 1, "2")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Result>() {
                    @Override
                    public void onCompleted() {
                        mAdapter.addData(chatMsg);
                        MyDB.insert(chatMsg);
                        mXRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                    }

                    @Override
                    public void onError(Throwable e) {
                        chatMsg.setIsShowPro(2);//发送失败，显示感叹号
                        mAdapter.addData(chatMsg);
                        MyDB.insert(chatMsg);
                        mXRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                    }

                    @Override
                    public void onNext(Result result) {
                    }
                });
    }

    private void OnSendImage(String image) {
        if (!TextUtils.isEmpty(image)) {
            OnSendBtnClick("[img]" + image);
        }
    }

    private void scrollToBottom() {
        mXRecyclerView.requestLayout();
        mXRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mXRecyclerView.scrollToPosition(mXRecyclerView.getBottom());
            }
        });
    }

    @Override
    public void OnFuncPop(int i) {//键盘展开时
        mXRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public void OnFuncClose() {//键盘关闭
    }
}

