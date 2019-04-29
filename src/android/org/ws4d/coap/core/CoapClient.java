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

package org.ws4d.coap.core;

import java.net.InetAddress;

import org.ws4d.coap.core.connection.api.CoapClientChannel;
import org.ws4d.coap.core.messages.api.CoapResponse;

/**
 * This is the base interface that has to be implemented by every CoAP client in
 * order to receive callbacks on different events.
 * 
 * @author Christian Lerche
 */
public interface CoapClient {

	/**
	 * This is a callback method which allows to handle a response at the
	 * application layer.
	 * 
	 * @param channel
	 *            - The {@link CoapClientChannel} where the message arrived.
	 * @param response
	 *            - The {@link CoapResponse} that arrived.
	 */
	public void onResponse(CoapClientChannel channel, CoapResponse response);

	/**
	 * This is a callback method which allows to handle a response to a
	 * multicast request at the application layer.
	 * 
	 * @param channel
	 *            - The {@link CoapClientChannel} where the message arrived.
	 * @param resonse
	 *            - The {@link CoapResponse} that arrived
	 * @param srcAddress
	 *            - The IP address of the origin server of the response.
	 * @param srcPort
	 *            - The Port of the origin server.
	 */
	public void onMCResponse(CoapClientChannel channel, CoapResponse response, InetAddress srcAddress, int srcPort);

	/**
	 * This is a callback method which allows to handle a connection failure at
	 * the application layer.
	 * 
	 * @param channel
	 *            - The {@link CoapClientChannel} where the connection failed.
	 * @param notReachable
	 *            - A flag to indicate that the server is not reachable
	 * @param resetByServer
	 *            - A flag to indicate that the remote server made a reset
	 */
	public void onConnectionFailed(CoapClientChannel channel, boolean notReachable, boolean resetByServer);
}
