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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.ws4d.coap.core.CoapServer;
import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.connection.BasicCoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapServerChannel;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.enumerations.CoapResponseCode;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;
import org.ws4d.coap.core.rest.api.CoapResource;
import org.ws4d.coap.core.rest.api.ResourceServer;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Bjorn Konieczek <bjoern.konieczek@uni-rostock.de>
 * @author Bjorn Butzin <bjoern.butzin@uni-rostock.de>
 */
public class CoapResourceServer implements ResourceServer {
	private int port = 0;
	private Map<String, byte[]> etags = new HashMap<String, byte[]>();
	private Map<String, CoapResource> resources = new HashMap<String, CoapResource>();
	private CoreResource coreResource = new CoreResource(this);
	/** toggle if the creation of resources is allowed on this server **/
	private boolean allowCreate = true;

	public Map<String, CoapResource> getResources() {
		return this.resources;
	}

	/**
	 * Adds a resource to the resources list and set up a resource listener.
	 * 
	 * @param resource
	 *            - The resource to add
	 */
	private void addResource(CoapResource resource) {
		resource.registerServerListener(this);
		this.resources.put(resource.getPath(), resource);
		this.coreResource.changed();
	}

	@Override
	public boolean createResource(CoapResource resource) {
		if (null != resource && !this.resources.containsKey(resource.getPath())) {
			addResource(resource);
			generateEtag(resource);
			this.coreResource.changed();
			return true;
		}
		return false;
	}

	@Override
	public boolean updateResource(CoapResource resource, CoapRequest request) {
		if (null != resource && this.resources.containsKey(resource.getPath())) {
			resource.put(request.getPayload(), request.getContentType());
			generateEtag(resource);
			return true;
		}
		return false;
	}

	@Override
	public boolean deleteResource(String path) {
		if (null != this.resources.remove(path)) {
			this.etags.remove(path);
			this.coreResource.changed();
			return true;
		}
		return false;
	}

	@Override
	public final CoapResource getResource(String path) {
		return this.resources.get(path);
	}

	@Override
	public void start() throws Exception {
		start(CoapConstants.COAP_DEFAULT_PORT);
	}

	/**
	 * Start the ResourceServer. This usually opens network ports and makes the
	 * resources available through a certain network protocol.
	 * 
	 * @param serverport
	 *            - The port to be used.
	 * @throws Exception
	 *             if the connection can not be established
	 * @see {@link #start()} To start the server on the standard port
	 */
	public void start(int serverport) throws Exception {
		this.coreResource = new CoreResource(this);
		this.resources.put(this.coreResource.getPath(), this.coreResource);
		this.port = serverport;
		BasicCoapChannelManager.getInstance().createServerListener(this, this.port);
	}

	@Override
	public void stop() {
		this.resources.clear();
		addResource(this.coreResource);
		this.etags.clear();
		this.coreResource.changed();
		// FIXME causes NullPointerException when starting the server again
		// at org.ws4d.coap.core.connection.BasicCoapChannelManager.createServerChannel(BasicCoapChannelManager.java:61)
		// BasicCoapChannelManager.getInstance().removeServerListener(this, this.port);
	}

	/**
	 * @return The port this server runs on.
	 */
	public int getPort() {
		return this.port;
	}

	@Override
	public URI getHostUri() {
		URI hostUri = null;
		try {
			hostUri = new URI("coap://" + getLocalIpAddress() + ":" + getPort());
		} catch (URISyntaxException e) {
		}
		return hostUri;
	}

	@Override
	public void resourceChanged(CoapResource resource) {
	}

	@Override
	public CoapServer onAccept(CoapRequest request) {
		return this;
	}

