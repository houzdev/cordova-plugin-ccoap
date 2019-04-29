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

import java.net.InetAddress;

import org.ws4d.coap.core.CoapClient;
import org.ws4d.coap.core.messages.api.CoapMessage;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public interface CoapSocketHandler {

	/**
	 * 
	 * @param client
	 * @param remoteAddress
	 * @param remotePort
	 * @return
	 */
	public CoapClientChannel connect(CoapClient client, InetAddress remoteAddress, int remotePort);

	/**
	 * 
	 */
	public void close();

	/**
	 * 
	 * @param msg
	 */
	public void sendMessage(CoapMessage msg);

	/**
	 * 
	 * @return
	 */
	public CoapChannelManager getChannelManager();

	/**
	 * 
	 * @return
	 */
	public int getLocalPort();

	/**
	 * 
	 * @param channel
	 */
	public void removeClientChannel(CoapClientChannel channel);

	/**
	 * 
	 * @param channel
	 */
	public void removeServerChannel(CoapServerChannel channel);
}
