/* Copyright 2016 University of Rostock
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

package org.ws4d.coap.core.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.api.ResourceHandler;

/**
 * @author Bjorn Butzin <bjoern.butzin@uni-rostock.de>
 */
public class MultiTypeResource extends BasicCoapResource {
	// representations
	private Map<CoapMediaType, ResourceHandler> resourceHandler = new HashMap<CoapMediaType, ResourceHandler>();
	private CoapMediaType defaultMediaType;

	public MultiTypeResource(String initPath, ResourceHandler defaultHandler) throws IllegalArgumentException {
		super(initPath, "", defaultHandler.getMediaType());
		this.defaultMediaType = defaultHandler.getMediaType();
		this.resourceHandler.put(defaultHandler.getMediaType(), defaultHandler);
	}

	public synchronized void addResourceHandler(ResourceHandler handler) {
		this.resourceHandler.put(handler.getMediaType(), handler);
	}

	public synchronized boolean removeResourceHandler(ResourceHandler handler) {
		return removeResourceHandler(handler.getMediaType());
	}

	public synchronized boolean removeResourceHandler(CoapMediaType type) {
		if (type == this.defaultMediaType) {
			return false;
		}
		this.resourceHandler.remove(type);
		return true;
	}

	@Override
	public synchronized Set<CoapMediaType> getAvailableMediaTypes() {
		return this.resourceHandler.keySet();
	}

	@Override
	public synchronized CoapData get(List<CoapMediaType> mediaTypesAccepted) {
		if (mediaTypesAccepted == null) {
			this.resourceHandler.get(this.defaultMediaType).handleGet();
		} else {
			for (CoapMediaType mt : mediaTypesAccepted) {
				if (this.getAvailableMediaTypes().contains(mt)) {
					return this.resourceHandler.get(mt).handleGet();
				}
			}
		}
		return null;
	}

	@Override
	public synchronized CoapData get(List<String> query, List<CoapMediaType> mediaTypesAccepted) {
		if (mediaTypesAccepted == null) {
			return this.resourceHandler.get(this.defaultMediaType).handleGet(query);
		}
		for (CoapMediaType mt : mediaTypesAccepted) {
			if (this.getAvailableMediaTypes().contains(mt)) {
				return this.resourceHandler.get(mt).handleGet(query);
			}
		}
		return null;
	}

	@Override
	public synchronized boolean post(byte[] data, CoapMediaType mediaType) {
		ResourceHandler rh = this.resourceHandler.get(mediaType);
		if(rh != null) {
			return rh.handlePost(data);
		}
		return false;
	}

	@Override
	public synchronized int getSizeEstimate() {
		return this.resourceHandler.get(this.defaultMediaType).handleGet().getPayload().length;
	}

	@Override
	public synchronized boolean put(byte[] data, CoapMediaType mediaType) {
		ResourceHandler rh = this.resourceHandler.get(mediaType);
		if(rh != null) {
			return rh.handlePut(data);
		}
		return false;
	}

	@Override
	public synchronized void delete() {
		this.resourceHandler.get(this.defaultMediaType).handleDelete();
	}
}