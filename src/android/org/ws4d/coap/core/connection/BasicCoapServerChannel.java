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

import org.ws4d.coap.core.CoapServer;
import org.ws4d.coap.core.connection.api.CoapChannel;
import org.ws4d.coap.core.connection.api.CoapServerChannel;
import org.ws4d.coap.core.connection.api.CoapSocketHandler;
import org.ws4d.coap.core.enumerations.CoapBlockSize;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.enumerations.CoapPacketType;
import org.ws4d.coap.core.enumerations.CoapRequestCode;
import org.ws4d.coap.core.enumerations.CoapResponseCode;
import org.ws4d.coap.core.messages.BasicCoapRequest;
import org.ws4d.coap.core.messages.BasicCoapResponse;
import org.ws4d.coap.core.messages.CoapBlockOption;
import org.ws4d.coap.core.messages.CoapEmptyMessage;
import org.ws4d.coap.core.messages.api.CoapMessage;
import org.ws4d.coap.core.messages.api.CoapRequest;
import org.ws4d.coap.core.messages.api.CoapResponse;

/**
 * @author Bjoern Konieczek <bjoern.konieczek@uni-rostock.de>
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */

public class BasicCoapServerChannel extends BasicCoapChannel implements CoapServerChannel {
	private CoapServer server = null;
	private CoapRequest lastRequest;
	private ServerBlockContext blockContext = null;

	public BasicCoapServerChannel(CoapSocketHandler socketHandler, CoapServer server, InetAddress remoteAddress,
			int remotePort) {
		super(socketHandler, remoteAddress, remotePort);
		this.server = server;
	}

	@Override
	public void close() {
		this.getSocketHandler().removeServerChannel(this);
	}

	@Override
	public void handleMessage(CoapMessage message) {
		/* message MUST be a request */
		if (message.getPacketType() == CoapPacketType.RST) {
			this.server.onReset(this.lastRequest);
			// TODO Notify Server to handle reset messages (Reset ongoing blockwise transfer --> delete BlockContext)
			// this.blockContext = null;
			return;
		}
		if (message.isEmpty()) {
			return;
		}

		if (!message.isRequest()) {
			return;
			// throw new IllegalStateException("Incomming server message is not
			// a request");
		}

		BasicCoapRequest request = (BasicCoapRequest) message;
		this.lastRequest = request;
		CoapBlockOption block1 = request.getBlock1();

		if (this.blockContext == null && block1 != null) {
			this.blockContext = new ServerBlockContext(block1, this.getMaxReceiveBlocksize());
			this.blockContext.setFirstRequest(request);
		}

		if (this.blockContext != null) {
			if (!this.blockContext.isFinished()) {
				BasicCoapResponse response = null;
				if ((this.blockContext.getFirstRequest().getRequestCode() == CoapRequestCode.PUT
						|| this.blockContext.getFirstRequest().getRequestCode() == CoapRequestCode.POST)
						&& request.getRequestCode() != CoapRequestCode.GET) {
					this.blockContext.addBlock(request, block1);
					if (!this.blockContext.isFinished()) {
						response = createResponse(request, CoapResponseCode.Continue_231);
						response.setBlock1(block1);
						sendMessage(response);
						return;
					}
				} else if (this.blockContext.getFirstRequest().getRequestCode() == CoapRequestCode.GET
						&& request.getRequestCode() == CoapRequestCode.GET) {
					CoapBlockOption newBlock = this.blockContext.getNextBlock();
					response = createResponse(request, CoapResponseCode.Content_205);
					response.setBlock2(newBlock);
					response.setPayload(this.blockContext.getNextPayload(newBlock));
					// System.out.println("Sending Block Number: " +
					// newBlock.getNumber()+"; Payload: " + new
					// String(response.getPayload()) );
					sendMessage(response);
					if (this.blockContext.isFinished()) {
						this.blockContext = null;
					}
					return;
				}
			}
		}

		if (this.blockContext == null || (this.blockContext.getFirstRequest().getRequestCode() != CoapRequestCode.GET
				&& this.blockContext.isFinished())) {
			CoapChannel channel = request.getChannel();
			if (this.blockContext != null) {
				request.setPayload(this.blockContext.getPayload());
				this.blockContext = null;
			}
			this.server.onRequest((CoapServerChannel) channel, request);
		}
	}

	@Override
	public void handleMCResponse(CoapMessage message, InetAddress srcAddress, int srcPort) {
		System.err.println("ERROR: Received a response on a Server");
	}

	public void lostConnection(boolean notReachable, boolean resetByServer) {
		this.server.onSeparateResponseFailed(this);
	}

