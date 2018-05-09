package com.baidu.duer.communication.sdk;

public abstract class BaseCommunication {

    public boolean isSupportDuerOS() {
        return false;
    }

    public void bind(IBindListener listener) {}

    public void unbind() {}

    public String getASRResult() {
        return null;
    }

    public void sendMessage(String content, int tag) {}

    public void addCallback(IReceiveListener receiver) {}

    public void playTTS(String speech, String type) {}

    public void openMicrophone() {}

    public void uploadClientContext(String clientContext){}

    public interface IReceiveListener {
        void onReceiveDirective(String directive);
        void onReceiveASR(String query);
    }

    public interface IBindListener {
        void onBindSuccess();
        void onBindFail();
        void onHandShakeSuccess();
    }
}
