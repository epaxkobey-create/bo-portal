package com.nv.commons.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

public class ResponseUtils {

	public static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

	public static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8";

	public static final String XML_CONTENT_TYPE = "text/xml, application/xml; charset=UTF-8";

	public static final String TEXT_CONTENT_TYPE = "text/plain; charset=UTF-8";

	public static final String TXT_CONTENT_TYPE = "application/octet-stream; charset=UTF-8";

	public static final String HTML_CONTENT_TYPE = "text/html;charset=UTF-8";

	public static final String ZIP_CONTENT_TYPE = "application/zip";
	public static final String CSV_CONTENT_TYPE = "application/csv; charset=UTF-8";

	public static void sendResponse(HttpServletResponse response, String message) {
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = null;

		try {
			response.setContentLength(StringUtil.countByteArrayLengthOfString(message));
			writer = response.getWriter();
			writer.write(message);
			writer.flush();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	/*
	 *
	 */
	public static void respondPlainTextMessage(HttpServletResponse response, String message) {
		response.setContentType(TEXT_CONTENT_TYPE);
		ResponseUtils.sendResponse(response, message);
	}

	/*
	 * return status: 200
	 */
	public static void respondSuccessWithMessage(HttpServletResponse response, String message) {
		response.setContentType(JSON_CONTENT_TYPE);
		ResponseUtils.sendResponse(response, JSONUtils.getJSONString("status", "200", "message", message));
	}

	/*
	 * return status 500
	 */
	public static void respondError(HttpServletResponse response, String errorMessage) {

		final String jsonString = JSONUtils.getJSONString(jGenerator -> {
			jGenerator.writeStringField("status", "500");
			jGenerator.writeStringField("message", errorMessage);
			// for web datatable ajax
			jGenerator.writeArrayFieldStart("data");
			jGenerator.writeEndArray();
		});

		response.setContentType(JSON_CONTENT_TYPE);
		ResponseUtils.sendResponse(response, jsonString);
	}

	public static void sendJsonErrorResponse(HttpServletResponse response, Object errorObj) throws IOException {
		sendJsonErrorResponse(response, JSONUtils.toJsonString(errorObj));
	}

	public static void sendJsonResponse(HttpServletResponse response, Object obj) throws IOException {
		sendJsonResponse(response, JSONUtils.toJsonString(obj));
	}

	public static void sendXMLResponse(HttpServletResponse response, String xml) throws IOException {
		response.setContentType(XML_CONTENT_TYPE);
		ResponseUtils.sendResponse(response, xml);
	}

	public static void sendJsonResponse(HttpServletResponse response, String json) {
		response.setContentType(JSON_CONTENT_TYPE);
		ResponseUtils.sendResponse(response, json);
	}

	public static void sendJsonErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
		response.setContentType(JSON_CONTENT_TYPE);
		ResponseUtils.sendResponse(response, JSONUtils.getJSONString("status", "500", "error", errorMessage));
	}

	public static void sendExcelFileResponse(HttpServletResponse response, String filename, byte[] outArray)
		throws IOException {
		response.setContentType(XLSX_CONTENT_TYPE);
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xlsx");

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			out.write(outArray);
			out.flush();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public static void sendTextFileResponse(HttpServletResponse response, String filename, byte[] outArray)
		throws IOException {
		response.setContentType(TXT_CONTENT_TYPE);
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".txt");

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			out.write(outArray);
			out.flush();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public static void sendCsvFileResponse(HttpServletResponse response, String filename, String data) {
		response.setContentType(TXT_CONTENT_TYPE);
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");

		OutputStreamWriter out = null;
		try {
			out = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
			out.write(data);
			out.flush();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public static void sendZipFileResponse(HttpServletResponse response, String filename, byte[] outArray)
		throws IOException {
		response.setContentType(HTML_CONTENT_TYPE);
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".zip");

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			out.write(outArray);
			out.flush();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public static void sendImageFileResponse(HttpServletResponse response, byte[] outArray)
		throws IOException {
		response.setContentType(TXT_CONTENT_TYPE);
		response.setHeader("Content-Disposition", "image/png");

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			out.write(outArray);
			out.flush();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void sendExcelFileResponse(HttpServletResponse response, byte[] outArray) throws IOException {
		response.setContentType(TXT_CONTENT_TYPE);
		response.setHeader("Content-Disposition", "xls/xlsx");

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			out.write(outArray);
			out.flush();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void respondPlainHtml(HttpServletResponse response, String message) {
		response.setContentType(HTML_CONTENT_TYPE);
		ResponseUtils.sendResponse(response, message);
	}

	public static void sendSqlFileResponse(HttpServletResponse response, String filename, String data) {
		response.setContentType(TXT_CONTENT_TYPE);
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".sql");

		OutputStreamWriter out = null;
		try {
			out = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
			out.write(data);
			out.flush();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public static void sendCsvFileResponse(HttpServletResponse response, String filename, byte[] outArray) {
		response.setContentType(CSV_CONTENT_TYPE);
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			out.write(outArray);
			out.flush();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void sendHtmlFileResponse(HttpServletResponse response, String filename, byte[] outArray) {
		response.setContentType(HTML_CONTENT_TYPE);
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".html");

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			out.write(outArray);
			out.flush();
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