	@Override
	public BasicCoapResponse createResponse(CoapMessage request, CoapResponseCode responseCode) {
		return createResponse(request, responseCode, null);
	}

	@Override
	public BasicCoapResponse createResponse(CoapMessage request, CoapResponseCode responseCode,
			CoapMediaType contentType) {
		BasicCoapResponse response;
		if (request.getPacketType() == CoapPacketType.CON) {
			response = new BasicCoapResponse(CoapPacketType.ACK, responseCode, request.getMessageID(),
					request.getToken());
		} else if (request.getPacketType() == CoapPacketType.NON) {
			response = new BasicCoapResponse(CoapPacketType.NON, responseCode, request.getMessageID(),
					request.getToken());
		} else {
			throw new IllegalStateException("Create Response failed, Request is neither a CON nor a NON packet");
		}
		if (contentType != null && contentType != CoapMediaType.UNKNOWN) {
			response.setContentType(contentType);
		}

		response.setChannel(this);
		return response;
	}

	@Override
	public CoapResponse createSeparateResponse(CoapRequest request, CoapResponseCode responseCode) {

		BasicCoapResponse response = null;
		if (request.getPacketType() == CoapPacketType.CON) {
			/*
			 * The separate Response is CON (normally a Response is ACK or NON)
			 */
			response = new BasicCoapResponse(CoapPacketType.CON, responseCode, this.getSocketHandler().getChannelManager().getNewMessageID(),
					request.getToken());
			/* send ack immediately */
			sendMessage(new CoapEmptyMessage(CoapPacketType.ACK, request.getMessageID()));
		} else if (request.getPacketType() == CoapPacketType.NON) {
			/* Just a normal response */
			response = new BasicCoapResponse(CoapPacketType.NON, responseCode, request.getMessageID(),
					request.getToken());
		} else {
			throw new IllegalStateException("Create Response failed, Request is neither a CON nor a NON packet");
		}

		response.setChannel(this);
		return response;
	}

	@Override
	public void sendSeparateResponse(CoapResponse response) {
		this.sendMessage(response);
	}

	@Override
	public CoapResponse createNotification(CoapRequest request, CoapResponseCode responseCode, int sequenceNumber) {
		/* use the packet type of the request: if con than con otherwise non */
		if (request.getPacketType() == CoapPacketType.CON) {
			return createNotification(request, responseCode, sequenceNumber, true);
		}
		return createNotification(request, responseCode, sequenceNumber, false);
	}

	@Override
	public CoapResponse createNotification(CoapRequest request, CoapResponseCode responseCode, int sequenceNumber,
			boolean reliable) {
		BasicCoapResponse response = null;
		CoapPacketType packetType;
		if (reliable) {
			packetType = CoapPacketType.CON;
		} else {
			packetType = CoapPacketType.NON;
		}

		response = new BasicCoapResponse(packetType, responseCode, this.getSocketHandler().getChannelManager().getNewMessageID(),
				request.getToken());
		response.setChannel(this);
		response.setObserveOption(sequenceNumber);
		return response;
	}

	@Override
	public void sendNotification(CoapResponse response) {
		this.sendMessage(response);
	}

	@Override
	public void sendMessage(CoapMessage msg) {
		super.sendMessage(msg);
	}

	public CoapResponse addBlockContext(CoapRequest request, byte[] payload) {
		CoapBlockSize bSize = request.getBlock2().getBlockSize();
		BasicCoapResponse response = this.createResponse(request, CoapResponseCode.Content_205);
		if (this.getMaxSendBlocksize() != null && bSize.compareTo(this.getMaxSendBlocksize()) > 0) {
			bSize = this.getMaxSendBlocksize();
		}

		if (bSize.getSize() >= payload.length) {
			response.setPayload(payload);
		} else {
			this.blockContext = new ServerBlockContext(bSize, payload);
			this.blockContext.setFirstRequest(request);
			CoapBlockOption block2 = new CoapBlockOption(0, true, bSize);
			response.copyHeaderOptions((BasicCoapRequest) request);
			response.setBlock2(block2);
			response.setBlock2(block2);
			response.setPayload(this.blockContext.getNextPayload(block2));
		}
		return response;
	}

	private class ServerBlockContext {

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
		public ServerBlockContext(CoapBlockOption blockOption, CoapBlockSize maxBlocksize) {
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
		public ServerBlockContext(CoapBlockSize maxBlocksize, byte[] payload) {
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
				try {
					this.incomingStream.close();
				} catch (IOException e) {
				}
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
				payloadBlock = new byte[block.getBlockSize().getSize()];
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

}
