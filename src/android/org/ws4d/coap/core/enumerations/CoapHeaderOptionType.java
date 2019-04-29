package org.ws4d.coap.core.enumerations;

/**
 * An enumeration of all CoAP header options.<br>
 * See RFC 7252 - 5.10.  Option Definitions<br>
 * <br>
 * Furthermore this includes CoAP header option for: <br>
 * - RFC 7641: Observing Resources in the Constrained Application Protocol (CoAP)<br>
 * - draft-ietf-core-block-20: Block-wise transfers in CoAP
 */
public enum CoapHeaderOptionType {
	If_Match(1,true,false,false,true),
	Uri_Host(3,true,true,false,false),
	Etag(4,false,false,false,true),
	If_None_Match(5,true,false,false,false),
	Uri_Port(7,true,true,false,false),
	Location_Path(8,false,false,false,true),
	Uri_Path(11,true,true,false,true),
	Content_Format(12,false,false,false,false),
	Max_Age(14,false,true,false,false),
	Uri_Query(15,true,true,false,true),
	Accept(17,true,false,false,false),
	Location_Query(20,false,false,false,true),
	Proxy_Uri(35,true,true,false,false),
	Proxy_Scheme(39,true,true,false,false),
	Size1(60,false,false,true,false),
	
	// RFC 7641 - 2. "The Observe Option"
	Observe(6,false,true,false,false),
	
	// draft-ietf-core-block-20 - 2.1. The Block2 and Block1 Options
	Block2(23,true,true,false,false),
	Block1(27,true,true,false,false),
	// draft-ietf-core-block-20 - 4. The Size2 and Size1 Options
	Size2(28,false,false,true,false);
	
	private int number;
	private boolean critical;
	private boolean unsafe;
	private boolean noCacheKey;
	private boolean repeatable;

	private CoapHeaderOptionType(int optionNumber, boolean iscritical, boolean isUnsafe, boolean isNoCacheKey, boolean isRepeatable) {
		this.number = optionNumber;
		this.critical = iscritical;
		this.unsafe = isUnsafe;
		this.noCacheKey = isNoCacheKey;
		this.repeatable = isRepeatable;
	}

	/**
	 * @param optionTypeValue the code indicating the option
	 * @return the corresponding CoAP header option enum element
	 */
	public static CoapHeaderOptionType parse(int optionTypeValue) {
		for(CoapHeaderOptionType t : CoapHeaderOptionType.values()){
			if(t.getValue() == optionTypeValue)	return t;
		}
		return null;
	}
	
	/**
	 * @return the code indicating this option
	 */
	public int getValue() {
		return this.number;
	}
	/**
	 * @return true, if this option is critical, and thus must be recognized<br>
	 *  false, if the option is elective
	 */
	public boolean isCritical(){
		return this.critical;
	}
	/**
	 * @return true if this option is unsafe, and thus should not be forwarded by a proxy if it does not recognize this option.
	 */
	public boolean isUnsafe(){
		return this.unsafe;
	}
	/**
	 * @return true, if this option is <b>not</b> intended to be part of a cache key.<br>
	 * false, if this option is intended to be part of a cache key.
	 */
	public boolean isNoCacheKey(){
		return this.noCacheKey;
	}
	/**
	 * @return  true, if this option can be present multiple times in a header.<br>
	 * false, if this option can only be present once per header.
	 */
	public boolean isRepeatable(){
		return this.repeatable;
	}
}