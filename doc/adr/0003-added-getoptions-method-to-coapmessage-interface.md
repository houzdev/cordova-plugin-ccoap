# 3. Added getOptions() method to CoapMessage interface

Date: 2019-02-02

## Status

Accepted

## Context

The CoapResponse interface does not provide a clean way to access all options passed by the server.

## Decision

Add a getOptions() method to the CoapMessage interface, since this method is already implemented at BaseCoapMessage which the CoapResponse is derived from.

## Consequences

This will make possible to pass back to the JS side all options received from the server, but it will add another step to update the jCoAP library, i.e. adding the getOptions() prototype to the CoapMessage interface.
