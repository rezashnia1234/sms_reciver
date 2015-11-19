package ir.smgroup.smslocationnotifier;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

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
            if (action.equals("share")) {
                Toast.makeText(this.cordova.getActivity().getApplicationContext(), "test taost from me", Toast.LENGTH_LONG).show();
                return true;
            }
            return false;
        }

        
    }