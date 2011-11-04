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

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	private static final Logger logger = Logger.getLogger(Nio2ServerSelector.class.getName());
	protected static final int SERVER_PORT = 8080;
	protected static final ConcurrentMap<String, String> CONNECTIONS = new ConcurrentHashMap<String, String>();
	private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(400);
	private static Selector selector;// = Selector.open();

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
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}

		logger.info("Starting NIO2 Synchronous Sever on port " + port + " ...");
		final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open().bind(
				new InetSocketAddress(port));

		logger.info("Open the channel selector...");
		selector = Selector.open();

		// Create a separate thread for the listen server channel
		Thread serverThread = new Thread() {
			public void run() {
				boolean running = true;
				while (running) {
					try {
						logger.log(Level.INFO, "Waiting for new connection");
						SocketChannel channel = serverSocketChannel.accept();
						if (channel != null) {
							logger.log(Level.INFO, "New connection received");
							String sessionId = SessionGenerator.generateId();
							InetSocketAddress socketAddress = (InetSocketAddress) channel
									.getRemoteAddress();
							String ip_port = socketAddress.getHostName() + ":"
									+ socketAddress.getPort();
							CONNECTIONS.put(ip_port, sessionId);
							channel.configureBlocking(false);
							channel.register(selector, channel.validOps());
						}
					} catch (Exception exp) {
						logger.log(Level.SEVERE, exp.getMessage(), exp);
						running = false;
					}
				}
				logger.info("The server thread is finished...");
			}
		};
		// Starting the server thread
		serverThread.start();
		logger.info("Server started successfully...");

		while (true) {
			logger.log(Level.INFO, "Waiting for new connections...");

			try {
				// Wait for an event
				selector.select();
			} catch (Exception e) {
				// Handle error with selector
				logger.log(Level.SEVERE, e.getMessage(), e);
				break;
			}

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
				}
			}
		}
		serverThread.join();
		logger.info("Server shutdown");
	}

	/**
	 * 
	 * @param selKey
	 * @throws Exception
	 */
	public static void processSelectionKey(SelectionKey selKey) throws Exception {
		if (selKey.isValid() && selKey.isAcceptable()) {
			SocketChannel channel = (SocketChannel) selKey.channel();
			InetSocketAddress socketAddress = (InetSocketAddress) channel.getRemoteAddress();
			String ip_port = socketAddress.getHostName() + ":" + socketAddress.getPort();
			// retrieve the session ID
			String sessionId = CONNECTIONS.get(ip_port);
			Nio2SelectorClientManager manager = new Nio2SelectorClientManager(channel);
			manager.setSessionId(sessionId);
			// Execute the client query
			THREAD_POOL.execute(manager);
		}
	}
}
