# 4. Removed log4j2 dependency

Date: 2019-02-02

## Status

Accepted

Amends [2. Moved from MBED java-coap to jcoap](0002-moved-from-mbed-java-coap-to-jcoap.md)

## Context

jCoAP depends on log4j2 to output logs. Adding this dependency may cause incompatibility with other plugins according to [Adding Dependency Libraries](https://cordova.apache.org/docs/en/latest/guide/platforms/android/plugin.html#adding-dependency-libraries).

## Decision

Remove all references to log4j2 from jCoAP.

## Consequences

This eliminates all dependencies from jCoAP, making it really hard to cause any problem with other plugins.
The cost is to always remove log4j2 from the sources.
In the future, options may include an script to remove not only log4j2 but also new dependencies that may be introduced into the project.
