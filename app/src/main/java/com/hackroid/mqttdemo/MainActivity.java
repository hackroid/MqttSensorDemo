package com.hackroid.mqttdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Locale;
import java.util.Formatter;

public class MainActivity extends AppCompatActivity implements MessageCallBack {

    private MyServiceConnection serviceConnection;
    private MQTTService mqttService;
    private SensorManager mSensorManager;
    private Sensor mGravity = null;
    private boolean push = false;
    private TextView dataReceived;
    private TextView dataReceived_x;
    private TextView dataReceived_y;
    private TextView dataReceived_z;
    private Button pub, sub;
    private EditText pubText;
    private EditText subText;
    private Switch sendSwitch;
    private String myTopic = "android";
    private String subTopic = "chrome1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataReceived = (TextView) findViewById(R.id.title_messages);
        dataReceived_x = (TextView) findViewById(R.id.data_x);
        dataReceived_y = (TextView) findViewById(R.id.data_y);
        dataReceived_z = (TextView) findViewById(R.id.data_z);
        pub = (Button) findViewById(R.id.pubButton);
        sub = (Button) findViewById(R.id.subButton);
        pubText = (EditText) findViewById(R.id.pubText);
        subText = (EditText) findViewById(R.id.subText);
        sendSwitch = (Switch) findViewById(R.id.sendSwitch);
        sendSwitch.setChecked(false);

        serviceConnection = new MyServiceConnection();
        serviceConnection.setMsgCallBack(MainActivity.this);
        Intent intent = new Intent(this, MQTTService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        pub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTopic = pubText.getText().toString();
                Log.i("TXTXTppppub", myTopic + " settle!");
            }
        });

        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subTopic = subText.getText().toString();
                MQTTService.sub(subTopic);
                dataReceived.setText(subTopic);
                Log.i("TXTXTssssub", subTopic + " settle!");
            }
        });

        sendSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                push = isChecked;
            }
        });
    }

    @Override
    public void setMessage(String message) {
        String[] splitData = message.split("\\s+");
        dataReceived_x.setText(splitData[0]);
        dataReceived_y.setText(splitData[1]);
        dataReceived_z.setText(splitData[2]);
        mqttService = serviceConnection.getMqttService();
        mqttService.toCreateNotification(message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSensor();
        Log.d("mGravity", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterSensor();
        Log.d("mGravity", "onPause");
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

    private void registerSensor() {
        mSensorManager.registerListener(eventListener, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterSensor() {
        mSensorManager.unregisterListener(eventListener);
    }

    private SensorEventListener eventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            StringBuilder sb = new StringBuilder();
            Formatter formatter = new Formatter(sb, Locale.US);
            if (event == null) {
                Log.d("mGravity", "事件為空");
            }
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    double xValue = event.values[0];
                    double yValue = event.values[1];
                    double zValue = event.values[2];
                    String xyzValue = formatter.format("%.4f %.4f %.4f", xValue, yValue, zValue).toString();
                    Log.d("mGravity", xyzValue);
                    if (push) {
                        MQTTService.publish(xyzValue, myTopic);
                    }
                    break;
                default:
                    Log.d("mGravity", "未知探测器触发");
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}