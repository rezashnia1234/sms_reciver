package ir.smgroup.smslocationnotifier;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;

public class LocationService extends Service implements LocationListener {

	private LocationManager locationManager;
	private String sendTo;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		
		return null;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		sendTo = intent.getExtras().getString("sendTo");
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		return START_STICKY;
	}
	@Override
	public void onLocationChanged(Location arg0)
	{
		locationManager.removeUpdates(this);
		String message = "LatLng(" + String.valueOf(arg0.getLatitude()) + "," + String.valueOf(arg0.getLongitude()) + ")";
		SmsManager.getDefault().sendTextMessage(sendTo, null, message, null, null);
		stopSelf();
	}
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

}
