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
The plugin is automagically loaded into global scope by cordova.
To access the plugin use the global *CCoap* object after the `deviceready` event.

```js
document.addEventListener("deviceready", () => {
    CCoap.get(...).then(...);
});
```

### Basic requests


## Examples
#### GET
```js
document.addEventListener("deviceready", () => {
    CCoap.get('coap://example.com/').then(res => {
        console.log(res);
    }).catch(err => {
        console.log(err);
    });
});
```

#### POST


#### PUT

#### DELETE

#### OBSERVE

#### Multicast 

#### Binary files

## Examples

## Making a request

Using promisses:

```
    declare var CCoap:any;

    const req = {
        id: 1,
        uri: "coap://example.com:5683/api/test&param1=0&param2=testquery",
        method: "post",
        payload: "Hello World",
        options: [
            {name: "Content-Format", value: "text/plain; charset=utf-8"},
            {name: "Accept", value: "text/plain; charset=utf-8"},
            {name: "Accept", value: "application/json"}
        ]
    }

    CCoap.request(req,
        res => {
            console.log(res.payload);
        },
        error => {
            console.log(error);
        }
    );
```

Using async/await:


```
    declare var CCoap:any;
    
    async function getRequest(){
        const req = {uri: "coap://example.com:5683/test"};

        try {
            const res = await CCoap.request(req);
            
            console.log(res.code);
            console.log(res.payload);
        } catch (error){
            console.log(error);
        }
    }
```

## Discovering devices

```
    declare var CCoap:any;

    async function discover (){
        try {
            const devices = await CCoap.discover(500);
            console.log(devices);
        } catch(error){
            console.log(error);
        }
    } 
```

# Limitations

- iOS support not implemented yet;
- Coap server not implemented yet;
- Observable is not yet implemented;
- Block transfer not yet implemented;
- CBOR content type is not supported;
- DTLS not implemented yet.

# License

This code is released under the MIT license, for full disclosure check the [LICENCE](LICENSE) file.

# Acknowledgements

- Android support is based on [jCoAP](https://gitlab.amd.e-technik.uni-rostock.de/ws4d/jcoap);
