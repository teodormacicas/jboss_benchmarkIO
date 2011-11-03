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
package org.jboss.nio2.client;

import org.jboss.nio2.server.selector.Nio2SelectorServer;

/**
 * {@code Nio2SelectorClient
 * 
 * Created on Oct 28, 2011 at 10:02:07 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Nio2SelectorClient extends Nio2AbstractClient {

	/**
	 * Create a new instance of {@code Nio2SelectorClient}
	 * 
	 * @param hostname
	 * @param port
	 * @param d_max
	 * @param delay
	 */
	public Nio2SelectorClient(String hostname, int port, int d_max, int delay) {
		super(hostname, port, d_max, delay);
	}

	/**
	 * Create a new instance of {@code Nio2SelectorClient}
	 * 
	 * @param hostname
	 * @param port
	 * @param delay
	 */
	public Nio2SelectorClient(String hostname, int port, int delay) {
		this(hostname, port, 55 * 1000 / delay, delay);
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		if (args.length < 1) {
			System.err.println("Usage: java " + Nio2SelectorClient.class.getName()
					+ " hostname [n] [delay]");
			System.err.println("\thostname: The server IP/hostname.");
			System.err.println("\tn: The number of threads. (default is 100)");
			System.err.println("\tdelay: The delay between writes. (default is 1000ms)");
			System.exit(1);
		}

		String hostname = args[0];
		int n = 100, delay = DEFAULT_DELAY;
		if (args.length > 1) {
			try {
				n = Integer.parseInt(args[1]);
				if (n < 1) {
					throw new IllegalArgumentException(
							"Number of threads may not be less than zero");
				}

				if (args.length > 2) {
					delay = Integer.parseInt(args[2]);
					if (delay < 1) {
						throw new IllegalArgumentException("Negative number: delay");
					}
				}
			} catch (Exception exp) {
				System.err.println("Error: " + exp.getMessage());
				System.exit(1);
			}
		}

		System.out.println("\nRunning test with parameters:");
		System.out.println("\tHostname: " + hostname);
		System.out.println("\tn: " + n);
		System.out.println("\tdelay: " + delay);

		Nio2SelectorClient clients[] = new Nio2SelectorClient[10];

		for (int i = 0; i < clients.length; i++) {
			int port = Nio2SelectorServer.SERVER_PORTS[i % Nio2SelectorServer.SERVER_PORTS.length];
			clients[i] = new Nio2SelectorClient(hostname, port, delay);
		}

		for (int i = 0; i < clients.length; i++) {
			clients[i].start();
		}

		for (int i = 0; i < clients.length; i++) {
			clients[i].join();
		}
	}
}
