package cordova.plugin.ccoap;

import org.apache.cordova.CallbackContext;

import cordova.plugin.ccoap.CCoapError;
import cordova.plugin.ccoap.CCoapException;
import cordova.plugin.ccoap.CCoapUtils;

import org.ws4d.coap.core.*;
import org.ws4d.coap.core.connection.*;
import org.ws4d.coap.core.connection.api.*;
import org.ws4d.coap.core.enumerations.*;
import org.ws4d.coap.core.messages.api.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * CCoapDiscovery
 * 
 * Make a multicast get request to '/.well-known/core' at address
 * '224.0.1.187:5683' and listen for incoming responses for 60 secconds.
 * 
 * @author David Krepsky
 */
public class CCoapDiscovery implements CoapClient {
    static final int TIMEOUT_MS = 60000;
    private CoapClientChannel channel;
    private CallbackContext callback;
    private Timer timer;

    /**
     * Timer task to close the socket after {@link TIMEOUT_MS} seconds.
     */
    private class StopDiscovery extends TimerTask {
        CoapClientChannel channel_;

        StopDiscovery(CoapClientChannel channel) {
            this.channel_ = channel;
        }

        @Override
        public void run() {
            channel_.close();
        }
    }

    /**
     * Constructor.
     * 
     * @param callbackContext Callbacks from the javascript side.
     */
    public CCoapDiscovery(CallbackContext callbackContext) {
        this.callback = callbackContext;
    }

    /**
     * Start the coap client and send a discover request.
     * 
     * @throws CCoapException Encapsulated error information.
     */
    public void start() throws CCoapException {

        CoapChannelManager manager = BasicCoapChannelManager.getInstance();

        InetAddress address;

        try {
            address = InetAddress.getByName(CoapConstants.COAP_ALL_NODES_IPV4_MC_ADDR);
        } catch (UnknownHostException e) {
            throw new CCoapException("Unknown host", CCoapError.INVALID_ARGUMENT, e);
        }

        channel = manager.connect(this, address, CoapConstants.COAP_DEFAULT_PORT);

        // initialize the request
        CoapRequest request = channel.createRequest(CoapRequestCode.GET, "/.well-known/core", false);

        // add a token to match outgoing multicast message and incoming unicast messages
        request.setToken("MCToken".getBytes());

        // send the request
        channel.sendMessage(request);

        timer = new Timer();

        try {
            timer.schedule(new StopDiscovery(channel), TIMEOUT_MS);
        } catch (IllegalArgumentException e) {
            channel.close();
            throw new CCoapException("Cannot start shutdown timer", CCoapError.UNKNOWN, e);
        } catch (IllegalStateException e) {
            channel.close();
            throw new CCoapException("Cannot start shutdown timer", CCoapError.UNKNOWN, e);
        } catch (NullPointerException e) {
            channel.close();
            throw new CCoapException("Cannot start shutdown timer", CCoapError.UNKNOWN, e);
        }
    }

    /**
     * Coap client callback to responses.
     * 
     * This function shoul never be called since we are using multicast. Send an
     * error to the javascript side if anything comes here.
     * 
     * @param channel  - {@link CoapClientChannel} where the message arrived.
     * @param response - {@link CoapResponse} that arrived.
     */
    @Override
    public void onResponse(CoapClientChannel channel, CoapResponse response) {
        callback.error(CCoapUtils.getErrorObject(-1, CCoapError.CONNECTION_FAILED, "Wrong protocol"));
    }

    /**
     * Coap client callback to multicast responses.
     * 
     * Each device response is handled here. Calls the received callback on the
     * javascript side passing as parameter the received device information.
     * 
     * On error, calls the javascript error callback.
     * 
     * @param channel    - The {@link CoapClientChannel} where the message arrived.
     * @param resonse    - The {@link CoapResponse} that arrived
     * @param srcAddress - The IP address of the origin server of the response.
     * @param srcPort    - The Port of the origin server.
     */
    @Override
    public void onMCResponse(CoapClientChannel channel, CoapResponse response, InetAddress srcAddress, int srcPort) {

        JSONObject res = new JSONObject();
        String payload = new String(response.getPayload());

        try {
            res.put("address", srcAddress.getHostAddress());
            res.put("port", srcPort);
            res.put("link_format", payload);
        } catch (JSONException e) {
            callback.error(CCoapUtils.getErrorObject(-1, CCoapError.UNKNOWN, "Cannot create JSON response", e));
            return;
        }

        callback.success(res);
    }

    /**
     * Coap client callback to handle connection problems.
     * 
     * This sould never be called discovery messages are non confirmable. Calls the
     * javascript error callback when called to indicate that something is not
     * working properlly.
     * 
     * @param channel       - The {@link CoapClientChannel} where the connection
     *                      failed.
     * @param notReachable  - A flag to indicate that the server is not reachable
     * @param resetByServer - A flag to indicate that the remote server made a reset
     */
    @Override
    public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer) {
        callback.error(CCoapUtils.getErrorObject(-1, CCoapError.CONNECTION_FAILED, "Connection failed"));
    }

}
