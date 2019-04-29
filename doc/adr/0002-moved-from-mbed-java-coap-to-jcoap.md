# 2. Moved from MBED java-coap to jcoap

Date: 2019-02-01

## Status

Accepted

Amended by [4. Removed log4j2 dependency](0004-removed-log4j2-dependency.md)

## Context

The MBED java-coap library does not provide a clear way to discovery devices through multicast.
Also, there is no documentation available, which makes it even harder to implement a functional plugin based on this lib.

## Decision

I'm moving the android coap library to [jCoAP] (https://gitlab.amd.e-technik.uni-rostock.de/ws4d/jcoap). 

## Consequences

The jCoAP library provides a better documentation and a really easy wait to do multicast discovery. This will affect the android plugin completely.
