package com.example.leedonggyu.magnetic_sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private SensorManager mySensorManager; // 센서 매니저
    private SensorEventListener magnetic_Listener; // 센서 리스너
    private Sensor myMagnetic; // 센서

    private TextView x_text;
    private TextView y_text;
    private TextView z_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        x_text = (TextView)findViewById(R.id.x);
        y_text = (TextView)findViewById(R.id.y);
        z_text = (TextView)findViewById(R.id.z);

        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 자이로스코프 센서를 사용하겠다고 등록
        myMagnetic = mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magnetic_Listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                double x = sensorEvent.values[0] * 10.0;
                double y = sensorEvent.values[1] * 10.0;
                double z = sensorEvent.values[2] * 10.0;

                x_text.setText("x"+String.format("%f",x));
                y_text.setText("y"+String.format("%f",y));
                z_text.setText("z"+String.format("%f",z));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    protected void onResume() {
        super.onResume();
        mySensorManager.registerListener(magnetic_Listener, myMagnetic,SensorManager.SENSOR_DELAY_UI);
    }
    protected void onPause() {
        super.onPause();
        mySensorManager.unregisterListener(magnetic_Listener);
    }

    protected void onStop() {
        super.onStop();
    }
}
