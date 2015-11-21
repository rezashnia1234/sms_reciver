package ir.smgroup.smslocationnotifier;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class MyLocationListener implements LocationListener {

	@Override
	public void onLocationChanged(Location arg0) {
		SmsNotifier.sendLocation(arg0.getLatitude(),arg0.getLongitude());
	}

	@Override
	public void onProviderDisabled(String arg0) {
	}

	@Override
	public void onProviderEnabled(String arg0) {
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

}
