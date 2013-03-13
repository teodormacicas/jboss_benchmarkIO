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
package org.jboss.server.nio2.async;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.jboss.logging.Logger;
import org.jboss.server.nio2.common.Nio2Utils;

/**
 * {@code WriteCompletionHandler}
 * 
 * Created on Nov 17, 2011 at 9:33:12 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
class WriteCompletionHandler implements CompletionHandler<Long, Long> {

	private static final Logger logger = Logger.getLogger(CompletionHandler.class.getName());
	private int offset = 0;
	private long written = 0;
	private AsynchronousSocketChannel channel;
	private String sessionId;
	private ByteBuffer buffers[];

	/**
	 * Create a new instance of {@code WriteCompletionHandler}
	 * 
	 * @param channel
	 * @param sessionId
	 */
	public WriteCompletionHandler(AsynchronousSocketChannel channel, String sessionId) {
		this.channel = channel;
		this.sessionId = sessionId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.CompletionHandler#completed(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public void completed(Long nBytes, Long total) {
		System.out.println("[" + sessionId + "] Number of bytes written: " + nBytes
				+ " from total: " + total);

		try {
			int socketBufferSize = channel.getOption(StandardSocketOptions.SO_SNDBUF);
			System.out.println("SO_SNDBUF = " + socketBufferSize);
		} catch (IOException e) {
			e.printStackTrace();
		}

		written += nBytes;
		if (written < total) {
			offset = (int) (written / buffers[0].capacity());
			channel.write(buffers, offset, buffers.length - offset, Nio2Utils.TIMEOUT,
					Nio2Utils.TIME_UNIT, total, this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.CompletionHandler#failed(java.lang.Throwable,
	 * java.lang.Object)
	 */
	@Override
	public void failed(Throwable exc, Long total) {
		logger.error("WRITE OPERATION FAILED : " + exc.getMessage(), exc);
		exc.printStackTrace();
	}

	/**
     *
     */
	protected void reset() {
		this.offset = 0;
		this.written = 0;
		this.buffers = null;
	}

	/**
	 * Setter for the buffers
	 * 
	 * @param buffers
	 *            the buffers to set
	 */
	public void setBuffers(ByteBuffer[] buffers) {
		this.buffers = buffers;
	}
}
