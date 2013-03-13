/**
 * JBoss, Home of Professional Open Source. Copyright 2011, Red Hat, Inc., and
 * individual
 * contributors as indicated by the @author tags. See the copyright.txt file in
 * the distribution
 * for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either
 * version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this
 * software; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor,
 * Boston, MA 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.server.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;

/**
 * {@code ClientManager}
 * 
 * Created on Oct 27, 2012 at 10:07:55 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public abstract class ClientManager<T extends Channel> implements Runnable {

	protected String sessionId;
	protected long fileLength;
	protected ByteBuffer readBuffer;
	protected ByteBuffer writeBuffers[];
	protected T channel;
	protected boolean intialized;

	/**
	 * Create a new instance of {@code ClientManager}
	 */
	public ClientManager(T channel) {
		this.channel = channel;
	}

	/**
	 * Initialize the client manager
	 */
	public void init() {
		this.readBuffer = ByteBuffer.allocateDirect(512);
		this.writeBuffers = FileLoader.cloneData();
		this.fileLength = FileLoader.getFileLength();
		this.intialized = true;
	}

	/**
	 * Close the channel
	 */
	public void close() {
		try {
			this.channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Flip all the write byte buffers
	 * 
	 * @param buffers
	 */
	protected static void flipAll(ByteBuffer[] buffers) {
		for (ByteBuffer bb : buffers) {
			bb.flip();
		}
	}

	/**
	 * 
	 * @param channel
	 * @throws Exception
	 *             void
	 */
	protected void writeResponse(T channel) throws Exception {
		try {
			// Write the file content to the channel
			write(channel, writeBuffers, fileLength);
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	/**
	 * 
	 * @param channel
	 * @param buffer
	 * @throws IOException
	 */
	protected abstract void write(T channel, ByteBuffer byteBuffer) throws Exception;

	/**
	 * Write the response to the client
	 * 
	 * @param channel
	 * @param buffers
	 * @param total
	 * @throws Exception
	 */
	public abstract void write(final T channel, final ByteBuffer[] buffers, long total)
			throws Exception;

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
