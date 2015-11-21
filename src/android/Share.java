package ir.smgroup.smslocationnotifier;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.Toast;

import android.content.Intent;

    /**
     * This class echoes a string called from JavaScript.
     */
    public class Share extends CordovaPlugin {

        @Override
        public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
            if (action.equals("share"))
            {
                SmsNotifier.currentContex = this.cordova.getActivity().getApplicationContext();
                String function_name = args.getString(0);
                String params = args.getString(1);
                String result = SmsNotifier.exec(function_name,params);
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK,"result:"+result));

                return true;
            }
            return false;
        }

        
    }