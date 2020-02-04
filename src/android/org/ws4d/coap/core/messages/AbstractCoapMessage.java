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

package org.ws4d.coap.core.messages;

import java.nio.ByteBuffer;
import java.util.Random;

import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.connection.api.CoapChannel;
import org.ws4d.coap.core.enumerations.CoapHeaderOptionType;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapPacketType;
import org.ws4d.coap.core.messages.api.CoapMessage;
import org.ws4d.coap.core.rest.CoapData;

import android.util.Log;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Sebastian Unger <sebastian.unger@uni-rostock.de>
 */
public abstract class AbstractCoapMessage implements CoapMessage {
	protected static final int HEADER_LENGTH = 4;

	/* Header */
	private int version;
	private CoapPacketType packetType;
	private int messageCodeValue;
	private int messageId;

	private byte[] token = null;
	private int tokenLength;

	/* Options */
	private CoapHeaderOptions options = new CoapHeaderOptions();

	/* Payload */
	private byte[] payload = null;
	private int payloadLength = 0;

	/* corresponding channel */
	private CoapChannel channel = null;

	/* Retransmission State */
	private int timeout = 0;
	private int retransmissionCounter = 0;

	public AbstractCoapMessage() {
		// intended to be empty
	}

	public AbstractCoapMessage(CoapPacketType packetType, int messageId) {
		this.version = 1;
		this.packetType = packetType;
		this.messageCodeValue = 0;
		this.messageId = messageId;
	}

	public AbstractCoapMessage(CoapPacketType packetType, int messageId, int messageCodeValue) {
		this.version = 1;
		this.packetType = packetType;
		this.messageCodeValue = messageCodeValue;
		this.messageId = messageId;
	}

	protected void deserialize(byte[] bytes, int length, int offset) {
		/* check length to avoid buffer overflow exceptions */
		this.version = 1;
		this.packetType = (CoapPacketType.parse((bytes[offset + 0] & 0x30) >> 4));
		this.tokenLength = bytes[offset + 0] & 0x0F;

		this.messageCodeValue = (bytes[offset + 1] & 0xFF);
		this.messageId = ((bytes[offset + 2] << 8) & 0xFF00) + (bytes[offset + 3] & 0xFF);

		this.token = new byte[this.tokenLength];
		for (int i = 0; i < this.tokenLength; i++) {
			this.token[i] = bytes[offset + HEADER_LENGTH + i];
		}

		/* serialize options */
		this.options = new CoapHeaderOptions(bytes, offset + HEADER_LENGTH + this.tokenLength, length);
		/* get and check payload length */
		this.payloadLength = length - HEADER_LENGTH - this.options.getDeserializedLength() - this.tokenLength;
		if (this.payloadLength < 0) {
			throw new IllegalStateException("Invaldid CoAP Message (payload length negative)");
		} else if (this.payloadLength > 0) {
			/* copy payload */
			this.payloadLength--;
			int payloadOffset = offset + HEADER_LENGTH + this.options.getDeserializedLength() + this.tokenLength + 1;
			this.payload = new byte[this.payloadLength];
			for (int i = 0; i < this.payloadLength; i++) {
				this.payload[i] = bytes[i + payloadOffset];
			}
		}
	}

	public static CoapMessage parseMessage(byte[] bytes, int length) {
		return parseMessage(bytes, length, 0);
	}

	public static CoapMessage parseMessage(byte[] bytes, int length, int offset) {
		/*
		 * we "peek" the header to determine the kind of message
		 */
		int messageCodeValue = (bytes[offset + 1] & 0xFF);

		if (messageCodeValue == 0) {
			return new CoapEmptyMessage(bytes, length, offset);
		} else if (messageCodeValue >= 0 && messageCodeValue <= 31) {
			return new BasicCoapRequest(bytes, length, offset);
		} else if (messageCodeValue >= 64 && messageCodeValue <= 191) {
			return new BasicCoapResponse(bytes, length, offset);
		} else {
			throw new IllegalArgumentException("unknown CoAP message");
		}
	}

	public int getVersion() {
		return this.version;
	}

	@Override
	public int getMessageCodeValue() {
		return this.messageCodeValue;
	}

	protected void setMessageCodeValue(int codeValue) {
		this.messageCodeValue = codeValue;
	}

	public CoapHeaderOptions getOptions() {
		return this.options;
	}

	@Override
	public CoapPacketType getPacketType() {
		return this.packetType;
	}

	public byte[] getPayload() {
		return this.payload;
	}

	public int getPayloadLength() {
		return this.payloadLength;
	}

	@Override
	public int getMessageID() {
		return this.messageId;
	}

	@Override
	public void setMessageID(int messageId) {
		this.messageId = messageId;
	}

