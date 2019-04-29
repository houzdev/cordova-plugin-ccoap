/* Copyright 2015 University of Rostock
 
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

package org.ws4d.coap.core.connection.api;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapResponseCode;
import org.ws4d.coap.core.messages.api.CoapMessage;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public interface CoapServerChannel extends CoapChannel {
	/**
	 * creates a normal response
	 * 
	 * @param request
	 * @param responseCode
	 * @return
	 */
	public CoapResponse createResponse(CoapMessage request, CoapResponseCode responseCode);

	/**
	 * creates a normal response
	 * 
	 * @param request
	 * @param responseCode
	 * @param contentType
	 * @return
	 */
	public CoapResponse createResponse(CoapMessage request, CoapResponseCode responseCode, CoapMediaType contentType);

	/**
	 * creates a separate response and acks the current request witch an empty
	 * ACK in case of a CON. The separate response can be send later using
	 * sendSeparateResponse()
	 * 
	 * @param request
	 * @param responseCode
	 * @return
	 */
	public CoapResponse createSeparateResponse(CoapRequest request, CoapResponseCode responseCode);

	/**
	 * used by a server to send a separate response
	 * 
	 * @param response
	 */
	public void sendSeparateResponse(CoapResponse response);

	/**
	 * used by a server to create a notification (observing resources),
	 * reliability is base on the request packet type (con or non)
	 * 
	 * @param request
	 * @param responseCode
	 * @param sequenceNumber
	 * @return
	 */
	public CoapResponse createNotification(CoapRequest request, CoapResponseCode responseCode, int sequenceNumber);

	/**
	 * used by a server to create a notification (observing resources)
	 * 
	 * @param request
	 * @param responseCode
	 * @param sequenceNumber
	 * @param reliable
	 * @return
	 */
	public CoapResponse createNotification(CoapRequest request, CoapResponseCode responseCode, int sequenceNumber,
			boolean reliable);

	/**
	 * used by a server to send a notification (observing resources)
	 * 
	 * @param response
	 */
	public void sendNotification(CoapResponse response);

	/**
	 * used by a server to add block context to the channel, when block wise GET
	 * request is received
	 * 
	 * @param request
	 * @param payload
	 * @return
	 */
	public CoapResponse addBlockContext(CoapRequest request, byte[] payload);

}
