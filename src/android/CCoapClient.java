package cordova.plugin.ccoap;

import org.apache.cordova.CallbackContext;

import cordova.plugin.ccoap.CCoapError;
import cordova.plugin.ccoap.CCoapException;
import cordova.plugin.ccoap.CCoapUtils;

import org.ws4d.coap.core.CoapClient;
import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.connection.BasicCoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapClientChannel;
import org.ws4d.coap.core.enumerations.CoapHeaderOptionType;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.messages.CoapHeaderOption;
import org.ws4d.coap.core.messages.CoapHeaderOptions;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;
import org.ws4d.coap.core.rest.CoapData;
import org.ws4d.coap.core.tools.Encoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.Channel;

/**
 * CCoapClient
 * 
 * This class is responsible for translating and executing a coap request from
 * the javascript side.
 * 
 * @author David Krepsky
 */
public class CCoapClient implements CoapClient {

    private int id;
    private CallbackContext callback;

    /**
     * Create and send a request.
     * 
     * @param req             Request information wrapped as a json object.
     * @param callbackContext Javascript received and error callbacks.
     * @throws CCoapException Thrown at any error {@link CCoapException}.
     */
    public void request(JSONObject req, CallbackContext callbackContext) throws CCoapException {

        this.id = req.optInt("id", -1);
        this.callback = callbackContext;

        CoapClientChannel channel = null;
        CoapRequest request = null;

        try {
            channel = createChannel(req);

            request = createRequest(req, channel);

            if (null == channel || null == request) {
                throw new CCoapException("Unknown error", CCoapError.UNKNOWN);
            }

            channel.sendMessage(request);

        } catch (CCoapException e) {
            if (null != channel) {
                channel.close();
            }

            throw e;
        }
    }

    /**
     * Parse the server address and return a CoapChannel connected to the server;
     * 
     * @param req Request information.
     * @return Chanel used to send the request.
     * 
     * @throws CCoapException Thrown at any error {@link CCoapException}.
     */
    private CoapClientChannel createChannel(JSONObject req) throws CCoapException {

        URI uri;

        try {
            uri = URI.create(req.getString("uri"));
        } catch (JSONException e) {
            throw new CCoapException("URI is missing", CCoapError.INVALID_ARGUMENT, e);
        }

        CoapChannelManager manager = BasicCoapChannelManager.getInstance();

        InetAddress addr;

        try {
            addr = InetAddress.getByName(uri.getHost());
        } catch (UnknownHostException e) {
            throw new CCoapException("Invalid server address", CCoapError.DESTINATION_IS_UNREACHABLE, e);
        }

        int port = uri.getPort();

        return manager.connect(this, addr, port);
    }

    /**
     * Parse the request method.
     * 
     * @param req Request information.
     * @return Type of request.
     * 
     * @throws CCoapException Thrown when the method is invalid or not specified.
     */
    private CoapRequestCode parseCode(JSONObject req) throws CCoapException {

        String method;

        try {
            method = req.getString("method");
        } catch (JSONException e) {
            method = "get";
        }

        if (method.equals("get")) {
            return CoapRequestCode.GET;
        } else if (method.equals("post")) {
            return CoapRequestCode.POST;
        } else if (method.equals("put")) {
            return CoapRequestCode.PUT;
        } else if (method.equals("delete")) {
            return CoapRequestCode.DELETE;
        } else {
            throw new CCoapException("Invalid Request Method", CCoapError.INVALID_ARGUMENT);
        }
    }

    /**
     * Return the requested resource path.
     * 
     * @param req Request information.
     * @return Resource path.
     * @throws CCoapException Thrown when URI is missing.
     */
    private String parsePath(JSONObject req) throws CCoapException {
        URI uri;

        try {
            uri = URI.create(req.getString("uri"));
        } catch (JSONException e) {
            throw new CCoapException("URI is missing", CCoapError.INVALID_ARGUMENT, e);
        }

        String path = uri.getPath();

        if (path == null) {
            path = "/";
        } else if (path.isEmpty()) {
            path = "/";
        }

        return path;
    }

