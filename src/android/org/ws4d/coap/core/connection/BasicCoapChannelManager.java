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

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Random;

import org.ws4d.coap.core.CoapClient;
import org.ws4d.coap.core.CoapServer;
import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.connection.api.CoapChannelManager;
import org.ws4d.coap.core.connection.api.CoapClientChannel;
import org.ws4d.coap.core.connection.api.CoapServerChannel;
import org.ws4d.coap.core.connection.api.CoapSocketHandler;
import org.ws4d.coap.core.messages.BasicCoapRequest;
import org.ws4d.coap.core.messages.api.CoapMessage;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */
public class BasicCoapChannelManager implements CoapChannelManager {
	// global message id
	private int globalMessageId;
	private static BasicCoapChannelManager instance;
	private HashMap<Integer, SocketInformation> socketMap = new HashMap<Integer, SocketInformation>();
	
	private BasicCoapChannelManager() {
		initRandom();
	}

	public synchronized static CoapChannelManager getInstance() {
		if (instance == null) {
			instance = new BasicCoapChannelManager();
		}
		return instance;
	}

	@Override
	public synchronized CoapServerChannel createServerChannel(CoapSocketHandler socketHandler, CoapMessage message,
			InetAddress addr, int port) {
		SocketInformation socketInfo = this.socketMap.get(socketHandler.getLocalPort());

		if (socketInfo.getServerListener() == null) {
			/* this is not a server socket */
			throw new IllegalStateException("Invalid server socket");
		}

		if (!message.isRequest()) {
			throw new IllegalStateException("Incomming message is not a request message");
		}

		CoapServer server = socketInfo.getServerListener().onAccept((BasicCoapRequest) message);
		if (server == null) {
			/* Server rejected channel */
			return null;
		}
		CoapServerChannel newChannel = new BasicCoapServerChannel(socketHandler, server, addr, port);
		return newChannel;
	}

	@Override
	public synchronized int getNewMessageID() {
		if (this.globalMessageId < CoapConstants.MESSAGE_ID_MAX) {
			++this.globalMessageId;
		} else
			this.globalMessageId = CoapConstants.MESSAGE_ID_MIN;
		return this.globalMessageId;
	}

	@Override
	public synchronized void initRandom() {
		// generate random 16 bit messageId
		Random random = new Random();
		this.globalMessageId = random.nextInt(CoapConstants.MESSAGE_ID_MAX + 1);
	}

	@Override
	public void createServerListener(CoapServer listener, int localPort) {
		if (!this.socketMap.containsKey(localPort)) {
			try {
				SocketInformation socketInfo = new SocketInformation(new BasicCoapSocketHandler(this, localPort),
						listener);
				this.socketMap.put(localPort, socketInfo);
			} catch (IOException e) {
			}
		} else {
			throw new IllegalStateException("address already in use");
		}
	}

	public void removeServerListener(CoapServer listener, int localPort) {
		if (this.socketMap.containsKey(localPort)) {
			SocketInformation socketInfo = this.socketMap.get(localPort);
			if (socketInfo.getServerListener().equals(listener)) {
				socketInfo.getSocketHandler().close();
				this.socketMap.remove(localPort);
			}
		}
	}

	@Override
	public CoapClientChannel connect(CoapClient client, InetAddress addr, int port) {
		CoapSocketHandler socketHandler = null;
		try {
			socketHandler = new BasicCoapSocketHandler(this);
			SocketInformation sockInfo = new SocketInformation(socketHandler, null);
			this.socketMap.put(socketHandler.getLocalPort(), sockInfo);
			return socketHandler.connect(client, addr, port);
		} catch (IOException e) {
		}
		return null;
	}

	@Override
	public void setMessageId(int globalMessageId) {
		this.globalMessageId = globalMessageId;
	}

	private class SocketInformation {
		private CoapSocketHandler handler = null;
		private CoapServer listener = null;

		public SocketInformation(CoapSocketHandler socketHandler, CoapServer serverListener) {
			super();
			this.handler = socketHandler;
			this.listener = serverListener;
		}

		public CoapSocketHandler getSocketHandler() {
			return this.handler;
		}

		public CoapServer getServerListener() {
			return this.listener;
		}
	}
}
