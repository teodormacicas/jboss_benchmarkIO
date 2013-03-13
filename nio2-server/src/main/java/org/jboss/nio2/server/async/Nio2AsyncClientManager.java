/**
 * JBoss, Home of Professional Open Source. Copyright 2011, Red Hat, Inc., and
 * individual contributors as indicated by the @author tags. See the
 * copyright.txt file in the distribution for a full listing of individual
 * contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.jboss.nio2.server.async;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * {@code NioAsyncClientManager}
 * 
 * Created on Oct 27, 2011 at 6:07:24 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Nio2AsyncClientManager implements Runnable {

	private static final Logger logger = Logger.getLogger(Nio2AsyncClientManager.class.getName());
	private AsynchronousSocketChannel channel;
	private String sessionId;

	/**
	 * Create a new instance of {@code Nio2AsyncClientManager}
	 * 
	 * @param socket
	 */
	public Nio2AsyncClientManager(AsynchronousSocketChannel socket) {
		this.channel = socket;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		ByteBuffer bb = ByteBuffer.allocate(1024);
		String response = null;
		try {
			// Initialization of the communication
			bb.clear();
			Future<Integer> future = channel.read(bb);
			bb.flip();
			byte bytes[] = new byte[future.get()];
			bb.get(bytes);
			System.out.println("[" + this.sessionId + "] " + new String(bytes).trim());
			response = "jSessionId: " + this.sessionId + "\n";
			// write initialization response to client
			this.write(bb, response);
			
			do {
				bb.clear();
				future = channel.read(bb);
				bb.flip();
				bytes = new byte[future.get()];
				bb.get(bytes);
				response = "[" + this.sessionId + "] Pong from server\n";
				System.out.println("[" + this.sessionId + "] " + new String(bytes).trim());
				// write response to client
				this.write(bb, response);
			} while (channel.isOpen());
			
		} catch (Exception exp) {
			logger.log(Level.SEVERE, "ERROR from client side");
		} finally {
			try {
				this.close();
			} catch (IOException ex) {
				logger.log(Level.SEVERE, "ERROR from server side", ex);
			}
		}
		logger.log(Level.INFO, "Client Manager shutdown");
	}

	/**
	 * 
	 * @param bb
	 * @param response
	 */
	private void write(ByteBuffer bb, String response) {
		bb.clear();
		bb.put(response.getBytes());
		bb.flip();
		channel.write(bb);
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		logger.log(Level.INFO, "Closing remote connection");
		this.channel.close();
	}

	/**
	 * Getter for sessionId
	 * 
	 * @return the sessionId
	 */
	public String getSessionId() {
		return this.sessionId;
	}

	/**
	 * Setter for the sessionId
	 * 
	 * @param sessionId
	 *            the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
