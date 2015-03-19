package com.starboardland.pedometer;

import android.app.Activity;
import android.content.Context;
import android.hardware.*;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class CounterActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView count;
    boolean activityRunning;

    //==========jun-start
    private static final int TIME_INTERVAL = 12000;//todo change back to 120000
    private static final int TOTAL_SEGS = 8;
    private int previousCount = 0;
    StepDatabaseHelper db;
    LinkedList<Float> counts = new LinkedList<Float>();
    int segmentCount = 0;
    boolean counting = true;
    float currentCount = 0;
    float countBase = 0;
    //===========jun-end


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        count = (TextView) findViewById(R.id.count);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //========jun-start
        db = StepDatabaseHelper.getInstance(getApplicationContext());//todo is this right?

        final Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if(++segmentCount >= TOTAL_SEGS){
//                    counting = false;
//                    count.setText("terminated");
//                    cancel();
//                }else {
//                    db.insertSteps(currentCount - countBase);
//                    counts.add(currentCount - countBase);
//                    countBase = currentCount;
//                    segmentCount++;
//                    count.setText("Segment" + (segmentCount + 1) + " " + (currentCount - countBase));
////                    System.out.println("one more round");
//                }
//            }
//        }, TIME_INTERVAL, TIME_INTERVAL);
//        System.out.println("=====" + db.getLastCount());
        new CountDownTimer(TIME_INTERVAL*TOTAL_SEGS, TIME_INTERVAL){ //todo change this to 120000(?)
            int i = 0;
            @Override
            public void onTick(long l) {
                db.insertSteps(currentCount - countBase);
                counts.add(currentCount - countBase);
                countBase = currentCount;
                segmentCount++;
                count.setText("Segment" + (segmentCount + 1) + " steps: " + (currentCount - countBase));
            }

            public void onFinish(){
                if(++segmentCount == TOTAL_SEGS){
                    counting = false;
                }else {
                    db.insertSteps(currentCount - countBase);
                    counts.add(currentCount - countBase);
                    countBase = currentCount;
                }
            }
        }.start();
        //========jun-end
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
        // if you unregister the last listener, the hardware will stop detecting step events
//        sensorManager.unregisterListener(this); 
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (activityRunning) {
//            count.setText(String.valueOf(event.values[0]));
            currentCount = event.values[0];
            count.setText("Segment" + (segmentCount + 1) + String.valueOf(currentCount - countBase));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
