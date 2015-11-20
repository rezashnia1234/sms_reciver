package ir.smgroup.smslocationnotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;


public class SmsNotifier extends BroadcastReceiver implements LocationListener
{

	private final SmsManager manager = SmsManager.getDefault();
	public static Context currentContex;
	private static final String TAG = "SMSLOCATIONNITIFIER";
	private static final String WHITE_LIST_KEY = "WHITE_LIST_KEY";
	private static final String REQUESTEE_LIST_KEY = "REQUESTEE_LIST_KEY";
	private static final String LOC_DATA_KEY = "LOC_DATA_KEY";
	private static final String REQUEST_MESSAGE = "where are you?";
	public static LocationManager locationManager;
	private final Boolean showDebugInfo=true;
	private String sendTo;

	@Override
	public void onReceive(Context ctx, Intent intent)
	{
		currentContex = ctx;
		final Bundle bundle = intent.getExtras();
		try
		{
			if (bundle != null)
			{
				final Object[] pdusObj = (Object[]) bundle.get("pdus");
				for (int i = 0; i < pdusObj.length; i++)
				{
					SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
					String phoneNumber = currentMessage.getDisplayOriginatingAddress();
					String senderNum = TrimNumber(phoneNumber);
					String message = currentMessage.getDisplayMessageBody();
					if (showDebugInfo)
					{
						Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);
						Toast toast = Toast.makeText(ctx, "senderNum: " + senderNum + ", message: " + message, Toast.LENGTH_LONG);
						toast.show();
					}
					if (isInWhiteList(senderNum) || isInRequesteeList(senderNum))
					{
						if (isMessageRequest(message))
						{
							locationManager = (LocationManager) currentContex.getSystemService(Context.LOCATION_SERVICE);
							locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
							sendTo = phoneNumber;
							showLocalNotification("", "");
						}
						else if (isMessageResponse(message))
						{
							showLocalNotification("", "");
							String loc = "";
							appendToLocationStorage(senderNum + ":" + loc);

						}

					}

				}
			}
		} catch (Exception e)
		{
			Log.d("[SmsLocationNotifier]", e.getMessage());
		}
	}

	@Override
	public void onLocationChanged(Location location)
	{
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		locationManager.removeUpdates(this);
		sendLocation(lat, lon);
	}

	private void sendLocation(double lat, double lon)
	{
		String message = "LatLng(" + String.valueOf(lat) + "," + String.valueOf(lon) + ")";
		SmsManager.getDefault().sendTextMessage(sendTo, null, message, null, null);
	}

	private static String TrimNumber(String number)
	{
		return number.substring(Math.max(0, number.length() - 10));
	}

	// ///////////////////////
	// sender and white list//
	// ///////////////////////
	private boolean isInWhiteList(String number)
	{
		number = TrimNumber(number);
		SharedPreferences pref = currentContex.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		String WhiteList = pref.getString(WHITE_LIST_KEY, "");
		if (WhiteList.length() > 0)
		{
			String[] numbers = WhiteList.split(",");
			for (int i = 0; i < numbers.length; i++)
			{
				String n =  TrimNumber(numbers[i]);
				if (n.equals(number))
					return true;
			}
		}
		return false;
	}

	private static void AddToRequesteeList(String number)
	{
		number = TrimNumber(number);
		SharedPreferences pref = currentContex.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		String requestee_list = pref.getString(REQUESTEE_LIST_KEY, "");

		requestee_list += "," + number;
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(REQUESTEE_LIST_KEY, requestee_list);
		editor.commit();
	}

	private Boolean isInRequesteeList(String number)
	{
		number = TrimNumber(number);
		SharedPreferences pref = currentContex.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		String requestee_list = pref.getString(REQUESTEE_LIST_KEY, "");
		if (requestee_list.length() > 0)
		{
			String[] numbers = requestee_list.split(",");
			for (int i = 0; i < numbers.length; i++)
			{
				if (numbers[i].equals(number))
					return true;
			}
		}
		return false;
	}

	/*
	 * request: where are you?
	 */
	// /////////////////////////
	// Messsage identification//
	// /////////////////////////
	private boolean isMessageRequest(String message)
	{
		if (message.equals(REQUEST_MESSAGE))
		{
			return true;
		}
		return false;
	}

	private boolean isMessageResponse(String message)
	{
		message = message.toLowerCase();
		message = message.replace("latlng(", "");
		message = message.replace(")", "");
		String[] parts = message.split(",");
		if (parts.length == 2)
		{
			try
			{
				double d1 = Double.parseDouble(parts[0]);
				double d2 = Double.parseDouble(parts[0]);
			} catch (NumberFormatException ex)
			{
				return false;
			}
			return true;

		}
		return false;
	}

	// ////////////////////
	// local notification//
	// ////////////////////
	private void showLocalNotification(String title, String text)
	{
		Intent intent = currentContex.getPackageManager().getLaunchIntentForPackage("ir.smgroup.arbaeen");
		PendingIntent contentIntent = PendingIntent.getActivity(currentContex, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder b = new NotificationCompat.Builder(currentContex);

		b.setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_ALL)
				.setWhen(System.currentTimeMillis())
				.setContentTitle(title)
				.setContentText(text)
				.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
				.setContentIntent(contentIntent)
				.setContentInfo("Info");

		NotificationManager notificationManager = (NotificationManager) currentContex.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(1, b.build());
	}

	// ///////////////
	// data transfer//
	// ///////////////
	private void appendToLocationStorage(String str)
	{
		SharedPreferences pref = currentContex.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		String requestee_list = pref.getString(LOC_DATA_KEY, "");
		requestee_list += "\n" + str;
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(LOC_DATA_KEY, requestee_list);
		editor.commit();
	}

	// /////////
	// //API////
	// /////////
	public static String exec(String function_name,String params)
	{
		if(function_name.equals("AppStart"))
		{
			return AppStart();
		}
		else if(function_name.equals("clearLocationStorage"))
		{
			clearLocationStorage();
			return "clearLocationStorage OK";
		}
		else if(function_name.equals("sendRequestMessage"))
		{
			sendRequestMessage(params);
			return "sendRequestMessage OK";
		}
		else if(function_name.equals("setWhiteList"))
		{
			setWhiteList(params);
			return "setWhiteList OK";
		}
		else if(function_name.equals("getWhiteList"))
		{
			getWhiteList();
			return "getWhiteList OK";
		}
		
		
		
		return "";
	}
	private static String AppStart()
	{
		SharedPreferences pref = currentContex.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		return pref.getString(LOC_DATA_KEY, "");
	}

	private static void clearLocationStorage()
	{
		SharedPreferences pref = currentContex.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(LOC_DATA_KEY, "");
		editor.commit();

	}

	private static void sendRequestMessage(String number)
	{
		AddToRequesteeList(TrimNumber(number));
		SmsManager.getDefault().sendTextMessage(number, null, REQUEST_MESSAGE, null, null);
	}

	private static void setWhiteList(String white_list)
	{
		SharedPreferences pref = currentContex.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(WHITE_LIST_KEY, white_list);
		editor.commit();
	}
	private static String getWhiteList()
	{
		SharedPreferences pref = currentContex.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		return pref.getString(WHITE_LIST_KEY, "");
	}
	
	@Override
	public void onProviderDisabled(String p)
	{
	}

	@Override
	public void onProviderEnabled(String p)
	{
	}

	@Override
	public void onStatusChanged(String p, int s, Bundle e)
	{
	}
}
