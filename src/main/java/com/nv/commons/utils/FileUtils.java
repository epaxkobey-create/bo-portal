package com.nv.commons.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

/**
 * File 相關工具類
 *
 * @author Rex
 */
public class FileUtils {

	private FileUtils() {
		throw new AssertionError();
	}

	public static void close(Closeable obj) {
		if (obj == null) {
			return;
		}
		try {
			obj.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static byte[] fileToByte(String filePath) {

		if (filePath == null) {
			return null;
		}

		FileInputStream fileInputStream = null;
		byte[] bytesArray = null;

		try {

			File file = new File(filePath);
			bytesArray = new byte[(int) file.length()];

			//read file into bytes[]
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bytesArray);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(fileInputStream);
		}

		return bytesArray;

	}

	public static byte[] fileToByte(File file) {

		if (!file.exists()) {
			return null;
		}

		FileInputStream fileInputStream = null;
		byte[] bytesArray = null;

		try {
			bytesArray = new byte[(int) file.length()];

			//read file into bytes[]
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bytesArray);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(fileInputStream);
		}

		return bytesArray;
	}

	public static String mkdir(String folder) {
		File uploadDir = new File(folder);
		if (!uploadDir.exists()) {
			boolean isSuccess = uploadDir.mkdirs();
			if (!isSuccess) {
				LogUtils.SYS.error("Create folder fail, path:{}", folder);
			}
		}

		return folder;
	}

//	public static void writeByteToFile(File file, byte[] imageByte) {
//
//		InputStream in = null;
//		OutputStream out = null;
//
//		try {
//			if (file.exists()) {
//				return;
//			}
//
//			in = new ByteArrayInputStream(imageByte);
//			out = new FileOutputStream(file);
//
//			/*
//			 * Copies bytes from an InputStream to an OutputStream.
//			 *
//			 * This method buffers the input internally, so there is no need to
//			 * use a BufferedInputStream.
//			 *
//			 * Large streams (over 2GB) will return a bytes copied value of -1
//			 * after the copy has completed since the correct number of bytes
//			 * cannot be returned as an int.
//			 *
//			 * For large streams use the copyLarge(InputStream, OutputStream)
//			 * method.
//			 *
//			 * Returns: the number of bytes copied, or -1 if > Integer.MAX_VALUE
//			 */
//			// int bytesCopied = IOUtils.copy(in, out);
//			IOUtils.copy(in, out);
//
//		} catch (Exception e) {
//			LogUtils.SYS.error(file.getAbsolutePath());
//			LogUtils.SYS.error(e.getMessage(), e);
//		} finally {
//			close(in);
//			close(out);
//		}
//	}

//	public static void deleteFile(String filePath) {
//		if (filePath == null) {
//			return;
//		}
//		try {
//			File file = new File(filePath);
//			Files.deleteIfExists(file.toPath());
//		} catch (Exception e) {
//			LogUtils.SYS.error("FileUtils.deleteFile()");
//			LogUtils.SYS.error(e.getMessage(), e);
//		}
//	}

}
