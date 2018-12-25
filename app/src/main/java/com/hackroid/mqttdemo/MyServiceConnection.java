package com.hackroid.mqttdemo;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MyServiceConnection implements ServiceConnection {

    private MQTTService mqttService;
    private MessageCallBack msgCallBack;

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mqttService = ((MQTTService.CustomBinder) iBinder).getService();
        mqttService.setMessageCallBack(msgCallBack);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    public MQTTService getMqttService() {
        return mqttService;
    }

    public void setMsgCallBack(MessageCallBack msgCallBack) {
        this.msgCallBack = msgCallBack;
    }
}
