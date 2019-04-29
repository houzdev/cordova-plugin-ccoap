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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import org.ws4d.coap.core.CoapClient;
import org.ws4d.coap.core.connection.api.CoapClientChannel;
import org.ws4d.coap.core.connection.api.CoapSocketHandler;
import org.ws4d.coap.core.enumerations.CoapBlockSize;
import org.ws4d.coap.core.enumerations.CoapPacketType;
import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.messages.BasicCoapRequest;
import org.ws4d.coap.core.messages.BasicCoapResponse;
import org.ws4d.coap.core.messages.CoapBlockOption;
import org.ws4d.coap.core.messages.CoapEmptyMessage;
import org.ws4d.coap.core.messages.api.CoapMessage;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.rest.CoapData;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Bjoern Konieczek <bjoern.konieczek@uni-rostock.de>
 */

public class BasicCoapClientChannel extends BasicCoapChannel implements CoapClientChannel {
	private CoapClient client = null;
	private ClientBlockContext blockContext = null;
	private CoapRequest lastRequest = null;
	private Object trigger = null;

	public BasicCoapClientChannel(CoapSocketHandler socketHandler, CoapClient client, InetAddress remoteAddress,
			int remotePort) {
		super(socketHandler, remoteAddress, remotePort);
		this.client = client;
	}

	@Override
	public void close() {
		this.getSocketHandler().removeClientChannel(this);
	}

	public byte[] getLastToken() {
		if (this.lastRequest != null) {
			return this.lastRequest.getToken();
		}
		return null;
	}

	@Override
	public void handleMessage(CoapMessage message) {
		if (message.isRequest()) {
			/* this is a client channel, no requests allowed */
			message.getChannel().sendMessage(new CoapEmptyMessage(CoapPacketType.RST, message.getMessageID()));
			return;
		}

		if (message.isEmpty() && message.getPacketType() == CoapPacketType.ACK) {
			/* this is the ACK of a separate response */
			// TODO: implement a handler or listener, that informs a client when a sep. resp. ack was received
			return;
		}

		if (message.getPacketType() == CoapPacketType.CON) {
			/* this is a separate response */
			/* send ACK */
			this.sendMessage(new CoapEmptyMessage(CoapPacketType.ACK, message.getMessageID()));
		}

		/* check for blockwise transfer */

		@SuppressWarnings("unused")
		CoapBlockOption block1 = message.getBlock1();

		CoapBlockOption block2 = message.getBlock2();
		if (this.blockContext == null && block2 != null) {
			/* initiate blockwise transfer */
			this.blockContext = new ClientBlockContext(block2, this.getMaxReceiveBlocksize());
			this.blockContext.setFirstRequest(this.lastRequest);
		}

		if (this.blockContext != null) {
			/* blocking option */
			if (this.blockContext.getFirstRequest() == null)
			/* If this is a response, to a blockwise GET, add the payload to the current BlockContext.
			 */
			if (this.blockContext.getFirstRequest().getRequestCode() == CoapRequestCode.GET) {
				if (!this.blockContext.addBlock(message, block2)) {
					/* Current block number and expected block number do not match! Hence, the block was not added to the BlockContext */
					/* TODO: implement a RST*/
				}
			}

			if (!this.blockContext.isFinished()) {
				/*
				 * TODO: implement a counter to avoid an infinity req/resp loop:
				 * if the same block is received more than x times -> rst the
				 * connection implement maxPayloadSize to avoid an infinity
				 * payload
				 */
				CoapBlockOption newBlock = this.blockContext.getNextBlock();
				if (this.lastRequest == null) {
				} else {
					/* create a new request for the next block */
					BasicCoapRequest request = new BasicCoapRequest(this.lastRequest.getPacketType(),
							this.lastRequest.getRequestCode(), this.getSocketHandler().getChannelManager().getNewMessageID());
					request.copyHeaderOptions((BasicCoapRequest) this.blockContext.getFirstRequest());
					request.setToken(this.blockContext.getFirstRequest().getToken());
					if (request.getRequestCode() == CoapRequestCode.GET) {
						request.setBlock2(newBlock);
					} else {
						request.setBlock1(newBlock);
						request.setPayload(this.blockContext.getNextPayload(newBlock));
					}
					sendMessage(request);
				}
				return;
			}
			/* blockwise transfer finished */

			message.setPayload(new CoapData(this.blockContext.getPayload(), message.getContentType()));
			this.blockContext = null;
		}

		/* normal or separate response */
		this.client.onResponse(this, (BasicCoapResponse) message);
	}

