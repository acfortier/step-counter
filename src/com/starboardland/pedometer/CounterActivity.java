package com.starboardland.pedometer;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

//public class CounterActivity extends Activity implements SensorEventListener {
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class CounterActivity extends Activity implements SensorEventListener, LocationListener {

    private SensorManager sensorManager;
    private TextView[] segments = new TextView[TOTAL_SEGS];
    private TextView currentSeg;
    boolean activityRunning;
    private MapFragment mapFrag;
    private GoogleMap map;

    //==========jun-start
    private static final int TIME_INTERVAL = 1200;//todo change back to 120000
    private static final int TOTAL_SEGS = 8;
    private int previousCount = 0;
    StepDatabaseHelper db;
    LinkedList<Float> counts = new LinkedList<Float>();
    int segmentCount = 0;
    boolean counting = true;
    float currentCount = 0;
    float countBase = 0;
    private TextView total;

//    private TextView count2;
//    private TextView count3;
//    private TextView count4;
//    private TextView count5;
//    private TextView count6;
//    private TextView count7;
//    private TextView count8;
    //===========jun-end


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        segments[0] = (TextView) findViewById(R.id.count);
        segments[1] = (TextView) findViewById(R.id.count2);
        segments[2] = (TextView) findViewById(R.id.count3);
        segments[3] = (TextView) findViewById(R.id.count4);
        segments[4] = (TextView) findViewById(R.id.count5);
        segments[5] = (TextView) findViewById(R.id.count6);
        segments[6] = (TextView) findViewById(R.id.count7);
        segments[7] = (TextView) findViewById(R.id.count8);
        total       = (TextView) findViewById(R.id.total);
        for(int i = 0; i < TOTAL_SEGS; ++i){
//            segments[i].setVisibility(View.INVISIBLE);
        }
        currentSeg = segments[0];
        currentSeg.setVisibility(View.VISIBLE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        map = mapFrag.getMap();
        map.setMyLocationEnabled(true);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);

        if (location != null) {
            onLocationChanged(location);
        }

        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);


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
        new CountDownTimer(TIME_INTERVAL*(TOTAL_SEGS+1), TIME_INTERVAL){ //todo change this to 120000(?)
            int i = 0;
            @Override
            public void onTick(long l) {
                db.insertSteps(currentCount - countBase);
                counts.add(currentCount - countBase);
                countBase = currentCount;
                segmentCount++;
                currentSeg.setText("Segment" + segmentCount + " steps: " + (int)(currentCount - countBase));

                if(segmentCount < TOTAL_SEGS) {
                    currentSeg = segments[segmentCount];
//                    currentSeg.setVisibility(View.VISIBLE);
                }
                //todo add toast
            }

            public void onFinish(){
                total.setText("Total Steps: " + currentCount);
//                if(++segmentCount == TOTAL_SEGS){
//                    counting = false;
//                }else {
//                    db.insertSteps(currentCount - countBase);
//                    counts.add(currentCount - countBase);
//                    countBase = currentCount;
//                }
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
//            currentSeg.setText("Segment" + segmentCount + String.valueOf((currentCount - countBase));
            currentSeg.setText("Segment" + segmentCount + " steps: " + (int)(currentCount - countBase));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);

        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

}
