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

import org.ws4d.coap.core.enumerations.CoapHeaderOptionType;
import org.ws4d.coap.core.enumerations.CoapPacketType;
import org.ws4d.coap.core.enumerations.CoapResponseCode;
import org.ws4d.coap.core.messages.api.CoapResponse;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public class BasicCoapResponse extends AbstractCoapMessage implements CoapResponse {
	private CoapResponseCode responseCode;

	public BasicCoapResponse(byte[] bytes, int length) {
		this(bytes, length, 0);
	}

	public BasicCoapResponse(byte[] bytes, int length, int offset) {
		deserialize(bytes, length, offset);
		/*
		 * check if response code is valid, this function throws an error in
		 * case of an invalid argument
		 */
		this.responseCode = CoapResponseCode.parse(this.getMessageCodeValue());
	}

	/* token can be null */
	public BasicCoapResponse(CoapPacketType packetType, CoapResponseCode responseCode, int messageId,
			byte[] requestToken) {
		
		super(packetType,messageId, responseCode.getValue());

		this.responseCode = responseCode;
		if (responseCode == CoapResponseCode.UNKNOWN) {
			throw new IllegalArgumentException("UNKNOWN Response Code not allowed");
		}

		if (requestToken.length > 0)
			setToken(requestToken);
	}

	@Override
	public void setToken(byte[] token) {
		/* this function is only public for a request */
		super.setToken(token);
	}

	@Override
	public CoapResponseCode getResponseCode() {
		return this.responseCode;
	}

	@Override
	public void setMaxAge(int maxAge) {
		if (this.getOptions().optionExists(CoapHeaderOptionType.Max_Age)) {
			throw new IllegalStateException("Max Age option already exists");
		}
		if (maxAge < 0) {
			throw new IllegalStateException("Max Age MUST be an unsigned value");
		}
		this.getOptions().addOption(CoapHeaderOptionType.Max_Age, long2CoapUint(maxAge));
	}

	@Override
	public long getMaxAge() {
		CoapHeaderOption option = this.getOptions().getOption(CoapHeaderOptionType.Max_Age);
		if (option == null) {
			return -1;
		}
		return coapUint2Long((this.getOptions().getOption(CoapHeaderOptionType.Max_Age).getOptionData()));
	}

	@Override
	public void setETag(byte[] etag) {
		if (etag == null) {
			throw new IllegalArgumentException("etag MUST NOT be null");
		}
		if (etag.length < 1 || etag.length > 8) {
			throw new IllegalArgumentException("Invalid etag length");
		}
		this.getOptions().addOption(CoapHeaderOptionType.Etag, etag);
	}

	@Override
	public byte[] getETag() {
		CoapHeaderOption option = this.getOptions().getOption(CoapHeaderOptionType.Etag);
		if (option == null) {
			return null;
		}
		return option.getOptionData();
	}

	@Override
	public boolean isRequest() {
		return false;
	}

	@Override
	public boolean isResponse() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public String toString() {
		CoapHeaderOptions options = getOptions();
		String out = this.getPacketType().toString() + ", " + this.responseCode.toString() + ", MsgId: " + getMessageID()
		+ ", #Options: " + options.getOptionCount();
		if(options.getOptionCount()>0){
			out += " (";
			for(CoapHeaderOption o : options){
				out += " "+o.getOptionType();
			}
			out += ")";
		}
		return out;
	}

	@Override
	public void setResponseCode(CoapResponseCode responseCode) {
		if (responseCode != CoapResponseCode.UNKNOWN) {
			this.responseCode = responseCode;
			this.setMessageCodeValue(responseCode.getValue());
		}
	}
}
