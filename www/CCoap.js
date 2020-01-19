/**
 * @module CCoap
 * @author David Krepsky
 * @copyright Houz Automacao
 * @version 0.2.0
 */
var exec = require('cordova/exec')

/**
 * Coap request.
 *
 * This function performs a coap request, returning a promisse.
 *
 * A request is composed with the following properties:
 * - *id:  number, optional* An uniq message identifier used to link a response to a request, this parameter is optional and is set -1 in responses if not specified;
 * - *method: string, optional* Coap request method {"get", "post", "put" "delete"}, defaults to "get" when not specified;
 * - *uri: string, required* Resource uri;
 * - *payload: binary data or string, optional* Data to be sent, this parameter is optional and only used with "POST" and "PUT" methods;
 * - *options: array of objects, optional* CoAP options are composed of an object with a property *name* of type string and a value with variable type. Available names and values are described at RFC-7252;
 * - *confirmable: boolean, optional* Set the CON flag, this parameter is optional, default is true.
 *
 * Below is an example of a request:
 *
 * const req = {
 *  id: 547,
 *  method: "post",
 *  uri: "coap://example.com:5683/post",
 *  payload: "Hello",
 *  options: [{name: 'Content-Format', value: 'text/plain; charset=utf-8'}],
 * }
 *
 * A response object is passed to the resolved callback, this object has the following properties:
 * - *id: number* Same identifier used at the request, or -1 if not specified;
 * - *code: number* Response code as per RFC-7252;
 * - *payload: binary or string* Response data, format is specified by the content type;
 * - *options: array of object* Response options in the same format as the request, available values are described at RFC-7252;
 *
 * @function request
 * @param {object} request Request parameters
 */
module.exports.request = function (request) {
  return new Promise(function (resolve, reject) {
    exec(resolve, reject, 'CCoap', 'request', [request])
  });
}

/**
 * Multicast discovery
 *
 * This function does a server and resource discovery according to RFC-7252 Section 7.
 *
 * Discovered devices are returned inside an array to the promise's resolve callback.
 *
 * Discovery request is sent to multicast address 224.0.1.187, port 5863.
 *
 * Each device found object have the following properties:
 * - *address: string* Device IPv4 address;
 * - *port: number* Device port number;
 * - *resources: string* Core Link Format string (check RFC-6690).
 *
 * @function discover
 * @param {number} timeout Time to wait for devices to announce themselves in milliseconds.
 * @return Array of discovered devices.
 * @note IPv4 only.
 */
module.exports.discover = function (timeout) {
  return new Promise(function (resolve, reject) {
    exec(resolve, reject, 'CCoap', 'discover', [timeout]);
  });
}