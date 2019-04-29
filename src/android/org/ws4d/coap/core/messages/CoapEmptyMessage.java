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

package org.ws4d.coap.core.messages;

import org.ws4d.coap.core.enumerations.CoapPacketType;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public class CoapEmptyMessage extends AbstractCoapMessage {

	public CoapEmptyMessage(byte[] bytes, int length) {
		this(bytes, length, 0);
	}

	public CoapEmptyMessage(byte[] bytes, int length, int offset) {
		deserialize(bytes, length, offset);
		/*
		 * check if response code is valid, this function throws an error in
		 * case of an invalid argument
		 */
		if (this.getMessageCodeValue() != 0) {
			throw new IllegalArgumentException("Not an empty CoAP message.");
		}

		if (length != HEADER_LENGTH) {
			throw new IllegalArgumentException("Invalid length of an empty message");
		}
	}

	public CoapEmptyMessage(CoapPacketType packetType, int messageId) {
		super(packetType, messageId);
	}

	@Override
	public boolean isRequest() {
		return false;
	}

	@Override
	public boolean isResponse() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

}
