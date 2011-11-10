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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
	private static final long TIMEOUT = 20;
	private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
	private static final ExecutorService pool = Executors.newFixedThreadPool(400);

	/**
	 * Create a new instance of {@code Nio2AsyncServer}
	 */
	public Nio2AsyncServer() {
		super();
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
		AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withThreadPool(pool);
		final AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel.open(
				threadGroup).bind(new InetSocketAddress(port));

		boolean running = true;
		logger.log(Level.INFO, "Asynchronous Sever started...");

		while (running) {
			logger.info("Waiting for new connections...");
			Future<AsynchronousSocketChannel> future = listener.accept();
			final AsynchronousSocketChannel channel = future.get();

			final ByteBuffer buffer = ByteBuffer.allocate(512);
			channel.read(buffer, channel, new CompletionHandlerImpl(buffer));
			/*
			 * final ByteBuffer buffer = ByteBuffer.allocate(512); final
			 * CompletionHandlerImpl handler = new
			 * CompletionHandlerImpl(buffer); final RequestManager manager = new
			 * RequestManager(channel, buffer, handler);
			 */

			/*
			 * Nio2AsyncClientManager manager = new
			 * Nio2AsyncClientManager(channel);
			 * manager.setSessionId(generateId()); pool.execute(manager);
			 */
		}

		listener.close();
	}

	/**
	 * {@code CompletionHandlerImpl}
	 * 
	 * Created on Nov 9, 2011 at 9:08:18 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	private static class CompletionHandlerImpl implements
			CompletionHandler<Integer, AsynchronousSocketChannel> {

		boolean initialized = false;
		private String response;
		private String sessionId;
		private final ByteBuffer buffer;
		private RequestManager manager;

		/**
		 * Create a new instance of {@code CompletionHandlerImpl}
		 */
		public CompletionHandlerImpl(ByteBuffer buffer) {
			this.buffer = buffer;
		}

		/**
		 * Create a new instance of {@code CompletionHandlerImpl}
		 * 
		 * @param buffer
		 * @param manager
		 */
		public CompletionHandlerImpl(ByteBuffer buffer, RequestManager manager) {
			this.buffer = buffer;
			this.manager = manager;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.nio.channels.CompletionHandler#completed(java.lang.Object,
		 * java.lang.Object)
		 */
		@Override
		public void completed(Integer nBytes, AsynchronousSocketChannel channel) {
			if (nBytes > 0) {
				byte bytes[] = new byte[nBytes];
				buffer.flip();
				buffer.get(bytes);

				if (!initialized) {
					this.sessionId = generateId();
					this.initialized = true;
					response = "JSESSION_ID: " + sessionId + "\n";
				} else {
					response = "[" + this.sessionId + "] Pong from server\n";
				}
				System.out.println("[" + this.sessionId + "] " + new String(bytes).trim());
				buffer.clear();
				buffer.put(response.getBytes());
				buffer.flip();
				channel.write(buffer);
				buffer.clear();

				/*
				 * if (this.manager == null) { this.manager = new
				 * RequestManager(channel, buffer, this); }
				 * 
				 * pool.execute(manager); return;
				 */
			}
			// Read again with the this CompletionHandler
			channel.read(buffer, TIMEOUT, TIME_UNIT, channel, this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.nio.channels.CompletionHandler#failed(java.lang.Throwable,
		 * java.lang.Object)
		 */
		@Override
		public void failed(Throwable exc, AsynchronousSocketChannel channel) {
			System.out.println("[" + this.sessionId + "] Operation failed");
			exc.printStackTrace();
			try {
				System.out.println("[" + this.sessionId + "] Closing remote connection");
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 
		 * @param initialized
		 */
		public void setInitialized(boolean initialized) {
			this.initialized = initialized;
		}
	}

	/**
	 * {@code RequestManager}
	 * 
	 * Created on Nov 9, 2011 at 1:12:42 PM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	private static class RequestManager implements Runnable {

		private AsynchronousSocketChannel channel;
		private ByteBuffer buffer;
		private CompletionHandlerImpl handler;

		/**
		 * Create a new instance of {@code RequestManager}
		 */
		public RequestManager() {
			super();
		}

		/**
		 * Create a new instance of {@code RequestManager}
		 * 
		 * @param channel
		 * @param buffer
		 */
		public RequestManager(AsynchronousSocketChannel channel, ByteBuffer buffer,
				CompletionHandlerImpl handler) {
			this.channel = channel;
			this.buffer = buffer;
			this.handler = handler;
			this.handler.manager = this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {

			String response = null;
			while (true) {
				Future<Integer> count = channel.read(buffer);
				try {
					int x = count.get(1, TimeUnit.MILLISECONDS);
					if (x > 0) {
						buffer.flip();
						byte bytes[] = new byte[x];
						buffer.get(bytes);

						if (!this.handler.initialized) {
							this.handler.sessionId = generateId();
							response = "JSESSION_ID: " + this.handler.sessionId + "\n";
							this.handler.initialized = true;
						} else {
							response = "[" + this.handler.sessionId + "] Pong from server\n";
						}

						System.out.println("[" + this.handler.sessionId + "] "
								+ new String(bytes).trim());

						this.write(response);
					} else {
						throw new Exception("Connection closed remotely");
					}
				} catch (TimeoutException e) {
					if (count.cancel(false)) {
						System.out.println("Future canceled successfully");
						// Delegate the read operation to completion handler
						channel.read(buffer, TIMEOUT, TIME_UNIT, channel, handler);
						// channel.read(buffer, channel, this);
					}
					break;
				} catch (Exception exp) {
					exp.printStackTrace();
					break;
				}
			}
		}

		public void write(String data) {
			buffer.clear();
			buffer.put(data.getBytes());
			buffer.flip();
			channel.write(buffer);
			buffer.clear();
		}
	}
}
