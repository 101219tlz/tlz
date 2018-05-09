package com.baidu.duer.communication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.duer.communication.sdk.BaseCommunication;
import com.baidu.duer.communication.sdk.CommunicationImpl;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "MainActivity";
    private CommunicationImpl mCommunicationSDK = CommunicationImpl.instance(this);

    private TextView contentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initCommunicationSDK();
    }

    private void initViews() {
        findViewById(R.id.bindBtn).setOnClickListener(this);
        findViewById(R.id.ttsBtn).setOnClickListener(this);
        findViewById(R.id.microphoneBtn).setOnClickListener(this);

        contentTextView = findViewById(R.id.content);
    }

    private void initCommunicationSDK() {
        mCommunicationSDK.addCallback(new BaseCommunication.IReceiveListener() {
            @Override
            public void onReceiveDirective(String directive) {
                Log.i(TAG, "receive directive: " + directive + "\n");
                contentTextView.append(directive);
            }

            @Override
            public void onReceiveASR(String query) {
                Log.i(TAG, "receive query: " + query + "\n");
                contentTextView.append(query);
            }
        });
    }

    /**
     * 上报应用的状态
     */
    private void uploadClientContext() {
        JSONObject jsonObject = new JSONObject();
        JSONObject sourceObject = new JSONObject();
        try {
            sourceObject.put("name", "JJ麻将");
            sourceObject.put("packageName", "org.jj.majiang");
            sourceObject.put("versionCode", "1.0.1");
            sourceObject.put("versionName", "1.0.1");

            jsonObject.put("source", sourceObject);
            jsonObject.put("status", "RUNNING");

            mCommunicationSDK.uploadClientContext(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bindBtn) {
            Button btn = (Button) view;
            if (!btn.isSelected()) {
                btn.setText("解绑");
                mCommunicationSDK.bind(new BaseCommunication.IBindListener() {
                    @Override
                    public void onBindSuccess() {
                        Log.i(TAG, "onBindSuccess");
                        contentTextView.append("绑定成功\n");
                        uploadClientContext();
                    }

                    @Override
                    public void onBindFail() {
                        Log.i(TAG, "onBindFail");
                        contentTextView.append("绑定失败\n");
                    }

                    @Override
                    public void onHandShakeSuccess() {
                        Log.i(TAG, "onHandShakeSuccess");
                        contentTextView.append("一次握手成功，可以正常通信\n");
                        uploadClientContext();
                    }
                });
            } else {
                btn.setText("绑定");
                mCommunicationSDK.unbind();
                contentTextView.append("解绑\n");
            }
            btn.setSelected(!btn.isSelected());
        } else if (view.getId() == R.id.ttsBtn) {
            mCommunicationSDK.playTTS("中华人民共和国", "");
        } else if (view.getId() == R.id.microphoneBtn) {
            mCommunicationSDK.openMicrophone();
        }
    }
}
