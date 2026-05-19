package com.nv.commons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for extracting requirements from Excel (.xlsx) files.
 * Uses Apache POI for reading Excel files.
 */
public class XlsxRequirementExtractor {

	private XlsxRequirementExtractor() {
		throw new AssertionError();
	}

	/**
	 * Extract all sheets from an xlsx file.
	 *
	 * @param filePath path to the xlsx file
	 * @return Map with sheet name as key and list of rows (each row is a list of cell values) as value
	 * @throws IOException if file cannot be read
	 */
	public static Map<String, List<List<String>>> extractAllSheets(String filePath) throws IOException {
		Map<String, List<List<String>>> result = new LinkedHashMap<>();

		try (FileInputStream fis = new FileInputStream(filePath);
			 Workbook workbook = new XSSFWorkbook(fis)) {

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				Sheet sheet = workbook.getSheetAt(i);
				String sheetName = sheet.getSheetName();
				List<List<String>> sheetData = extractSheetData(sheet);
				result.put(sheetName, sheetData);
			}
		}

		return result;
	}

	/**
	 * Extract a specific sheet by name from an xlsx file.
	 *
	 * @param filePath path to the xlsx file
	 * @param sheetName name of the sheet to extract
	 * @return List of rows (each row is a list of cell values)
	 * @throws IOException if file cannot be read
	 * @throws IllegalArgumentException if sheet name not found
	 */
	public static List<List<String>> extractSheet(String filePath, String sheetName) throws IOException {
		try (FileInputStream fis = new FileInputStream(filePath);
			 Workbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				throw new IllegalArgumentException("Sheet not found: " + sheetName);
			}

			return extractSheetData(sheet);
		}
	}

	/**
	 * Extract a specific sheet by index from an xlsx file.
	 *
	 * @param filePath path to the xlsx file
	 * @param sheetIndex index of the sheet (0-based)
	 * @return List of rows (each row is a list of cell values)
	 * @throws IOException if file cannot be read
	 */
	public static List<List<String>> extractSheetByIndex(String filePath, int sheetIndex) throws IOException {
		try (FileInputStream fis = new FileInputStream(filePath);
			 Workbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheetAt(sheetIndex);
			return extractSheetData(sheet);
		}
	}

	/**
	 * Extract all sheets from an xlsx file and convert to JSON string.
	 *
	 * @param filePath path to the xlsx file
	 * @return JSON string representation of all sheets
	 * @throws IOException if file cannot be read or JSON conversion fails
	 */
	public static String extractToJson(String filePath) throws IOException {
		Map<String, List<List<String>>> data = extractAllSheets(filePath);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
	}

	/**
	 * Extract all sheets from an xlsx file and convert to Markdown format.
	 *
	 * @param filePath path to the xlsx file
	 * @return Markdown string representation of all sheets
	 * @throws IOException if file cannot be read
	 */
	public static String extractToMarkdown(String filePath) throws IOException {
		Map<String, List<List<String>>> data = extractAllSheets(filePath);
		StringBuilder markdown = new StringBuilder();

		String fileName = Paths.get(filePath).getFileName().toString();
		markdown.append("# ").append(fileName).append("\n\n");

		for (Map.Entry<String, List<List<String>>> entry : data.entrySet()) {
			String sheetName = entry.getKey();
			List<List<String>> rows = entry.getValue();

			markdown.append("## ").append(sheetName).append("\n\n");

			if (rows.isEmpty()) {
				markdown.append("*Empty sheet*\n\n");
				continue;
			}

			// Create table header
			List<String> headerRow = rows.get(0);
			markdown.append("|");
			for (String cell : headerRow) {
				markdown.append(" ").append(escapeMarkdown(cell)).append(" |");
			}
			markdown.append("\n");

			// Create separator row
			markdown.append("|");
			for (int i = 0; i < headerRow.size(); i++) {
				markdown.append("---|");
			}
			markdown.append("\n");

			// Create data rows
			for (int i = 1; i < rows.size(); i++) {
				List<String> row = rows.get(i);
				markdown.append("|");
				for (int j = 0; j < headerRow.size(); j++) {
					String cellValue = j < row.size() ? row.get(j) : "";
					markdown.append(" ").append(escapeMarkdown(cellValue)).append(" |");
				}
				markdown.append("\n");
			}
			markdown.append("\n");
		}

		return markdown.toString();
	}

	/**
	 * Get sheet names from an xlsx file.
	 *
	 * @param filePath path to the xlsx file
	 * @return List of sheet names
	 * @throws IOException if file cannot be read
	 */
	public static List<String> getSheetNames(String filePath) throws IOException {
		List<String> sheetNames = new ArrayList<>();

		try (FileInputStream fis = new FileInputStream(filePath);
			 Workbook workbook = new XSSFWorkbook(fis)) {

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				sheetNames.add(workbook.getSheetName(i));
			}
		}

		return sheetNames;
	}

	/**
	 * Process all xlsx files in a directory.
	 *
	 * @param directoryPath path to the directory containing xlsx files
	 * @return Map with file name as key and extracted data as value
	 * @throws IOException if directory cannot be read
	 */
	public static Map<String, Map<String, List<List<String>>>> processDirectory(String directoryPath) throws IOException {
		Map<String, Map<String, List<List<String>>>> result = new LinkedHashMap<>();

		try (Stream<Path> paths = Files.walk(Paths.get(directoryPath), 1)) {
			paths.filter(Files::isRegularFile)
				 .filter(p -> p.toString().toLowerCase().endsWith(".xlsx"))
				 .sorted()
				 .forEach(path -> {
					 try {
						 String fileName = path.getFileName().toString();
						 Map<String, List<List<String>>> fileData = extractAllSheets(path.toString());
						 result.put(fileName, fileData);
					 } catch (IOException e) {
						 System.err.println("Error processing file: " + path + " - " + e.getMessage());
					 }
				 });
		}

		return result;
	}

	private static List<List<String>> extractSheetData(Sheet sheet) {
		List<List<String>> rows = new ArrayList<>();

		int lastRowNum = sheet.getLastRowNum();
		int maxCols = 0;

		// First pass: determine max columns
		for (int i = 0; i <= lastRowNum; i++) {
			Row row = sheet.getRow(i);
			if (row != null && row.getLastCellNum() > maxCols) {
				maxCols = row.getLastCellNum();
			}
		}

		// Second pass: extract data
		for (int i = 0; i <= lastRowNum; i++) {
			Row row = sheet.getRow(i);
			List<String> rowData = new ArrayList<>();

			if (row == null) {
				// Add empty row
				for (int j = 0; j < maxCols; j++) {
					rowData.add("");
				}
			} else {
				for (int j = 0; j < maxCols; j++) {
					Cell cell = row.getCell(j);
					rowData.add(getCellValueAsString(cell));
				}
			}

			// Skip completely empty rows
			if (!isRowEmpty(rowData)) {
				rows.add(rowData);
			}
		}

		return rows;
	}

	private static String getCellValueAsString(Cell cell) {
		if (cell == null) {
			return "";
		}

		CellType cellType = cell.getCellType();
		if (cellType == CellType.FORMULA) {
			cellType = cell.getCachedFormulaResultType();
		}

		switch (cellType) {
			case STRING:
				return cell.getStringCellValue().trim();
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					return cell.getLocalDateTimeCellValue().toString();
				}
				double numValue = cell.getNumericCellValue();
				if (numValue == Math.floor(numValue)) {
					return String.valueOf((long) numValue);
				}
				return String.valueOf(numValue);
			case BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			case BLANK:
				return "";
			case ERROR:
				return "ERROR";
			default:
				return "";
		}
	}

	private static boolean isRowEmpty(List<String> row) {
		for (String cell : row) {
			if (cell != null && !cell.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private static String escapeMarkdown(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		return text.replace("|", "\\|")
				   .replace("\n", "<br>")
				   .replace("\r", "");
	}

	/**
	 * Main method for CLI usage.
	 * Usage: java XlsxRequirementExtractor <file-or-directory> [output-format]
	 * Output formats: json, markdown, text (default: text)
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: java XlsxRequirementExtractor <file-or-directory> [output-format]");
			System.out.println("Output formats: json, markdown, text (default: text)");
			System.exit(1);
		}

		String path = args[0];
		String format = args.length > 1 ? args[1].toLowerCase() : "text";

		try {
			File file = new File(path);

			if (file.isDirectory()) {
				Map<String, Map<String, List<List<String>>>> allData = processDirectory(path);

				for (Map.Entry<String, Map<String, List<List<String>>>> fileEntry : allData.entrySet()) {
					System.out.println("=== " + fileEntry.getKey() + " ===");
					printData(fileEntry.getValue(), format);
					System.out.println();
				}
			} else if (file.isFile() && path.toLowerCase().endsWith(".xlsx")) {
				switch (format) {
					case "json":
						System.out.println(extractToJson(path));
						break;
					case "markdown":
					case "md":
						System.out.println(extractToMarkdown(path));
						break;
					default:
						Map<String, List<List<String>>> data = extractAllSheets(path);
						printData(data, "text");
				}
			} else {
				System.err.println("Error: Path must be a directory or .xlsx file");
				System.exit(1);
			}
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}

	private static void printData(Map<String, List<List<String>>> data, String format) {
		if ("json".equals(format)) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data));
			} catch (IOException e) {
				System.err.println("Error converting to JSON: " + e.getMessage());
			}
		} else {
			for (Map.Entry<String, List<List<String>>> entry : data.entrySet()) {
				System.out.println("Sheet: " + entry.getKey());
				for (List<String> row : entry.getValue()) {
					System.out.println("  " + String.join(" | ", row));
				}
				System.out.println();
			}
		}
	}
}
