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
import org.ws4d.coap.core.enumerations.CoapBlockSize;
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
import java.util.Random;

import android.util.Log;

/**
 * CCoapClient
 * 
 * This class is responsible for translating and executing a coap request from
 * the javascript side.
 * 
 * @author David Krepsky
 */
public class CCoapClient implements CoapClient {

    private JSONObject jreq = null;
    private CallbackContext callback;
    private URI uri;
    private CoapRequest request = null;
    private CoapClientChannel channel = null;
    private Random tokenGenerator = null;

    /**
     * Create and send a request.
     * 
     * @param req             Request information wrapped as a json object.
     * @param callbackContext Javascript received and error callbacks.
     * @throws CCoapException Thrown at any error {@link CCoapException}.
     */
    public void request(final JSONObject req, final CallbackContext callbackContext) throws CCoapException {

        Log.v("CCoap", "Request");

        this.jreq = req;
        this.callback = callbackContext;

        try {
            createURI();
            createChannel();
            createRequest();
            appendOptions();
            appendQuery();
            appendPayload();
            this.channel.sendMessage(this.request);
        } catch (final CCoapException e) {
            if (null != this.channel) {
                this.channel.close();
            }

            throw e;
        }
    }

    /**
     * Create a java URI object from the request json.
     * 
     * @throws CCoapException Thrown when the uri is missing in json request.
     */
    private void createURI() throws CCoapException {
        try {
            this.uri = URI.create(this.jreq.getString("uri"));
        } catch (final JSONException e) {
            throw new CCoapException("URI is missing", CCoapError.INVALID_ARGUMENT, e);
        }
    }

    /**
     * Parse the server address and creates a CoapChannel connected to the server;
     * 
     * @throws CCoapException Thrown at any error {@link CCoapException}.
     */
    private void createChannel() throws CCoapException {

        final CoapChannelManager manager = BasicCoapChannelManager.getInstance();

        InetAddress addr;

        try {
            addr = InetAddress.getByName(this.uri.getHost());
        } catch (final UnknownHostException e) {
            throw new CCoapException("Invalid server address", CCoapError.DESTINATION_IS_UNREACHABLE, e);
        }

        final int port = this.uri.getPort();

        this.channel = manager.connect(this, addr, port);

        this.channel.setMaxReceiveBlocksize(CoapBlockSize.BLOCK_1024);
        this.channel.setMaxSendBlocksize(CoapBlockSize.BLOCK_1024);
    }

    /**
     * Create a {@link CoapRequest} object from the request information.
     */
    private void createRequest() {

        String path = this.uri.getPath();

        if (path == null || path.isEmpty())
            path = "/";

        final CoapRequestCode code = CoapRequestCode.parse(this.jreq.optString("method", "get"));

        final boolean confirmable = this.jreq.optBoolean("confirmable", true);

        this.request = this.channel.createRequest(code, path, confirmable);
    }

    /**
     * Append the request options to a {@link CoapRequest} object.
     * 
     * @throws CCoapException Thrown when an invalid option is found.
     */
    private void appendOptions() throws CCoapException {

        final JSONArray options = this.jreq.optJSONArray("options");

        if (null == options)
            return;

        try {
            for (int i = 0; i < options.length(); i++) {
                final JSONObject option = options.getJSONObject(i);
                final String name = option.getString("name");

                if (name.equals("Accept")) {
                    final String mimeType = option.getString("value");
                    this.request.addAccept(CoapMediaType.parse(mimeType));
                } else if (name.equals("Content-Format")) {
                    final String mimeType = option.getString("value");
                    this.request.setContentType(CoapMediaType.parse(mimeType));
                } else if (name.equals("ETag")) {
                    final byte[] value = CCoapUtils.jarray2barray(option.getJSONArray("value"));
                    this.request.addETag(value);
                } else if (name.equals("If-None-Match")) {
                    final boolean value = option.getBoolean("value");
                    this.request.setIfNoneMatchOption(value);
                } else if (name.equals("If-Match")) {
                    final byte[] value = CCoapUtils.jarray2barray(option.getJSONArray("value"));
                    this.request.addIfMatchOption(value);
                } else {
                    throw new CCoapException("Unknown option", CCoapError.INVALID_ARGUMENT);
                }
            }
        } catch (final JSONException e) {
            throw new CCoapException("Malformed option", CCoapError.UNKNOWN, e);
        }
    }

    /**
     * Append the URI query to a {@link CoapRequest} object.
     */
    private void appendQuery() {

        final String query = this.uri.getQuery();

        if (null != query && !query.isEmpty()) {
            this.request.setUriQuery(query);
        }
    }

