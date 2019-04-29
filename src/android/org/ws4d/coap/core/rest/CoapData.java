package org.ws4d.coap.core.rest;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.tools.Encoder;

/**
 * This class serves as a container for data to be transmitted and its corresponding media type
 * As by this class both entities are bound together, setting the corresponding media type thus cannot be forgotten.
 */
public class CoapData {

	/** The actual data*/
	private final byte[] data;
	
	/** The media type of the data*/
	private final CoapMediaType type;

	/** 
	 * Constructing a CoapData container from a binary representation.
	 * This is recommended for use with any format incorporating a binary representation.
	 * For string based formats we recommend using {@link #CoapData(String dataPayload, CoapMediaType coapMediaType)} as this method will apply a proper string encoding.
	 * @param dataPayload the actual data (in binary form)
	 * @param coapMediaType the media type of the data
	 */
	public CoapData(byte[] dataPayload, CoapMediaType coapMediaType) {
		if (null == dataPayload || null == coapMediaType) {
			throw new IllegalArgumentException();
		}
		this.data = dataPayload;
		this.type = coapMediaType;
	}
	
	/** 
	 * Constructing a CoapData container from a string representation.
	 * This is recommended for use with any format incorporating a textual representation, as a proper string encoding will be applied.
	 * For binary formats we recommend using {@link #CoapData(byte[] dataPayload, CoapMediaType coapMediaType)}
	 * @param dataPayload the actual data (as string representation)
	 * @param coapMediaType the media type of the data
	 */
	public CoapData(String dataPayload, CoapMediaType coapMediaType) {
		if (null == dataPayload || null == coapMediaType) {
			throw new IllegalArgumentException();
		}
		this.data = Encoder.StringToByte(dataPayload);
		this.type = coapMediaType;
	}

	/**
	 * @return The actual data from this container in binary form.
	 * To get a payload that comprises a string representation use {@link #getPayloadAsString()} instead
	 */
	public byte[] getPayload() {
		return this.data;
	}
	
	/**
	 * @return the actual data from this container in a String representation
	 */
	public String getPayloadAsString() {
		return Encoder.ByteToString(this.data);
	}

	/**
	 * @return The media type assigned to this data.
	 */
	public CoapMediaType getMediaType() {
		return this.type;
	}
}
