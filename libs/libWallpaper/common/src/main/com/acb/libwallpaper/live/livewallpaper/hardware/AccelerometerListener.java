package com.acb.libwallpaper.live.livewallpaper.hardware;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

public class AccelerometerListener extends AbstractListener {
	private static final float GRAVITY_Y_BASELINE = 6.6f;
	public final float gravity[] = new float[]{0, 7.37f, 6.60f};
	public final float linear[] = new float[]{0, 0, 0};
	public final float values[] = new float[]{0, 0, 0};

	public AccelerometerListener(Context context) {
		super(context);
	}

	public boolean register() {
		return register(Sensor.TYPE_ACCELEROMETER);
	}
	
	public boolean isOrientationLand;
	float y;
	float x;

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (last > 0) {
			final float a = .9f;
			final float b = 1f - a;

			// Simple handle baseline, will cause stagger nearby -9.8f.
			// A good solution is : SensorManager.getOrientation, then rotate angle, then asin(angle).
			y = event.values[isOrientationLand ? 0 : 1] - GRAVITY_Y_BASELINE;
			if (y < -9.8f) {
				y = 9.8f + GRAVITY_Y_BASELINE + y;
			}
			x = event.values[isOrientationLand ? 1 : 0];

			gravity[0] = a * gravity[0] + b * x;
			gravity[1] = a * gravity[1] + b * y;
			gravity[2] = a * gravity[2] + b * event.values[2];

			linear[0] = x - gravity[0];
			linear[1] = y - gravity[1];
			linear[2] = event.values[2] - gravity[2];

			values[0] = x;
			values[1] = y;
			values[2] = event.values[2];
		}

		last = event.timestamp;
	}
}
