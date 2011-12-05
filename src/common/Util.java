package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Provides static utility functions
 * @author stevearc
 *
 */
public class Util {
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
			} while(two_halfs++ < 1);
		} 
		return buf.toString();
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
		Random rand = new Random();
		String s = rand.nextLong() + "" + rand.nextLong() + "" + rand.nextLong();
		return s.substring(0, 40);
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
}
