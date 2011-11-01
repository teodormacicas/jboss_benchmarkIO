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
package org.jboss.nio2.server.selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

/**
 * {@code Nio2SelectorServer}
 * 
 * Created on Oct 28, 2011 at 10:01:30 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Nio2SelectorServer extends Thread {

	public static final int SERVER_PORT[] = { 4560, 4561, 4562 };
	private static final Logger logger = Logger.getLogger(Nio2SelectorServer.class.getName());

	private Selector selector;
	private ServerSocketChannel channels[];
	private ExecutorService pool;

	/**
	 * Create a new instance of @ Nio2SelectorServer}
	 * 
	 * @throws IOException
	 */
	public Nio2SelectorServer() throws IOException {
		super();
		this.init();
	}

	@PostConstruct
	protected void init() throws IOException {
		logger.log(Level.INFO, "Intializing NIO2 Selector Server");
		this.pool = Executors.newFixedThreadPool(200);
		this.selector = Selector.open();
		this.channels = new ServerSocketChannel[SERVER_PORT.length];
		for (int i = 0; i < SERVER_PORT.length; i++) {
			channels[i] = ServerSocketChannel.open();
			channels[i].configureBlocking(false);
			channels[i].socket().bind(new InetSocketAddress(SERVER_PORT[i]));
			channels[i].register(selector, channels[i].validOps());
		}
		logger.log(Level.INFO, "NIO2 Selector Server Intialized");
	}

	@Override
	protected void finalize() throws Throwable {
		for (int i = 0; i < channels.length; i++) {
			this.channels[i].close();
			this.channels[i] = null;
		}
		this.selector.close();
		this.selector = null;
		super.finalize();
	}

	@Override
	public void run() {
		while (true) {
			try {
				logger.log(Level.INFO, "Waiting for new events");
				// Wait for an event
				selector.select();
				logger.log(Level.INFO, "Receiving a new event(s)");
			} catch (IOException e) {
				// Handle error with selector
				logger.log(Level.SEVERE, "Errror occur when waiting for events", e);
				break;
			}

			logger.log(Level.INFO, "Processing events");
			// Get list of selection keys with pending events
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey selKey = (SelectionKey) it.next();
				// Remove it from the list to indicate that it is being
				// processed
				it.remove();
				try {
					processSelectionKey(selKey);
				} catch (Exception e) {
					// Handle error with channel and unregister
					selKey.cancel();
					logger.severe(e.getMessage());
				}
			}
		}
	}

	/**
	 * 
	 * @param selKey
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void processSelectionKey(SelectionKey selKey) throws Exception {
		logger.log(Level.INFO, "Starting processing selection key");

		if (selKey.isAcceptable()) {
			logger.log(Level.INFO, "Processing acceptable selection key");
			ServerSocketChannel serverChannel = (ServerSocketChannel) selKey.channel();
			SocketChannel channel = serverChannel.accept();
			Nio2SelectorClientManager manager = new Nio2SelectorClientManager(channel);
			this.pool.execute(manager);
			logger.log(Level.INFO, "Acceptable selection key is being processed");
		}

		logger.log(Level.INFO, "Ending processing selection key");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Thread server = new Nio2SelectorServer();
		server.start();
		server.join();
	}
}
