package com.baidu.duer.communication.sdk;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import javax.security.auth.login.LoginException;

public class CommunicationImpl extends BaseCommunication {

    private static final int HANDSHAKE = 0x00;
    private static final int ASR = 0x01;
    private static final int DIRECTIVE = 0x02;
    private static final int CLIENTCONTEXT = 0x03;
    private static final int PLAYTTS = 0x04;
    private static final int OPENMICROPHONE = 0x05;

    private String TAG = "CommunicationImpl";

    private Context mContext;
    private boolean mBound = false;
    private static volatile CommunicationImpl instance;

    private Messenger serviceMessenger;
    private Messenger clientMessenger = new Messenger(new ClientHandler());

    private IReceiveListener mIReceiveListener;
    private IBindListener mIBindListener;

    private CommunicationImpl(Context context) {
        mContext = context;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceMessenger = new Messenger(service);
            // 一次握手，建立连接
            handShake();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMessenger = null;
        }
    };

    public static CommunicationImpl instance(Context context) {
        if (instance == null) {
            synchronized (CommunicationImpl.class) {
                if (instance == null) {
                    instance = new CommunicationImpl(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void bind(IBindListener listener) {
        super.bind(listener);
        mBound = true;
        mIBindListener = listener;

        Intent intent = new Intent("com.baidu.duer.communication");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setPackage("com.baidu.duer.apps.xtv");

        if(mContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)) {
            if (mIBindListener != null) {
                mIBindListener.onBindSuccess();
            }
        } else {
            if (mIBindListener != null) {
                mIBindListener.onBindFail();
            }
        }
    }

    @Override
    public void unbind() {
        super.unbind();

        mBound = false;
        mContext.unbindService(serviceConnection);
    }

    @Override
    public boolean isSupportDuerOS() {
        super.isSupportDuerOS();

        // 此处要进行判断，是否要进行鉴权处理
        return true;
    }

    @Override
    public String getASRResult() {
        return super.getASRResult();
    }

    @Override
    public void sendMessage(String content, int tag) {
        super.sendMessage(content, tag);
        try {
            Message msg = Message.obtain();
            msg.replyTo = clientMessenger;
            msg.what = tag;
            Bundle bundle = new Bundle();
            bundle.putString("data", content);
            msg.setData(bundle);
            serviceMessenger.send(msg);
            Log.d(TAG, "sendMessage " + content);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception " + e.toString());
        }
    }

    @Override
    public void addCallback(IReceiveListener receiver) {
        super.addCallback(receiver);
        mIReceiveListener = receiver;
    }

    @Override
    public void uploadClientContext(String clientContext) {
        super.uploadClientContext(clientContext);

        sendMessage(clientContext, CLIENTCONTEXT);
    }

    @Override
    public void playTTS(String speech, String type) {
        super.playTTS(speech, type);

        sendMessage(speech, PLAYTTS);
    }

    @Override
    public void openMicrophone() {
        super.openMicrophone();

        sendMessage("", OPENMICROPHONE);
    }

    /**
     * 登录成功之后，需要与服务端进行一次握手，交换两者信使
     */
    private void handShake() {
        try {
            Message msg = Message.obtain();
            msg.replyTo = clientMessenger;
            serviceMessenger.send(msg);
        } catch (Exception ignored) {

        }
    }

    /**
     * Client 的的 Handler，处理来自远程服务结果
     */
    @SuppressLint("HandlerLeak")
    private class ClientHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage " + msg.toString());
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case HANDSHAKE:
                    Log.i(TAG, "与服务端一次握手成功");
                    if (mIBindListener != null) {
                        mIBindListener.onHandShakeSuccess();
                    }
                    break;
                case ASR:
                    if (mIReceiveListener != null) {
                        mIReceiveListener.onReceiveASR(bundle.getString("data"));
                    }
                    break;
                case DIRECTIVE:

                    if (mIReceiveListener != null) {
                        mIReceiveListener.onReceiveDirective(bundle.getString("data"));
                    }
                    break;
            }
        }
    }
}
