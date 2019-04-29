package cordova.plugin.ccoap;

import java.lang.Throwable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cordova.plugin.ccoap.CCoapError;
import cordova.plugin.ccoap.CCoapException;

/**
 * CCoapUtils
 * 
 * Provides helper functions.
 * 
 * @author David Krepsky
 */
public class CCoapUtils {

    /**
     * Convert a JSONArray to a byte array.
     * 
     * Returned array is filled with zeros if JSONArray does not contain numbers.
     * 
     * @param jarray JSONArray to be transformed.
     * @return Byte array.
     */
    public static byte[] jarray2barray(JSONArray jarray) {

        byte[] barray = new byte[jarray.length()];

        for (int i = 0; i < jarray.length(); i++) {
            barray[i] = (byte) jarray.optInt(i, 0);
        }

        return barray;

    }

    /**
     * Convert a byte array into a json array.
     * 
     * @param barray Byte array.
     * @return JSONArray.
     */
    public static JSONArray barray2jarray(byte[] barray) {
        JSONArray jarray = new JSONArray();

        for (int i = 0; i < barray.length; i++) {
            jarray.put(barray[i]);
        }

        return jarray;
    }

    /**
     * Overloaded method.
     * 
     * @param exception
     * @return
     */
    public static JSONObject getErrorObject(CCoapException exception) {
        return getErrorObject(-1, exception.getErrorCode(), exception.getMessage(), exception.getCause());
    }

    /**
     * Overloaded method.
     * 
     * @param error
     * @param message
     * @return
     */
    public static JSONObject getErrorObject(CCoapError error, String message) {
        return getErrorObject(-1, error, message, null);
    }

    /**
     * Overloaded method.
     * 
     * @param id
     * @param error
     * @param message
     * @return
     */
    public static JSONObject getErrorObject(int id, CCoapError error, String message) {
        return getErrorObject(id, error, message, null);
    }

    /**
     * Pack an error into a JSONObject to be sent to the javascript side.
     * 
     * @param id      Request ID, defaul is -1.
     * @param error   Error code.
     * @param message Error message.
     * @param cause   Original exception, when available.
     * @return JSONObject.
     */
    public static JSONObject getErrorObject(int id, CCoapError error, String message, Throwable cause) {

        JSONObject errorObject = new JSONObject();
        try {
            errorObject.put("id", id);
            errorObject.put("code", error.getCode());

            if (!message.isEmpty()) {
                errorObject.put("message", message);
            }

            if (null != cause) {
                errorObject.put("extra", cause.getMessage());
            }
        } catch (JSONException e) {
            // FIXME: Dont known what to do with this yet.
        }

        return errorObject;
    }
}