	public byte[] serialize() {
		/* serialize header options first to get the length */
		int optionsLength = 0;
		byte[] optionsArray = null;
		if (this.options != null) {
			optionsArray = this.options.serialize();
			optionsLength = this.options.getSerializedLength();
		}

		/* allocate memory for the complete packet */
		int length = HEADER_LENGTH + this.tokenLength + optionsLength + this.payloadLength;
		if (this.payloadLength > 0)
			length++;

		byte[] serializedPacket = new byte[length];

		/* serialize header */
		serializedPacket[0] = (byte) ((this.version & 0x03) << 6);
		serializedPacket[0] |= (byte) ((this.packetType.getValue() & 0x03) << 4);
		serializedPacket[0] |= (byte) (this.tokenLength & 0x0F);
		serializedPacket[1] = (byte) (this.getMessageCodeValue() & 0xFF);
		serializedPacket[2] = (byte) ((this.messageId >> 8) & 0xFF);
		serializedPacket[3] = (byte) (this.messageId & 0xFF);

		/* insert token into packet */
		for (int i = 0; i < this.tokenLength; i++) {
			serializedPacket[HEADER_LENGTH + i] = this.token[i];
		}

		/* copy serialized options to the final array */
		int offset = HEADER_LENGTH + this.tokenLength;
		for (int i = 0; i < optionsLength; i++)
			if (optionsArray != null) {
				serializedPacket[i + offset] = optionsArray[i];
			}

		/* insert payload marker */
		offset = HEADER_LENGTH + this.tokenLength + optionsLength;
		if (this.payloadLength > 0) {
			serializedPacket[offset] = (byte) 0xFF;
			offset += 1;
		}

		/* copy payload to the final array */
		for (int i = 0; i < this.payloadLength; i++) {
			serializedPacket[i + offset] = this.payload[i];
		}

		return serializedPacket;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
		if (payload != null)
			this.payloadLength = payload.length;
		else
			this.payloadLength = 0;
	}

	public void setPayload(char[] payload) {
		this.payload = new byte[payload.length];
		for (int i = 0; i < payload.length; i++) {
			this.payload[i] = (byte) payload[i];
		}
		this.payloadLength = payload.length;
	}

	public void setPayload(String payload) {
		setPayload(payload.toCharArray());
	}

	public void setPayload(CoapData data) {
		setPayload(data.getPayload());
		this.setContentType(data.getMediaType());
	}

	@Override
	public void setContentType(CoapMediaType mediaType) {
		CoapHeaderOption option = this.options.getOption(CoapHeaderOptionType.Content_Format);

		if (option != null) {
			/* content Type MUST only exists once */
			this.options.removeOption(CoapHeaderOptionType.Content_Format);
		}

		if (mediaType == CoapMediaType.UNKNOWN) {
			/* If content type is unknown, treat as binary */
			Log.w("CCoap", "Unknown content type");
			mediaType = CoapMediaType.octet_stream;
		}

		/* convert value */
		byte[] data = long2CoapUint(mediaType.getValue());
		/* no need to check result, mediaType is safe */
		/* add option to Coap Header */
		this.options.addOption(new CoapHeaderOption(CoapHeaderOptionType.Content_Format, data));
	}

	@Override
	public CoapMediaType getContentType() {
		CoapHeaderOption option = this.options.getOption(CoapHeaderOptionType.Content_Format);

		if (option == null)
			return CoapMediaType.UNKNOWN;

		/* no need to check length, CoapMediaType parse function will do */
		int mediaTypeCode = (int) coapUint2Long(
				this.options.getOption(CoapHeaderOptionType.Content_Format).getOptionData());

		return CoapMediaType.parse(mediaTypeCode);
	}

	@Override
	public byte[] getToken() {
		return this.token;
	}

	public void setToken(byte[] token) {
		if (token == null) {
			this.tokenLength = 0;
			return;
		}
		if (token.length < 1 || token.length > 8) {
			throw new IllegalArgumentException("Invalid Token Length");
		}
		this.token = token;
		this.tokenLength = token.length;
	}

	@Override
	public CoapBlockOption getBlock1() {
		CoapHeaderOption option = this.options.getOption(CoapHeaderOptionType.Block1);
		if (option == null) {
			return null;
		}

		CoapBlockOption blockOpt = new CoapBlockOption(option.getOptionData());
		return blockOpt;
	}

	@Override
	public void setBlock1(CoapBlockOption blockOption) {
		CoapHeaderOption option = this.options.getOption(CoapHeaderOptionType.Block1);
		if (option != null) {
			// option already exists
			this.options.removeOption(CoapHeaderOptionType.Block1);
		}
		this.options.addOption(CoapHeaderOptionType.Block1, blockOption.getBytes());
		option = this.options.getOption(CoapHeaderOptionType.Block1);
	}

