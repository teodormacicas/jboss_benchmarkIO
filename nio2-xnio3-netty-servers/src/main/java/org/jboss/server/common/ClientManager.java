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
import org.jboss.server.nio2.common.Nio2Utils;

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
        protected ByteBuffer headerBuffer;
	protected T channel;
	protected boolean intialized;

	/**
	 * Create a new instance of {@code ClientManager}
	 */
	public ClientManager(T channel) {
		this.channel = channel;
                this.readBuffer = ByteBuffer.allocateDirect(512);
	}

	/**
	 * Initialize the client manager
	 */
	public void init(String filename) {
                // here read the data into buffer
		this.writeBuffers = FileLoader.cloneData(filename);
		this.fileLength = FileLoader.getFileLength();
                initHeaderBuffer();
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
	 * Initialize the buffer containing the HTTP response header
	 * 
	 */
        protected void initHeaderBuffer() {
            StringBuffer tmp = new StringBuffer();
            tmp.append("HTTP/1.1 200 OK\n");
            // this is important as based on the content-length the client knows how much to read
            tmp.append("Content-Length: "+this.fileLength+"\n");
            // an empty line separes the header to the content
            tmp.append("Content-Type: application/octet-stream\n");
            tmp.append(Nio2Utils.CRLF);
            this.headerBuffer = ByteBuffer.allocate(tmp.toString().getBytes().length);
            this.headerBuffer.put(tmp.toString().getBytes());
        }
        
	/**
	 * 
	 * @param channel
	 * @throws Exception
	 *             void
	 */
	protected void writeResponse(T channel) throws Exception {
		try {
                        // Write the HTTP header
                        write(channel, headerBuffer);
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
