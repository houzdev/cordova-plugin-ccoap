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

package org.ws4d.coap.core.enumerations;

/**
 * This Enumeration contains all response codes available for CoAP. <br>
 * See RFC 7252 - 5.9. "Response Code Definitions" for further details.
 * 
 * @author Bjoern Konieczek <bjoern.konieczek@uni-rostock.de>
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public enum CoapResponseCode {
	// Success 2.xx
	Created_201(65),
	Deleted_202(66),
	Valid_203(67),
	Changed_204(68),
	Content_205(69),

	// Client Error 4.xx
	Bad_Request_400(128),
	Unauthorized_401(129),
	Bad_Option_402(130),
	Forbidden_403(131),
	Not_Found_404(132),
	Method_Not_Allowed_405(133),
	Not_Acceptable_406(134),
	Precondition_Failed_412(140),
	Request_Entity_To_Large_413(141),
	Unsupported_Media_Type_415(143),

	// Server Error 5.xx
	Internal_Server_Error_500(160),
	Not_Implemented_501(161),
	Bad_Gateway_502(162),
	Service_Unavailable_503(163),
	Gateway_Timeout_504(164),
	Proxying_Not_Supported_505(165),

	// draft-ietf-core-block-20 - 6.  IANA Considerations
	Continue_231(95),
	Request_Entity_Incomplete_408(136),
	
	// additional
	UNKNOWN(-1);

	private int code;

	private CoapResponseCode(int code) {this.code = code;}

	/**
	 * @param codeValue
	 *            the code for the response code.
	 * @return The ENUM element matching the codeValue. <br>
	 *         UNKNOWN, if the codeValue doesn't match any ENUM element.
	 * @throws IllegalArgumentException
	 *             if codeValue is out of range.
	 */
	public static CoapResponseCode parse(int codeValue) {
		for(CoapResponseCode t : CoapResponseCode.values()){
			if(t.getValue() == codeValue) return t;
		}
		if (codeValue >= 32 && codeValue <= 191) {
			return UNKNOWN;
		}
		throw new IllegalArgumentException("Invalid Response Code");
	}

	/**
	 * @return The codeValue of the ENUM element.
	 */
	public int getValue() {return this.code;}
}