    /**
     * Return if the message is confirmable or not.
     * 
     * If 'confirmable' is not present, returns true.
     * 
     * @param req Request information.
     * @return True if the message is confirmable, false otherwise.
     */
    private boolean parseConfirmable(JSONObject req) {
        boolean confirmable;

        try {
            confirmable = req.getBoolean("confirmable");
        } catch (JSONException e) {
            confirmable = true;
        }

        return confirmable;
    }

    /**
     * Translate a mime type string to a {@link CoapMediaType}.
     * 
     * For valid media types, check RFC-7252.
     * 
     * If mime type is not known, returns CoapMediaType.UNKNOWN.
     * 
     * @param mediaType Mime type string.
     * @return CoapMediaType value.
     */
    private CoapMediaType stringToMediaType(String mediaType) {
        if (mediaType.equals(CoapMediaType.text_plain.getMimeType())) {
            return CoapMediaType.text_plain;
        } else if (mediaType.equals(CoapMediaType.link_format.getMimeType())) {
            return CoapMediaType.link_format;
        } else if (mediaType.equals(CoapMediaType.xml.getMimeType())) {
            return CoapMediaType.xml;
        } else if (mediaType.equals(CoapMediaType.octet_stream.getMimeType())) {
            return CoapMediaType.octet_stream;
        } else if (mediaType.equals(CoapMediaType.exi.getMimeType())) {
            return CoapMediaType.exi;
        } else if (mediaType.equals(CoapMediaType.json.getMimeType())) {
            return CoapMediaType.json;
        } else if (mediaType.equals("application/cbor")) {
            return CoapMediaType.parse(60);
        } else {
            return CoapMediaType.UNKNOWN;
        }
    }

    /**
     * Get the request content format as mime type.
     * 
     * If content format is not specified in the request, returns 'unknown';
     * 
     * @param req Request information.
     * @return Mime type for the content format.
     */
    private CoapMediaType parseContentFormat(JSONObject req) {
        // Try to get content type.
        JSONArray options = null;
        String contentFormat = "unknown";

        try {
            options = req.getJSONArray("options");
        } catch (JSONException e) {
            options = null;
        }

        if (null != options) {
            JSONObject option = null;

            try {
                for (int i = 0; i < options.length(); i++) {
                    option = options.getJSONObject(i);

                    String name = option.getString("name");

                    if (name.equals("Content-Format")) {
                        contentFormat = option.getString("value");
                    }
                }
            } catch (JSONException e) {
                // Content-Format not defined, use default unknown.
            }
        }

        return stringToMediaType(contentFormat);
    }

    /**
     * Transform the request payload into a {@link CoapData} object.
     * 
     * @param req Request information
     * @return CoapData object when the payload is present and valid. *null*
     *         otherwise.
     */
    private CoapData parsePayload(JSONObject req) {

        if (req.has("payload") == false) {
            return null;
        }

        CoapData data = null;

        CoapMediaType type = parseContentFormat(req);

        JSONArray dataArray = null;
        String dataString = null;

        // Get payload if exist;
        dataArray = req.optJSONArray("payload");
        dataString = req.optString("payload");

        if (null != dataArray) {
            data = new CoapData(CCoapUtils.jarray2barray(dataArray), type);
        } else if (null != dataString) {
            data = new CoapData(dataString, type);
        }

        return data;
    }

