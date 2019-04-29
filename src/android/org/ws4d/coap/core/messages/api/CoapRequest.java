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

package org.ws4d.coap.core.messages.api;

import java.util.Vector;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.messages.CoapHeaderOptions;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public interface CoapRequest extends CoapMessage {

	/**
	 * 
	 * @param host
	 */
	public void setUriHost(String host);

	/**
	 * 
	 * @param port
	 */
	public void setUriPort(int port);

	/**
	 * 
	 * @param path
	 */
	public void setUriPath(String path);

	/**
	 * 
	 * @param query
	 */
	public void setUriQuery(String query);

	/**
	 * 
	 * @param proxyUri
	 */
	public void setProxyUri(String proxyUri);

	/**
	 * 
	 */
	public void setToken(byte[] token);

	/**
	 * Add a media typs that is accepted as response.
	 * 
	 * @param mediaType
	 */
	public void addAccept(CoapMediaType mediaType);

	/**
	 * get the media types that are accepted as a response
	 * 
	 * @param mediaType
	 * @return
	 */
	public Vector<CoapMediaType> getAccept();

	/**
	 * 
	 * @return
	 */
	public String getUriHost();

	/**
	 * 
	 * @return
	 */
	public int getUriPort();

	/**
	 * 
	 * @return
	 */
	public String getUriPath();

	/**
	 * 
	 * @return
	 */
	public Vector<String> getUriQuery();

	/**
	 * 
	 * @return
	 */
	public String getProxyUri();

	/**
	 * 
	 * @param etag
	 */
	public void addETag(byte[] etag);

	/**
	 * 
	 * @return
	 */
	public Vector<byte[]> getETag();

	/**
	 * 
	 * @return
	 */
	public boolean getIfNoneMatchOption();

	/**
	 * 
	 * @param value
	 */
	public void setIfNoneMatchOption(boolean value);

	/**
	 * 
	 * @return
	 */
	public Vector<byte[]> getIfMatchOption();

	/**
	 * 
	 * @param etag
	 */
	public void addIfMatchOption(byte[] etag);

	/**
	 * 
	 * @return
	 */
	public CoapRequestCode getRequestCode();

	/**
	 * 
	 * @param requestCode
	 */
	public void setRequestCode(CoapRequestCode requestCode);
	
	public CoapHeaderOptions getOptions();
}
