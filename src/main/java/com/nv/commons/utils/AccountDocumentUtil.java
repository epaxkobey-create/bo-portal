package com.nv.commons.utils;

import java.util.Optional;

import com.nv.commons.constants.DocumentType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.manager.KycDocumentManager;

public class AccountDocumentUtil {

	public static String uploadDocumentImage(byte[] documentFile, String fileExtension, DocumentType documentType, String userId, WebSiteType webSiteType) {

		if (documentFile == null) {
			return null;
		}

		String fileName = KycDocumentManager.generateFileName(fileExtension);

		String filePath = KycDocumentManager.generateLocalDocumentFolder(webSiteType, documentType)
						  + fileName;

		String fileKey = UploadImageFileUtil.uploadKycDocument(documentFile, filePath);

		if (Optional.ofNullable(fileKey).isEmpty()) {
			//			String whichSide = isFront ? "front" : "back";
			LogUtils.accountDocument
				.error("upload photo Error: website:{}, userId:{}, document type:{}",
					webSiteType, userId, documentType);

			throw new Deviation("global.text.pleaseContactCustomerService");
		}
		return fileKey;
	}

}
