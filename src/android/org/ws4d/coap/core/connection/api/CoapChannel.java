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

package org.ws4d.coap.core.connection.api;

import java.net.InetAddress;

import org.ws4d.coap.core.enumerations.CoapBlockSize;
import org.ws4d.coap.core.messages.api.CoapMessage;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public interface CoapChannel {

	/**
	 * 
	 * @param msg
	 */
	public void sendMessage(CoapMessage msg);

	/**
	 * 
	 */
	public void close();

	/**
	 * 
	 * @return
	 */
	public InetAddress getRemoteAddress();

	/**
	 * 
	 * @return
	 */
	public int getRemotePort();

	/**
	 * handles an incoming message
	 * 
	 * @param message
	 *            - the message to be handled
	 */
	public void handleMessage(CoapMessage message);

	/**
	 * handles an incoming multicast response
	 * 
	 * @param message
	 *            - the message to be handled
	 * @param srcAddress
	 *            - the source address of the multicast response
	 * @param srcPort
	 *            - the source port of the multicast response
	 */
	public void handleMCResponse(CoapMessage message, InetAddress srcAddress, int srcPort);

	/**
	 * 
	 * @param notReachable
	 * @param resetByServer
	 */
	public void lostConnection(boolean notReachable, boolean resetByServer);

	/**
	 * 
	 * @return
	 */
	public CoapBlockSize getMaxReceiveBlocksize();

	/**
	 * 
	 * @param maxReceiveBlocksize
	 */
	public void setMaxReceiveBlocksize(CoapBlockSize maxReceiveBlocksize);

	/**
	 * 
	 * @return
	 */
	public CoapBlockSize getMaxSendBlocksize();

	/**
	 * 
	 * @param maxSendBlocksize
	 */
	public void setMaxSendBlocksize(CoapBlockSize maxSendBlocksize);
}
