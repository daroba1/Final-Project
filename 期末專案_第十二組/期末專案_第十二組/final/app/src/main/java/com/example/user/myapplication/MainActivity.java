package com.example.user.myapplication;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;



public class MainActivity extends ActionBarActivity  implements SensorEventListener{

    private SensorManager sManager;
    private Sensor mSensorAccelerometer;
    private TextView tv_step;
    private int step = 0;   //步数
    private double oriValue = 0;  //原始值
    private double lstValue = 0;  //上次的值
    private double curValue = 0;  //当前值
    private boolean motiveState = true;   //是否处于运动状态
    private boolean processState = false;   //标记当前是否已经在计步

    private Button btnStart, btnStop, btnZero, btnEnd;
    private TextView txtClock;
    private int tSec = 0, cSec = 0, cMin = 0; //總時間、秒、分
    private Timer timer;
    private TimerTask timerTask;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorAccelerometer = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        tv_step = (TextView) findViewById(R.id.tv_step);
        txtClock = (TextView) findViewById(R.id.txtClock);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnZero = (Button) findViewById(R.id.btnZero);
        btnEnd = (Button)findViewById(R.id.btnEnd);
        btnStart.setOnClickListener(listener);
        btnStop.setOnClickListener(listener);
        btnZero.setOnClickListener(listener);
        btnEnd.setOnClickListener(listener);


    }


    private Button.OnClickListener listener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStart:  //開始
                    timer = new Timer();
                    processState = true;
                    timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            tSec++; //時間加 1 秒
                            Message message = new Message(); //傳送訊息給 Handler
                            message.what = 1;
                            handler.sendMessage(message);
                        }
                    };
                    timer.scheduleAtFixedRate(timerTask, 1000, 1000);
                    break;
                case R.id.btnStop:  //停止
                    timer.cancel();
                    processState = false;
                    break;
                case R.id.btnZero:  //歸零
                    timer.cancel();
                    tSec=0;
                    txtClock.setText("00 : 00");
                    tv_step.setText("0");
                    processState = false;
                    break;
                case R.id.btnEnd:  //結束
                    finish();
            }
        }
    };

    private android.os.Handler handler = new android.os.Handler() {
        public void handleMessage(Message msg) { //接收訊息
            super.handleMessage(msg);
            switch (msg.what) {
                case 1: //timerTask 的訊息
                    cSec = tSec % 60; //取得秒數
                    cMin = tSec / 60; //取得分鐘數
                    String str = "";
                    if (cMin < 10) str = "0" + cMin; //個位分鐘數補零
                    else str = "" + cMin;
                    if (cSec < 10) str = str + " : 0" + cSec;  //個位秒數補零
                    else str = str + " : " + cSec;
                    txtClock.setText(str); //顯示時間
                    break;
            }
        }
    };


    @Override
    public void onSensorChanged(SensorEvent event) {
        double range = 1;   //設定一個精度範圍
        float[] value = event.values;
        curValue = magnitude(value[0], value[1], value[2]);   //計算當前的模
        //向上加速的狀態
        if (motiveState == true) {
            if (curValue >= lstValue) lstValue = curValue;
            else {
                //檢測到一次峰值
                if (Math.abs(curValue - lstValue) > range) {
                    oriValue = curValue;
                    motiveState = false;
                }
            }
        }
        //向下加速的狀態
        if (motiveState == false) {
            if (curValue <= lstValue) lstValue = curValue;
            else {
                if (Math.abs(curValue - lstValue) > range) {
                    //檢測到一次峰值
                    oriValue = curValue;
                    if (processState == true) {
                        step++;  //步數 + 1
                        if (processState == true) {
                            tv_step.setText(step + "");    //讀數更新
                        }
                    }
                    motiveState = true;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }



    //向量求模
    public double magnitude(float x, float y, float z) {
        double magnitude = 0;
        magnitude = Math.sqrt(x * x + y * y + z * z);
        return magnitude;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sManager.unregisterListener(this);
    }
}