	@Override
	public CoapBlockOption getBlock2() {
		CoapHeaderOption option = this.options.getOption(CoapHeaderOptionType.Block2);

		if (option == null) {
			Log.e("CCoap", "Option is null");
			return null;
		}

		CoapBlockOption blockOpt = new CoapBlockOption(option.getOptionData());
		return blockOpt;
	}

	@Override
	public void setBlock2(CoapBlockOption blockOption) {
		CoapHeaderOption option = this.options.getOption(CoapHeaderOptionType.Block2);
		if (option != null) {
			// option already exists
			this.options.removeOption(CoapHeaderOptionType.Block2);
		}
		this.options.addOption(CoapHeaderOptionType.Block2, blockOption.getBytes());
	}

	@Override
	public Integer getObserveOption() {
		CoapHeaderOption option = this.options.getOption(CoapHeaderOptionType.Observe);
		if (option == null) {
			return null;
		}
		byte[] data = option.getOptionData();

		if (data.length < 0 || data.length > 2) {
			return null;
		}
		return (int) AbstractCoapMessage.coapUint2Long(data);
	}

	@Override
	public void setObserveOption(int sequenceNumber) {
		CoapHeaderOption option = this.options.getOption(CoapHeaderOptionType.Observe);
		if (option != null) {
			this.options.removeOption(CoapHeaderOptionType.Observe);
		}

		byte[] data = long2CoapUint(sequenceNumber);

		if (data.length < 0 || data.length > 2) {
			throw new IllegalArgumentException("invalid observe option length");
		}

		this.options.addOption(CoapHeaderOptionType.Observe, data);
	}

	public void copyHeaderOptions(AbstractCoapMessage origin) {
		this.options.removeAll();
		this.options.copyFrom(origin.options);
	}

	public void removeOption(CoapHeaderOptionType optionType) {
		this.options.removeOption(optionType);
	}

	@Override
	public CoapChannel getChannel() {
		return this.channel;
	}

	@Override
	public void setChannel(CoapChannel channel) {
		this.channel = channel;
	}

	@Override
	public int getTimeout() {
		if (this.timeout == 0) {
			Random random = new Random();
			this.timeout = CoapConstants.RESPONSE_TIMEOUT_MS
					+ random.nextInt((int) (CoapConstants.RESPONSE_TIMEOUT_MS * CoapConstants.RESPONSE_RANDOM_FACTOR)
							- CoapConstants.RESPONSE_TIMEOUT_MS);
		}
		return this.timeout;
	}

	@Override
	public boolean maxRetransReached() {
		if (this.retransmissionCounter < CoapConstants.MAX_RETRANSMIT) {
			return false;
		}
		return true;
	}

	@Override
	public void incRetransCounterAndTimeout() {
		this.retransmissionCounter += 1;
		this.timeout *= 2;
	}

	@Override
	public boolean isReliable() {
		if (this.packetType == CoapPacketType.NON) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.channel == null) ? 0 : this.channel.hashCode());
		result = prime * result + getMessageID();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractCoapMessage other = (AbstractCoapMessage) obj;
		if (this.channel == null) {
			if (other.channel != null)
				return false;
		} else if (!this.channel.equals(other.channel))
			return false;
		if (getMessageID() != other.getMessageID())
			return false;
		return true;
	}

	protected static long coapUint2Long(byte[] data) {
		/* avoid buffer overflow */
		if (data.length > 8) {
			return -1;
		}

		/* fill with leading zeros */
		byte[] tmp = new byte[8];
		for (int i = 0; i < data.length; i++) {
			tmp[i + 8 - data.length] = data[i];
		}

		/* convert to long */
		ByteBuffer buf = ByteBuffer.wrap(tmp);
		/* byte buffer contains 8 bytes */
		return buf.getLong();
	}

	protected static byte[] long2CoapUint(long value) {
		/* only unsigned values supported */
		if (value < 0) {
			return null;
		}

		/* a zero length value implies zero */
		if (value == 0) {
			return new byte[0];
		}

		/* convert long to byte array with a fixed length of 8 byte */
		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.putLong(value);
		byte[] tmp = buf.array();

		/* remove leading zeros */
		int leadingZeros = 0;
		for (int i = 0; i < tmp.length; i++) {
			if (tmp[i] == 0) {
				leadingZeros = i + 1;
			} else {
				break;
			}
		}
		/* copy to byte array without leading zeros */
		byte[] result = new byte[8 - leadingZeros];
		for (int i = 0; i < result.length; i++) {
			result[i] = tmp[i + leadingZeros];
		}

		return result;
	}

}
