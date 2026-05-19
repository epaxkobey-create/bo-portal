package com.nv.commons.constants;

import com.nv.commons.exceptions.Deviation;

import java.nio.charset.StandardCharsets;
import java.util.Set;
public enum ImageType {
	JPEG {
		@Override
		public boolean isValidImageType(byte[] header) {
			assert header != null;
			if (header.length < 3) {
				return false;
			}
			// JPEG: FF D8 FF
			return (header[0] & 0xFF) == 0xFF
				&& (header[1] & 0xFF) == 0xD8
				&& (header[2] & 0xFF) == 0xFF;
		}

		@Override
		public boolean isValidFileExtension(String extension) {
			return "jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension);
		}
	},
	PNG {
		@Override
		public boolean isValidImageType(byte[] header) {
			if(header.length < 8) {
				return false;
			}
			// PNG: 89 50 4E 47 0D 0A 1A 0A
			return ((header[0] & 0xFF) == 0x89 &&
				(header[1] & 0xFF) == 0x50 &&
				(header[2] & 0xFF) == 0x4E &&
				(header[3] & 0xFF) == 0x47 &&
				(header[4] & 0xFF) == 0x0D &&
				(header[5] & 0xFF) == 0x0A &&
				(header[6] & 0xFF) == 0x1A &&
				(header[7] & 0xFF) == 0x0A);
		}

		@Override
		public boolean isValidFileExtension(String extension) {
			return "png".equalsIgnoreCase(extension);
		}
	},
	PDF {
		@Override
		public boolean isValidImageType(byte[] header) {
			if (header.length < 4) {
				return false;
			}
			// PDF: 25 50 44 46 (%PDF)
			return ((header[0] & 0xFF) == 0x25 &&
				(header[1] & 0xFF) == 0x50 &&
				(header[2] & 0xFF) == 0x44 &&
				(header[3] & 0xFF) == 0x46);
		}

		@Override
		public boolean isValidFileExtension(String extension) {
			return "pdf".equalsIgnoreCase(extension);
		}
	},
	WEBP {
		@Override
		public boolean isValidImageType(byte[] header) {
			if (header.length < 12) {
				return false;
			}
			// WEBP: RIFF....WEBP (52 49 46 46 ... 57 45 42 50)
			return ((header[0] & 0xFF) == 0x52 &&
				(header[1] & 0xFF) == 0x49 &&
				(header[2] & 0xFF) == 0x46 &&
				(header[3] & 0xFF) == 0x46 &&
				(header[8] & 0xFF) == 0x57 &&
				(header[9] & 0xFF) == 0x45 &&
				(header[10] & 0xFF) == 0x42 &&
				(header[11] & 0xFF) == 0x50);
		}

		@Override
		public boolean isValidFileExtension(String extension) {
			return "webp".equalsIgnoreCase(extension);
		}
	},
	HEIC {
		private final Set<String> HEIF_BRANDS = Set.of("heic", "heix", "mif1");

		@Override
		public boolean isValidImageType(byte[] header) {
			if (header.length < 12) {
				return false;
			}
			// HEIC: ftyp + heic/heix/mif1 (more complex, simplified check)
			if ((header[4] & 0xFF) == 0x66 &&
				(header[5] & 0xFF) == 0x74 &&
				(header[6] & 0xFF) == 0x79 &&
				(header[7] & 0xFF) == 0x70) {
				// Check for 'heic', 'heix', or 'mif1' at bytes 8-11
				String subtype = new String(new byte[] {header[8], header[9], header[10], header[11]}, StandardCharsets.US_ASCII);
				return HEIF_BRANDS.contains(subtype.toLowerCase());
			}
			return false;
		}

		@Override
		public boolean isValidFileExtension(String extension) {
			return "heic".equalsIgnoreCase(extension) || "heix".equalsIgnoreCase(extension);
		}
	},
	;

	public abstract boolean isValidImageType(byte[] header);

	public abstract boolean isValidFileExtension(String extension);


	public static boolean isSupportedImageType(byte[] header) {
		if (header == null) return false;
		for (ImageType imageType : ImageType.values()) {
			if (imageType.isValidImageType(header)) {
				return true;
			}
		}
		return false;
	}

	public static String getSupportedFileExtension(String fileName) {
		// Extract file extension
		String extension = "";
		int lastDotIndex = fileName.lastIndexOf('.');
		if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
			extension = fileName.substring(lastDotIndex + 1).toLowerCase();
		}

		for (ImageType imageType : ImageType.values()) {
			if (imageType.isValidFileExtension(extension)) {
				return extension;
			}
		}

		throw new Deviation("Upload failed: Only JPG, PNG, HEIC, WEBP or PDF files under 50 MB are allowed.");
	}
}
