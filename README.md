# cordova-plugin-ccoap

## Introduction

Plugin to add support for the Constrained Application Protocol 
[CoAP](https://tools.ietf.org/html/rfc7252 "Coap") on 
[Cordova](https://cordova.apache.org/ "Apache Cordova") based mobile 
applications.

## Installation

To add this plugin to your application use:

```
    ionic cordova plugin add https://github.com/houzdev/cordova-plugin-ccoap.git
```

To update the plugin you need to remove the plugin and then reinstall:

```
ionic cordova plugin rm cordova-plugin-ccoap
ionic cordova plugin add https://github.com/houzdev/cordova-plugin-ccoap.git
```
## Typescript support
The plugin comes with defined types, but since these are not published as a npm 
package you need to manually install them. To do so, add the *types* directory 
as a as a type root to your *tsconfig.json* and enable the typings by adding 
CCoap to *types*.

Example config:

```js
//tsconfig.json
{
  "compilerOptions": {
    "typeRoots" : [
      "./plugins/cordova-plugin-ccoap/types"
    ],
    "types": [
      "CCoap"
    ],
  }
  ```
  
## Supported Platforms
Only the Android platform is supported by now.

## Usage

### Accessing the plugin
The plugin is automagically loaded into global scope by Cordova.
To access the plugin use the global **CCoap** object after the `deviceready` event.

```js
document.addEventListener("deviceready", () => {
    CCoap.get(...).then(...);
});
```

### Basic requests

The plugin exposes the methods **get**, **post**, **put** and **delete** to perform basic 
transactions easily. All methods require an URI string as parameter and methods 
**post** and **put** have an optional payload parameter that can be of type string, 
object or array. The payload Content-Format is set automatically based on the 
parameter type, being *"text-plain"* if payload is a string, *"application/json"* if 
payload is an object or *"octet-stream"* if payload is an array.

Each method returns a promise that resolves to a **CCoapResponse** object on success
or an error message when rejected.

The **CCoapResponse** object contains the following fields:

* **code**: response code from the server;
* **payload**: data sent from the server as either string or array;
* **options**: array of CCoapOption.

#### GET example

Function: `CCoap.get(uri: string) : Promise<CCoapResponse>`

```js
document.addEventListener("deviceready", () => {
    CCoap.get('coap://example.com:5683/get?query=1').then(res => {
        console.log(res);
    }).catch(err => {
        console.log(err);
    });
});
```

#### POST example

Function: `CCoap.post(uri: string, payload: string | Array) : Promise<CCoapResponse>`

```js
document.addEventListener("deviceready", () => {
    CCoap.post('coap://example.com:5683/post?query=1', "Hello").then(res => {
        console.log(res);
    }).catch(err => {
        console.log(err);
    });
});
```

#### PUT example

Function: `CCoap.put(uri: string, payload: string | Array) : Promise<CCoapResponse>`

```js
document.addEventListener("deviceready", () => {
    CCoap.put('coap://example.com:5683/put?query=1', "Hello").then(res => {
        console.log(res);
    }).catch(err => {
        console.log(err);
    });
});
```

#### DELETE example

Function: `CCoap.delete(uri: string) : Promise<CCoapResponse>`

```js
document.addEventListener("deviceready", () => {
    CCoap.delete('coap://example.com:5683/delete?query=1').then(res => {
        console.log(res);
    }).catch(err => {
        console.log(err);
    });
});
```

### Discovering devices and services

The plugin comes with a discover function, which does a server and resource discovery according to [RFC-7252](https://tools.ietf.org/html/rfc7252) Section 7, by sending a multicast request to  address 224.0.1.187 at port 5683 and path */.well-known/core*. A **timeout** parameter, in **milliseconds**, limits the amount of time the plugin keeps listening for new responses, if timeout is not specified, a default of 2000 ms is used.

The discover function returns a promise and the discovered devices are returned in an array to the promise's resolve callback as a **CCoapDiscoveredDevice** object.
  
Each **CCoapDiscoveredDevice** object have the following properties:
* **address: string** Device IPv4 address;
* **port: number** Device port number;
* **resources: string** Core Link Format string (check [RFC-6690](https://tools.ietf.org/html/rfc6690)).

#### DISCOVER example

Function: `CCoap.discover(timeout: integer): Promise<CCoapDiscoveredDevice[]>`

```js
document.addEventListener("deviceready", () => {
    // Wait for 500ms
    CCoap.discover(500).then(devices => {
        console.log(devices);
    }).catch(error => {
        console.log(error);
    });
});
```

### Block transfer
Methods *get*, *post*, *put*, *delete* and *request* handle block transfers
automatically when the message body (payload) is greater than 1024 bytes, either
during requests or responses, i.e. if the payload size in a post or put request is greater than 1024 bytes, the payload will be tranferred in chunks to the server through the [Block1](https://tools.ietf.org/html/rfc7959#section-2.5) option, 
also, if the server sends a [Block2](https://tools.ietf.org/html/rfc7959#section-2.4)
option, the payload inside a CCoapResponse will be the complete message body, 
with all chunks reassembled.


## Limitations

- Coap server not implemented yet;
- Observable is not yet implemented;
- CBOR content type is not supported;
- DTLS not implemented yet.

## License

This code is released under the MIT license, for full disclosure check the [LICENCE](LICENSE) file.

## Acknowledgements

- Android support is based on [jCoAP](https://gitlab.amd.e-technik.uni-rostock.de/ws4d/jcoap);
