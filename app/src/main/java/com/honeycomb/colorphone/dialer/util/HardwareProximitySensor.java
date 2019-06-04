package com.honeycomb.colorphone.dialer.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
 
public class HardwareProximitySensor implements SensorEventListener {
 
    private static final String TAG = "ProximitySensor";
    private static HardwareProximitySensor instance;
 
    private SensorManager mSensorManager;
    private float distance = -1;
    private boolean mHasStarted = false;
 
    private HardwareProximitySensor() {
    }
 
    public static HardwareProximitySensor getInstance() {
        if (instance == null) {
            instance = new HardwareProximitySensor();
        }
        return instance;
    }
 

    public static void start(Context context) {
        HardwareProximitySensor.getInstance().registerListener(context);
    }
 

    public static void stop() {
        HardwareProximitySensor.getInstance().unregisterListener();
    }
 
 
    // ------------------------------------------------------------------------------
 
    /**
     * Use this method to start listening of the sensor
     */
    private void registerListener(Context context) {
        if (mHasStarted) {
            return;
        }
        mHasStarted = true;
        mSensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor proximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor != null) {
            mSensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
 
    /**
     * Use this method to stop listening of the sensor
     */
    private void unregisterListener() {
        if (!mHasStarted || mSensorManager == null) {
            return;
        }
        mHasStarted = false;
        mSensorManager.unregisterListener(this);
    }
 
    public float getValue() {
        if (!mHasStarted) {
            Log.w(TAG, "proximity sensor has not start!");
        }
        return distance;
    }
 
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            // values[0]: Proximity sensor distance measured in centimeters
            distance = event.values[0];
            Log.v(TAG, "proximity sensor distance: " + distance);
        }
    }
 
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
 
}
