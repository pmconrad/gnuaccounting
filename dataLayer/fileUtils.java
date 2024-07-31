package dataLayer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class fileUtils {
	/*
	 * based on the CSV reader at
	 * http://www.java2s.com/Code/Java/Development-Class
	 * /SimpledemoofCSVmatchingusingRegularExpressions.htm modification:
	 * starmoney "txt" uses ";" as delimiter, not ","
	 */
	/*
	 * Copyright (c) Ian F. Darwin, http://www.darwinsys.com/, 1996-2002. All
	 * rights reserved. Software written by Ian F. Darwin and others. $Id:
	 * LICENSE,v 1.8 2004/02/09 03:33:38 ian Exp $
	 * 
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions are
	 * met: 1. Redistributions of source code must retain the above copyright
	 * notice, this list of conditions and the following disclaimer. 2.
	 * Redistributions in binary form must reproduce the above copyright notice,
	 * this list of conditions and the following disclaimer in the documentation
	 * and/or other materials provided with the distribution.
	 * 
	 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
	 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
	 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
	 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE
	 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
	 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
	 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
	 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
	 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
	 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
	 * THE POSSIBILITY OF SUCH DAMAGE.
	 * 
	 * Java, the Duke mascot, and all variants of Sun's Java "steaming coffee
	 * cup" logo are trademarks of Sun Microsystems. Sun's, and James Gosling's,
	 * pioneering role in inventing and promulgating (and standardizing) the
	 * Java language and environment is gratefully acknowledged.
	 * 
	 * The pioneering role of Dennis Ritchie and Bjarne Stroustrup, of AT&T, for
	 * inventing predecessor languages C and C++ is also gratefully
	 * acknowledged.
	 */
	public static final String CSV_SEMICOLON_PATTERN = "\"([^\"]+?)\";?|([^;]+);?|;"; //$NON-NLS-1$
	private static Pattern csvRESemicolon = Pattern
			.compile(CSV_SEMICOLON_PATTERN);
	public static final String CSV_COMMA_PATTERN = "\"([^\"]+?)\",?|([^,]+),?|,"; //$NON-NLS-1$
	private static Pattern csvREComma = Pattern.compile(CSV_COMMA_PATTERN);

	/**
	 * @param filePath
	 *            the name of the file to open. Not sure if it can accept URLs
	 *            or just filenames. Path handling could be better, and buffer
	 *            sizes are hardcoded
	 */
	public static String readFileAsString(String filePath)
			throws java.io.IOException {
		File inFile = new File(filePath);
		BufferedReader reader = new BufferedReader(new FileReader(inFile));

		int cap = (int) inFile.length();
		if (inFile.length() > Integer.MAX_VALUE) {
			cap = Integer.MAX_VALUE;
		}
		StringBuffer fileData = new StringBuffer(cap);
		char[] buf = new char[32 * 1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
		}
		reader.close();
		return fileData.toString();
	}

	/**
	 * Returns the contents of the file in a byte array. This works with files
	 * <Integer.max_value (~2Gig) and is binary safe
	 * 
	 * @param filename
	 * @return byte array of the contents
	 * @throws IOException
	 */
	public static byte[] readFileAsBytes(String filename) throws IOException {
		InputStream is = new FileInputStream(filename);

		// Get the size of the file
		long length = new File(filename).length();
		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
			throw new IOException(
					String.format(
							Messages.getString("fileUtils.fileTooLargeException"), filename, Integer.MAX_VALUE)); //$NON-NLS-1$
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException(
					Messages.getString("fileUtils.ExceptionCouldNotReadFile") + filename); //$NON-NLS-1$
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	/**
	 * Parse one line.
	 * 
	 * @return List of Strings, minus their double quotes
	 */
	public static String[] parseCSVLineSemicolon(String line) {
		List<String> list = new ArrayList<String>();
		Matcher m = csvRESemicolon.matcher(line);
		// For each field
		while (m.find()) {
			String match = m.group();
			if (match == null)
				break;
			if (match.endsWith(";")) { // trim trailing , //$NON-NLS-1$
				match = match.substring(0, match.length() - 1);
			}
			if (match.startsWith("\"")) { // assume also ends with //$NON-NLS-1$
				match = match.substring(1, match.length() - 1);
			}
			if (match.length() == 0)
				match = null;
			list.add(match);
		}
		String[] res = new String[list.size()];

		for (int currentIndex = 0; currentIndex < list.size(); currentIndex++) {
			res[currentIndex] = list.get(currentIndex);
		}
		return res;
	}

	/**
	 * Parse one line.
	 * 
	 * @return List of Strings, minus their double quotes
	 */
	public static String[] parseCSVLineComma(String line) {
		List<String> list = new ArrayList<String>();
		Matcher m = csvREComma.matcher(line);
		// For each field
		while (m.find()) {
			String match = m.group();
			if (match == null)
				break;
			if (match.endsWith(",")) { // trim trailing , //$NON-NLS-1$
				match = match.substring(0, match.length() - 1);
			}
			if (match.startsWith("\"")) { // assume also ends with //$NON-NLS-1$
				match = match.substring(1, match.length() - 1);
			}
			if (match.length() == 0)
				match = null;
			list.add(match);
		}
		String[] res = new String[list.size()];

		for (int currentIndex = 0; currentIndex < list.size(); currentIndex++) {
			res[currentIndex] = list.get(currentIndex);
		}
		return res;
	}

	// source:
	// http://www.anyexample.com/programming/java/java_simple_class_to_compute_sha_1_hash.xml
	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	// source:
	// http://www.anyexample.com/programming/java/java_simple_class_to_compute_sha_1_hash.xml,
	// http://www.jguru.com/faq/view.jsp?EID=4316
	public static String SHA1(byte[] payload) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
		byte[] sha1hash = new byte[40];
		// using only byte[] we stay binary compatible up to 2gig files
		md.update(payload);
		sha1hash = md.digest();
		return convertToHex(sha1hash);
	}

	/**
	 * based on http://www.rgagnon.com/javadetails/java-0064.html
	 * */
	public static void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			// magic number for Windows, 64Mb - 32Kb)
			int maxCount = (64 * 1024 * 1024) - (32 * 1024);
			long size = inChannel.size();
			long position = 0;
			while (position < size) {
				position += inChannel
						.transferTo(position, maxCount, outChannel);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	/**
	 * the following methods are based on Apache's common io FileUtils
	 * http://commons.apache.org/downloads/download_io.cgi to enable
	 * */
	// -----------------------------------------------------------------------
	/**
	 * Opens a {@link FileOutputStream} for the specified file, checking and
	 * creating the parent directory if it does not exist.
	 * <p>
	 * At the end of the method either the stream will be successfully opened,
	 * or an exception will have been thrown.
	 * <p>
	 * The parent directory will be created if it does not exist. The file will
	 * be created if it does not exist. An exception is thrown if the file
	 * object exists but is a directory. An exception is thrown if the file
	 * exists but cannot be written to. An exception is thrown if the parent
	 * directory cannot be created.
	 * 
	 * @param file
	 *            the file to open for output, must not be <code>null</code>
	 * @return a new {@link FileOutputStream} for the specified file
	 * @throws IOException
	 *             if the file object is a directory
	 * @throws IOException
	 *             if the file cannot be written to
	 * @throws IOException
	 *             if a parent directory needs creating but that fails
	 * @since Commons IO 1.3
	 */
	public static FileOutputStream openOutputStream(File file)
			throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException(String.format(Messages
						.getString("fileUtils.fileIsDirectoryException"), file)); //$NON-NLS-1$
			}
			if (file.canWrite() == false) {
				throw new IOException(
						String.format(
								Messages.getString("fileUtils.fileUnwritableException"), file)); //$NON-NLS-1$
			}
		} else {
			File parent = file.getParentFile();
			if (parent != null && parent.exists() == false) {
				if (parent.mkdirs() == false) {
					throw new IOException(
							String.format(
									Messages.getString("fileUtils.fileNotCreatedException"), file)); //$NON-NLS-1$
				}
			}
		}
		return new FileOutputStream(file);
	}

	// -----------------------------------------------------------------------
	/**
	 * Copies bytes from the URL <code>source</code> to a file
	 * <code>destination</code>. The directories up to <code>destination</code>
	 * will be created if they don't already exist. <code>destination</code>
	 * will be overwritten if it already exists.
	 * 
	 * @param source
	 *            the <code>URL</code> to copy bytes from, must not be
	 *            <code>null</code>
	 * @param destination
	 *            the non-directory <code>File</code> to write bytes to
	 *            (possibly overwriting), must not be <code>null</code>
	 * @throws IOException
	 *             if <code>source</code> URL cannot be opened
	 * @throws IOException
	 *             if <code>destination</code> is a directory
	 * @throws IOException
	 *             if <code>destination</code> cannot be written
	 * @throws IOException
	 *             if <code>destination</code> needs creating but can't be
	 * @throws IOException
	 *             if an IO error occurs during copying
	 */
	public static void copyURLToFile(URL source, File destination)
			throws IOException {
		InputStream input = source.openStream();
		try {
			FileOutputStream output = openOutputStream(destination);
			try {
				copy(input, output);

			} finally {
				closeQuietly(output);

			}
		} finally {
			closeQuietly(input);
		}
	}

	// copy from InputStream
	// -----------------------------------------------------------------------
	/**
	 * Copy bytes from an <code>InputStream</code> to an
	 * <code>OutputStream</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 * Large streams (over 2GB) will return a bytes copied value of
	 * <code>-1</code> after the copy has completed since the correct number of
	 * bytes cannot be returned as an int. For large streams use the
	 * <code>copyLarge(InputStream, OutputStream)</code> method.
	 * 
	 * @param input
	 *            the <code>InputStream</code> to read from
	 * @param output
	 *            the <code>OutputStream</code> to write to
	 * @return the number of bytes copied
	 * @throws NullPointerException
	 *             if the input or output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws ArithmeticException
	 *             if the byte count is too large
	 * @since Commons IO 1.1
	 */
	public static int copy(InputStream input, OutputStream output)
			throws IOException {
		long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	/**
	 * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
	 * <code>OutputStream</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * 
	 * @param input
	 *            the <code>InputStream</code> to read from
	 * @param output
	 *            the <code>OutputStream</code> to write to
	 * @return the number of bytes copied
	 * @throws NullPointerException
	 *             if the input or output is null
	 * @throws IOException
	 *             if an I/O error occurs
	 * @since Commons IO 1.3
	 */
	/**
	 * The default buffer size to use.
	 */
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	public static long copyLarge(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	// read toByteArray
	// -----------------------------------------------------------------------
	/**
	 * Get the contents of an <code>InputStream</code> as a <code>byte[]</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * 
	 * @param input
	 *            the <code>InputStream</code> to read from
	 * @return the requested byte array
	 * @throws NullPointerException
	 *             if the input is null
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output);
		return output.toByteArray();
	}

	/**
	 * Unconditionally close an <code>InputStream</code>.
	 * <p>
	 * Equivalent to {@link InputStream#close()}, except any exceptions will be
	 * ignored. This is typically used in finally blocks.
	 * 
	 * @param input
	 *            the InputStream to close, may be null or already closed
	 */
	public static void closeQuietly(InputStream input) {
		try {
			if (input != null) {
				input.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}

	/**
	 * Unconditionally close an <code>OutputStream</code>.
	 * <p>
	 * Equivalent to {@link OutputStream#close()}, except any exceptions will be
	 * ignored. This is typically used in finally blocks.
	 * 
	 * @param output
	 *            the OutputStream to close, may be null or already closed
	 */
	public static void closeQuietly(OutputStream output) {
		try {
			if (output != null) {
				output.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}

}
