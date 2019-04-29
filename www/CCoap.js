/**
 * @module CCoap
 * @author David Krepsky
 * @copyright Houz Automacao
 * @version 1.0.0
 */
var exec = require('cordova/exec')

/**
 * Enum with error codes.
 * @readonly
 * @enum {number}
 */
var CCOAP_ERROR = {
  NO_ERROR: 0,
  INVALID_ARGUMENT: 1,
  INVALID_MESSAGE: 2,
  INVALID_TRANSPORT: 3,
  INVALID_ACTION: 4,
  CONNECTION_FAILED: 5,
  DESTINATION_IS_UNREACHABLE: 6,
  UNKNOWN: 7
}

/**
 * Coap request.
 *
 * This function receives an object that contains information about the request
 * and executes the received callback when the server responds.
 *
 * A request is composed with the following properties:
 * - (id: number) An uniq message identifier used to link a response to a
 *      request, this parameter is options and return -1 if not specified;
 * - (method: string) Coap request method {"GET", "POST", "PUT" "DELETE"};
 * - (uri: string) Resource uri;
 * - (payload: array of number or string) Data to be sent, this parameter is
 *      optional;
 * - (options: array of objects) CoAP options are composed of an object with a
 *      property *name* of type string and a value with variable type. Available
 *      names and values are described at RFC-7252,it is mandatory to provide at
 *      least the Content-Format option;
 * - (confirmable: boolean) Set the CON flag, this parameter is optional,
 *      default is true.
 *
 * Below is an example of a request:
 *
 * let request = {
 *  id: 547,
 *  method: "post",
 *  uri: "coap://example.com:5683/post",
 *  payload: "Hello",
 *  options: [{name: 'Content-Format', value: 'text/plain; charset=utf-8'}],
 * }
 *
 * A response object is passed to the received callback, this object has the
 * following properties:
 * - (id: number) Same identifier used at the request, or -1 if not specified;
 * - (code: number) Response code as per RFC-7252;
 * - (payload: array of char or string) Response data, format is specified by
 *      the content type;
 * - (options: array of object) Response options in the same format as the
 *      request, available values are described at RFC-7252;
 *
 * @function request
 * @param {object} request Request parameters
 * @param {Request~ReceivedCallback} received Received callback
 * @param {ErrorCallback} error Error callback
 */
module.exports.request = function(request, received, error) {
  exec(received, error, 'CCoap', 'request', [request])
}

/**
 * Multicast discovery
 *
 * This function does a server and resource discovery according to RFC-7252
 * Section 7.
 *
 * Discovered devices are passed as parameters to the *received* callback
 * function.
 *
 * Discovery request is sent to multicast address 224.0.1.187, port 5863.
 *
 * Returned device object does have the following properties:
 * - (address: string) Server IPv4 address;
 * - (port: number) Server port number;
 * - (link_format: string) Core Link Format string (check RFC-6690).
 *
 * @function discover
 * @param {Discovery~ReceivedCallback} received Received callback
 * @param {ErrorCallback} error Error callback
 *
 * @note IPv4 only.
 */
module.exports.discover = function(received, error) {
  exec(received, error, 'CCoap', 'discover', null)
}

/**
 * Called when the client receives an answer from the server.
 * Check {@link request}
 *
 * @callback Request~ReceivedCallback
 * @param {object} response Server response.
 */

/**
 * Called when the discovery client receives a response.
 * Check {@link received}
 *
 * @callback Discovery~ReceivedCallback
 * @param {object} device Device object.
 *
 */

/**
 * Returns an error object.
 *
 * Error information object:
 * - (id: number) Request id;
 * - (code: CCOAP_ERROR) Error code {@link CCOAP_ERROR};
 * - (message: string) Error message;
 * - (extra: string) Optional exception message;
 *
 * @callback ErrorCallback
 * @param {object} error Error information.
 */
