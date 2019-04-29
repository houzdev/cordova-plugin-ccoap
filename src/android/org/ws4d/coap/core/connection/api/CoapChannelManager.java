/* Copyright 2011 University of Rostock
 
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

import java.net.InetAddress;

import org.ws4d.coap.core.CoapClient;
import org.ws4d.coap.core.CoapServer;
import org.ws4d.coap.core.messages.api.CoapMessage;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public interface CoapChannelManager {

	/**
	 * Creates a new, global message ID for a new CoAP message
	 * 
	 * @return Message ID
	 */
	public int getNewMessageID();

	/**
	 * called by the socket Listener to create a new Server Channel the Channel
	 * Manager then asked the Server Listener if he wants to accept a new
	 * connection
	 * 
	 * @param socketHandler
	 * @param message
	 * @param addr
	 * @param port
	 * @return
	 */
	public CoapServerChannel createServerChannel(CoapSocketHandler socketHandler, CoapMessage message, InetAddress addr,
			int port);

	/**
	 * creates a server socket listener for incoming connections
	 * 
	 * @param serverListener
	 * @param localPort
	 */
	public void createServerListener(CoapServer serverListener, int localPort);

	/**
	 * removes a server socket listener for incoming connections
	 * 
	 * @param serverListener
	 * @param localPort
	 */
	public void removeServerListener(CoapServer coapResourceServer, int port);

	/**
	 * called by a client to create a connection
	 * 
	 * @param client
	 * @param addr
	 * @param port
	 * @return
	 */
	public CoapClientChannel connect(CoapClient client, InetAddress addr, int port);

	/**
	 * This function is for testing purposes only, to have a determined message
	 * id
	 * 
	 * @param globalMessageId
	 */
	public void setMessageId(int globalMessageId);

	/**
	 * Initializes the message ID with a random value.
	 */
	public void initRandom();
}
