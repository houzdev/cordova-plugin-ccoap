package org.ws4d.coap.core.messages;

import org.ws4d.coap.core.enumerations.CoapHeaderOptionType;

public class CoapHeaderOption implements Comparable<CoapHeaderOption> {

	private CoapHeaderOptionType optionType;
	private int optionTypeValue; /* integer representation of optionType */
	private byte[] optionData;
	private int shortLength;
	private int longLength;
	private int deserializedLength;

	public int getDeserializedLength() {
		return this.deserializedLength;
	}

	public CoapHeaderOption(CoapHeaderOptionType optionType, byte[] value) throws IllegalArgumentException{
		if (value == null) {
			throw new IllegalArgumentException("Header option value MUST NOT be null");
		}

		this.optionType = optionType;
		this.optionTypeValue = optionType.getValue();
		this.optionData = value;
		if (value.length < 13) {
			this.shortLength = value.length;
			this.longLength = 0;
		} else if (value.length > 12 && value.length < 269) {
			this.shortLength = 13;
			this.longLength = value.length - this.shortLength;
		} else {
			this.shortLength = 14;
			this.longLength = value.length - 269;
		}
	}

	public CoapHeaderOption(byte[] bytes, int offset, int lastOptionNumber) throws IllegalArgumentException {
		int headerLength = 1;

		/* parse option type */
		this.optionTypeValue = ((bytes[offset] & 0xF0) >> 4);
		if (this.optionTypeValue == 13) {
			this.optionTypeValue += bytes[offset + 1] + lastOptionNumber;
			headerLength++;
		} else if (this.optionTypeValue == 14) {
			int part1 = ((bytes[offset + 1] & 0xFF) << 8);
			int part2 = bytes[offset + 2];
			this.optionTypeValue += part1 + part2 + lastOptionNumber;
			headerLength += 2;
		} else {
			this.optionTypeValue += lastOptionNumber;
		}

		this.optionType = CoapHeaderOptionType.parse(this.optionTypeValue);
		if (this.optionType == null) {
			if (this.optionTypeValue % 14 == 0) {
				/* no-op: no operation for deltas > 14 */
			} else {
				if(this.isCritical()){
					throw new IllegalArgumentException("Unknown critical header option: "+this.optionTypeValue);
				}
			}
		}
		/* parse length */
		this.shortLength = (bytes[offset] & 0x0F);
		if ((bytes[offset] & 0x0F) == 13) {
			this.longLength = (bytes[offset + headerLength] & 0xFF);
			headerLength++;
		} else if (this.shortLength == 14) {
			this.shortLength = 269;
			int part1 = ((bytes[offset + headerLength] & 0xFF) << 8);
			int part2 = (bytes[offset + headerLength + 1] & 0xFF);
			this.longLength = part1 + part2;
			headerLength += 2;
		} else {
			this.longLength = 0;
		}

		/* copy value */
		this.optionData = new byte[this.shortLength + this.longLength];
		for (int i = 0; i < this.shortLength + this.longLength; i++) {
			this.optionData[i] = bytes[i + headerLength + offset];
		}

		this.deserializedLength += headerLength + this.shortLength + this.longLength;
	}

	@Override
	public int compareTo(CoapHeaderOption option) {
		if (this.optionTypeValue != option.optionTypeValue)
			return this.optionTypeValue < option.optionTypeValue ? -1 : 1;
		return 0;
	}

	public boolean hasLongLength() {
		if (this.shortLength == 13 || this.shortLength == 14) {
			return true;
		}
		return false;
	}

	public int getLongLength() {
		return this.longLength;
	}

	public int getShortLength() {
		return this.shortLength;
	}

	public int getOptionTypeValue() {
		return this.optionTypeValue;
	}

	public byte[] getOptionData() {
		return this.optionData;
	}

	public int getSerializeLength() {
		int serializedLength = this.optionData.length;
		if (hasLongLength()) {
			// If shortLength is 14, two extra length bytes follow the
			// initial byte
			if (this.shortLength == 14)
				serializedLength += 3;
			// If shortLength is 13, only one extra length byte follows the
			// initial byte
			else
				serializedLength += 2;
		} else {
			// If shortLength < 13, only the initial byte, containing 4 bit
			// optionDelta and 4 bit option value length is added
			serializedLength++;
		}

		if (this.optionTypeValue > 13) {
			serializedLength++;
		}

		if (this.optionTypeValue > 268) {
			serializedLength++;
		}

		return serializedLength;
	}

	@Override
	public String toString() {
		char[] printableOptionValue = new char[this.optionData.length];
		for (int i = 0; i < this.optionData.length; i++)
			printableOptionValue[i] = (char) this.optionData[i];
		return "Option Number: " + " (" + this.optionTypeValue + ")" + ", Option Value: "
				+ String.copyValueOf(printableOptionValue);
	}

	public CoapHeaderOptionType getOptionType() {
		return this.optionType;
	}

	public byte[] serializeOption(int lastOptionNumber) {

		byte[] data = new byte[this.getSerializeLength()];
		int arrayIndex = 0;

		int optionDelta = this.getOptionTypeValue() - lastOptionNumber;
		if (optionDelta > 12 && optionDelta < 269) {
			data[arrayIndex++] = (byte) (((13 & 0x0F) << 4) | (this.getShortLength() & 0x0F));
			data[arrayIndex++] = (byte) (((optionDelta - 13) & 0x0F));
		} else if (optionDelta >= 269) {
			data[arrayIndex++] = (byte) (((14 & 0x0F) << 4) | (this.getShortLength() & 0x0F));
			data[arrayIndex++] = (byte) (((optionDelta - 269) & 0xFFFF >> 8));
			data[arrayIndex++] = (byte) (((optionDelta - 269) & 0xFF));
		} else {
			data[arrayIndex++] = (byte) (((optionDelta & 0x0F) << 4) | (this.getShortLength() & 0x0F));
		}

		if (this.hasLongLength()) {
			if (this.getShortLength() == 13)
				data[arrayIndex++] = (byte) (this.getLongLength() & 0xFF);
			else if (this.getShortLength() == 14) {
				data[arrayIndex++] = (byte) ((this.getLongLength() & 0xFF00) >> 8);
				data[arrayIndex++] = (byte) (this.getLongLength() & 0x00FF);
			}
		}

		byte[] value = this.getOptionData();
		for (int i = 0; i < value.length; i++) {
			data[arrayIndex++] = value[i];
		}

		return data;
	}
	
	/**
	 * @param optionTypeValue the number of this option
	 * @return true, if this option is critical, and thus must be recognized<br>
	 *  false, if the option is elective
	 */
	public boolean isCritical(){
		return 1 == (this.optionTypeValue % 2);
	}
	/**
	 * @return true if this option is unsafe, and thus should not be forwarded by a proxy if it does not recognize this option.
	 */
	public boolean isUnsafe(){
		return 1 == ((this.optionTypeValue >>> 1) % 2);
	}
	/**
	 * @return true, if this option is <b>not</b> intended to be part of a cache key.<br>
	 * false, if this option is intended to be part of a cache key.
	 */
	public boolean isNoCacheKey(){
		return 7 == ((this.optionTypeValue >>> 2) & 0x7);
	}
}