package ir.smgroup.smslocationnotifier;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.net.Uri;


public class SmsNotifier extends BroadcastReceiver
{

	private final SmsManager manager = SmsManager.getDefault();
	
	public static Context currentContex;
	private static final String TAG = "SMSLOCATIONNITIFIER";
	private static final String WHITE_LIST_KEY = "WHITE_LIST_KEY";
	private static final String REQUESTEE_LIST_KEY = "REQUESTEE_LIST_KEY";
	private static final String LOC_DATA_KEY = "LOC_DATA_KEY";
	private static final String REQUEST_MESSAGE = "شما کجایید؟";
	public static LocationManager locationManager;
	private final Boolean showDebugInfo=false;
	private static String sendTo;
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
					
					if (isMessageRequest(message) && isInWhiteList(senderNum))
					{
						Intent serviceIntent = new Intent(ctx,LocationService.class);
						serviceIntent.putExtra("sendTo", phoneNumber);
						ctx.startService(serviceIntent);
						showLocalNotification("درخواست موقعیت",   "موقعیت شما برای "+senderNum+" ارسال می شود.");
						abortBroadcast();
					}
					else if (isMessageResponse(message))
					{
						showLocalNotification("notif title", "notif text");
						showLocalNotification("دریافت موقعیت", "موقعیت " + senderNum + " دریافت شد.");
						appendToLocationStorage(senderNum + ":" + message);

					}

					

				}
			}
		} catch (Exception e)
		{
			Log.d("[SmsLocationNotifier]", e.getMessage());
		}
	}
	/*public static void sendLocation(double lat, double lon)
	{
		locationManager.removeUpdates(locationListener);
		String message = "LatLng(" + String.valueOf(lat) + "," + String.valueOf(lon) + ")";
		SmsManager.getDefault().sendTextMessage(sendTo, null, message, null, null);
		
	}*/

	private static String TrimNumber(String number)
	{
		number =  number.substring(Math.max(0, number.length() - 10));
		number.trim();
		number = number.replaceAll("\\s+","");
		return number;
	}

	// ///////////////////////
	// sender and white list//
	// ///////////////////////
	private static boolean isInWhiteList(String number)
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

	private static Boolean isInRequesteeList(String number)
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
		message = message.replace("ي", "ی");
		message = message.replace("ك", "ک");
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
	private static void showLocalNotification(String title, String text)
	{
		Intent intent = currentContex.getPackageManager().getLaunchIntentForPackage(currentContex.getPackageName());
		PendingIntent contentIntent = PendingIntent.getActivity(currentContex, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder b = new NotificationCompat.Builder(currentContex);

		b.setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_ALL)
				.setWhen(System.currentTimeMillis())
				.setSmallIcon(currentContex.getResources().getIdentifier("icon", "drawable", currentContex.getPackageName()))
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
			return getWhiteList();
		}
		else if(function_name.equals("isInWhiteList"))
		{
			if(isInWhiteList(params))
				return "true";
			else
				return "false";
		}
		else if(function_name.equals("getRequesteeList"))
		{
			return getRequesteeList();
		}
		else if(function_name.equals("isInRequesteeList"))
		{
			if(isInRequesteeList(params))
				return "true";
			else
				return "false";
		}
		else if(function_name.equals("isGpsEnabled"))
		{
			if(isGpsEnabled())
				return "true";
			else
				return "false";
		}
		else if(function_name.equals("openGpsSettings"))
		{
			openGpsSettings();
			return "openGpsSettings OK";
		}
		else if(function_name.equals("openMessageApp"))
		{
			openMessageApp(params);
			return "openMessageApp: OKD" + params;
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

	private static void sendRequestMessage(final String number)
	{
		String DELIVERED = "SMS_DELIVERED";
		PendingIntent deliveredPI;
		deliveredPI = PendingIntent.getBroadcast(currentContex, 0, new Intent(DELIVERED), PendingIntent.FLAG_CANCEL_CURRENT);
		BroadcastReceiver deliveryBroadcastReceiver;
		deliveryBroadcastReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            switch (getResultCode()) {
	            case Activity.RESULT_OK:
	            	showLocalNotification("درخواست موقعیت", "درخواست موقعیت به "+number+" تحویل شد");
	                break;
	            case Activity.RESULT_CANCELED:
	                break;                        
	            }
	            currentContex.unregisterReceiver(this);
	        }
	    };
	    currentContex.registerReceiver(deliveryBroadcastReceiver, new IntentFilter(DELIVERED));  
		SmsManager.getDefault().sendTextMessage(number, null, REQUEST_MESSAGE, null, deliveredPI);
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
	private static String getRequesteeList()
	{
		SharedPreferences pref = currentContex.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		return pref.getString(REQUESTEE_LIST_KEY, "");
	}
	private static boolean isGpsEnabled()
	{
		final LocationManager manager = (LocationManager) currentContex.getSystemService( Context.LOCATION_SERVICE );

    	if (manager.isProviderEnabled( LocationManager.GPS_PROVIDER ))
        	return true;
        return false;

	}
	private static void openGpsSettings()
	{
		Intent i = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		currentContex.startActivity(i);
	}
	private static void openMessageApp(String message_text)
	{
		Intent intent2 = new Intent(); intent2.setAction(Intent.ACTION_SEND);
		intent2.setType("text/plain");
		intent2.putExtra(Intent.EXTRA_TEXT, message_text );  
		intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		currentContex.startActivity(Intent.createChooser(intent2, "ارسال با"));
	}
	
}
