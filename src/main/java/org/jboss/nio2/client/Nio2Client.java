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
package org.jboss.nio2.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * {@code Nio2Client}
 * <p/>
 * 
 * Created on Oct 27, 2011 at 12:13:33 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Nio2Client extends Thread {

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

	/**
	 * Create a new instance of {@code Nio2Client}
	 * 
	 * @param hostname
	 * @param port
	 * @param d_max
	 * @param delay
	 */
	public Nio2Client(String hostname, int port, int d_max, int delay) {
		this.hostname = hostname;
		this.port = port;
		this.max = d_max;
		this.delay = delay;
	}

	/**
	 * Create a new instance of {@code Nio2Client}
	 * 
	 * @param hostname
	 * @param port
	 * @param delay
	 */
	public Nio2Client(String hostname, int port, int delay) {
		this(hostname, port, 55 * 1000 / delay, delay);
	}

	@Override
	public void run() {
		try {
			// Open connection with server
			SocketAddress socketAddress = new InetSocketAddress(this.hostname, this.port);
			this.channel = SocketChannel.open(socketAddress);
			// Allocate byte buffer for read/write data
			this.byteBuffer = ByteBuffer.allocate(1024);
			// wait for 2 seconds until all threads are ready
			init();
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

		/*
		 * Random random = new Random(); long delay = 1000 +
		 * random.nextInt(1000); int max = 10 + random.nextInt(20); ByteBuffer
		 * bb = ByteBuffer.allocate(1024);
		 * 
		 * System.out.println("[Thread-" + getId() +
		 * "] Running client with max = " + max + ", delay = " + delay);
		 * 
		 * while ((max--) > 0) { try { sleep(delay); bb.put(("Ping from client "
		 * + getId()).getBytes()); bb.flip(); channel.write(bb); bb.clear(); int
		 * count = channel.read(bb); if (count > 0) { bb.flip(); byte[] bytes =
		 * new byte[count]; bb.get(bytes);
		 * System.out.println("Received from server : " + new String(bytes)); }
		 * bb.clear(); } catch (Exception ex) { ex.printStackTrace(); } }
		 */
	}

	/**
	 * 
	 * @throws Exception
	 */
	protected void init() throws Exception {
		System.out.println("Initializing communication...");
		write("Ping from client " + getId() + "\n");
		String response = read();
		System.out.println("Communication intialized -> " + response);
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
	private void write(String data) throws IOException {
		if (data != null) {
			this.byteBuffer.clear();
			this.byteBuffer.put(data.getBytes());
			this.byteBuffer.flip();
			channel.write(this.byteBuffer);
		}
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private String read() throws IOException {
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
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			System.err.println("Usage: java " + Nio2Client.class.getName()
					+ " hostname port [n] [delay]");
			System.err.println("\thostname: The server IP/hostname.");
			System.err.println("\tport: The server port number.");
			System.err.println("\tn: The number of threads. (default is 100)");
			System.err.println("\tdelay: The delay between writes. (default is 1000ms)");
			System.exit(1);
		}

		String hostname = args[0];
		int port = Integer.parseInt(args[1]);
		int n = 100, max = 1000, delay = DEFAULT_DELAY;
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
		System.out.println("\tmax: " + max);
		System.out.println("\tdelay: " + delay);

		Nio2Client clients[] = new Nio2Client[n];
		for (int i = 0; i < clients.length; i++) {
			clients[i] = new Nio2Client(hostname, port, delay);
		}

		for (int i = 0; i < clients.length; i++) {
			clients[i].start();
		}

		for (int i = 0; i < clients.length; i++) {
			clients[i].join();
		}
	}
}