    /**
     * Append the request options to a {@link CoapRequest} object.
     * 
     * @param req     Request information.
     * @param request CoapRequest object.
     * @throws CCoapException Thrown when an invalid option is found.
     */
    private void appendOptions(JSONObject req, CoapRequest request) throws CCoapException {

        if (req.has("options") == false) {
            return;
        }

        JSONArray options = null;

        try {
            options = req.getJSONArray("options");
        } catch (JSONException e) {
            throw new CCoapException("Invalid option", CCoapError.INVALID_ARGUMENT, e);
        }

        JSONObject option = null;
        String name;

        try {
            for (int i = 0; i < options.length(); i++) {
                option = options.getJSONObject(i);

                name = option.getString("name");

                if (name.equals("Accept")) {
                    String value = option.getString("value");

                    request.addAccept(stringToMediaType(value));
                } else if (name.equals("Content-Format")) {
                    String value = option.getString("value");

                    request.setContentType(stringToMediaType(value));
                } else if (name.equals("ETag")) {
                    byte[] value = CCoapUtils.jarray2barray(option.getJSONArray("value"));

                    request.addETag(value);
                } else if (name.equals("If-None-Match")) {
                    boolean value = option.getBoolean("value");

                    request.setIfNoneMatchOption(value);
                } else if (name.equals("If-Match")) {
                    byte[] value = CCoapUtils.jarray2barray(option.getJSONArray("value"));

                    request.addIfMatchOption(value);
                } else {
                    throw new CCoapException("Unknown option", CCoapError.INVALID_ARGUMENT);
                }
            }
        } catch (JSONException e) {
            throw new CCoapException("Malformed option", CCoapError.UNKNOWN, e);
        }
    }

    /**
     * Append the URI query to a {@link CoapRequest} object.
     * 
     * @param req     Request information.
     * @param request CoapRequest object.
     * @throws CCoapException Thrown if URI is missing.
     */
    private void appendQuery(JSONObject req, CoapRequest request) throws CCoapException {
        URI uri;

        try {
            uri = URI.create(req.getString("uri"));
        } catch (JSONException e) {
            throw new CCoapException("URI is missing", CCoapError.INVALID_ARGUMENT, e);
        }

        String query = uri.getQuery();

        if (null != query) {
            if (!query.isEmpty()) {
                request.setUriQuery(query);
            }
        }
    }

    /**
     * Create a {@link CoapRequest} object from the request information and a client
     * channel {@link CoapClientChannel}.
     * 
     * @param req     Request information.
     * @param channel Client channel.
     * @return CoapRequest object.
     * @throws CCoapException Thrown at any error.
     */
    private CoapRequest createRequest(JSONObject req, CoapClientChannel channel) throws CCoapException {

        CoapRequestCode code = parseCode(req);

        String path = parsePath(req);

        boolean confirmable = parseConfirmable(req);

        CoapData payload = parsePayload(req);

        CoapRequest request = channel.createRequest(code, path, confirmable);

        if (null != payload) {
            request.setPayload(payload);
        }

        appendOptions(req, request);

        appendQuery(req, request);

        return request;
    }

    /**
     * Transform the options received into a JSONArray to be sent to the javascript
     * side.
     * 
     * Returns null if no options are specified.
     * 
     * @param response {@link CoapResponse} object.
     * @return JSONArray with options.
     * @throws JSONException Thown if fails to inser a new option into the array.
     */
    private JSONArray extractOptions(CoapResponse response) throws JSONException {
        JSONArray opts = new JSONArray();

        CoapHeaderOptions options = response.getOptions();

        JSONObject opt = null;

        for (CoapHeaderOption option : options) {
            CoapHeaderOptionType type = option.getOptionType();

            if (type == CoapHeaderOptionType.Content_Format) {
                opt = new JSONObject();

                int number = (int) option.getOptionData()[0];

                CoapMediaType contentType = CoapMediaType.parse(number);

                opt.put("name", "Content-Format");
                opt.put("value", contentType.getMimeType());
                opts.put(opt);
            } else if (type == CoapHeaderOptionType.Size1) {
                opt = new JSONObject();

                byte[] data = option.getOptionData();

                int size1 = 0;

                for (int i = 0; i < data.length; i++) {
                    size1 <<= 8;
                    size1 |= (int) data[i];
                }

                opt.put("name", "Size1");
                opt.put("value", size1);
                opts.put(opt);
            } else if (type == CoapHeaderOptionType.Etag) {
                opt = new JSONObject();

                JSONArray etag = CCoapUtils.barray2jarray(option.getOptionData());

                opt.put("name", "Etag");
                opt.put("value", etag);
                opts.put(opt);
            } else if (type == CoapHeaderOptionType.Location_Path) {
                opt = new JSONObject();

                String locationPath = new String(option.getOptionData());

                opt.put("name", "Location-Path");
                opt.put("value", locationPath);
                opts.put(opt);
            } else if (type == CoapHeaderOptionType.Location_Query) {
                opt = new JSONObject();

                String locationQuery = new String(option.getOptionData());

                opt.put("name", "Location-Query");
                opt.put("value", locationQuery);
                opts.put(opt);
            } else if (type == CoapHeaderOptionType.Max_Age) {
                opt = new JSONObject();

                byte[] data = option.getOptionData();

                int maxAge = 0;

                for (int i = 0; i < data.length; i++) {
                    maxAge <<= 8;
                    maxAge |= (int) data[i];
                }

                opt.put("name", "Max-Age");
                opt.put("value", maxAge);
                opts.put(opt);
            } else if (type == CoapHeaderOptionType.Accept) {
                opt = new JSONObject();

                int number = (int) option.getOptionData()[0];
                CoapMediaType accept = CoapMediaType.parse(number);

                opt.put("name", "Accept");
                opt.put("value", accept.getMimeType());
                opts.put(opt);
            }
        }

        if (opts.length() > 0) {
            return opts;
        } else {
            return null;
        }
    }

