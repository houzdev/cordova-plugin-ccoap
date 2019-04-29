package cordova.plugin.ccoap;

import cordova.plugin.ccoap.CCoapError;

import java.lang.Exception;
import java.lang.Throwable;

/**
 * CCoapException
 * 
 * Custom exception.
 * 
 * @author David Krepsky
 */
public class CCoapException extends Exception {
    private static final long serialVersionUID = 1L;
    private CCoapError error_ = CCoapError.UNKNOWN;
    private Throwable cause_ = null;

    /**
     * Overloaded method.
     * 
     * @param errorMessage
     */
    public CCoapException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Overloaded method.
     * 
     * @param errorMessage
     * @param cause
     */
    public CCoapException(String errorMessage, Throwable cause) {
        super(errorMessage);
        this.cause_ = cause;
    }

    /**
     * Overloaded method.
     * 
     * @param errorMessage
     * @param error
     */
    public CCoapException(String errorMessage, CCoapError error) {
        super(errorMessage);
        this.error_ = error;
    }

    /**
     * Creates a new exception.
     * 
     * @param errorMessage Error message.
     * @param error        Error code.
     * @param cause        Original exception.
     */
    public CCoapException(String errorMessage, CCoapError error, Throwable cause) {
        super(errorMessage);
        this.error_ = error;
        this.cause_ = cause;
    }

    /**
     * Return error code.
     * 
     * @return Error code.
     */
    public CCoapError getErrorCode() {
        return this.error_;
    }

    /**
     * Return original exception when available or null.
     * 
     * @return Original exception.
     */
    public Throwable getCause() {
        return this.cause_;
    }
}