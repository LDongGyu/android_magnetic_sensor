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
    private Sensor myAccele;

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float[] Rotation = new float[9];
    private float[] I = new float[9];

    private float azimuth;
    private float azimuthFix;

    private TextView x_text;
    private TextView y_text;
    private TextView z_text;
    private TextView max;
    private TextView min;
    private TextView correct;

    private double max_dir;
    private double min_dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        x_text = (TextView)findViewById(R.id.x);
        y_text = (TextView)findViewById(R.id.y);
        z_text = (TextView)findViewById(R.id.z);
        max = (TextView)findViewById(R.id.max);
        min = (TextView)findViewById(R.id.min);
        correct = (TextView)findViewById(R.id.correct);

        max_dir = 0.0;
        min_dir = 0.0;

        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 자이로스코프 센서를 사용하겠다고 등록
        myMagnetic = mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        myAccele = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        magnetic_Listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                final float alpha = 0.97f;

                synchronized (this) {
                    if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                        mGravity[0] = alpha * mGravity[0] + (1-alpha)*sensorEvent.values[0];
                        mGravity[1] = alpha * mGravity[1] + (1-alpha)*sensorEvent.values[1];
                        mGravity[2] = alpha * mGravity[2] + (1-alpha)*sensorEvent.values[2];
                    }
                    if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                        mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * sensorEvent.values[0];
                        mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * sensorEvent.values[1];
                        mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * sensorEvent.values[2];
                    }

                    boolean success = SensorManager.getRotationMatrix(Rotation,I,mGravity,mGeomagnetic);

                    if(success){
                        float orientaion[] = new float[3];
                        SensorManager.getOrientation(Rotation,orientaion);
                        azimuth = (float)Math.toDegrees(orientaion[0]);
                        azimuth = (azimuth + 360)%360;
                    }
                        double x = azimuth;
                     /*   double y = sensorEvent.values[1];
                        double z = sensorEvent.values[2];*/

                        if (x > max_dir) {
                            max_dir = x;
                            max.setText("max" + String.format("%f", max_dir));
                        }
                        if (x < min_dir) {
                            min_dir = x;
                            min.setText("min" + String.format("%f", min_dir));
                        }

                        if(25 <= x && x <=30){
                            correct.setText("건물 정보 출력");
                        }
                        else{
                            correct.setText("신호 없음");
                        }
                        
                        x_text.setText("x" + String.format("%f", x));
                     /*   y_text.setText("y" + String.format("%f", y));
                        z_text.setText("z" + String.format("%f", z));*/

                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    protected void onResume() {
        super.onResume();
        mySensorManager.registerListener(magnetic_Listener, myMagnetic,SensorManager.SENSOR_DELAY_UI);
        mySensorManager.registerListener(magnetic_Listener, myAccele,SensorManager.SENSOR_DELAY_UI);

    }
    protected void onPause() {
        super.onPause();
        mySensorManager.unregisterListener(magnetic_Listener);
    }

    protected void onStop() {
        super.onStop();
    }

    public void setAzimuthFix(float fix) {
        azimuthFix = fix;
    }

    public void resetAzimuthFix() {
        setAzimuthFix(0);
    }

}
