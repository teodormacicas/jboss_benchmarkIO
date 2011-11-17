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
package org.jboss.nio2.server.async;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.jboss.logging.Logger;

/**
 * {@code CompletionHandlerImpl}
 * 
 * Created on Nov 16, 2011 at 8:59:51 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
class CompletionHandlerImpl implements CompletionHandler<Integer, AsynchronousSocketChannel> {

	/**
	 * 
	 */
	public static final String CRLF = "\r\n";
	private static final Logger logger = Logger.getLogger(CompletionHandler.class.getName());
	private String sessionId;
	private ByteBuffer byteBuffer;

	/**
	 * Create a new instance of {@code CompletionHandlerImpl}
	 * 
	 * @param sessionId
	 * @param byteBuffer
	 */
	public CompletionHandlerImpl(String sessionId, ByteBuffer byteBuffer) {
		this.sessionId = sessionId;
		this.byteBuffer = byteBuffer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.CompletionHandler#completed(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public void completed(Integer nBytes, AsynchronousSocketChannel channel) {
		if (nBytes > 0) {
			byteBuffer.flip();
			byte bytes[] = new byte[nBytes];
			byteBuffer.get(bytes);
			byteBuffer.clear();
			System.out.println("[" + this.sessionId + "] " + new String(bytes).trim());

			try {
				// write response to client
				writeResponse(channel);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
		// Read again with the this CompletionHandler
		channel.read(byteBuffer, Nio2AsyncServer.TIMEOUT, Nio2AsyncServer.TIME_UNIT, channel, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.CompletionHandler#failed(java.lang.Throwable,
	 * java.lang.Object)
	 */
	@Override
	public void failed(Throwable exc, AsynchronousSocketChannel channel) {
		System.out.println("[" + this.sessionId + "] Operation failed");
		exc.printStackTrace();
		try {
			System.out.println("[" + this.sessionId + "] Closing remote connection");
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the response to client
	 * 
	 * @param channel
	 *            the {@code AsynchronousSocketChannel} channel to which write
	 * @throws Exception
	 */
	protected void writeResponse(AsynchronousSocketChannel channel) throws Exception {

		FileInputStream fis = new FileInputStream("data" + File.separatorChar + "file.txt");
		// BufferedReader in = new BufferedReader(new
		// InputStreamReader(fis));

		ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
		try {
			int nBytes = -1;
			byte bytes[] = new byte[8 * 1024];
			int off = 0;
			int remain = 0;

			while ((nBytes = fis.read(bytes)) != -1) {

				if (buffer.remaining() >= nBytes) {
					buffer.put(bytes);
				} else {
					off = buffer.remaining();
					remain = nBytes - off;
					buffer.put(bytes, 0, off);
				}
				// write data to the channel when the buffer is full
				if (!buffer.hasRemaining()) {
					write(channel, buffer);
					buffer.put(bytes, off, remain);
					remain = 0;
				}
			}

			/*
			 * while ((line = in.readLine()) != null) { int length =
			 * line.length();
			 * 
			 * if (buffer.remaining() >= length) { buffer.put(line.getBytes());
			 * } else { off = buffer.remaining(); remain = length - off;
			 * buffer.put(line.getBytes(), 0, off); } // write data to the
			 * channel when the buffer is full if (!buffer.hasRemaining()) {
			 * write(channel, buffer); buffer.put(line.getBytes(), off, remain);
			 * remain = 0; } }
			 */

			// If still some data to write
			if (buffer.remaining() < buffer.capacity()) {
				write(channel, buffer);
			}
			// write the CRLF characters
			buffer.put(CRLF.getBytes());
			write(channel, buffer);
		} catch (Exception exp) {
			logger.error("Exception: " + exp.getMessage(), exp);
			exp.printStackTrace();
		} finally {
			// in.close();
			fis.close();
		}
	}

	/**
	 * Write the byte buffer to the specified channel
	 * 
	 * @param channel
	 *            the {@code AsynchronousSocketChannel} channel
	 * @param byteBuffer
	 *            the data that will be written to the channel
	 * @throws IOException
	 */
	protected void write(AsynchronousSocketChannel channel, ByteBuffer byteBuffer)
			throws IOException {
		byteBuffer.flip();
		channel.write(byteBuffer);
		byteBuffer.clear();
	}
}
