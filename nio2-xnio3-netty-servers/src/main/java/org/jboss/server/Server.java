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

import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.UnknownHostException;
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
        public static Inet4Address DEFAULT_LISTEN_ADDRESS;
	public static final Logger LOG = Logger.getLogger(Server.class);
        public static String workingDirectory;
        
	/**
	 * Create a new instance of {@code Server}
	 */
	public Server() {
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
                DEFAULT_LISTEN_ADDRESS = (Inet4Address) Inet4Address.getLocalHost();
		if (args.length < 3) {
			System.err.println("Usage: java " + Server.class.getName() + " type mode [port]\n");
			System.err.println("  --> type: xnio, nio or netty (Allowed values: \"xnio3\", \"nio2\" and \"netty\")");
			System.err.print("  --> mode: the channel processing mode, i.e, sync/async (");
			System.err.println("Allowed values: \"sync\" or \"async\"; Netty is always asynch)");
			System.err.println("  --> port: the server port number to which the server channel will bind.");
			System.err.println("            Default value: " + DEFAULT_SERVER_PORT);
                        System.err.println("  --> listen address: the server ip address to which the server channel will bind.");
			System.err.println("            Default value: " + DEFAULT_LISTEN_ADDRESS.toString());
                        System.err.println("  --> working directory: the place where the data files are stored ");
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
                
                Inet4Address addr = DEFAULT_LISTEN_ADDRESS;
		if (args.length >= 4) {
			try {
				addr = (Inet4Address) Inet4Address.getByName(args[3]);
			} catch (Throwable e) {
				LOG.errorv("Invalid listen address format: {0}", args[3]);
				LOG.infov("Using the default listen address {0}", addr);
			}
		}
                
                workingDirectory = "./";
                if (args.length >= 5) {
                        workingDirectory = args[4];
		}

                //IMPORTANT FOR TESTING TOOL; DO NOT DELETE!
                String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
                System.out.println("PID: "+PID);
                System.out.flush();
                
		switch (args[0]) {
			case "nio2":
				org.jboss.server.nio2.MainServer.run(args[1], addr, port);
				break;
			case "xnio3":
				org.jboss.server.xnio3.MainServer.run(args[1], addr, port);
				break;
                        case "netty":
                                if( args[1].equals("sync") ) { 
                                    System.err.println("There is no version of synchronized Netty!");
                                    System.exit(-2);
                                }
                                org.jboss.server.netty.async.NettyHttpServer.run(port);
                                break;
			default:
				System.err.println("ERROR: unknown server type \"" + args[0] + "\"");
				System.err.println("Allowed values: \"xnio3\" and \"nio2\"");
				break;
		}
	}

}
