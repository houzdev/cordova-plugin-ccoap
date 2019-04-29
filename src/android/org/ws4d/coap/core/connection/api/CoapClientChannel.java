/* Copyright 2015 University of Rostock
 
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

package org.ws4d.coap.core.connection.api;

import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.messages.api.CoapRequest;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public interface CoapClientChannel extends CoapChannel {

	/**
	 * @deprecated Use {@link #createRequest(CoapRequestCode requestCode, String path, boolean reliable)} instead.
	 * @param reliable - confirmable or non-confirmable request
	 * @param requestCode - determine the request type
	 * @return A request message that can be further modified or sent
	 */
	@Deprecated
	public CoapRequest createRequest(boolean reliable, CoapRequestCode requestCode);
	
	/**
	 * @param requestCode - determine the request type
	 * @param path - path of the resource requested
	 * @param reliable - confirmable or non-confirmable request
	 * @return A request message that can be further modified or sent
	 */
	public CoapRequest createRequest(CoapRequestCode requestCode, String path, boolean reliable);

	/**
	 * 
	 * @param request
	 * @return
	 */
	public CoapRequest addBlockContext(CoapRequest request);

	/**
	 * 
	 * @param o
	 */
	public void setTrigger(Object o);

	/**
	 * 
	 * @return
	 */
	public Object getTrigger();

	/**
	 * 
	 * @return
	 */
	public byte[] getLastToken();
}
