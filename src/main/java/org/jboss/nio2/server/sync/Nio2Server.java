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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.nio2.server.SessionGenerator;

/**
 * {@code Nio2Server}
 * <p/>
 * 
 * Created on Oct 27, 2011 at 11:42:12 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Nio2Server {

	private static final Logger logger = Logger.getLogger(Nio2Server.class.getName());
	protected static final int SERVER_PORT = 8080;

	/**
	 * Create a new instance of {@code Nio2Server}
	 */
	public Nio2Server() {
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

		logger.log(Level.INFO, "Starting NIO2 Synchronous Sever on port {0} ...", port);
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open().bind(
				new InetSocketAddress(port));

		logger.info("Server started successfully...");

		boolean running = true;
		ExecutorService pool = Executors.newFixedThreadPool(400);
		while (running) {
			logger.log(Level.INFO, "Waiting for new connection");
			SocketChannel channel = serverSocketChannel.accept();
			if (channel != null) {
				logger.log(Level.INFO, "New connection received");
				Nio2ClientManager clientManager = new Nio2ClientManager(channel);
				clientManager.setSessionId(SessionGenerator.generateId());
				pool.execute(clientManager);
			}
		}
		logger.info("Server shutdown");
	}
}