	@Override
	public void handleMCResponse(CoapMessage message, InetAddress srcAddress, int srcPort) {

		if (message.isRequest()) {
			/* this is a client channel, no requests allowed */
			message.getChannel().sendMessage(new CoapEmptyMessage(CoapPacketType.RST, message.getMessageID()));
			return;
		}

		if (message.isEmpty() && message.getPacketType() == CoapPacketType.ACK) {
			/* this is the ACK of a separate response */
			// TODO: implement a handler or listener, that informs a client when a sep. resp. ack was received
			return;
		}

		if (message.getPacketType() == CoapPacketType.CON) {
			/* this is a separate response */
			/* send ACK */
			this.sendMessage(new CoapEmptyMessage(CoapPacketType.ACK, message.getMessageID()));
		}

		if (message.getBlock1() != null || message.getBlock2() != null) {
		} else {
			this.client.onMCResponse(this, (BasicCoapResponse) message, srcAddress, srcPort);
		}
	}

	@Override
	public void lostConnection(boolean notReachable, boolean resetByServer) {
		this.client.onConnectionFailed(this, notReachable, resetByServer);

	}

	@Override
	public BasicCoapRequest createRequest(boolean reliable, CoapRequestCode requestCode) {
		BasicCoapRequest msg = new BasicCoapRequest(reliable ? CoapPacketType.CON : CoapPacketType.NON, requestCode,
				this.getSocketHandler().getChannelManager().getNewMessageID());
		msg.setChannel(this);
		return msg;
	}
	
	@Override
	public BasicCoapRequest createRequest(CoapRequestCode requestCode, String path, boolean reliable) {
		BasicCoapRequest msg = new BasicCoapRequest(reliable ? CoapPacketType.CON : CoapPacketType.NON, requestCode,
				this.getSocketHandler().getChannelManager().getNewMessageID());
		msg.setChannel(this);
		msg.setUriPath(path);
		return msg;
	}

	@Override
	public void sendMessage(CoapMessage msg) {
		super.sendMessage(msg);

		// Check whether msg is a CoapRequest --> otherwise do nothing
		if (msg.isRequest())
			this.lastRequest = (CoapRequest) msg;
	}

	/**
	 * This function should be called to initiate any blockwise POST or PUT
	 * request. Adds the context for the blockwise transaction to the client
	 * channel.
	 * 
	 * @param maxBlocksize
	 *            Maximal BlockSize for sending
	 * @param request
	 *            The CoapRequest-Object, that was obtained through
	 *            ClientChannel.createRequest() function. The request must
	 *            already contain the payload!
	 * 
	 * @return The first request that should be send via
	 *         ClientChannel.sendMessage() function. The requests for the
	 *         following Blocks are handled automatically
	 * 
	 */
	public BasicCoapRequest addBlockContext(CoapRequest request) {

		if (request.getRequestCode() == CoapRequestCode.POST || request.getRequestCode() == CoapRequestCode.PUT) {
			CoapBlockOption block1 = request.getBlock1();
			CoapBlockSize bSize = this.getMaxSendBlocksize();

			if (block1 != null && block1.getBlockSize().getSize() < this.getMaxSendBlocksize().getSize())
				bSize = block1.getBlockSize();

			this.blockContext = new ClientBlockContext(bSize, request.getPayload());

			BasicCoapRequest firstRequest = createRequest(request.isReliable(), request.getRequestCode());
			firstRequest.copyHeaderOptions((BasicCoapRequest) request);
			firstRequest.setToken(request.getToken());

			if (request.getPayloadLength() <= bSize.getSize())
				block1 = new CoapBlockOption(0, false, bSize);
			else
				block1 = new CoapBlockOption(0, true, bSize);

			firstRequest.setBlock1(block1);
			firstRequest.setPayload(this.blockContext.getNextPayload(block1));
			this.blockContext.setFirstRequest(firstRequest);
			return firstRequest;
		}
		System.err.println("ERROR: Tried to manually add BlockContext to GET request!");
		return (BasicCoapRequest) request;
	}

	private class ClientBlockContext {

		private ByteArrayOutputStream incomingStream;
		private ByteArrayInputStream outgoingStream;
		private boolean finished = false;
		private boolean sending = false; // false=receiving; true=sending
		private CoapBlockSize blockSize; // null means no block option
		private int blockNumber;
		private int maxBlockNumber;
		private CoapRequest request;
		
