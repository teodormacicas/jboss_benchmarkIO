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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code NioAsyncServer}
 * 
 * Created on Oct 27, 2011 at 5:47:30 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Nio2AsyncServer {

	public static final int SERVER_PORT = 8081;
	private static final Logger logger = Logger.getLogger(Nio2AsyncServer.class.getName());

	/**
	 * Create a new instance of {@code Nio2AsyncServer}
	 */
	public Nio2AsyncServer() {
		super();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {

		int port = SERVER_PORT;
		if (args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}

		logger.log(Level.INFO, "Starting NIO2 Synchronous Sever on port {0} ...", port);
		ExecutorService pool = Executors.newFixedThreadPool(400);
		AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withThreadPool(pool);
		final AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel.open(
				threadGroup).bind(new InetSocketAddress(port));

		boolean running = true;
		logger.log(Level.INFO, "Asynchronous Sever started...");

		while (running) {
			logger.info("Waiting for new connections...");
			// listener.accept(null, new CompletionHandlerImpl());

			Future<AsynchronousSocketChannel> future = listener.accept();
			final AsynchronousSocketChannel channel = future.get();
			final ByteBuffer buffer = ByteBuffer.allocate(512);
			channel.read(buffer, null, new CompletionHandler<Integer, Void>() {

				boolean initialized = false;
				private String response;
				private String sessionId;

				@Override
				public void completed(Integer nBytes, Void attachment) {
					if (nBytes > 0) {
						byte bytes[] = new byte[nBytes];
						buffer.get(bytes);
						System.out.println("[" + this.sessionId + "] " + new String(bytes));

						if (!initialized) {
							this.sessionId = generateId();
							initialized = true;
							response = "jSessionId: " + sessionId + "\n";
						} else {
							response = "[" + this.sessionId + "] Pong from server\n";
						}

						buffer.clear();
						buffer.put(response.getBytes());
						buffer.flip();
						channel.write(buffer);
						buffer.clear();
					}
				}

				@Override
				public void failed(Throwable exc, Void attachment) {
					try {
						channel.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

			/*
			 * Future<AsynchronousSocketChannel> future = listener.accept();
			 * Nio2AsyncClientManager manager = new
			 * Nio2AsyncClientManager(future.get());
			 * manager.setSessionId(SessionGenerator.generateId());
			 * pool.execute(manager);
			 */
		}

		listener.close();
	}

	/**
	 * 
	 * @return
	 */
	public static String generateId() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	/**
	 * {@code CompletionHandlerImpl}
	 * 
	 * Created on Oct 28, 2011 at 4:09:21 PM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	protected static class CompletionHandlerImpl implements
			CompletionHandler<AsynchronousSocketChannel, Void> {
		private final ByteBuffer buffer = ByteBuffer.allocate(512);

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.nio.channels.CompletionHandler#completed(java.lang.Object,
		 * java.lang.Object)
		 */
		@Override
		public void completed(final AsynchronousSocketChannel channel, Void attachment) {

			buffer.clear();
			channel.read(buffer, null, new CompletionHandler<Integer, Void>() {
				boolean initialized = false;
				private String response;
				private String sessionId;

				@Override
				public void completed(Integer nBytes, Void attachment) {
					if (nBytes > 0) {
						byte bytes[] = new byte[nBytes];
						buffer.get(bytes);
						System.out.println("[" + this.sessionId + "] " + new String(bytes));

						if (!initialized) {
							this.sessionId = generateId();
							initialized = true;
							response = "jSessionId: " + sessionId + "\n";
						} else {
							response = "[" + this.sessionId + "] Pong from server\n";
						}

						buffer.clear();
						buffer.put(response.getBytes());
						buffer.flip();
						channel.write(buffer);
						buffer.clear();
					}
				}

				@Override
				public void failed(Throwable exc, Void attachment) {
					try {
						channel.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.nio.channels.CompletionHandler#failed(java.lang.Throwable,
		 * java.lang.Object)
		 */
		@Override
		public void failed(Throwable exc, Void attachment) {
			System.err.println(exc.getMessage());
		}
	}
}
