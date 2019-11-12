package com.honeycomb.colorphone.wallpaper.livewallpaper.hardware;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class AbstractListener implements SensorEventListener {
	long last = 0;

	private final SensorManager sensorManager;

	private boolean listening = false;
	private Sensor sensor;

	AbstractListener(Context context) {
		sensorManager = (SensorManager) context.getSystemService(
				Context.SENSOR_SERVICE);
	}

	public void unregister() {
		if (sensor == null || !listening) {
			return;
		}

		sensorManager.unregisterListener(this);
		listening = false;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		last = event.timestamp;
	}

	boolean register(int type) {
		if (listening || sensorManager == null || (sensor == null &&
				(sensor = sensorManager.getDefaultSensor(type)) == null)) {
			return false;
		}

		last = 0;

		return listening = sensorManager.registerListener(this, sensor,
				SensorManager.SENSOR_DELAY_GAME);
	}
}
