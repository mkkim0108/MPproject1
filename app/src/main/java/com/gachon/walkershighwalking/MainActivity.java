package com.gachon.walkershighwalking;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    SensorManager sensorManager;
    Sensor stepCountSensor;
    TextView stepCountView;
    TextView distanceCountView;
    Button resetButton;

    Sensor shakingSensor; //step detector 오류 : 자이로스코프로 변환
    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;

    private static final int SHAKE_THRESHOLD = 800;
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;


    // 현재 걸음 수
    int currentSteps = 0;
    boolean running = false;
    double walkingDistance = 0.0;
    private double Kcal = 0.0;
    
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stepCountView = findViewById(R.id.stepCountView);
        distanceCountView = findViewById(R.id.distanveCountView);
        resetButton = findViewById(R.id.resetButton);


        // 활동 퍼미션 체크
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){

            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        // 걸음 센서 연결
        // * 옵션
        // - TYPE_STEP_DETECTOR:  리턴 값이 무조건 1, 앱이 종료되면 다시 0부터 시작
        // - TYPE_STEP_COUNTER : 앱 종료와 관계없이 계속 기존의 값을 가지고 있다가 1씩 증가한 값을 리턴
        //
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        // 디바이스에 걸음 센서의 존재 여부 체크
        if (stepCountSensor == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show();
        }

        // 리셋 버튼 추가 - 리셋 기능 / 날짜 지나면 리셋되고 서버에 업로드 되도록 변경해야 함
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 걸음수 초기화
                currentSteps = 0;
                stepCountView.setText(String.valueOf(currentSteps));
                Log.v("test_walking", "reset");
            }
        });
        shakingSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }


    public void onStart() {
        super.onStart();
        if(stepCountSensor !=null) {
            Log.v("test_walking", "stepCountSensor available");
            sensorManager.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_GAME);
        }

        if(shakingSensor !=null) {
            Log.v("test_walking", "shakingSensor available");
            sensorManager.registerListener(this,shakingSensor,SensorManager.SENSOR_DELAY_GAME);
        }
    }


//여기가 안됨
    @Override
    public void onSensorChanged(SensorEvent event) {
        // 걸음 센서 이벤트 발생시
//        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
//            Log.v("test_walking", "sensor detected : onSensorChanged");
//            if (event.values[0] == 1.0f) {
//                // 센서 이벤트가 발생할때 마다 걸음수 증가
//                currentSteps += 1;
//                stepCountView.setText(String.valueOf(currentSteps));
//                Log.v("test_walking", "walks : " + currentSteps);
//            }
//        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
            if (gabOfTime > 100) {
                lastTime = currentTime;
                x = event.values[SensorManager.DATA_X];
                y = event.values[SensorManager.DATA_Y];
                z = event.values[SensorManager.DATA_Z];

                speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    // 걸음
                    Log.v("test_walking", "sensor detected : Accelerometer");
                    currentSteps += 1;
                    stepCountView.setText(String.valueOf(currentSteps));
                    distanceCountView.setText(String.valueOf(walkingDistance+"m"));
                }

                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER); // 걸음수 측정 센서
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Sensor not found!", Toast.LENGTH_SHORT).show();

        }

        Sensor shakingSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // 가속도 측정 센서
        if (countSensor != null) {
            sensorManager.registerListener(this, shakingSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Sensor not found!", Toast.LENGTH_SHORT).show();

        }
    }
    //거리 측정
    public void calculateKcal(double height){ // 키를 이용해서 보폭 + 거리 계산
        double step = height * 0.37; //임의로 성인 평균 보폭 계산(.37)
        walkingDistance = currentSteps * step;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}