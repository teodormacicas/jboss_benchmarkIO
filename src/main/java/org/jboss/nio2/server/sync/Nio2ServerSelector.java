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
package org.jboss.nio2.server.sync;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.nio2.server.SessionGenerator;

/**
 * {@code Nio2ServerSelector}
 * <p/>
 * 
 * Created on Oct 27, 2011 at 11:42:12 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Nio2ServerSelector {

	protected static final int SERVER_PORT = 8080;
	protected static final ConcurrentMap<String, String> CONNECTIONS = new ConcurrentHashMap<String, String>();
	private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(50);
	private static Selector selector;

	/**
	 * Create a new instance of {@code Nio2ServerSelector}
	 */
	public Nio2ServerSelector() {
		super();
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		int port = SERVER_PORT;
		if (args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("ERROR: " + e.getMessage());
			}
		}

		System.out.println("Open the channel selector...");
		// Open the selector
		selector = Selector.open();
		System.out.println("Starting NIO2 Synchronous Sever on port " + port + " ...");
		final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open().bind(
				new InetSocketAddress(port));
		// Create a separate thread for the listen server channel
		Thread server = new ThreadServer(serverSocketChannel);
		// Starting the server thread
		server.start();
		System.out.println("Server started successfully...");
		int count = -1;
		while (true) {
			try {
				// Wait for an event
				count = selector.select(20);
			} catch (Exception e) {
				// Handle error with selector
				System.err.println("ERROR: " + e.getMessage());
				break;
			}

			if (count > 0) {
				// Get list of selection keys with pending events
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				// Process each key at a time
				while (it.hasNext()) {
					// Get the selection key
					SelectionKey selKey = (SelectionKey) it.next();
					// Remove it from the list to indicate that it is being
					// processed
					it.remove();

					try {
						processSelectionKey(selKey);
					} catch (Exception e) {
						// Handle error with channel and unregister
						selKey.cancel();
						System.err.println("Process selection key exception -> " + e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}
		server.join();
		System.out.println("Server shutdown");
	}

	/**
	 * 
	 * @param selKey
	 * @throws Exception
	 */
	public static void processSelectionKey(SelectionKey selKey) throws Exception {

		if (selKey.isValid()) {
			SocketChannel channel = (SocketChannel) selKey.channel();

			if (channel.isOpen() && channel.isConnected()) {
				InetSocketAddress socketAddress = (InetSocketAddress) channel.getRemoteAddress();
				String ip_port = socketAddress.getHostName() + ":" + socketAddress.getPort();
				// retrieve the session ID
				String sessionId = CONNECTIONS.get(ip_port);
				Nio2SelectorClientManager manager = new Nio2SelectorClientManager(channel);
				manager.setSessionId(sessionId);

				/*
				 * Due to the inherent delay between key cancellation and
				 * channel deregistration, a channel may remain registered for
				 * some time after all of its keys have been cancelled. A
				 * channel may also remain registered for some time after it is
				 * closed.
				 */
				if (sessionId != null) {
					// Execute the client query
					THREAD_POOL.execute(manager);
				}
			} else {
				System.out.println("The channel of the selected key is not connected");
				selKey.cancel();
			}
		}
	}

	/**
	 * 
	 * @param host_port
	 */
	public static void removeConnection(String host_port) {
		CONNECTIONS.remove(host_port);
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
	 * {@code ThreadServer}
	 * 
	 * Created on Nov 5, 2011 at 12:52:28 PM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	private static class ThreadServer extends Thread {

		private ServerSocketChannel serverSocketChannel;

		/**
		 * Create a new instance of {@code ThreadServer}
		 * 
		 * @param serverSocketChannel
		 */
		public ThreadServer(ServerSocketChannel serverSocketChannel) {
			this.serverSocketChannel = serverSocketChannel;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			boolean running = true;
			int number = 0;
			while (running) {
				try {
					System.out.println("Waiting for new connection");
					SocketChannel channel = this.serverSocketChannel.accept();
					if (channel != null) {
						System.out.println("New connection received -> " + (++number));
						String sessionId = generateId();
						InetSocketAddress socketAddress = (InetSocketAddress) channel
								.getRemoteAddress();
						String hostname_port = socketAddress.getHostName() + ":"
								+ socketAddress.getPort();
						CONNECTIONS.put(hostname_port, sessionId);
						channel.configureBlocking(false);
						System.out
								.println("Registering the new connection [" + hostname_port + "]");
						channel.register(selector, SelectionKey.OP_READ);
						System.out.println("The new connection [" + hostname_port
								+ "] has been registered");
					}
				} catch (Exception exp) {
					System.err.println("ERROR: " + exp.getMessage());
					running = false;
				}
			}
			System.out.println("The server thread is finished...");
		}
	}

}
