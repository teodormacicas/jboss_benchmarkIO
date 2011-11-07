/*
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
package org.jboss.nio2.server.sync;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * {@code Nio2SelectorClientManager}
 * 
 * Created on Nov 4, 2011 at 4:03:27 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Nio2SelectorClientManager implements Runnable {

	private SocketChannel channel;
	private String sessionId;
	private ByteBuffer byteBuffer = ByteBuffer.allocate(512);

	/**
	 * Create a new instance of {@code Nio2SelectorClientManager}
	 * 
	 * @param channel
	 */
	public Nio2SelectorClientManager(SocketChannel channel) {
		this.channel = channel;
	}

	@Override
	public void run() {

		int count = -1;
		try {
			if (channel.isOpen() && channel.isConnected()) {
				count = channel.read(byteBuffer);
				byteBuffer.flip();
				byte bytes[] = new byte[count];
				byteBuffer.get(bytes);
				String request = new String(bytes);
				if (!request.matches("\\s*")) {
					System.out.println("[" + this.sessionId + "] -> " + request);
					String response = null;
					if (request.startsWith("POST")) {
						response = "jSessionId: " + this.sessionId + "\n";
					} else {
						response = "[" + this.sessionId + "] Pong from server\n";
					}
					
					byteBuffer.clear();
					byteBuffer.put(response.getBytes());
					byteBuffer.flip();
					channel.write(byteBuffer);
					byteBuffer.clear();
				}
			}
		} catch (Exception exp) {
			System.err.println("ERROR from client side -> " + exp.getMessage());
			try {
				System.out.println("Closing remote connection");
				Nio2ServerSelector.removeConnection(channel);
				SelectionKey key = channel.keyFor(Nio2ServerSelector.selector);
				if (key != null) {
					key.cancel();
				}
				this.channel.close();
				this.channel = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
