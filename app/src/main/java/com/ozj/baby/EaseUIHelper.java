package com.ozj.baby;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity;
import com.hyphenate.easeui.model.EmojiconExampleGroupData;
import com.hyphenate.easeui.receiver.CallReceiver;
import com.hyphenate.easeui.shortcutbadger.ShortCutBadgerManager;
import com.hyphenate.util.EMLog;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.ozj.baby.mvp.views.home.activity.MainActivity;
import com.ozj.baby.util.MessageModel;

import java.util.List;
import java.util.Map;


/**
 * Created by YX201603-6 on 2016/5/4.
 */
public class EaseUIHelper {
    private EaseUI easeUI;

    private final Context mAppContext;


    public EaseUIHelper(Context context) {
        this.mAppContext = context;
    }

    public SpeechSynthesizer mTts;

    public void init() {
        if (EaseUI.getInstance().init(mAppContext, null)) {
            easeUI = EaseUI.getInstance();

            setEmojiProvider();
            EMConnectionListener connectionListener = new EMConnectionListener() {
                @Override
                public void onConnected() {

                }

                @Override
                public void onDisconnected(int i) {
                    if (i == EMError.USER_REMOVED) {
                        onCurrentAccountRemoved();
                    } else if (i == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                        onConnectionConflict();
                    }

                }
            };
            //注册连接监听
            EMClient.getInstance().addConnectionListener(connectionListener);
            CallReceiver callReceiver = null;
            IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
            if (callReceiver == null) {
                callReceiver = new CallReceiver();
            }

            //注册通话广播接收者
            mAppContext.registerReceiver(callReceiver, callFilter);
            registerEventListener();
        }

    }

    private void setEmojiProvider() {
        easeUI.setEmojiconInfoProvider(new EaseUI.EaseEmojiconInfoProvider() {
            @Override
            public EaseEmojicon getEmojiconInfo(String emojiconIdentityCode) {
                EaseEmojiconGroupEntity data = EmojiconExampleGroupData.getData();
                for (EaseEmojicon emojicon : data.getEmojiconList()) {
                    if (emojicon.getIdentityCode().equals(emojiconIdentityCode)) {
                        return emojicon;
                    }
                }
                return null;
            }

            @Override
            public Map<String, Object> getTextEmojiconMapping() {
                return null;
            }
        });

    }

    /**
     * 账号在别的设备登录
     */
    protected void onConnectionConflict() {
        Intent intent = new Intent(mAppContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EaseConstant.ACCOUNT_CONFLICT, true);
        mAppContext.startActivity(intent);
    }

    /**
     * 账号被移除
     */
    protected void onCurrentAccountRemoved() {
        Intent intent = new Intent(mAppContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EaseConstant.ACCOUNT_REMOVED, true);
        mAppContext.startActivity(intent);
    }