    private void appendPayload() throws CCoapException {

        final boolean hasPayload = this.jreq.has("payload");
        final boolean isPostPut = (request.getRequestCode() == CoapRequestCode.PUT)
                || (request.getRequestCode() == CoapRequestCode.POST);

        if ((hasPayload == false) || (isPostPut == false)) {
            return;
        }

        CoapMediaType type = this.request.getContentType();

        byte[] raw;

        final Object payload = this.jreq.opt("payload");

        if (payload instanceof String) {
            raw = Encoder.StringToByte((String) payload);
            if (type == null)
                type = CoapMediaType.text_plain;
        } else if (payload instanceof JSONArray) {
            raw = CCoapUtils.jarray2barray((JSONArray) payload);
            if (type == null)
                type = CoapMediaType.octet_stream;
        } else if (payload instanceof JSONObject) {
            raw = Encoder.StringToByte(((JSONObject) payload).toString());
            if (type == null)
                type = CoapMediaType.json;
        } else {
            throw new CCoapException("Invalid payload format", CCoapError.INVALID_ARGUMENT);
        }

        final CoapData data = new CoapData(raw, type);

        this.request.setPayload(data);

        if (raw.length > 1024) {
            Log.v("CCoap", "Init block1 transfer");
            this.request = this.channel.addBlockContext(this.request);
        }
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
    private JSONArray extractOptions(final CoapResponse response) throws JSONException {
        final JSONArray opts = new JSONArray();

        final CoapHeaderOptions options = response.getOptions();

        JSONObject opt = null;

        for (final CoapHeaderOption option : options) {
            final CoapHeaderOptionType type = option.getOptionType();

            if (type == CoapHeaderOptionType.Content_Format) {
                opt = new JSONObject();

                int number = 0;
                if (option.getOptionData().length > 0) {
                    number = (int) option.getOptionData()[0];
                }

                final CoapMediaType contentType = CoapMediaType.parse(number);

                opt.put("name", "Content-Format");
                opt.put("value", contentType.getMimeType());
                opts.put(opt);
            } else if (type == CoapHeaderOptionType.Size1) {
                opt = new JSONObject();

                final byte[] data = option.getOptionData();

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

                final JSONArray etag = CCoapUtils.barray2jarray(option.getOptionData());

                opt.put("name", "Etag");
                opt.put("value", etag);
                opts.put(opt);
            } else if (type == CoapHeaderOptionType.Location_Path) {
                opt = new JSONObject();

                final String locationPath = new String(option.getOptionData());

                opt.put("name", "Location-Path");
                opt.put("value", locationPath);
                opts.put(opt);
            } else if (type == CoapHeaderOptionType.Location_Query) {
                opt = new JSONObject();

                final String locationQuery = new String(option.getOptionData());

                opt.put("name", "Location-Query");
                opt.put("value", locationQuery);
                opts.put(opt);
            } else if (type == CoapHeaderOptionType.Max_Age) {
                opt = new JSONObject();

                final byte[] data = option.getOptionData();

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

                final int number = (int) option.getOptionData()[0];
                final CoapMediaType accept = CoapMediaType.parse(number);

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
     * Create a new message token with @p size bytes.
     * 
     * TODO: Add token to request.
     * 
     * @param size Length of the token.
     * @return New random token.
     */
    private byte[] createToken(final int size) {

        if (this.tokenGenerator == null) {
            this.tokenGenerator = new Random();
        }

        final byte[] token = new byte[size];

        this.tokenGenerator.nextBytes(token);

        return token;
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
    public void onConnectionFailed(final CoapClientChannel channel, final boolean notReachable,
            final boolean resetByServer) {
        Log.e("CCoap", "Connection Failed");

        if (notReachable) {
            this.callback.error(
                    CCoapUtils.getErrorObject(-1, CCoapError.DESTINATION_IS_UNREACHABLE, "Destination is unreachable"));
        } else {
            this.callback.error(CCoapUtils.getErrorObject(-1, CCoapError.CONNECTION_FAILED, "Connection Failed"));
        }

        this.channel.close();
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
    public void onResponse(final CoapClientChannel channel, final CoapResponse response) {

        Log.v("CCoap", "Received");

        try {
            final JSONObject jres = new JSONObject();

            // Append response code.
            final int mcode = response.getResponseCode().getValue();
            final int code = ((mcode >> 5) * 100) | (mcode & 0x1F);
            jres.put("code", code);

            // Append payload, if exists.
            final boolean hasPayload = response.getPayload() != null;

            if (hasPayload) {
                Object payload = null;
                final CoapMediaType type = response.getContentType();

                final boolean isString = (type == CoapMediaType.text_plain) || (type == CoapMediaType.xml)
                        || (type == CoapMediaType.link_format);
                final boolean isJson = (type == CoapMediaType.json);

                /// TODO: Parse json as object in furute versions.
                if (isString || isJson) {
                    payload = Encoder.ByteToString(response.getPayload());
                    jres.put("payload", (String) payload);
                }
                /// NOTE: Removed to keep compactible with v0.2.0.
                // else if (isJson) {
                // payload = new JSONObject(Encoder.ByteToString(response.getPayload()));
                // jres.put("payload", (JSONObject) payload);
                // }
                else {
                    payload = CCoapUtils.barray2jarray(response.getPayload());
                    jres.put("payload", (JSONArray) payload);
                }
            }

            // Append options.
            final JSONArray options = extractOptions(response);

            if (null != options) {
                jres.put("options", options);
            }

            this.callback.success(jres);
        } catch (final JSONException e) {
            this.callback.error(CCoapUtils.getErrorObject(-1, CCoapError.UNKNOWN, "Cannot create response JSON"));
        } finally {
            this.channel.close();
        }
    }

    /**
     * Multicast received callback.
     * 
     * Not used since the client does not use multicast messages.
     * 
     * This callback reports an erro and closes the connection.
     */
    @Override
    public void onMCResponse(final CoapClientChannel channel, final CoapResponse response, final InetAddress srcAddress,
            final int srcPort) {
        this.callback
                .error(CCoapUtils.getErrorObject(-1, CCoapError.INVALID_TRANSPORT, "Received a multicast response"));
    }
}
