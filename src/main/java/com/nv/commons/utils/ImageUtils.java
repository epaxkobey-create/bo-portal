package com.nv.commons.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.nv.commons.cache.SystemSettingCache;
import com.nv.commons.constants.SystemSettingKeyConstants;
import jakarta.xml.bind.DatatypeConverter;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

public class ImageUtils {

	private static final Pattern imagePattern = Pattern.compile("src=(['\"])(.*?)\\1");

	private static final String IMAGE_CRASH_DIR = SystemSettingCache.getInstance()
		.getByKey(SystemSettingKeyConstants.DOWNLOARD_TEMPLATE_FLODER).getValue();

	/**
	 * 包含檔名前綴描述
	 * data:image/jpeg;base64,imageInBase64...
	 */
	public static byte[] convertBase64StringToBytes(String base64String) {
		int startOfBase64Data = base64String.indexOf(",") + 1;
		String imageInBase64 = base64String.substring(startOfBase64Data);
		return DatatypeConverter.parseBase64Binary(imageInBase64);
	}

	public static String convertImageToBase64String(String imagePath) throws IOException {
		BufferedImage originalImage = ImageIO.read(new File(imagePath));
		String filenameExtension = imagePath.substring(imagePath.lastIndexOf(".") + 1);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(originalImage, filenameExtension, baos);
			baos.flush();
		} finally {
			baos.close();
		}
		return Base64.encodeBase64String(baos.toByteArray());
	}

	/*
	 * 包含檔名前綴描述
	 * data:image/jpeg;base64,imageInBase64...
	 * Base64編碼原理:
	 * 把3个8位字节（3*8=24）转化为4个6位的字节（4*6=24），之后在6位的前面补两个0，形成8位一个字节的形式。 如果剩下的字符不足3个字节，用0填充，输出字符使用’=’
	 *  每8的size會補2個 '0'
		所以要把 =>   (最後的長度/8)*2  代表多補的長度減掉
		長度-(長度/8)*2 = 文件大小
	 */
	public static int getBase64StringImageSize(String base64String) {
		int startOfBase64Data = base64String.indexOf(",") + 1; //移除前綴
		String imageInBase64 = base64String.substring(startOfBase64Data,
			base64String.length());
		int index = imageInBase64.indexOf('='); //移除後補 '='
		if (index > 0) {
			imageInBase64 = imageInBase64.substring(0, index);
		}
		int length = imageInBase64.length();
		return length - (length / 8) * 2;
	}

	/**
	 * 共用方法，取得原始QR code的64位元字串，並轉為
	 *
	 * @param text   圖片字串
	 * @param width  圖片寬度
	 * @param height 圖片高度
	 * @return 圖片原碼字串
	 */
	public static String getQRCodeImageString(String text, int width, int height) throws IOException {
		String img = "";
		ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
		try {
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

			MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
			byte[] pngData = pngOutputStream.toByteArray();
			byte[] encodeBase64 = org.apache.tomcat.util.codec.binary.Base64.encodeBase64(pngData, true);
			img = new String(encodeBase64, "UTF-8");
		} catch (Exception e) {
			pngOutputStream.flush();
			LogUtils.paymentGateway.error("", e);
		} finally {
			pngOutputStream.close();
		}

		return img;
	}

	public static String getQRCodeImageByte(byte[] pngData) {
		String img = "";
		try {
			byte[] encodeBase64 = org.apache.tomcat.util.codec.binary.Base64.encodeBase64(pngData, true);
			img = new String(encodeBase64, "UTF-8");
		} catch (Exception e) {
			LogUtils.paymentGateway.error("", e);
		}
		return img;
	}

//	/**
//	 * 取得WebContent內容，篩選出圖片名稱
//	 *
//	 * @param value Content內容
//	 * @return 圖片名稱
//	 */
//	public static List<String> getUploadImageName(String value) {
//		List<String> values = new ArrayList<>();
//		if (value != null) {
//
//			value = value.replaceAll("\0", "");
//
//			Matcher matcher = imagePattern.matcher(value);
//			while (matcher.find()) {
//				values.add(matcher.group(2).replace(ContentImageType.CONTENT.getImagePath(), ""));
//			}
//		}
//		return values;
//	}

	public static byte[] compress(byte[] image, float quality) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

		BufferedImage imageJPG = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
			BufferedImage.TYPE_INT_RGB);

		imageJPG.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);

		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
		ByteArrayOutputStream resultOS = new ByteArrayOutputStream();
		ImageOutputStream ios = ImageIO.createImageOutputStream(resultOS);
		writer.setOutput(ios);

		ImageWriteParam param = writer.getDefaultWriteParam();

		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(quality);

		writer.write(null, new IIOImage(imageJPG, null, null), param);

		// close all streams
		resultOS.close();
		ios.close();
		writer.dispose();

		return resultOS.toByteArray();
	}

	public static byte[] compress(byte[] image) throws IOException {
		return compress(image, 0.1F);
	}

	public static void compress(File src, File dist, float quality) throws IOException {

		BufferedImage bufferedImage = ImageIO.read(new FileInputStream(src));

		BufferedImage imageJPG = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
			BufferedImage.TYPE_INT_RGB);

		imageJPG.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);

		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
		OutputStream os = new FileOutputStream(dist);
		ImageOutputStream ios = ImageIO.createImageOutputStream(os);
		writer.setOutput(ios);

		ImageWriteParam param = writer.getDefaultWriteParam();
		if (param.canWriteCompressed()) {
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(quality);
		}

		writer.write(null, new IIOImage(imageJPG, null, null), param);

		// close all streams
		os.close();
		ios.close();
		writer.dispose();
	}

	public static void compress(File src, File dist) throws IOException {
		compress(src, dist, 0.1F);
	}

	public static byte[] decompressZipToBytes(File file, String password) throws IOException {
		ZipInputStream zipInputStream = new ZipInputStream(
			new ByteArrayInputStream(FileUtils.readFileToByteArray(file)), password.toCharArray());
		int readLen;
		byte[] readBuffer = new byte[4096];
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		while (zipInputStream.getNextEntry() != null) {
			byteArrayOutputStream = new ByteArrayOutputStream();
			while ((readLen = zipInputStream.read(readBuffer)) != -1) {
				byteArrayOutputStream.write(readBuffer, 0, readLen);
			}
		}
		byteArrayOutputStream.close();
		return byteArrayOutputStream.toByteArray();
	}

	public static boolean checkImageFormat(String fileName) {
		return Validator.isValidateImageFormat(fileName);
	}

	public static String getCrashImage() {
		return Base64.encodeBase64String(
			com.nv.commons.utils.FileUtils.fileToByte(IMAGE_CRASH_DIR + File.separator + "image_crash.png"));
	}

	private static BufferedImage decodeGif(InputStream inputStream) {
		GifDecoder gifDecoder = new GifDecoder();
		gifDecoder.read(inputStream);
		return gifDecoder.getImage();

	}

//	public static BufferedImage read(FileItem fileItem) throws IOException {
//		BufferedImage bufferedImage;
//		try {
//			bufferedImage = ImageIO.read(fileItem.getInputStream());
//		} catch (ArrayIndexOutOfBoundsException e) {
//			// jdk 1.8版對於含有特殊規格的gif讀取會出現問題，在jdk還沒升版前使用特製的GifDecoder處理
//			//			if("java.lang.ArrayIndexOutOfBoundsException: 4096".equals(e.toString())) {
//			bufferedImage = decodeGif(fileItem.getInputStream());
//			//			}
//		}
//		return bufferedImage;
//	}
}
