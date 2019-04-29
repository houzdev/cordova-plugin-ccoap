# Introduction

Plugin to add support for the Constrained Application Protocol [CoAP](https://tools.ietf.org/html/rfc7252 "Coap") on [Cordova](https://cordova.apache.org/ "Apache Cordova") based mobile applications.

# Usage

To add this plugin to your application use:

```
    ionic cordova plugin add https://github.com/DKrepsky/cordova-plugin-ccoap.git
```

To update the plugin you need to first remove the plugin then reinstall:

```
ionic cordova plugin rm cordova-plugin-ccoap
ionic cordova plugin add https://github.com/DKrepsky/cordova-plugin-ccoap.git
```

Plugin interface documentation can be found at [CCoap.js](www/CCoap.js).

# Examples

## Making a request

```
    declare var CCoap:any;

    var request = {
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

CCoap.request(
    request,
    received => {
        alert(JSON.stringify(received))
    },
    error => {
        alert(JSON.stringify(error))
    }
)`
```

## Discovering devices

```
    declare var CCoap:any;

    CCoap.discover(
        device => {
            alert("New device found: \n" + JSON.stringfy(device))
        },
        error => {
            alert(JSON.stringfy(error))
        }
    )
```

# Limitations

- iOS support not implemented yet;
- Multicast discovery is not supported on android emulator [more info](https://developer.android.com/studio/run/emulator-networking);
- Coap server not implemented yet;
- Observable is not yet implemented;
- Block transfer not yet implemented;
- CBOR content type is not supported;
- DTLS not implemented yet.

# License

This code is released under the MIT license, for full disclosure check the [LICENCE](LICENSE) file.

# Acknowledgements

- Android support is based on [jCoAP](https://gitlab.amd.e-technik.uni-rostock.de/ws4d/jcoap);