    /**
     * 全局事件监听
     * 因为可能会有UI页面先处理到这个消息，所以一般如果UI页面已经处理，这里就不需要再次处理
     * activityList.size() <= 0 意味着所有页面都已经在后台运行，或者已经离开Activity Stack
     */
    protected void registerEventListener() {
        EMMessageListener messageListener = new EMMessageListener() {
            private BroadcastReceiver broadCastReceiver;

            @Override
            public void onMessageReceived(List<EMMessage> messages) {

                ShortCutBadgerManager.getInstance(mAppContext).applyCount(EMClient.getInstance().chatManager().getUnreadMsgsCount());

                for (EMMessage message : messages) {
                    EMLog.d("EaseuiHelper", "onMessageReceived id : " + message.getMsgId());

                    //应用在后台，不需要刷新UI,通知栏提示新消息
                    if (!easeUI.hasForegroundActivies()) {
                        easeUI.getNotifier().onNewMsg(message);

                        mTts = SpeechSynthesizer.createSynthesizer(mAppContext, myInitListener);
                        //设置发音人
                        mTts.setParameter(SpeechConstant.VOICE_NAME,"xiaoyan");
                        //设置音调
                        mTts.setParameter(SpeechConstant.PITCH,"50");
                        //设置音量
                        mTts.setParameter(SpeechConstant.VOLUME,"50");

                        if(getheadsetStatsu()!=1){//为1时说明未插入耳机，不显示语音播报
                            if(message.getType().toString().equals("TXT")){
                                if(((EMTextMessageBody)message.getBody()).getMessage().contains("[")){
                                    mTts.startSpeaking("小可爱发来了一个表情", mTtsListener);
                                }else {
                                    mTts.startSpeaking("小可爱说："+((EMTextMessageBody)message.getBody()).getMessage(),mTtsListener);
                                }
                            }else if(message.getType().toString().equals("LOCATION")){
                                mTts.startSpeaking("小可爱发来了一个地址", mTtsListener);
                            }else if(message.getType().toString().equals("VIDEO")){
                                mTts.startSpeaking("小可爱发来了一个视频",mTtsListener);
                            }else if(message.getType().toString().equals("VOICE")){
                                mTts.startSpeaking("小可爱发来了一段语音",mTtsListener);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                for (EMMessage message : messages) {
                    EMLog.d("EaseuiHelper", "收到透传消息");

                    //获取消息body
                    EMCmdMessageBody cmdMsgBody = (EMCmdMessageBody) message.getBody();
                    final String action = cmdMsgBody.action();//获取自定义action

                    //获取扩展属性 此处省略
                    //message.getStringAttribute("");
                    EMLog.d("EaseuiHelper", String.format("透传消息：action:%s,message:%s", action, message.toString()));
                    final String str = mAppContext.getString(com.hyphenate.easeui.R.string.receive_the_passthrough);

                    final String CMD_TOAST_BROADCAST = "hyphenate.demo.cmd.toast";
                    IntentFilter cmdFilter = new IntentFilter(CMD_TOAST_BROADCAST);

                    if (broadCastReceiver == null) {
                        broadCastReceiver = new BroadcastReceiver() {

                            @Override
                            public void onReceive(Context context, Intent intent) {
                                Toast.makeText(mAppContext, intent.getStringExtra("cmd_value"), Toast.LENGTH_SHORT).show();
                            }
                        };

                        //注册广播接收者
                        mAppContext.registerReceiver(broadCastReceiver, cmdFilter);
                    }

                    Intent broadcastIntent = new Intent(CMD_TOAST_BROADCAST);
                    broadcastIntent.putExtra("cmd_value", str + action);
                    mAppContext.sendBroadcast(broadcastIntent, null);

//                    int code = mTts.startSpeaking(message.getBody().toString().substring(4), mTtsListener);
//                    Log.d("mylog",message.getBody().toString().substring(4));

                }
            }

            @Override
            public void onMessageReadAckReceived(List<EMMessage> messages) {
            }

            @Override
            public void onMessageDeliveryAckReceived(List<EMMessage> message) {
            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {

            }
        };

        EMClient.getInstance().chatManager().addMessageListener(messageListener);
    }


    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
        }
        @Override
        public void onSpeakPaused() {
        }
        @Override
        public void onSpeakResumed() {
        }
        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
        }
        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }



        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }

        @Override
        public void onCompleted(SpeechError error) {
            if(error!=null)
            {

            }
            else
            {

            }
        }
    };

    private InitListener myInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d("mySynthesiezer:", "InitListener init() code = " + code);
        }
    };


    public int getheadsetStatsu(){
        AudioManager  audoManager = (AudioManager)mAppContext.getSystemService(Context.AUDIO_SERVICE);

//      IntentFilter iFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
//      Intent iStatus = registerReceiver(null, iFilter);
//      boolean isConnected = iStatus.getIntExtra("state", 0) == 1;
//
//      if(isConnected){
//         Toast.makeText(MainActivity.this,"耳机ok",Toast.LENGTH_SHORT).show();
//      }

        if(audoManager.isWiredHeadsetOn()){
            return 1;
        }else{
            Toast.makeText(mAppContext,"耳机不ok",Toast.LENGTH_SHORT).show();
        }

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
//      int isBlueCon;//蓝牙适配器是否存在，即是否发生了错误
        if (ba == null){
//         isBlueCon = -1;     //error
            return -1;
        }
        else if(ba.isEnabled()) {
            int a2dp = ba.getProfileConnectionState(BluetoothProfile.A2DP);              //可操控蓝牙设备，如带播放暂停功能的蓝牙耳机
            int headset = ba.getProfileConnectionState(BluetoothProfile.HEADSET);        //蓝牙头戴式耳机，支持语音输入输出
            int health = ba.getProfileConnectionState(BluetoothProfile.HEALTH);          //蓝牙穿戴式设备

            //查看是否蓝牙是否连接到三种设备的一种，以此来判断是否处于连接状态还是打开并没有连接的状态
            int flag = -1;
            if (a2dp == BluetoothProfile.STATE_CONNECTED) {
                flag = a2dp;
            } else if (headset == BluetoothProfile.STATE_CONNECTED) {
                flag = headset;
            } else if (health == BluetoothProfile.STATE_CONNECTED) {
                flag = health;
            }
            //说明连接上了三种设备的一种
            if (flag != -1) {
//            isBlueCon = 1;            //connected
                return 2;
            }
        }
        return -2;
    }

}

