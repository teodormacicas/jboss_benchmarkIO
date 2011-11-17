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
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

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
	private ByteBuffer readBuffer;
	private ByteBuffer writeBuffer;

	/**
	 * Create a new instance of {@code CompletionHandlerImpl}
	 * 
	 * @param sessionId
	 * @param byteBuffer
	 */
	public CompletionHandlerImpl(String sessionId, ByteBuffer byteBuffer) {
		this.sessionId = sessionId;
		this.readBuffer = byteBuffer;
		this.writeBuffer = ByteBuffer.allocate(8 * 1024);
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
			readBuffer.flip();
			byte bytes[] = new byte[nBytes];
			readBuffer.get(bytes);
			readBuffer.clear();
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
		channel.read(readBuffer, Nio2AsyncServer.TIMEOUT, Nio2AsyncServer.TIME_UNIT, channel, this);
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

		File file = new File("data/file.txt");
		Path path = FileSystems.getDefault().getPath(file.getAbsolutePath());
		SeekableByteChannel sbc = null;
		try {
			sbc = Files.newByteChannel(path, StandardOpenOption.READ);
			int nBytes = -1;
			byte bytes[] = new byte[8 * 1024];
			int off = 0;
			int remain = 0;

			while ((nBytes = sbc.read(writeBuffer)) > 0) {
				writeBuffer.rewind();
				channel.write(writeBuffer);
				writeBuffer.flip();
			}

			/*
			while ((nBytes = fis.read(bytes)) != -1) {

				if (this.writeBuffer.remaining() >= nBytes) {
					this.writeBuffer.put(bytes);
				} else {
					off = this.writeBuffer.remaining();
					remain = nBytes - off;
					this.writeBuffer.put(bytes, 0, off);
				}
				// write data to the channel when the buffer is full
				if (!this.writeBuffer.hasRemaining()) {
					write(channel, this.writeBuffer);
					this.writeBuffer.put(bytes, off, remain);
					remain = 0;
				}
			}
		    */
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
			if (this.writeBuffer.remaining() < this.writeBuffer.capacity()) {
				write(channel, this.writeBuffer);
			}
			// write the CRLF characters
			this.writeBuffer.put(CRLF.getBytes());
			write(channel, this.writeBuffer);
		} catch (Exception exp) {
			logger.error("Exception: " + exp.getMessage(), exp);
			exp.printStackTrace();
		} finally {
			// in.close();
			fis.close();
			if(sbc != null) {
				sbc.close();
			}
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
	protected void write(AsynchronousSocketChannel channel, ByteBuffer byteBuffer) throws Exception {
		byteBuffer.flip();
		Future<Integer> count = channel.write(byteBuffer);
		int written = count.get();
		byteBuffer.clear();
	}
}
