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

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;

/**
 * {@code Nio2AsyncClient}
 * 
 * Created on Oct 27, 2011 at 5:49:28 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Nio2AsyncClient extends Nio2AbstractClient {

	/**
	 * 
	 */
	private AsynchronousSocketChannel channel;

	/**
	 * Create a new instance of {@code Nio2AsyncClient}
	 * 
	 * @param hostname
	 * @param port
	 * @param d_max
	 * @param delay
	 */
	public Nio2AsyncClient(String hostname, int port, int d_max, int delay) {
		super(hostname, port, d_max, delay);
	}

	/**
	 * Create a new instance of {@code Nio2AbstractClient}
	 * 
	 * @param hostname
	 * @param port
	 * @param delay
	 */
	public Nio2AsyncClient(String hostname, int port, int delay) {
		super(hostname, port, delay);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.nio2.client.Nio2AbstractClient#connect(java.net.SocketAddress)
	 */
	@Override
	protected void connect(SocketAddress socketAddress) throws Exception {
		// Open connection with server
		this.channel = AsynchronousSocketChannel.open();
		Future<Void> connected = channel.connect(socketAddress);
		connected.get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.nio2.client.Nio2AbstractClient#read()
	 */
	protected String read() throws Exception {
		this.getByteBuffer().clear();
		int count = this.channel.read(this.getByteBuffer()).get();
		if (count > 0) {
			this.getByteBuffer().flip();
			byte[] bytes = new byte[count];
			this.getByteBuffer().get(bytes);
			return new String(bytes);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.nio2.client.Nio2AbstractClient#writeToChannel()
	 */
	protected void writeToChannel() throws IOException {
		this.channel.write(this.getByteBuffer());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.nio2.client.Nio2AbstractClient#close()
	 */
	public void close() throws IOException {
		this.channel.close();
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			System.err.println("Usage: java " + Nio2AsyncClient.class.getName()
					+ " hostname port [n] [delay]");
			System.err.println("\thostname: The server IP/hostname.");
			System.err.println("\tport: The server port number.");
			System.err.println("\tn: The number of threads. (default is 100)");
			System.err.println("\tdelay: The delay between writes. (default is 1000ms)");
			System.exit(1);
		}

		String hostname = args[0];
		int port = Integer.parseInt(args[1]);
		int n = 100, delay = DEFAULT_DELAY;
		if (args.length > 2) {
			try {
				n = Integer.parseInt(args[2]);
				if (n < 1) {
					throw new IllegalArgumentException(
							"Number of threads may not be less than zero");
				}

				if (args.length > 3) {
					delay = Integer.parseInt(args[3]);
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
		System.out.println("\tPort: " + port);
		System.out.println("\tn: " + n);
		System.out.println("\tdelay: " + delay);

		Nio2AsyncClient clients[] = new Nio2AsyncClient[n];
		for (int i = 0; i < clients.length; i++) {
			clients[i] = new Nio2AsyncClient(hostname, port, delay);
		}

		for (int i = 0; i < clients.length; i++) {
			clients[i].start();
		}

		for (int i = 0; i < clients.length; i++) {
			clients[i].join();
		}
	}
}
