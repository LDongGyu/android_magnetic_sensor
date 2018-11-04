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
    private Sensor myGyroscope;

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float[] mGyroValues = new float[3];
    private float[] Rotation = new float[9];
    private float[] I = new float[9];

    private float azimuth;
    private double pitch; // y

    /* 단위 시간을 구하기 위한 변수 */
    private double timestamp = 0.0;
    private double dt;

    /* 회전각을 구하기 위한 변수 */
    private double rad_to_dgr = 180 / Math.PI;
    private static final float NS2S = 1.0f/1000000000.0f;


    private TextView gyro_magn;
    private TextView first_x;

    private int isFirst;
    private double x = 0;
    private double sight_degree;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        first_x = (TextView)findViewById(R.id.first_x);
        gyro_magn = (TextView)findViewById(R.id.gyro_magn);

        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 자이로스코프 센서를 사용하겠다고 등록
        myMagnetic = mySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        myAccele = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        myGyroscope = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        isFirst =0;
        magnetic_Listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                final float alpha = 0.97f;
                sight_degree = 0; // 카메라로 비추는 각도 담는 변수
                synchronized (this) {
                    if(isFirst<100) { // 처음에 센서 값이 0 또는 쓰레기 값이라 100번째 읽어온 값을 기준으로 자이로센서에 적용 ( 아 지금 내가 보고 있는 게 x도 이구나! )

                        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) { // 가속도 센서 값 가져오기 ( 마그네틱 센서 보완 용으로 가속도 센서 사용 중 )
                            mGravity[0] = alpha * mGravity[0] + (1 - alpha) * sensorEvent.values[0];
                            mGravity[1] = alpha * mGravity[1] + (1 - alpha) * sensorEvent.values[1];
                            mGravity[2] = alpha * mGravity[2] + (1 - alpha) * sensorEvent.values[2];
                        }
                        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) { // 마그네틱 센서 값 가져오기
                            mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * sensorEvent.values[0];
                            mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * sensorEvent.values[1];
                            mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * sensorEvent.values[2];
                        }

                        boolean success = SensorManager.getRotationMatrix(Rotation, I, mGravity, mGeomagnetic); // 마그네틱, 가속도 센서 두 개 mapping ? (좀 더 공부해야할듯..)

                        if (success) {
                            float orientaion[] = new float[3];
                            SensorManager.getOrientation(Rotation, orientaion);
                            azimuth = (float) Math.toDegrees(orientaion[0]);
                            azimuth = (azimuth + 360) % 360;
                        }
                        x = azimuth; // x, azimuth는 읽어온 나침반 각도 값
                        isFirst++;
                    }

                    if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) { // 자이로센서 값 가져오기
                        mGyroValues = sensorEvent.values;

                        double gyroY = sensorEvent.values[1]; // y축으로 돌리는 (pitch 축) 각속도
                        double text = 0.0;

                        /* 단위시간 계산 */
                        dt = (sensorEvent.timestamp - timestamp) * NS2S;
                        timestamp = sensorEvent.timestamp;

                        /* 시간이 변화했으면 */
                        if (dt - timestamp * NS2S != 0) {
                            pitch = pitch + gyroY * dt; // 자이로에서 pitch 값 가져오기 (각속도 적분해서 회전각 구하기)
                            text = -(pitch * rad_to_dgr) % 360; // 자이로는 반시계로 돌릴 때 값이 양수로 증가, 시계는 음수로 증가, 나침반이라 반대라서 부호 바꿔줌

                            first_x.setText("마그네틱 센서로 찾은 각도 : "+String.format("%f",x));
                            sight_degree = text+x; // text는 자이로센서의 변화값( 자이로센서가 처음 보고 있는 곳을 0, 반시계로 돌리면 양수, 시계로 돌리면 음수), x는 우리가 지정해준 방향각 ( 아 내가 보고 있는 곳이 270도구나 )
                            if(sight_degree < 0){ // 예외처리 각이 음수가 나오는 경우가 생겨버려서
                                sight_degree = 360 + sight_degree;
                            }
                            sight_degree = sight_degree%360; // sight_degree = 핸드폰 들고 나침반 각도
                            gyro_magn.setText("자이로 센서로 찾고 있는 각도 : "+String.format("%f",sight_degree));
/*                            if(handling_x>=20 && handling_x<=40) {
                                building_text.setText("건물있당!");
                                building_text.setX((float)(width-width*(handling_x-20)/20));
                            }
                            else{
                                building_text.setText("");
                            }
*/
                        }
                    }
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
        mySensorManager.registerListener(magnetic_Listener, myGyroscope,SensorManager.SENSOR_DELAY_UI);

    }
    protected void onPause() {
        super.onPause();
        mySensorManager.unregisterListener(magnetic_Listener);
    }

    protected void onStop() {
        super.onStop();
    }

}
