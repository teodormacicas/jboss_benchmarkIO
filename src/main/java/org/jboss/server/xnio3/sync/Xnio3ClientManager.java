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
package org.jboss.server.xnio3.sync;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jboss.server.common.ClientManager;
import org.xnio.channels.StreamChannel;

/**
 * {@code ReadChannelListener}
 * 
 * Created on Nov 22, 2011 at 4:44:01 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Xnio3ClientManager extends ClientManager<StreamChannel> {

	/**
	 * Create a new instance of {@code ClientManager}
	 * 
	 * @param channel
	 */
	public Xnio3ClientManager(StreamChannel channel) {
		super(channel);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if (!this.intialized) {
			this.init();
		}
		while (this.channel.isOpen()) {
			try {
				// Block until there is some data available to read
				channel.awaitReadable();
				int nBytes = channel.read(readBuffer);
				if (nBytes < 0) {
					// means that the connection was closed remotely
					channel.close();
					return;
				}

				if (nBytes > 0) {
					readBuffer.flip();
					byte bytes[] = new byte[nBytes];
					readBuffer.get(bytes).clear();
					writeResponse(channel);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.server.common.ClientManager#write(java.nio.channels.Channel,
	 * java.nio.ByteBuffer[], long)
	 */
	@Override
	public void write(final StreamChannel channel, final ByteBuffer[] buffers, long total)
			throws Exception {

		for (ByteBuffer bb : buffers) {
			write(channel, bb);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.server.common.ClientManager#write(java.nio.channels.Channel,
	 * java.nio.ByteBuffer)
	 */
	@Override
	protected void write(StreamChannel channel, ByteBuffer buffer) throws IOException {
		if (buffer.position() > 0) {
			buffer.flip();
		}

		while (buffer.hasRemaining()) {
			// Wait until the channel becomes writable again
			channel.awaitWritable();
			int x = channel.write(buffer);
			if (x < 0) {
				throw new IOException("The channel is closed");
			}
		}
	}

}
