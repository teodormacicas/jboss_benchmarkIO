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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;

import org.jboss.server.nio2.NioServer;
import org.jboss.server.nio2.common.Nio2Utils;

/**
 * {@code NioAsyncServer}
 * 
 * Created on Oct 27, 2011 at 5:47:30 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class AsyncServer extends NioServer {

	/**
	 * Create a new instance of {@code AsyncServer}
	 */
	public AsyncServer(int port) {
		super(port);
		this.async = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.server.nio2.common.NioServer#processChannel(java.nio.channels
	 * .AsynchronousSocketChannel)
	 */
	public void processChannel(final AsynchronousSocketChannel channel) throws Exception {

		channel.setOption(StandardSocketOptions.SO_SNDBUF, Nio2Utils.SO_SNDBUF);
		final String sessionId = generateSessionId();
		final ByteBuffer buffer = ByteBuffer.allocate(512);
		channel.read(buffer, channel, new CompletionHandler<Integer, AsynchronousSocketChannel>() {

			@Override
			public void completed(Integer nBytes, AsynchronousSocketChannel attachment) {
				if (nBytes < 0) {
					failed(new ClosedChannelException(), attachment);
					return;
				}
				if (nBytes > 0) {
					byte bytes[] = new byte[nBytes];
					buffer.get(bytes);
					System.out.println("[" + sessionId + "] " + new String(bytes).trim());
					String response = "jSessionId: " + sessionId + CRLF;
					// write initialization response to client
					buffer.clear();
					buffer.put(response.getBytes()).flip();
					attachment.write(buffer, attachment,
							new CompletionHandler<Integer, AsynchronousSocketChannel>() {

								@Override
								public void completed(Integer nBytes,
										AsynchronousSocketChannel attachment) {
									if (nBytes < 0) {
										failed(new ClosedChannelException(), attachment);
										return;
									}
									if (nBytes > 0) {
										channel.read(buffer, Nio2Utils.TIMEOUT,
												Nio2Utils.TIME_UNIT, channel,
												new ReadCompletionHandler(sessionId, buffer));
									}
								}

								@Override
								public void failed(Throwable exc,
										AsynchronousSocketChannel attachment) {
									exc.printStackTrace();
									try {
										attachment.close();
									} catch (IOException e) {
										// NOPE
									}
								}
							});
				}
			}

			@Override
			public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
				exc.printStackTrace();
				try {
					attachment.close();
				} catch (IOException e) {
					// NOPE
				}
			}
		});

	}
}
