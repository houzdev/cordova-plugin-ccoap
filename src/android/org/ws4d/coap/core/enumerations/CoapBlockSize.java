package org.ws4d.coap.core.enumerations;

/**
 * An enumeration of all possible block sizes.<br>
 * See draft-ietf-core-block-20 - 2.2. Structure of a Block Option
 */
public enum CoapBlockSize {
	BLOCK_16(0),
	BLOCK_32(1),
	BLOCK_64(2),
	BLOCK_128(3),
	BLOCK_256(4),
	BLOCK_512(5),
	BLOCK_1024(6),
	
	//additional
	UNKNOWN(-1);
	
	private int exp;

	private CoapBlockSize(int exponent) {
		this.exp = exponent;
	}

	/**
	 * @param exponent indicating the block size, ranging from 0-6
	 * @return The CoapBlockSize enum element matching this exponent. 
	 */
	public static CoapBlockSize parse(int exponent) {
		for(CoapBlockSize t : CoapBlockSize.values()){
			if(exponent == t.getExponent())	return t;
		}
		return UNKNOWN;
	}

	/**
	 * @return the exponent that indicates this block size, ranging from 0-6
	 */
	public int getExponent() {return this.exp;}
	
	/**
	 * @return the length of a block indicated by this option
	 */
	public int getSize() {return 1 << (this.exp + 4);}
}