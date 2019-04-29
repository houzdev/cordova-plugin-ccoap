/* Copyright 2011 University of Rostock
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

package org.ws4d.coap.core.connection;

import java.net.InetAddress;

import org.ws4d.coap.core.connection.api.CoapChannel;
import org.ws4d.coap.core.connection.api.CoapSocketHandler;
import org.ws4d.coap.core.enumerations.CoapBlockSize;
import org.ws4d.coap.core.messages.api.CoapMessage;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public abstract class BasicCoapChannel implements CoapChannel {
	private CoapSocketHandler socketHandler;
	private InetAddress remoteAddress;
	private int remotePort;
	private int localPort;
	/** null means no block option */
	private CoapBlockSize maxReceiveBlocksize;
	/** null means no block option */
	private CoapBlockSize maxSendBlocksize;

	public BasicCoapChannel(CoapSocketHandler socketHandler, InetAddress remoteAddress, int remotePort) {
		this.socketHandler = socketHandler;
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.localPort = socketHandler.getLocalPort();
	}
	
	CoapSocketHandler getSocketHandler(){
		return this.socketHandler;
	}

	@Override
	public void sendMessage(CoapMessage msg) {
		msg.setChannel(this);
		this.socketHandler.sendMessage(msg);
	}

	@Override
	public CoapBlockSize getMaxReceiveBlocksize() {
		return this.maxReceiveBlocksize;
	}

	@Override
	public void setMaxReceiveBlocksize(CoapBlockSize maxReceiveBlocksize) {
		this.maxReceiveBlocksize = maxReceiveBlocksize;
	}

	@Override
	public CoapBlockSize getMaxSendBlocksize() {
		return this.maxSendBlocksize;
	}

	@Override
	public void setMaxSendBlocksize(CoapBlockSize maxSendBlocksize) {
		this.maxSendBlocksize = maxSendBlocksize;
	}

	@Override
	public InetAddress getRemoteAddress() {
		return this.remoteAddress;
	}

	@Override
	public int getRemotePort() {
		return this.remotePort;
	}

	/*
	 * A channel is identified (and therefore unique) by its remote address,
	 * remote port and the local port
	 */
	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + this.localPort;
		result = prime * result + ((this.remoteAddress == null) ? 0 : this.remoteAddress.hashCode());
		result = prime * result + this.remotePort;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicCoapChannel other = (BasicCoapChannel) obj;
		if (this.localPort != other.localPort)
			return false;
		if (this.remoteAddress == null) {
			if (other.remoteAddress != null)
				return false;
		} else if (!this.remoteAddress.equals(other.remoteAddress))
			return false;
		if (this.remotePort != other.remotePort)
			return false;
		return true;
	}
}
