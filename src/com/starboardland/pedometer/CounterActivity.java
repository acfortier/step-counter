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
    private TextView count;
    boolean activityRunning;
    private MapFragment mapFrag;
    private GoogleMap map;

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
                count.setText("Segment" + segmentCount + " steps: " + (currentCount - countBase));
                //todo add toast
            }

            public void onFinish(){

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
            count.setText("Segment" + (segmentCount + 1) + String.valueOf(currentCount - countBase));
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