	@Override
	public void onRequest(CoapServerChannel channel, CoapRequest request) {
		CoapResponse response = null;
		CoapRequestCode requestCode = request.getRequestCode();
		String targetPath = request.getUriPath();
		int eTagMatch = -1;
		CoapResource resource = getResource(targetPath);

		switch (requestCode) {
		case GET:
			if (null != request.getETag()) {
				eTagMatch = request.getETag().indexOf(this.etags.get(targetPath));
			}
			if (null == resource) {
				response = channel.createResponse(request, CoapResponseCode.Not_Found_404);
			} else if (eTagMatch != -1) {
				response = channel.createResponse(request, CoapResponseCode.Valid_203, CoapMediaType.text_plain);
				response.setETag(request.getETag().get(eTagMatch));
			} else if (!resource.isGetable()) {
				response = channel.createResponse(request, CoapResponseCode.Method_Not_Allowed_405);
			} else {
				// Accept Formats?
				boolean matchAccept = true;
				List<CoapMediaType> mediaTypesAccepted = request.getAccept();
				if (null != mediaTypesAccepted) {
					matchAccept = false;
					for (CoapMediaType mt : mediaTypesAccepted) {
						if (resource.getAvailableMediaTypes().contains(mt)) {
							matchAccept = true;
							break;
						}
					}
					if (!matchAccept) {
						// accepted formats option present but resource is not
						// available in the requested format
						response = channel.createResponse(request, CoapResponseCode.Not_Acceptable_406);
					}
				}
				if (matchAccept) {
					// accepted formats option not present
					// URI queries
					Vector<String> uriQueries = request.getUriQuery();
					CoapData responseValue = (null == uriQueries ? resource.get(mediaTypesAccepted)
							: resource.get(uriQueries, mediaTypesAccepted));
					// BLOCKWISE transfer?
					if (null != request.getBlock2() || null != channel.getMaxSendBlocksize()) {
						response = channel.addBlockContext(request, responseValue.getPayload());
					} else {
						response = channel.createResponse(request, CoapResponseCode.Content_205,
								responseValue.getMediaType());
						response.setPayload(responseValue);
					}
					// OBSERVE?
					if (null != request.getObserveOption() && resource.addObserver(request)) {
						response.setObserveOption(resource.getObserveSequenceNumber());
					}
				}
			}
			break;
		case DELETE:
			// resource not exist or can be deleted -> delete
			if (null == resource || resource.isDeletable()) {
				if (null != resource)
					resource.delete();
				deleteResource(targetPath);
				response = channel.createResponse(request, CoapResponseCode.Deleted_202);
			} else {
				response = channel.createResponse(request, CoapResponseCode.Method_Not_Allowed_405);
			}
			break;
		case POST:
			if (null == resource && this.allowCreate) {
				// resource not exist & creation is allowed -> create
				createResource(createResourceFromRequest(request));
				response = channel.createResponse(request, CoapResponseCode.Created_201);
			} else if (null != resource && resource.isPostable()) {
				// resource exist & accepts post requests -> change
				resource.post(request.getPayload(), request.getContentType());
				response = channel.createResponse(request, CoapResponseCode.Changed_204);
			} else {
				response = channel.createResponse(request, CoapResponseCode.Method_Not_Allowed_405);
			}
			break;
		case PUT:
			if (null != request.getIfMatchOption()) {
				eTagMatch = request.getIfMatchOption().indexOf(this.etags.get(targetPath));
			}
			if (null == resource) {
				// create
				if (null != request.getIfMatchOption()) {
					// client intended to update an existing resource
					response = channel.createResponse(request, CoapResponseCode.Precondition_Failed_412);
				} else if (!this.allowCreate) {
					// it is not allowed to create resources
					response = channel.createResponse(request, CoapResponseCode.Method_Not_Allowed_405);
				} else {
					// all fine, create resource
					createResource(createResourceFromRequest(request));
					response = channel.createResponse(request, CoapResponseCode.Created_201);
				}
			} else {
				// update
				if (request.getIfNoneMatchOption() || (null != request.getIfMatchOption() && -1 == eTagMatch)) {
					// client not intent to update an existing resource
					// OR client intended to update a resource with a specific
					// eTag which is not there (anymore)
					response = channel.createResponse(request, CoapResponseCode.Precondition_Failed_412);
				} else if (!resource.isPutable()) {
					// resource did not accept put requests
					response = channel.createResponse(request, CoapResponseCode.Method_Not_Allowed_405);
				} else {
					updateResource(resource, request);
					response = channel.createResponse(request, CoapResponseCode.Changed_204);
				}
			}
			break;
		default:
			response = channel.createResponse(request, CoapResponseCode.Bad_Request_400);
			break;
		}
		channel.sendMessage(response);

	}

	/**
	 * This is used to create a new resource when requested.
	 * 
	 * @param request
	 *            - the request that leads to the creation
	 * @return The resource created
	 */
	private static CoapResource createResourceFromRequest(CoapRequest request) {
		BasicCoapResource resource = new BasicCoapResource(request.getUriPath(), request.getPayload(),
				request.getContentType());
		resource.setPostable(true);
		resource.setPutable(true);
		resource.setReadable(true);
		resource.setDeletable(true);
		resource.setObservable(true);
		return resource;
	}

	@Override
	public void onSeparateResponseFailed(CoapServerChannel channel) {
	}

	@Override
	public void onReset(CoapRequest lastRequest) {
		CoapResource resource = getResource(lastRequest.getUriPath());
		if (resource != null) {
			resource.removeObserver(lastRequest.getChannel());
		}
	}

	/**
	 * @return A string containing the representation of the current IP address
	 *         or null
	 */
	private static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
		}
		return null;
	}

	/**
	 * Generates an eTag for a resource and puts it to the list of eTags
	 * {@link #etags}
	 * 
	 * @param resource
	 *            - The resource that should be tagged / re-tagged
	 */
	private void generateEtag(CoapResource resource) {
		this.etags.put(resource.getPath(), ("" + resource.hashCode()).getBytes());
	}

	public boolean allowRemoteResourceCreation(boolean allow) {
		this.allowCreate = allow;
		return true;
	}
}