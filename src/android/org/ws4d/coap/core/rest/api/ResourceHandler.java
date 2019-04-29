package org.ws4d.coap.core.rest.api;

import java.util.List;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.CoapData;

public interface ResourceHandler {

	public CoapMediaType getMediaType();

	public CoapData handleGet();

	public CoapData handleGet(List<String> queryString);

	public boolean handlePost(byte[] data);

	public boolean handlePut(byte[] data);

	public boolean handleDelete();

}