    /**
     * Coap client callback on failed requests.
     * 
     * If the server is not found, calls the javascript error callback with error
     * code DESTINATION_IS_UNREACHABLE. Else, informe the error.
     * 
     * The client is closed after this call is executed.
     * 
     * @param channel       The {@link CoapClientChannel} where the error happened.
     * @param notReachable  Indicates that the server is not within reach.
     * @param resetByServer The server sent a connection reset.
     */
    @Override
    public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer) {
        if (notReachable) {
            callback.error(CCoapUtils.getErrorObject(this.id, CCoapError.DESTINATION_IS_UNREACHABLE,
                    "Destination is unreachable"));
        } else {
            callback.error(CCoapUtils.getErrorObject(this.id, CCoapError.CONNECTION_FAILED, "Connection Failed"));
        }

        channel.close();
    }

    /**
     * Coap client callback on valid responses.
     * 
     * Encapsulate the received response into a JSONObject and send to the
     * javascript's received callback.
     * 
     * On error, calls the javascript's error callback.
     * 
     * This function closes the connection.
     * 
     * @param channel  The {@link CoapClientChannel} where the response arrived.
     * @param response The {@link CoapResponse} that was received.
     */
    @Override
    public void onResponse(CoapClientChannel channel, CoapResponse response) {

        // Send response to javascript side.
        JSONObject res = new JSONObject();

        int mcode = response.getResponseCode().getValue();
        int code = ((mcode >> 5) * 100) | (mcode & 0x1F);

        String payloadString = null;
        JSONArray payloadRaw = null;

        boolean isBinary = (response.getContentType() == CoapMediaType.UNKNOWN)
                || (response.getContentType() == CoapMediaType.octet_stream)
                || (response.getContentType() == CoapMediaType.exi);

        if (isBinary) {
            payloadRaw = CCoapUtils.barray2jarray(response.getPayload());
        } else {
            payloadString = new String(response.getPayload());
        }

        JSONArray options = null;
        try {
            options = extractOptions(response);
        } catch (JSONException e) {
            callback.error(CCoapUtils.getErrorObject(this.id, CCoapError.UNKNOWN, "Error parsing options"));
        }

        try {
            res.put("id", this.id);
            res.put("code", code);

            if (isBinary) {
                res.put("payload", payloadRaw);
            } else {
                res.put("payload", payloadString);
            }

            if (null != options) {
                res.put("options", options);
            }

            callback.success(res);
        } catch (JSONException e) {
            callback.error(CCoapUtils.getErrorObject(this.id, CCoapError.UNKNOWN, "Cannot create response JSON"));
        }

        channel.close();
    }

    /**
     * Multicast received callback.
     * 
     * Not used since the client does not use multicast messages.
     * 
     * This callback reports an erro and closes the connection.
     */
    @Override
    public void onMCResponse(CoapClientChannel channel, CoapResponse response, InetAddress srcAddress, int srcPort) {
        callback.error(
                CCoapUtils.getErrorObject(this.id, CCoapError.INVALID_TRANSPORT, "Received a multicast response"));
    }
}
