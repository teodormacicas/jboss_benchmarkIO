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

package org.jboss.server;

import org.jboss.logging.Logger;

/**
 * {@code Server}
 * 
 * Created on Oct 29, 2012 at 12:19:48 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public abstract class Server {

	/**
	 *
	 */
	public static final int DEFAULT_SERVER_PORT = 8080;
	public static final Logger LOG = Logger.getLogger(Server.class);

	/**
	 * Create a new instance of {@code Server}
	 */
	public Server() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println();
		if (args.length < 2) {
			System.err.println("Usage: java " + Server.class.getName() + " type mode [port]\n");
			System.err.println("  --> type: xnio or nio (Allowed values: \"xnio3\" and \"nio2\")");
			System.err.print("  --> mode: the channel processing mode, i.e, sync/async (");
			System.err.println("Allowed values: \"sync\" or \"async\")");
			System.err
					.println("  --> port: the server port number to which the server channel will bind.");
			System.err.println("            Default value: 8080");
			System.out.println();
			System.exit(-1);
		}

		int port = DEFAULT_SERVER_PORT;
		if (args.length >= 3) {
			try {
				port = Integer.valueOf(args[2]);
			} catch (Throwable e) {
				LOG.errorv("Invalid port number format: {0}", args[2]);
				LOG.infov("Using the default port number {0}", port);
			}
		}

		switch (args[0]) {
			case "nio2":
				org.jboss.server.nio2.MainServer.run(args[1], port);
				break;
			case "xnio3":
				org.jboss.server.xnio3.MainServer.run(args[1], port);
				break;
			default:
				System.err.println("ERROR: unknown server type \"" + args[0] + "\"");
				System.err.println("Allowed values: \"xnio3\" and \"nio2\"");
				break;
		}
	}

}
