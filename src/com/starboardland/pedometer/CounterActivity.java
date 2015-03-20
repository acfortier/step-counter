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
import java.util.ArrayList;
import java.util.List;
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
    private static final int TIME_INTERVAL = 120000;//todo change back to 120000
    private static final int TOTAL_SEGS = 8;
    private int previousCount = 0; 
    StepDatabaseHelper db;
    LinkedList<Float> counts = new LinkedList<Float>();
    List<Float> segs = new ArrayList<Float>();
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

        currentSeg = segments[0];
//        currentSeg.setText("Segment" + (segmentCount+1) + " steps: " + (int)(currentCount - countBase));

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
        new CountDownTimer(TIME_INTERVAL*TOTAL_SEGS + 200, TIME_INTERVAL){
            boolean started = false;
            @Override
            public void onTick(long l) {
                segs.add(currentCount - countBase);
                db.insertSteps(currentCount - countBase);
                counts.add(currentCount - countBase);
                countBase = currentCount;

                if(!started ){
                    currentSeg.setText("Segment " + (segmentCount+1) + " steps: " + (int)(currentCount - countBase) );
                    started = true;
                    return;
                }

                currentSeg = segments[++segmentCount];
                toast();
                countBase = currentCount;

                currentSeg.setText("Segment " + (segmentCount+1) + " steps: " + (int)(currentCount - countBase) );

            }

            public void onFinish(){
                counting = false;
                segs.add(currentCount - countBase);
                segmentCount++;
                toast();
                total.setText("Total Steps: " + (int)currentCount);
//                if(++segmentCount == TOTAL_SEGS){
//                    counting = false;
//                }else {
//                    db.insertSteps(currentCount - countBase);
//                    counts.add(currentCount - countBase);
//                    countBase = currentCount;
//                }
            }

            private void toast(){
                Context context = getApplicationContext();
                CharSequence text = "Segment " + segmentCount + " steps: " + segs.get(segmentCount);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
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
        if (activityRunning && counting) {
//            count.setText(String.valueOf(event.values[0]));
            currentCount = event.values[0];
//            currentSeg.setText("Segment" + segmentCount + String.valueOf((currentCount - countBase));
            currentSeg.setText("Segment " + (segmentCount+1) + " steps: " + (int)(currentCount - countBase));
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
