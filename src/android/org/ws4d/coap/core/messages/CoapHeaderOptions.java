package org.ws4d.coap.core.messages;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import org.ws4d.coap.core.enumerations.CoapHeaderOptionType;

public class CoapHeaderOptions implements Iterable<CoapHeaderOption> {

	private Vector<CoapHeaderOption> headerOptions = new Vector<CoapHeaderOption>();
	private int deserializedLength;
	private int serializedLength = 0;

	/*
	 * public CoapHeaderOptions(byte[] bytes, int option_count){ this(bytes,
	 * option_count, option_count); }
	 */

	public CoapHeaderOptions(byte[] bytes, int offset, int length) {
		/* note: we only receive deltas and never concrete numbers */
		/* TODO: check integrity */
		this.deserializedLength = 0;
		int lastOptionNumber = 0;
		int optionOffset = offset;
		while (bytes[optionOffset] != -1 && optionOffset < length - 1) {
			CoapHeaderOption option = new CoapHeaderOption(bytes, optionOffset, lastOptionNumber);
			lastOptionNumber = option.getOptionTypeValue();
			this.deserializedLength += option.getDeserializedLength();
			optionOffset += option.getDeserializedLength();
			addOption(option);
		}
	}

	public CoapHeaderOptions() {
		/* creates empty header options */
	}

	public CoapHeaderOption getOption(int optionNumber) {
		for (CoapHeaderOption headerOption : this.headerOptions) {
			if (headerOption.getOptionTypeValue() == optionNumber) {
				return headerOption;
			}
		}
		return null;
	}

	public CoapHeaderOption getOption(CoapHeaderOptionType optionType) {
		for (CoapHeaderOption headerOption : this.headerOptions) {
			if (headerOption.getOptionTypeValue() == optionType.getValue()) {
				return headerOption;
			}
		}
		return null;
	}

	public boolean optionExists(CoapHeaderOptionType optionType) {
		CoapHeaderOption option = getOption(optionType);
		if (option == null) {
			return false;
		}
		return true;
	}

	public void addOption(CoapHeaderOption option) {
		this.headerOptions.add(option);
		Collections.sort(this.headerOptions);
	}

	public void addOption(CoapHeaderOptionType optionType, byte[] value) {
		addOption(new CoapHeaderOption(optionType, value));
	}

	public void removeOption(CoapHeaderOptionType optionType) {
		CoapHeaderOption headerOption;
		// get elements of Vector

		/*
		 * note: iterating over and changing a vector at the same time is
		 * not allowed
		 */
		int i = 0;
		while (i < this.headerOptions.size()) {
			headerOption = this.headerOptions.get(i);
			if (headerOption.getOptionTypeValue() == optionType.getValue()) {
				this.headerOptions.remove(i);
			} else {
				/* only increase when no element was removed */
				i++;
			}
		}
		Collections.sort(this.headerOptions);
	}

	public void removeAll() {
		this.headerOptions.clear();
	}

	public void copyFrom(CoapHeaderOptions origin) {
		for (CoapHeaderOption option : origin) {
			addOption(option);
		}
	}

	public int getOptionCount() {
		return this.headerOptions.size();
	}

	public byte[] serialize() {
		/*
		 * options are serialized here to be more efficient (only one byte
		 * array necessary)
		 */
		int length = 0;

		/* calculate the overall length first */
		for (CoapHeaderOption option : this.headerOptions) {
			length += option.getSerializeLength();
		}

		byte[] data = new byte[length];
		byte[] opt = null;
		int arrayIndex = 0;

		int lastOptionNumber = 0; /* let's keep track of this */
		for (CoapHeaderOption headerOption : this.headerOptions) {
			opt = headerOption.serializeOption(lastOptionNumber);
			for (int i = 0; i < opt.length; i++) {
				data[arrayIndex++] = opt[i];
			}
			lastOptionNumber = headerOption.getOptionTypeValue();
		}
		this.serializedLength = length;
		return data;
	}

	public int getDeserializedLength() {
		return this.deserializedLength;
	}

	public int getSerializedLength() {
		return this.serializedLength;
	}

	@Override
	public Iterator<CoapHeaderOption> iterator() {
		return this.headerOptions.iterator();
	}

	@Override
	public String toString() {
		String result = "\tOptions:\n";
		for (CoapHeaderOption option : this.headerOptions) {
			result += "\t\t" + option.toString() + "\n";
		}
		return result;
	}
}