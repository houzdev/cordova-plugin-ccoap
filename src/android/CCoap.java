package cordova.plugin.ccoap;

import cordova.plugin.ccoap.CCoapClient;
import cordova.plugin.ccoap.CCoapDiscovery;
import cordova.plugin.ccoap.CCoapError;
import cordova.plugin.ccoap.CCoapException;
import cordova.plugin.ccoap.CCoapUtils;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * CCoap
 * 
 * Cordova plugin interface class. Handles the incoming javascript calls.
 * 
 * @author David Krepsky
 */
public class CCoap extends CordovaPlugin {

    /**
     * Decode and execute native function.
     * 
     * @param action          Native function to be called.
     * @param args            Parameters passed to the native function.
     * @param callbackContext Javascript callbacks.
     * @return True if action was executed with success. *false* on error.
     * @throws JSONException Not used, but necessary to override base class method.
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals("request")) {
            return this.request(args, callbackContext);
        } else if (action.equals("discover")) {
            return this.discover(callbackContext);
        } else {
            callbackContext.error(CCoapUtils.getErrorObject(CCoapError.INVALID_ACTION, "Invalid action"));
        }
        return false;
    }

    /**
     * Create a {@link CCoapClient} and make a request.
     * 
     * @param args            Array with the request information within.
     * @param callbackContext Javascript's callback.
     * @return True if success, false on error.
     */
    private boolean request(JSONArray args, CallbackContext callbackContext) {

        JSONObject req;

        try {
            req = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(CCoapUtils.getErrorObject(CCoapError.INVALID_ARGUMENT, "Invalid argument"));
            return false;
        }

        CCoapClient client = new CCoapClient();

        try {
            client.request(req, callbackContext);
        } catch (CCoapException e) {
            callbackContext.error(CCoapUtils.getErrorObject(e));
        }

        return true;
    }

    /**
     * Create a {@link CCoapDiscovery} object and start a multicast discovery.
     * 
     * @param callbackContext Javascript's callback.
     * @return True on success, false on error.
     */
    private boolean discover(CallbackContext callbackContext) {

        CCoapDiscovery discovery = new CCoapDiscovery(callbackContext);

        try {
            discovery.start();
        } catch (CCoapException e) {
            callbackContext.error(CCoapUtils.getErrorObject(e));
            return false;
        }

        return true;
    }
}
