package cordova.plugin.ccoap;

/**
 * CCoapError
 * 
 * Custom error codes.
 * 
 * @author David Krepsky
 */
public enum CCoapError {
    NO_ERROR(0), /// Not used.
    INVALID_ARGUMENT(1), /// Invalid argument passed to plugin.
    INVALID_MESSAGE(2), /// Malformed coap message.
    INVALID_TRANSPORT(3), /// Wrong transport socket.
    INVALID_ACTION(4), /// Function call does not exist.
    CONNECTION_FAILED(5), /// Connection failed.
    DESTINATION_IS_UNREACHABLE(6), /// Destination is unreachable.
    UNKNOWN(7); /// Unknown error.

    private int code_;

    /**
     * Constructor.
     * 
     * @param code Error code.
     */
    CCoapError(int code) {
        this.code_ = code;
    }

    /**
     * Get code as integer;
     * 
     * @return Integer error code.
     */
    public int getCode() {
        return this.code_;
    }
}