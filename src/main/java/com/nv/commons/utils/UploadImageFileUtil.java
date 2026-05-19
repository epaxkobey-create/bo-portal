package com.nv.commons.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UploadImageFileUtil {

	public static String uploadKycDocument(byte[] documentFile, String filePath) {
		// save byte array to file
		try {
			Path fullPath = Paths.get(filePath);

			Files.createDirectories(fullPath.getParent());

			java.nio.file.Files.write(
				fullPath,
				documentFile);

		} catch (Exception e) {
			LogUtils.SYS.error("Failed to write document file: " + e.getMessage(), e);
			return null;
		}
		return filePath;
	}


}
