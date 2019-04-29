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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ws4d.coap.core.connection.api.CoapChannel;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.rest.CoapData;

/**
 * A CoapResource takes care of the resources content, its permissions and
 * observers. In order to be served over a network connection it needs to be
 * added to a {@link ResourceServer}
 * 
 * @author Nico Laum <nico.laum@uni-rostock.de>
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Bjorn Butzin <bjoern.butzin@uni-rostock.de>
 */
public interface CoapResource {

	/**
	 * Can be called to inform the resource about changed content.
	 */
	public void changed();

	/**
	 * Adds an observer to this resource
	 * 
	 * @param request
	 *            - the client request to observe this resource
	 * @return False if and only if the observer can not be added. <br>
	 *         This might have several reasons e.g. that the resource is not
	 *         observable.
	 */
	public boolean addObserver(CoapRequest request);

	/**
	 * Removes an observer from this resource
	 * 
	 * @param channel
	 *            - the channel that should be removed from the observers list.
	 *            In most cases this will be the CoapRequest.getChannel() from
	 *            the clients request
	 */
	public void removeObserver(CoapChannel channel);

	/**
	 * @return The last sequence number used for notification. Will not be
	 *         greater than 0xFFFF (2 byte integer)
	 */
	public int getObserveSequenceNumber();

	/**
	 * @return The Unix time (in milliseconds), when the resource expires. -1,
	 *         if the resource never expires.
	 */
	public long expires();

	/**
	 * @return true if and only if the resource is expired
	 */
	public boolean isExpired();

	/**
	 * Get the unique name of this resource
	 * 
	 * @return The unique name of the resource.
	 */
	public String getPath();

	/**
	 * Get the name of this resource. Might not be unique
	 * 
	 * @return The unique name of the resource.
	 */
	public String getShortName();

	/**
	 * Get the current value of the resource as byte[] in the most preferred
	 * media type possible.
	 * 
	 * @return the current value in the most preferred media type possible
	 * @see {@link #getMimeType(List<CoapMediaType>)} to get the encoding of the
	 *      data
	 * @see {@link #getValue(List, List<CoapMediaType>)} If you want to pass a
	 *      query string public
	 */
	CoapData get(List<CoapMediaType> mediaTypesAccepted);

	/**
	 * If can use this method to get the current value of the resource as byte[]
	 * with respect to query parameters and in the most preferred media type
	 * possible.
	 * 
	 * @return the current value in the most preferred media type possible
	 * @see {@link #getMimeType()} to get the encoding of the data
	 */
	public CoapData get(List<String> query, List<CoapMediaType> mediaTypesAccepted);

	/**
	 * Use this method to hand posted data to the resource. The behavior
	 * strongly depends on the resource itself.
	 * 
	 * @param data
	 *            - The data posted
	 * @return true if and only if the resource accepted the post and did the
	 *         respective changes
	 */
	public boolean post(byte[] data, CoapMediaType mediaType);

	/**
	 * Use this method to hand data put to the resource. The behavior strongly
	 * depends on the resource itself.
	 * 
	 * @param data
	 *            - The data posted
	 * @return true if and only if the resource accepted the post and did the
	 *         respective changes
	 */
	public boolean put(byte[] data, CoapMediaType mediaType);

	/**
	 * Use this method to indicate that this resource is going to be deleted.
	 */
	public void delete();

	/**
	 * Give the resource a callback to inform the resource server about changes.
	 * 
	 * @param server
	 *            - The resource server that handles this resource
	 */
	public void registerServerListener(ResourceServer server);

	/**
	 * Remove the callback handle to inform the resource server about changes.
	 * Changes of the resource will not be propagated to this resource server
	 * anymore.
	 * 
	 * @param server
	 *            - The resource server handle to be removed
	 */
	public void unregisterServerListener(ResourceServer server);

	/**
	 * @deprecated use {@link #isGetable()} instead
	 * @return True, if and only if the resource is readable.
	 */
	@Deprecated
	public boolean isReadable();
	
