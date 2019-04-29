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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Vector;

import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.enumerations.CoapHeaderOptionType;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapPacketType;
import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.messages.api.CoapRequest;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public class BasicCoapRequest extends AbstractCoapMessage implements CoapRequest {

	private CoapRequestCode requestCode;

	public BasicCoapRequest(byte[] bytes, int length) {
		/* length ought to be provided by UDP header */
		this(bytes, length, 0);
	}

	public BasicCoapRequest(byte[] bytes, int length, int offset) {
		deserialize(bytes, length, offset);
		/*
		 * check if request code is valid, this function throws an error in case
		 * of an invalid argument
		 */
		this.requestCode = CoapRequestCode.parse(this.getMessageCodeValue());
	}

	public BasicCoapRequest(CoapPacketType packetType, CoapRequestCode requestCode, int messageId) {
		super(packetType, messageId, requestCode.getValue());
		this.requestCode = requestCode;

	}

	@Override
	public void setToken(byte[] token) {
		/* this function is only public for a request */
		super.setToken(token);
	}

	@Override
	public CoapRequestCode getRequestCode() {
		return this.requestCode;
	}

	@Override
	public void setUriHost(String host) {
		if (host == null)
			return;
		if (this.getOptions().optionExists(CoapHeaderOptionType.Uri_Host)) {
			throw new IllegalArgumentException("Uri-Host option already exists");
		}
		if (host.length() < 1 || host.length() > CoapConstants.MAX_SEGMENT_LENGTH) {
			throw new IllegalArgumentException("Invalid Uri-Host option length");
		}
		/* TODO: check if host is a valid address */
		this.getOptions().addOption(CoapHeaderOptionType.Uri_Host, host.getBytes());
	}

	@Override
	public void setUriPort(int port) {
		if (port < 0)
			return;
		if (this.getOptions().optionExists(CoapHeaderOptionType.Uri_Port)) {
			throw new IllegalArgumentException("Uri-Port option already exists");
		}
		byte[] value = long2CoapUint(port);
		if (value.length < 0 || value.length > 2) {
			throw new IllegalStateException("Illegal Uri-Port length");
		}
		this.getOptions().addOption(new CoapHeaderOption(CoapHeaderOptionType.Uri_Port, value));
	}

	@Override
	public void setUriPath(String path) {
		if (path == null)
			return;

		/* delete old options if present */
		this.getOptions().removeOption(CoapHeaderOptionType.Uri_Path);

		/* create substrings */
		String[] pathElements = path.split("/");
		/* add a Uri Path option for each part */
		for (String element : pathElements) {
			/* check length */
			if (element.length() < 0 || element.length() > CoapConstants.MAX_SEGMENT_LENGTH) {
				throw new IllegalArgumentException("Invalid Uri-Path length!");
			} else if (element.length() > 0) {
				/* ignore empty substrings */
				this.getOptions().addOption(CoapHeaderOptionType.Uri_Path, element.getBytes());
			}
		}
	}

	@Override
	public void setUriQuery(String query) {
		if (query == null)
			return;

		/* delete old options if present */
		this.getOptions().removeOption(CoapHeaderOptionType.Uri_Query);

		/* create substrings */
		String[] pathElements = query.split("&");
		/* add a Uri Path option for each part */
		for (String element : pathElements) {
			/* check length */
			if (element.length() < 0 || element.length() > CoapConstants.MAX_SEGMENT_LENGTH) {
				throw new IllegalArgumentException("Invalid Uri-Query");
			} else if (element.length() > 0) {
				/* ignore empty substrings */
				this.getOptions().addOption(CoapHeaderOptionType.Uri_Query, element.getBytes());
			}
		}

	}

	@Override
	public void setProxyUri(String proxyUri) {
		if (proxyUri == null)
			return;

		if (this.getOptions().optionExists(CoapHeaderOptionType.Proxy_Uri)) {
			throw new IllegalArgumentException("Proxy Uri already exists");
		}

		if (proxyUri.length() < 1) {
			throw new IllegalArgumentException("Proxy Uri must be at least one byte long");
		}

		if (proxyUri.length() > CoapConstants.MAX_PROXI_URI_LENGTH) {
			throw new IllegalArgumentException("Proxy Uri longer then 1034 bytes are not supported!");
		}

		this.getOptions().addOption(CoapHeaderOptionType.Proxy_Uri, proxyUri.getBytes());
	}

	@Override
	public Vector<String> getUriQuery() {
		Vector<String> queryList = new Vector<String>();

		for (CoapHeaderOption option : this.getOptions()) {
			if (option.getOptionType() == CoapHeaderOptionType.Uri_Query) {
				queryList.add(new String(option.getOptionData()));
			}
		}
		return queryList;
	}

	@Override
	public String getUriHost() {
		return new String(this.getOptions().getOption(CoapHeaderOptionType.Uri_Host).getOptionData());
	}

	@Override
	public int getUriPort() {
		CoapHeaderOption option = this.getOptions().getOption(CoapHeaderOptionType.Uri_Port);
		if (option == null) {
			return CoapConstants.COAP_DEFAULT_PORT;
		}
		byte[] value = option.getOptionData();

		if (value.length < 0 || value.length > 2) {
			/*
			 * should never happen because this is an internal variable and
			 * should be checked during serialization
			 */
			throw new IllegalStateException("Illegal Uri-Port Option length");
		}
		/* checked length -> cast is safe */
		return (int) coapUint2Long(this.getOptions().getOption(CoapHeaderOptionType.Uri_Port).getOptionData());
	}

	@Override
	public String getUriPath() throws IllegalArgumentException {
		if (this.getOptions().getOption(CoapHeaderOptionType.Uri_Path) == null) {
			return null;
		}
		StringBuilder uriPathBuilder = new StringBuilder();
		try {
			for (CoapHeaderOption option : this.getOptions()) {
				if (option.getOptionType() == CoapHeaderOptionType.Uri_Path) {
					uriPathBuilder.append("/");
					uriPathBuilder.append(new String(option.getOptionData(), "UTF-8"));
				}
			}
			return URLDecoder.decode(uriPathBuilder.toString(), "UTF-8");
		} catch (@SuppressWarnings("unused") UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Invalid Encoding");
		}
	}

	@Override
	public void addAccept(CoapMediaType mediaType) {
		this.getOptions().addOption(CoapHeaderOptionType.Accept, long2CoapUint(mediaType.getValue()));
	}

	@Override
	public Vector<CoapMediaType> getAccept() {
		if (this.getOptions().getOption(CoapHeaderOptionType.Accept) == null) {
			return null;
		}
		Vector<CoapMediaType> acceptList = new Vector<CoapMediaType>();
		for (CoapHeaderOption option : this.getOptions()) {
			if (option.getOptionType() == CoapHeaderOptionType.Accept) {
				CoapMediaType accept = CoapMediaType.parse((int) coapUint2Long(option.getOptionData()));
				// if (accept != CoapMediaType.UNKNOWN){
				/* add also UNKNOWN types to list */
				acceptList.add(accept);
				// }
			}
		}
		return acceptList;
	}

	@Override
	public String getProxyUri() {
		CoapHeaderOption option = this.getOptions().getOption(CoapHeaderOptionType.Proxy_Uri);
		if (option == null)
			return null;
		return new String(option.getOptionData());
	}

	@Override
	public void addETag(byte[] etag) {
		if (etag == null) {
			throw new IllegalArgumentException("etag MUST NOT be null");
		}
		if (etag.length < 1 || etag.length > 8) {
			throw new IllegalArgumentException("Invalid etag length");
		}
		this.getOptions().addOption(CoapHeaderOptionType.Etag, etag);
	}

	@Override
	public Vector<byte[]> getETag() {
		if (this.getOptions().getOption(CoapHeaderOptionType.Etag) == null) {
			return null;
		}

		Vector<byte[]> etagList = new Vector<byte[]>();
		for (CoapHeaderOption option : this.getOptions()) {
			if (option.getOptionType() == CoapHeaderOptionType.Etag) {
				byte[] data = option.getOptionData();
				if (data.length >= 1 && data.length <= 8) {
					etagList.add(option.getOptionData());
				}
			}
		}
		return etagList;
	}

	public void setIfNoneMatchOption(boolean value) {
		if (value) {
			this.getOptions().addOption(CoapHeaderOptionType.If_None_Match, null);
		} else {
			this.getOptions().removeOption(CoapHeaderOptionType.If_None_Match);
		}
	}

	public boolean getIfNoneMatchOption() {
		return this.getOptions().optionExists(CoapHeaderOptionType.If_None_Match);
	}

	public Vector<byte[]> getIfMatchOption() {
		if (this.getOptions().getOption(CoapHeaderOptionType.If_Match) == null) {
			return null;
		}

		Vector<byte[]> ifMatchList = new Vector<byte[]>();
		for (CoapHeaderOption option : this.getOptions()) {
			if (option.getOptionType() == CoapHeaderOptionType.If_Match) {
				byte[] data = option.getOptionData();
				if (data.length >= 1 && data.length <= 8) {
					ifMatchList.add(option.getOptionData());
				}
			}
		}
		return ifMatchList;
	}

	public void addIfMatchOption(byte[] etag) {
		this.getOptions().addOption(CoapHeaderOptionType.If_Match, etag);
	}

	@Override
	public boolean isRequest() {
		return true;
	}

	@Override
	public boolean isResponse() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public String toString() {
		String out = this.getPacketType().toString() + ", " + this.requestCode.toString() + ",\tMsgId: "
				+ getMessageID() + ", #Options: " + this.getOptions().getOptionCount();
		if (this.getOptions().getOptionCount() > 0) {
			out += " (";
			for (CoapHeaderOption o : this.getOptions()) {
				out += " " + o.getOptionType();
			}
			out += ")";
		}
		return out;
	}

	@Override
	public void setRequestCode(CoapRequestCode requestCode) {
		this.requestCode = requestCode;
	}

	@Override
	public boolean equals(Object object) {
		
		if(!(object instanceof CoapRequest)){
			return false;
		}
		CoapRequest other = (CoapRequest) object;
		/*
		 * Two CoapRequests are equal, if:
		 * 
		 * 1. the presented request methods match,
		 * 
		 * 2. all options match between both requests (which includes the
		 * request URI), except that there is no need for a match of any request
		 * options marked as NoCacheKey (Section 5.4) or recognized by the Cache
		 * and fully interpreted with respect to its specified cache behavior
		 * (such as the ETag request option described in Section 5.10.6; see
		 * also Section 5.4.2), and
		 */
		
		this.requestCode.equals(other.getRequestCode());
		
		// all options of this request are present in the other request?
		Iterator<CoapHeaderOption> it = this.getOptions().iterator();
		while(it.hasNext()){
			CoapHeaderOption opt = it.next();
			// noCacheKey options can be ignored
			if(!opt.isNoCacheKey()){
				// option existent and equal?
				CoapHeaderOption otherOpt = other.getOptions().getOption(opt.getOptionTypeValue());
				if(null == otherOpt || !opt.getOptionData().equals(otherOpt.getOptionData())){
					return false;
				}
			}
		}
		// all options of the other request are present in this request?
		it = other.getOptions().iterator();
		while(it.hasNext()){
			CoapHeaderOption opt = it.next();
			// noCacheKey options can be ignored
			if(!opt.isNoCacheKey()){
				// option existent and equal?
				CoapHeaderOption myOpt = this.getOptions().getOption(opt.getOptionTypeValue());
				if(null == myOpt || !opt.getOptionData().equals(myOpt.getOptionData())){
					return false;
				}
			}
		}
		return true;
	}

	public CoapHeaderOption getOption(int optionTypeValue) {
		return this.getOptions().getOption(optionTypeValue);
	}
}
