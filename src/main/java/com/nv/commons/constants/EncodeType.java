package com.nv.commons.constants;

public enum EncodeType {
	Base64() {
		public String encodeToString(byte[] content) {
			return java.util.Base64.getEncoder().encodeToString(content);
		}

		public byte[] decode(String content) {
			return java.util.Base64.getDecoder().decode(content);
		}
	},
	Hex() {
		public String encodeToString(byte[] content) {
			StringBuilder output = new StringBuilder(content.length * 2);
			for (byte anInput : content) {
				int current = anInput & 0xff;
				if (current < 16) {
					output.append("0");
				}
				output.append(Integer.toString(current, 16));
			}
			return output.toString();
		}

		public byte[] decode(String content) {
			int len = content.length();
			byte[] byteArray = new byte[len / 2];
			for (int i = 0; i < len; i += 2) {
				byteArray[i / 2] = (byte) ((Character.digit(content.charAt(i), 16) << 4)
					+ Character.digit(content.charAt(i + 1), 16));
			}
			return byteArray;
		}
	};

	public abstract String encodeToString(byte[] content);

	public abstract byte[] decode(String content);
}