		/**
		 * Create BlockContext for GET requests. This is done automatically, if
		 * the sent GET request or the obtained response contain a
		 * Block2-Option.
		 * 
		 * @param blockOption
		 *            The CoapBlockOption object, that contains the block size
		 *            indicated by the server
		 * @param maxBlocksize
		 *            Indicates the maximum block size supported by the client
		 */
		public ClientBlockContext(CoapBlockOption blockOption, CoapBlockSize maxBlocksize) {

			this.incomingStream = new ByteArrayOutputStream();
			this.outgoingStream = null;

			/* determine the right blocksize (min of remote and max) */
			if (maxBlocksize == null) {
				this.blockSize = blockOption.getBlockSize();
			} else {
				int max = maxBlocksize.getSize();
				int remote = blockOption.getBlockSize().getSize();
				if (remote < max) {
					this.blockSize = blockOption.getBlockSize();
				} else {
					this.blockSize = maxBlocksize;
				}
			}
			this.blockNumber = blockOption.getNumber();
			this.sending = false;
		}

		/**
		 * Create BlockContext for POST or PUT requests. Is only called by
		 * addBlockContext().
		 * 
		 * @param maxBlocksize
		 *            Indicates the block size for the transaction
		 * @param payload
		 *            The whole payload, that should be transferred
		 */
		public ClientBlockContext(CoapBlockSize maxBlocksize, byte[] payload) {
			this.outgoingStream = new ByteArrayInputStream(payload);
			this.incomingStream = null;
			this.blockSize = maxBlocksize;

			this.blockNumber = 0;
			this.maxBlockNumber = payload.length / this.blockSize.getSize() - 1;
			if (payload.length % this.blockSize.getSize() > 0)
				this.maxBlockNumber++;

			this.sending = true;
		}

		public byte[] getPayload() {

			if (!this.sending) {
				return this.incomingStream.toByteArray();
			} else if (this.outgoingStream != null) {
				byte[] payload = new byte[this.outgoingStream.available()];
				this.outgoingStream.read(payload, 0, this.outgoingStream.available());
				return payload;
			} else
				return null;
		}

		/**
		 * Adds the new obtained data block to the complete payload, in the case
		 * of blockwise GET requests.
		 * 
		 * @param msg
		 *            The received CoAP message
		 * @param block
		 *            The block option of the CoAP message, indicating which
		 *            block of data was received and whether there are more to
		 *            follow.
		 * @return Indicates whether the operation was successful.
		 */
		public boolean addBlock(CoapMessage msg, CoapBlockOption block) {
			int number = block.getNumber();
			if (number > this.blockNumber)
				return false;

			this.blockNumber++;
			try {
				this.incomingStream.write(msg.getPayload());
			} catch (IOException e) {
	
			}
			if (block.isLast()) {
				this.finished = true;
			}

			return true;
		}

		/**
		 * Retrieve the next block option, indicating the requested (GET) or
		 * send (POST or PUT) block
		 * 
		 * @return BlockOption to indicate the next block, that should be send
		 *         (POST or PUT) or received (GET)
		 */
		public CoapBlockOption getNextBlock() {
			if (!this.sending) {
				this.blockNumber++; // ignore the rest (no rest should be there)
				return new CoapBlockOption(this.blockNumber, false, this.blockSize);
			}
			this.blockNumber++;
			if (this.blockNumber == this.maxBlockNumber)
				return new CoapBlockOption(this.blockNumber, false, this.blockSize);
			return new CoapBlockOption(this.blockNumber, true, this.blockSize);
		}

		/**
		 * Get the next block of payload, that should be send in a POST or PUT
		 * request
		 * 
		 * @param block
		 *            Indicates which block of data should be send next.
		 * @return The next part of the payload
		 */
		public byte[] getNextPayload(CoapBlockOption block) {
			int number = block.getNumber();
			byte[] payloadBlock;

			if (number == this.maxBlockNumber) {
				payloadBlock = new byte[this.outgoingStream.available()];
				this.outgoingStream.read(payloadBlock, 0, this.outgoingStream.available());
				this.finished = true;
			} else {
				payloadBlock = new byte[block.getBlockSize().getSize()];
				this.outgoingStream.read(payloadBlock, 0, block.getBlockSize().getSize());
			}

			return payloadBlock;
		}

		public boolean isFinished() {
			return this.finished;
		}

		public CoapRequest getFirstRequest() {
			return this.request;
		}

		public void setFirstRequest(CoapRequest request) {
			this.request = request;
		}
	}

	@Override
	public void setTrigger(Object o) {
		this.trigger = o;

	}

	@Override
	public Object getTrigger() {
		return this.trigger;
	}

}
