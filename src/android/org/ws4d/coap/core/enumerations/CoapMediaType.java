/* Copyright 2015 University of Rostock
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/

package org.ws4d.coap.core.enumerations;

/**
 * This enum defines a subset of Internet media types to be used in CoAP.<br>
 * See RFC 7252 - 12.3. CoAP Content-Formats Registry
 * 
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public enum CoapMediaType {
	text_plain(0, "text/plain; charset=utf-8"),
	link_format(40, "application/link-format"),
	xml(41, "application/xml"),
	octet_stream(42, "application/octet-stream"),
	exi(47, "application/exi"),
	json(50, "application/json"),
	
	//additional
	UNKNOWN(-1, "");

	private int mediaType;
	private String mimeType;

	private CoapMediaType(int mediaType, String mimeType) {
		this.mediaType = mediaType;
		this.mimeType = mimeType;
	}

	/**
	 * @param mediaType code of the media type.
	 * @return The enum element matching the media type code. <br>
	 *         UNKNOWN, if the media type code is not known.
	 */
	public static CoapMediaType parse(int mediaType) {
		for(CoapMediaType t : CoapMediaType.values()){
			if(t.getValue() == mediaType) return t;
		}
		return UNKNOWN;
	}

	/**
	 * @return The media type code of the ENUM element.
	 */
	public int getValue() {return this.mediaType;}

	/**
	 * @return The mime type code of respective coapMediaType.
	 */
	public String getMimeType() {return this.mimeType;}
}