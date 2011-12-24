package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides static utility functions
 * @author stevearc
 *
 */
public class Util {
	private static final String HEXES = "0123456789ABCDEF";

	public static String convertToHex(byte[] raw) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b: raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4))
			.append(HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

	/**
	 * 
	 * @param text
	 * @return The SHA1 hash of the input string
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String SHA1(String text) {
		try {
			MessageDigest md;
			md = MessageDigest.getInstance("SHA-1");
			byte[] sha1hash = new byte[40];
			md.update(text.getBytes("iso-8859-1"), 0, text.length());
			sha1hash = md.digest();
			return convertToHex(sha1hash);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Could not complete SHA1 hash!");
	}

	/**
	 * Calculate a SHA1 checksum of a file
	 * @param filename
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static byte[] SHA1Checksum(String filename) throws NoSuchAlgorithmException, IOException {
		InputStream fis =  new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("SHA1");
		int numRead;
		while ((numRead = fis.read(buffer)) != -1) {
			complete.update(buffer, 0, numRead);
		}
		fis.close();
		return complete.digest();
	}

	/**
	 * 
	 * @param filename
	 * @return The data from the file
	 * @throws IOException
	 */
	public static byte[] getFileData(String filename) throws IOException {
		File file = new File(filename);
		if (!file.exists())
			throw new IOException("File " + filename + " does not exist");
		FileInputStream is = new FileInputStream(file);

		long length = file.length();

		// Create the byte array to hold the data
		byte[] data = new byte[(int)length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < data.length
				&& (numRead=is.read(data, offset, data.length-offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < data.length) {
			throw new IOException("Could not completely read file "+filename);
		}

		is.close();
		return data;
	}

	public static String formatTime(long millis) {
		long secs = millis/1000;
		long s = secs % 60;
		long m = (secs/60) % 60;
		long h = secs/3600 % 24;
		long d = secs/(3600 * 24);
		StringBuilder sb = new StringBuilder();
		if (d > 0)
			sb.append(d + "d ");
		if (d > 0 || h > 0)
			sb.append(h + "h ");
		if (d > 0 || m > 0 || h > 0)
			sb.append(m + "m ");
		sb.append(s + "s");

		return sb.toString();
	}
	
	public static void writeFileData(File dataFile, String targetFile) throws IOException {
		FileInputStream istream = new FileInputStream(dataFile);
		FileOutputStream ostream = new FileOutputStream(targetFile);
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = istream.read(buffer)) != -1) {
			ostream.write(buffer, 0, len);
		}
		istream.close();
		ostream.close();
	}

	public static boolean initializedBattlecode() {
		File bserver = new File("./battlecode/lib/battlecode-server.jar");
		return bserver.exists();
	}
}
