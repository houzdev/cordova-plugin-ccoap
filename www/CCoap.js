/**
 * @module CCoap
 * @author David Krepsky
 * @copyright Houz Automacao
 * @version 0.4.0
 */
var exec = require('cordova/exec')

class CCoap {

  get(uri) {
    const req = {
      method: "get",
      uri: uri,
      confirmable: true
    };

    return this.request(req);
  }

  post(uri, payload) {
    const req = {
      method: "post",
      uri: uri,
      payload: payload,
      confirmable: true
    };

    return this.request(req);
  }

  put(uri, payload) {
    const req = {
      method: "put",
      uri: uri,
      payload: payload,
      confirmable: true
    };

    return this.request(req);
  }

  delete(uri) {
    const req = {
      method: "delete",
      uri: uri,
      confirmable: true
    };

    return this.request(req);
  }

  discover(timeout) {
    return new Promise(function (resolve, reject) {
      exec(resolve, reject, 'CCoap', 'discover', [timeout]);
    });
  }

  request(req) {
    return new Promise(function (resolve, reject) {
      exec(resolve, reject, 'CCoap', 'request', [req])
    });
  }
}

module.exports = new CCoap;