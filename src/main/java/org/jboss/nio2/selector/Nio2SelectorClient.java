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
package org.jboss.nio2.selector;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * {@code Nio2SelectorClient}
 * 
 * Created on Oct 28, 2011 at 10:02:07 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Nio2SelectorClient extends Thread {

	private SocketChannel channel;

	/**
	 * Create a new instance of {@code Nio2SelectorClient}
	 * 
	 * @param channel
	 */
	public Nio2SelectorClient(SocketChannel channel) {
		this.channel = channel;
	}

	@Override
	public void run() {

		Random random = new Random();
		long delay = 1000 + random.nextInt(1000);
		int max = 10 + random.nextInt(20);
		ByteBuffer bb = ByteBuffer.allocate(1024);

		System.out.println("[Thread-" + getId() + "] Running client with max = " + max
				+ ", delay = " + delay);

		while ((max--) > 0) {
			try {
				sleep(delay * 2);
				bb.put(("Ping from client " + getId()).getBytes());
				bb.flip();
				channel.write(bb);
				bb.clear();
				int count = channel.read(bb);
				if (count > 0) {
					bb.flip();
					byte[] bytes = new byte[count];
					bb.get(bytes);
					System.out.println("Received from server : " + new String(bytes));
				}
				bb.clear();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Nio2SelectorClient clients[] = new Nio2SelectorClient[10];
		for (int i = 0; i < clients.length; i++) {
			SocketChannel channel = SocketChannel.open(new InetSocketAddress("localhost",
					Nio2SelectorServer.SERVER_PORT[i % Nio2SelectorServer.SERVER_PORT.length]));
			clients[i] = new Nio2SelectorClient(channel);
			clients[i].start();
		}

		for (int i = 0; i < clients.length; i++) {
			clients[i].join();
		}
	}

}
