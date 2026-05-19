package com.nv.commons.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;

/**
 * for read properties, json, yaml files
 * <p>
 * 參考 quartz public void initialize() {}
 * <p>
 * ref <a href="https://github.com/quartz-scheduler/quartz/blob/main/quartz/src/main/java/org/quartz/impl/StdSchedulerFactory.java">...</a>
 */
public class ResourceUtils {

	/**
	 *
	 */
	@NotNull
	public static InputStream getResourceAsStream(String fileName) throws IOException {

		InputStream in;

		File targetFile = new File(fileName);

		if (targetFile.exists()) {

			in = new BufferedInputStream(new FileInputStream(fileName));

		} else {
			ClassLoader loader = ResourceUtils.class.getClassLoader();

			if (loader == null) {
				loader = findClassloader();
			}
			if (loader == null) {
				throw new RuntimeException("Unable to find a class loader on the current thread or class.");
			}

			in = loader.getResourceAsStream(fileName);

			if (in == null) {
				// for JUnit test
				in = ResourceUtils.class.getResourceAsStream(fileName);
			}
			if (in == null) {
				throw new RuntimeException(fileName + " not found in class path");
			}
		}

		return in;
	}

	public static Map<String, Object> readProperties(List<String> keys, String propertiesFilePath) {
		Map<String, Object> resultMap = new HashMap<>();
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = findClassloader().getResourceAsStream(propertiesFilePath);
			// load a properties file
			prop.load(input);

			// get the property value and print it out
			for (String key : keys) {
				resultMap.put(key, prop.getProperty(key));
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return resultMap;
	}

	/**
	 *
	 */
	public static ClassLoader findClassloader() {
		// work-around set context loader for windows-service started JVMs (QUARTZ-748)
		if (Thread.currentThread().getContextClassLoader() == null) {

			ClassLoader loader = ResourceUtils.class.getClassLoader();
			if (loader != null) {
				Thread.currentThread().setContextClassLoader(loader);
			}
		}
		return Thread.currentThread().getContextClassLoader();
	}

}
