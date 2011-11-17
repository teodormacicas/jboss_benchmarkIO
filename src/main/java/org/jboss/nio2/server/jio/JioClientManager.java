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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.jboss.logging.Logger;

/**
 * {@code JioClientManager}
 * 
 * Created on Nov 17, 2011 at 11:47:28 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
class JioClientManager implements Runnable {

	private static final Logger logger = Logger.getLogger(JioClientManager.class.getName());

	private Socket socket;
	private String sessionId;
	private BufferedReader in;
	private OutputStream out;

	/**
	 * Create a new instance of {@code JioClientManager}
	 * 
	 * @param socket
	 * @throws Exception
	 */
	public JioClientManager(Socket socket) throws Exception {
		this.socket = socket;
		this.out = socket.getOutputStream();
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			initSession();
			String request = null;
			while ((request = this.read()) != null) {
				System.out.println("[" + this.sessionId + "] " + request.trim());
				writeResponse();
			}
			logger.info("Connection closed remotely");
			this.socket.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param socket
	 * @param buffer
	 * @param sessionId
	 * @throws Exception
	 */
	protected void initSession() throws Exception {
		this.sessionId = JioServer.generateId();
		String request = this.read();
		System.out.println("[" + this.sessionId + "] " + request);
		String response = "jSessionId: " + this.sessionId + JioServer.CRLF;
		this.write(response.getBytes());
	}

	/**
	 * @throws Exception
	 */
	public void writeResponse() throws Exception {

		ByteBuffer writeBuffer = ByteBuffer.allocate(8 * 1024);
		File file = new File("data" + File.separatorChar + "file.txt");
		Path path = FileSystems.getDefault().getPath(file.getAbsolutePath());
		SeekableByteChannel sbc = null;
		try {
			sbc = Files.newByteChannel(path, StandardOpenOption.READ);
			// Read from file and write to the asynchronous socket channel
			int nBytes = -1;
			while ((nBytes = sbc.read(writeBuffer)) > 0) {
				byte[] bytes = new byte[nBytes];
				writeBuffer.flip();
				writeBuffer.get(bytes);
				write(bytes);
			}
			// write the CRLF characters
			write(JioServer.CRLF.getBytes());
		} catch (Exception exp) {
			logger.error("Exception: " + exp.getMessage(), exp);
			exp.printStackTrace();
		} finally {
			if (sbc != null) {
				sbc.close();
			}
		}

	}

	/**
	 * @return the line read from client
	 * @throws IOException
	 */
	public String read() throws IOException {
		return this.in.readLine();
	}

	/**
	 * 
	 * @param buffer
	 * @throws IOException
	 */
	public void write(byte[] buffer) throws IOException {
		this.out.write(buffer);
		this.out.flush();
	}

	/**
	 * 
	 * @param sessionId
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
