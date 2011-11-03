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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * {@code Nio2AbstractClient}
 * 
 * Created on Nov 3, 2011 at 9:06:40 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public abstract class Nio2AbstractClient extends Thread {

	public static final int MAX = 1000;
	public static final int N_THREADS = 100;
	public static final int DEFAULT_DELAY = 1000; // default wait delay 1000ms
	private long max_time = Long.MIN_VALUE;
	private long min_time = Long.MAX_VALUE;
	private double avg_time = 0;
	private String hostname;
	private int port;
	private int max;
	private int delay;
	private SocketChannel channel;
	private ByteBuffer byteBuffer;
	private String sessionId;

	/**
	 * Create a new instance of {@code Nio2AbstractClient}
	 * 
	 * @param hostname
	 * @param port
	 * @param d_max
	 * @param delay
	 */
	public Nio2AbstractClient(String hostname, int port, int d_max, int delay) {
		this.hostname = hostname;
		this.port = port;
		this.max = d_max;
		this.delay = delay;
	}

	/**
	 * Create a new instance of {@code Nio2AbstractClient}
	 * 
	 * @param hostname
	 * @param port
	 * @param delay
	 */
	public Nio2AbstractClient(String hostname, int port, int delay) {
		this(hostname, port, 55 * 1000 / delay, delay);
	}

	@Override
	public void run() {
		try {
			// Initialize the communication between client and server
			init();
			// wait for 2 seconds until all threads are ready
			sleep(2 * DEFAULT_DELAY);
			runit();
		} catch (Exception exp) {
			System.err.println("Exception: " + exp.getMessage());
		} finally {
			System.out.println("[Thread-" + getId() + "] terminated -> "
					+ System.currentTimeMillis());
			try {
				close();
			} catch (IOException ioex) {
				System.err.println("Exception: " + ioex.getMessage());
			}
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	protected void init() throws Exception {
		// Connect to the server
		SocketAddress socketAddress = new InetSocketAddress(this.getHostname(), this.getPort());
		this.connect(socketAddress);
		// Allocate byte buffer for read/write data
		this.byteBuffer = ByteBuffer.allocate(1024);
		System.out.println("Initializing communication...");
		write("POST /session-" + getId());
		String response = read();
		String tab[] = response.split("\\s+");
		this.sessionId = tab[1];
		System.out.println("Communication intialized -> Session ID:" + this.sessionId);
	}

	/**
	 * 
	 * @throws Exception
	 */
	protected void connect(SocketAddress socketAddress) throws Exception {
		// Open connection with server
		this.channel = SocketChannel.open(socketAddress);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void runit() throws Exception {
		// Wait a delay to ensure that all threads are ready
		Random random = new Random();

		sleep(DEFAULT_DELAY + random.nextInt(300));
		long time = 0;
		String response = null;
		int counter = 0;

		int min_count = 10 * 1000 / delay;
		int max_count = 50 * 1000 / delay;
		while ((this.max--) > 0) {
			sleep(this.delay);
			time = System.currentTimeMillis();
			write("Ping from client " + getId() + "\n");
			response = read();
			time = System.currentTimeMillis() - time;
			System.out.println("Received from server -> " + response);
			// update the maximum response time
			if (time > max_time) {
				max_time = time;
			}
			// update the minimum response time
			if (time < min_time) {
				min_time = time;
			}
			// update the average response time
			if (counter >= min_count && counter <= max_count) {
				avg_time += time;
			}
			counter++;
		}

		avg_time /= (max_count - min_count + 1);
		// For each thread print out the maximum, minimum and average response
		// times
		System.out.println(max_time + " \t " + min_time + " \t " + avg_time);
	}

	/**
	 * 
	 * @param data
	 * @throws IOException
	 */
	protected void write(String data) throws IOException {
		if (data != null) {
			this.byteBuffer.clear();
			this.byteBuffer.put(data.getBytes());
			this.byteBuffer.flip();
			this.channel.write(this.byteBuffer);
		}
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	protected String read() throws Exception {
		this.byteBuffer.clear();
		int count = channel.read(this.byteBuffer);
		if (count > 0) {
			this.byteBuffer.flip();
			byte[] bytes = new byte[count];
			this.byteBuffer.get(bytes);
			return new String(bytes);
		}

		return null;
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void close() throws IOException {
		this.channel.close();
	}

	/**
	 * Getter for hostname
	 *
	 * @return the hostname
	 */
	public String getHostname() {
		return this.hostname;
	}

	/**
	 * Setter for the hostname
	 *
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * Getter for port
	 *
	 * @return the port
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Setter for the port
	 *
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Getter for sessionId
	 *
	 * @return the sessionId
	 */
	public String getSessionId() {
		return this.sessionId;
	}

	/**
	 * Setter for the sessionId
	 *
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Getter for byteBuffer
	 *
	 * @return the byteBuffer
	 */
	public ByteBuffer getByteBuffer() {
		return this.byteBuffer;
	}
}
