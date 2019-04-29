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

import org.ws4d.coap.core.enumerations.CoapBlockSize;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public class CoapBlockOption {
	private int number;
	private boolean more;
	private CoapBlockSize blockSize;

	/**
	 * Parse the block option from its serialized form
	 * 
	 * @param data
	 *            The serialized form of the block option.
	 */
	public CoapBlockOption(byte[] data) {
		if (data.length < 1 || data.length > 3) {
			throw new IllegalArgumentException("invalid block option");
		}
		long val = AbstractCoapMessage.coapUint2Long(data);

		this.blockSize = CoapBlockSize.parse((int) (val & 0x7));
		if (this.blockSize == null) {
			throw new IllegalArgumentException("invalid block options");
		}

		if ((val & 0x8) == 0) {
			// more bit not set
			this.more = false;
		} else {
			this.more = true;
		}
		this.number = (int) (val >> 4);
	}

	/**
	 * Create new block option.
	 * 
	 * @param number
	 * @param more
	 * @param blockSize
	 */
	public CoapBlockOption(int number, boolean more, CoapBlockSize blockSize) {
		if (blockSize == null) {
			throw new IllegalArgumentException();
		}
		if (number < 0 || number > 0xFFFFFF) {
			// not an unsigned 20 bit value
			throw new IllegalArgumentException();
		}

		this.blockSize = blockSize;
		this.number = number;
		this.more = more;
	}

	public int getNumber() {
		return this.number;
	}

	public boolean isLast() {
		return !this.more;
	}

	public CoapBlockSize getBlockSize() {
		return this.blockSize;
	}

	public int getBytePosition() {
		return this.number << (this.blockSize.getExponent() + 4);
	}

	public byte[] getBytes() {
		int value = this.number << 4;
		value |= this.blockSize.getExponent();
		if (this.more) {
			value |= 0x8;
		}
		if (value == 0) {
			byte[] b = new byte[1];
			b[0] = 0x00;
			return b;
		}

		return AbstractCoapMessage.long2CoapUint(value);
	}

}
