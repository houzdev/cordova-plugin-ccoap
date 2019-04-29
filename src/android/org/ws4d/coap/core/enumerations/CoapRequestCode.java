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
 * This Enumeration contains all request codes available for CoAP.<br>
 * See RFC 7252 - 5.8. Method Definitions and 12.1.1. Method Codes
 * 
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public enum CoapRequestCode {
	GET(1), 
	POST(2),
	PUT(3),
	DELETE(4);

	private int code;

	private CoapRequestCode(int code) {	this.code = code;}

	/**
	 * @return The method code of the ENUM element.
	 */
	public int getValue() {	return this.code;}

	/**
	 * @param codeValue the method code for the request code.
	 * @return The ENUM element matching the codeValue.
	 * @throws IllegalArgumentException, if codeValue is out of range.
	 */
	public static CoapRequestCode parse(int codeValue) {
		for(CoapRequestCode t : CoapRequestCode.values()){
			if(t.getValue() == codeValue) return t;
		}
		throw new IllegalArgumentException("Invalid Request Code");
	}
}
