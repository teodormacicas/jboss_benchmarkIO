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
package org.jboss.nio2.server.jio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;

/**
 * {@code JioServer}
 * 
 * Created on Nov 17, 2011 at 11:42:21 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class JioServer {

	/**
	 * 
	 */
	public static final String CRLF = "\r\n";
	/**
	 * 
	 */
	public static final int SERVER_PORT = 8081;
	private static final Logger logger = Logger.getLogger(JioServer.class.getName());
	protected static final long TIMEOUT = 20;
	protected static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
	private static final ExecutorService pool = Executors.newFixedThreadPool(400);

	/**
	 * Create a new instance of {@code JioServer}
	 */
	public JioServer() {
		super();
	}

	/**
	 * Generate a random and unique session Id
	 * 
	 * @return a random and unique session Id
	 */
	public static String generateId() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
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
				logger.error(e.getMessage(), e);
			}
		}

		logger.infov("Starting JavaIO Sever on port {0} ...", port);
		final ServerSocket listener = new ServerSocket(port);
		boolean running = true;
		logger.info("JavaIO Sever started...");

		while (running) {
			logger.info("Waiting for new connections...");
			Socket socket = listener.accept();
			logger.info("New connection accepted");

			JioClientManager manager = new JioClientManager(socket);
			pool.execute(manager);
		}

		listener.close();
	}

	/**
	 * 
	 * @param socket
	 * @param buffer
	 * @param sessionId
	 * @throws Exception
	 */
	protected static void initSession(Socket socket, String sessionId) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String request = reader.readLine();
		System.out.println("[" + sessionId + "] " + request);
		String response = "jSessionId: " + sessionId + CRLF;
		socket.getOutputStream().write(response.getBytes());
		socket.getOutputStream().flush();
	}

}
