/* Copyright 2011 University of Rostock
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

package org.ws4d.coap.core.messages.api;

import org.ws4d.coap.core.connection.api.CoapChannel;
import org.ws4d.coap.core.enumerations.CoapHeaderOptionType;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapPacketType;
import org.ws4d.coap.core.messages.CoapBlockOption;
import org.ws4d.coap.core.messages.CoapHeaderOptions;
import org.ws4d.coap.core.rest.CoapData;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public interface CoapMessage {

	/**
	 * @return Value of the internal message code.<br>
	 *         -1, in case of an error.
	 */
	public int getMessageCodeValue();

	/**
	 * @return The ID of the message.
	 */
	public int getMessageID();

	/**
	 * 
	 * @param msgID
	 *            - The ID of the Message to be set.
	 */
	public void setMessageID(int msgID);

	/**
	 * Convert the CoAP message into its serialized form for transmission.<br>
	 * See rfc7252 - 3. "Message Format" for further details.
	 * 
	 * @return The serialized CoAP message.
	 */
	public byte[] serialize();

	/**
	 * increments the retransmission counter and doubles the timeout
	 */
	public void incRetransCounterAndTimeout();

	/**
	 * @return The packet type of the message (CON, NON, ACK, RST).
	 */
	public CoapPacketType getPacketType();

	/**
	 * Get the payload of the message for further use on application level.
	 * 
	 * @return The payload of the message
	 */
	public byte[] getPayload();

	/**
	 * Set the payload of the message to be sent.
	 * 
	 * @param payload
	 *            the payload of the message to be sent.
	 * @deprecated use {@link setPayload(CoapData data)} instead
	 */
	@Deprecated
	public void setPayload(byte[] payload);

	/**
	 * Set the payload of the message to be sent.
	 * 
	 * @param payload
	 *            the payload of the message to be sent.
	 * @deprecated use {@link setPayload(CoapData data)} instead
	 */
	@Deprecated
	public void setPayload(char[] payload);

	/**
	 * Set the payload of the message to be sent.
	 * 
	 * @param payload
	 *            the payload of the message to be sent.
	 * @deprecated use {@link setPayload(CoapData data)} instead
	 */
	@Deprecated
	public void setPayload(String payload);
	
	/**
	 * Set the payload of the message to be sent.
	 * 
	 * @param payload
	 *            the payload of the message to be sent.
	 */
	public void setPayload(CoapData data);

	/**
	 * @return The size of the message payload in byte.
	 */
	public int getPayloadLength();

	/**
	 * Change the media type of the message.
	 * 
	 * @param mediaType
	 *            The new media type.
	 */
	public void setContentType(CoapMediaType mediaType);

	/**
	 * @return The media type of the message.
	 */
	public CoapMediaType getContentType();

	/**
	 * Set the token of the message. The token value is used to correlate
	 * requests and responses.
	 * 
	 * @param token
	 *            The token to be set
	 */
	public void setToken(byte[] token);

	/**
	 * The token value is used to correlate requests and responses.
	 * 
	 * @return The token of the message.
	 */
	public byte[] getToken();

	/**
	 * @return The block option for get requests.
	 */
	CoapBlockOption getBlock1();

	/**
	 * @param blockOption
	 *            The block option for get requests.
	 */
	void setBlock1(CoapBlockOption blockOption);

	/**
	 * @return The block option for POST & PUT requests.
	 */
	CoapBlockOption getBlock2();

	/**
	 * @param blockOption
	 *            The block option for POST & PUT requests.
	 */
	void setBlock2(CoapBlockOption blockOption);

	/**
	 * 
	 * @return
	 */
	public Integer getObserveOption();

	/**
	 * 
	 * @param sequenceNumber
	 */
	public void setObserveOption(int sequenceNumber);

	/**
	 * @return Message options.
	*/
	public CoapHeaderOptions getOptions();
	
	/**
	 * 
	 * @param optionType
	 */
	public void removeOption(CoapHeaderOptionType optionType);

	@Override
	public String toString();

	/**
	 * 
	 * @return
	 */
	public CoapChannel getChannel();

	/**
	 * 
	 * @param channel
	 */
	public void setChannel(CoapChannel channel);

	/**
	 * 
	 * @return
	 */
	public int getTimeout();

	/**
	 * 
	 * @return
	 */
	public boolean maxRetransReached();

	/**
	 * 
	 * @return
	 */
	public boolean isReliable();

	/**
	 * 
	 * @return
	 */
	public boolean isRequest();

	/**
	 * 
	 * @return
	 */
	public boolean isResponse();

	/**
	 * 
	 * @return
	 */
	public boolean isEmpty();

	/* unique by remote address, remote port, local port and message id */
	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

}
