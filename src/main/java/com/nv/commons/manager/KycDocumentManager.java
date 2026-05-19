package com.nv.commons.manager;

import java.io.File;
import java.util.UUID;

import com.nv.commons.constants.DocumentType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.utils.FileUtils;

public class KycDocumentManager {

//	public final static int DOCUMENT_LIMIT_COUNT = 3;

//	public final static int DOCUMENT_LIMIT_SIZE = 5 * 1024 * 1024; // 5MB

//	public final static int DOCUMENT_LIMIT_SIZE_ACE88 = 1024 * 1024; // 1MB

//	public static int getDocumentLimitSize(WebSiteType webSiteType) {
//		return DOCUMENT_LIMIT_SIZE;
//	}

	//	@Deprecated
	//	public static String getDocumentFolder() {
	//		return SystemSettingCache.getInstance().getByKey(SystemSettingKeyConstants.KYC_DOCUMENT_FOLDER).getValue();
	//	}

	//	public static String generateFileName() {
	//		return UUID.randomUUID() + ".zip";
	//	}
	public static String generateFileName(String fileExtension) {
		return UUID.randomUUID() + "." + fileExtension;
	}

	//	public static String generateDocumentFileKey(WebSiteType webSiteType, DocumentType documentType) {
	//		String documentFolder = getDocumentFolder();
	//		String dateStr = FormatUtils.dateFormat(DateTimeBuilder.localDateTime().toTimestamp(), "yyyyMMdd");
	//		return documentFolder.substring(documentFolder.lastIndexOf("/") + 1) + "/" + webSiteType.getShortName() + "/"
	//			   + documentType.name() + "/" + dateStr + "/";
	//	}

	public static String generateLocalDocumentFolder(WebSiteType webSiteType, DocumentType documentType) {
		String folder =
			"/usr/local/data/upload/" + webSiteType.getShortName() + "/" + documentType.name() + "/";

		File fileFolder = new File(folder);

		if (!fileFolder.exists()) {
			FileUtils.mkdir(folder);
		}
		return folder;
	}

}
