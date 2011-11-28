package com.all.rds.loader.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hashcoder {

	private static MessageDigest md;

	private Hashcoder() {
	}

	static {
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
		}
	}

	public static String createHashCode(File file) {
		if (file == null) {
			return null;
		}
		byte[] hashCode = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);

			byte[] buffer = new byte[65536];

			bis.read(buffer);

			bis.close();
			fis.close();

			hashCode = md.digest(buffer);

		} catch (Exception e) {
		}
		return toHex(hashCode);
	}

	public static String toHex(byte[] hashCode) {
		if (hashCode != null) {
			StringBuilder builder = new StringBuilder();
			for (byte number : hashCode) {
				int value = number & 0x000000ff;
				builder.append(Integer.toHexString(value / 16));
				builder.append(Integer.toHexString(value % 16));
			}
			return builder.toString();
		}
		return null;
	}

	public static String digest(byte[] data) {
		return toHex(md.digest(data));
	}
}
