/**
 * JBoss, Home of Professional Open Source. Copyright 2011, Red Hat, Inc., and
 * individual
 * contributors as indicated by the @author tags. See the copyright.txt file in
 * the distribution
 * for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either
 * version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this
 * software; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor,
 * Boston, MA 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.server.xnio3;

import org.jboss.server.xnio3.async.AsyncServer;
import org.jboss.server.xnio3.sync.SyncServer;

/**
 * {@code MainServer}
 * 
 * Created on Oct 27, 2012 at 8:33:59 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class MainServer {

	/**
	 * Runs the NIO.2 server
	 * 
	 * @param mode
	 *            the server mode, sync/async
	 * @param port
	 *            the server port number
	 * @throws Exception
	 */
	public static void run(String mode, int port) throws Exception {
		Runnable target = null;
		switch (mode) {
			case "sync":
				target = new SyncServer(port);
				break;
			case "async":
				target = new AsyncServer(port);
				break;

			default:
				throw new Exception("Invalid mode: " + mode);
		}

		Thread thread = new Thread(target);
		thread.start();
		thread.join();
	}
}