	/**
	 * @return True, if and only if the resource is readable.
	 */
	public boolean isGetable();

	/**
	 * Set, if the resource accepts get messages
	 * @deprecated use {@link #setGetable(boolean)} instead
	 * @return the changed CoapResource
	 */
	@Deprecated
	public CoapResource setReadable(boolean readable);
	
	/**
	 * Set, if the resource accepts get messages
	 * 
	 * @return the changed CoapResource
	 */
	public CoapResource setGetable(boolean getable);

	/**
	 * @return True, if and only if the resource accepts post requests.
	 */
	public boolean isPostable();

	/**
	 * Set, if the resource accepts post messages
	 * 
	 * @return the changed CoapResource
	 */
	public CoapResource setPostable(boolean postable);

	/**
	 * @return True, if and only if the resource accepts put requests.
	 */
	public boolean isPutable();

	/**
	 * Set, if the resource accepts put messages
	 * 
	 * @return the changed CoapResource
	 */
	public CoapResource setPutable(boolean putable);

	/**
	 * @return True, if and only if the resource is observable.
	 */
	public boolean isObservable();

	/**
	 * Set, if the resource can be observed
	 * 
	 * @return the changed CoapResource
	 */
	public CoapResource setObservable(boolean observable);

	/**
	 * @return True, if and only if the resource is delete-able.
	 */
	public boolean isDeletable();

	/**
	 * Set, if the resource can be deleted
	 * 
	 * @return the changed CoapResource
	 */
	public CoapResource setDeletable(boolean deletable);

	/**
	 * @return the the list of supported CoAP Media Types for this resource
	 */
	public Set<CoapMediaType> getAvailableMediaTypes();

	/**
	 * This method is used to get the resource type of this resource.
	 * 
	 * @return The string representing the resource type or null.
	 */
	public String getResourceType();

	/**
	 * This method is used to get the interface description of this resource.
	 * 
	 * @return The string representing the interface description or null.
	 */
	public String getInterfaceDescription();
	
	/**
	 * This method is used to get all tags assigned to this resource
	 * 
	 * @return The map containing all tags assigned. If a key is present but the value is null, the tag can be assumed to be a flag.
	 * Otherwise it can be assumed to be a key-value-pair
	 */
	public Map<String, String> getTags();
	
	/**
	 * This method is used to get all tags assigned to this resource
	 * 
	 * @return The map containing all tags assigned. If a key is present but the value is null, the tag can be assumed to be a flag.
	 * Otherwise it can be assumed to be a key-value-pair
	 */
	public void addTag(String key, String value);
	
	/**
	 * This method is used to get all tags assigned to this resource
	 * 
	 * @return The map containing all tags assigned. If a key is present but the value is null, the tag can be assumed to be a flag.
	 * Otherwise it can be assumed to be a key-value-pair
	 */
	public void addFlag(String flag);
	
	/**
	 * This method is used to get all tags assigned to this resource
	 * 
	 * @return The map containing all tags assigned. If a key is present but the value is null, the tag can be assumed to be a flag.
	 * Otherwise it can be assumed to be a key-value-pair
	 */
	public void removeTag(String tag);

	/**
	 * @return an integer value representing the size of the resources value
	 */
	public int getSizeEstimate();

	/**
	 * Sets if notifications are made in a reliable fashion
	 * 
	 * @param reliableNotification
	 *            True, if notifications are made in a reliable fashion by
	 *            default False, if notifications are made in an unreliable
	 *            fashion by default NULL, if the client can decide weather it
	 *            want to be notified in a reliable fashion or not
	 */
	public CoapResource setReliableNotification(Boolean reliableNotification);

	/**
	 * 
	 * @return True, if notifications are made in a reliable fashion by default
	 *         False, if notifications are made in an unreliable fashion by
	 *         default NULL, if the client can decide weather it want to be
	 *         notified in a reliable fashion or not
	 */
	public Boolean getReliableNotification();
}
