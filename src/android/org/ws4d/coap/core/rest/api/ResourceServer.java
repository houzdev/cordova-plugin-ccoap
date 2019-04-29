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

package org.ws4d.coap.core.rest.api;

import java.net.URI;
import java.util.Map;

import org.ws4d.coap.core.CoapServer;
import org.ws4d.coap.core.messages.api.CoapRequest;

/**
 * A ResourceServer manages resources, takes care of resource permissions and
 * handles GET, POST, PUT and DELETE requests.
 * 
 * @author Nico Laum
 * @author Christian Lerche
 * @author Bjorn Butzin <bjoern.butzin@uni-rostock.de>
 */
public interface ResourceServer extends CoapServer {

	/**
	 * Creates a resource. Resource must not exist.
	 * 
	 * @param resource
	 *            The resource to be handled
	 * @return false, if resource exists
	 */
	public boolean createResource(CoapResource resource);

	/**
	 * @param path
	 * @return resource at the given path. <br>
	 *         null, if no resource exists
	 */
	public CoapResource getResource(String path);

	/**
	 * Updates a resource. The resource must exist and is NOT created otherwise.
	 * 
	 * @param resource
	 *            - the resource to be updated
	 * @param request
	 * @return false, if resource not exists
	 */
	public boolean updateResource(CoapResource resource, CoapRequest request);

	/**
	 * deletes the resource at path
	 * 
	 * @param path
	 *            path of the resource to be deleted
	 * @return false, if resource does not exist
	 */
	public boolean deleteResource(String path);

	/**
	 * Start the ResourceServer. This usually opens network connections and
	 * makes the resources available through a certain network protocol.
	 */
	public void start() throws Exception;

	/**
	 * Stops the ResourceServer. This usually closes network connections and
	 * makes the resources unavailable through on the network.
	 */
	public void stop();

	/**
	 * Can be used to obtain the current URI of this resource server.
	 * 
	 * @return the host URI of this resource server
	 */
	public URI getHostUri();

	/**
	 * This method can be used to inform the resource server about a changed
	 * resource.
	 * 
	 * @param resource
	 *            - the resource that has changed
	 */
	public void resourceChanged(CoapResource resource);

	/**
	 * This method is used to get all resources managed by the server.
	 * 
	 * @return A map of resource paths to the resource objects.
	 */
	public Map<String, CoapResource> getResources();

	/**
	 * Defines if the server will create resources on remote requests
	 * 
	 * @param allow
	 *            set to true if you want to allow the creation of resources,
	 *            false otherwise
	 * @return true - if and only if successful
	 */
	public boolean allowRemoteResourceCreation(boolean allow);
}
