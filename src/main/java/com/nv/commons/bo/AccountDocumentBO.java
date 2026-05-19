package com.nv.commons.bo;

import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.DocumentStatusType;
import com.nv.commons.constants.DocumentType;
import com.nv.commons.constants.KycDocumentStatusType;
import com.nv.commons.constants.UpdatedAttributeType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dao.AccountDAO;
import com.nv.commons.dao.AccountDocumentDAO;
import com.nv.commons.dto.AccountDocument;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.KycPersonalInfo;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.exceptions.Deviation;
import com.nv.commons.model.database.DBPool;
import com.nv.commons.utils.AccountDocumentUtil;
import com.nv.commons.utils.AccountUtils;
import com.nv.commons.utils.DbUtils;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import jakarta.servlet.http.Part;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AccountDocumentBO {

	@Deprecated
	public static void apply(String userId, WebSiteType webSiteType, AccountDocument... documents)
		throws Exception {

		for (AccountDocument document : documents) {
			DocumentType documentType = DocumentType.getInstance(document.getDocumentType());
			// upload image & set image path
			String frontImagePath = AccountDocumentUtil.uploadDocumentImage(document.getOriginalFrontImage(),
				document.getFrontImageExtension(),
				documentType, userId, webSiteType);

			String backImagePath = AccountDocumentUtil.uploadDocumentImage(document.getOriginalBackImage(),
				document.getBackImageExtension(),
				documentType, userId, webSiteType);

			String residenceImagePath = AccountDocumentUtil.uploadDocumentImage(document.getOriginalAddressImage(),
				document.getAddressImageExtension(),
				documentType, userId, webSiteType);

			document.setFrontImagePath(frontImagePath);
			document.setBackImagePath(backImagePath);
			document.setResidenceImagePath(residenceImagePath);
		}

		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			AccountDocumentDAO.batchInsert(conn, documents);

			AccountDAO.updateUpdatedAttribute(conn, userId, webSiteType,
				UpdatedAttributeType.ACCOUNT_DOCUMENT);

			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.accountDocument.error(e.getMessage(), e);
			throw new Deviation("global.text.pleaseContactCustomerService");
		} finally {
			DbUtils.close(conn);
		}
	}

	public static long apply(String userId, WebSiteType webSiteType, AccountDocument document)
		throws Exception {

		DocumentType documentType = DocumentType.getInstance(document.getDocumentType());
		// upload image & set image path
		String frontImagePath = AccountDocumentUtil.uploadDocumentImage(
			document.getOriginalFrontImage(), document.getFrontImageExtension(),
			documentType, userId, webSiteType);

		String backImagePath = AccountDocumentUtil.uploadDocumentImage(
			document.getOriginalBackImage(), document.getBackImageExtension(),
			documentType, userId, webSiteType);

		String residenceImagePath = AccountDocumentUtil.uploadDocumentImage(
			document.getOriginalAddressImage(), document.getAddressImageExtension(),
			documentType, userId, webSiteType);

		document.setFrontImagePath(frontImagePath);
		document.setBackImagePath(backImagePath);
		document.setResidenceImagePath(residenceImagePath);

		long accountDocumentId;

		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			accountDocumentId = AccountDocumentDAO.insert(conn, document);

			if (accountDocumentId == 0) {
				throw new Exception("AccountDocument Insert Error");
			}

			AccountDAO.updateUpdatedAttribute(conn, userId, webSiteType,
				UpdatedAttributeType.ACCOUNT_DOCUMENT);

			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.accountDocument.error(e.getMessage(), e);
			throw new Deviation("global.text.pleaseContactCustomerService");
		} finally {
			DbUtils.close(conn);
		}

		return accountDocumentId;
	}

	public static AccountDocument findById(long accountDocumentId) {
		try (Connection conn = DBPool.getReadConnection()) {
			return AccountDocumentDAO.findById(conn, accountDocumentId);
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return null;
	}

	public static List<AccountDocument> findAccountDocuments(String userId, WebSiteType webSiteType) {
		List<AccountDocument> accountDocuments = null;

		try (Connection conn = DBPool.getReadConnection()) {
			accountDocuments = AccountDocumentDAO.findDocumentByUserId(conn, userId, webSiteType);

			LogUtils.accountDocument.info("found documents: {}", JSONUtils.getJSONArrayString(accountDocuments));
		} catch (Exception e) {
			LogUtils.accountDocument.error(e.getMessage(), e);
		}

		if (accountDocuments == null) {
			accountDocuments = Collections.emptyList();
		}

		return accountDocuments;
	}

	public static void updateAfterSumsubReviewed(AccountDocument accountDocument, CurrencyType currencyType,
		List<AccountUpdateLog> accountUpdateLogList) throws SQLException {
		Connection conn = null;

		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			AccountDocument accountDocumentOld = findById(accountDocument.getId());

			int updateCount = AccountDocumentDAO.updateAfterSumsubReviewed(conn, accountDocument);

			if (updateCount != 1) {
				throw new RuntimeException("AccountDocument Update Error");
			}

			if (accountUpdateLogList != null) {
				List<Object[]> updates = List.of(
					new Object[] {AccountUpdateType.DOCUMENT_EXPIRY_DATE, "Expiry Date",
						accountDocumentOld.getExpiredDateStr(), accountDocument.getExpiredDateStr()},
					new Object[] {AccountUpdateType.DOCUMENT_FRONT_PHOTO, "Document Photo (Front-Side)",
						accountDocumentOld.getFrontImagePath(), accountDocument.getFrontImagePath()},
					new Object[] {AccountUpdateType.DOCUMENT_BACK_PHOTO, "Document Photo (Back-Side)",
						accountDocumentOld.getBackImagePath(), accountDocument.getBackImagePath()},
					new Object[] {AccountUpdateType.DOCUMENT_ADDRESS_PHOTO, "Document Photo (Address)",
						accountDocumentOld.getResidenceImagePath(), accountDocument.getResidenceImagePath()},
					new Object[] {AccountUpdateType.KYC_VERIFICATION_REMARK, "Verification Remark",
						accountDocumentOld.getApprovedRemark(), accountDocument.getApprovedRemark()}
				);

				updates.forEach(update -> {
					AccountUpdateType type = (AccountUpdateType) update[0];
					String fieldName = (String) update[1];
					String before = Objects.toString(update[2], "-");
					String after = Objects.toString(update[3], "-");

					if (!before.equals(after)) {
						accountUpdateLogList.add(AccountUtils.getAccountUpdateLog(
							accountDocument.getUserId(), accountDocument.getWebsiteType(), type,
							new UpdateRecord(before, after, "Sumsub modified " + fieldName + "."),
							"Sumsub", "0.0.0.0", currencyType.unique()));
					}
				});
			}

			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.accountDocument.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.close(conn);
		}

	}

	public static boolean updateStatus(long id, String userId, WebSiteType webSiteType, DocumentType documentType,
		DocumentStatusType status,
		String updater) {
		Connection conn = null;

		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			AccountDocumentDAO.updateStatus(conn, id, userId, webSiteType, documentType, status, updater);

			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.accountDocument.error(e.getMessage(), e);
			return false;
		} finally {
			DbUtils.close(conn);
		}

		return true;
	}

	public static KycDocumentStatusType getKycDocumentStatus(int status) {

		return switch (DocumentStatusType.getInstance(status)) {
			case NO_RECORD, REMOVED, CREATED -> KycDocumentStatusType.UNVERIFIED;
			case PENDING, ON_HOLD -> KycDocumentStatusType.VERIFYING;
			case APPROVED -> KycDocumentStatusType.VERIFIED;
			case REJECTED -> KycDocumentStatusType.FAILED;
			default -> KycDocumentStatusType.UNVERIFIED;
		};
	}

	public static void updateRemarkOrImagePathOrExpiryDate(AccountDocument accountDocument,
		Part frontPhotoFileItem, Part backPhotoFileItem, Part addressPhotoFileItem,
		String updater, String verificationRemark, String updaterIp, int currencyId, Timestamp expiryDate,
		boolean frontPhotoChanged, boolean backPhotoChanged, boolean addressPhotoChanged)
		throws IOException {

		long accountDocumentId = accountDocument.getId();
		String userId = accountDocument.getUserId();
		WebSiteType webSiteType = WebSiteType.getInstance(accountDocument.getWebsiteType());
		DocumentType documentType = DocumentType.getInstance(accountDocument.getDocumentType());

		// If changed but Part is null, use empty string to indicate "clear this field"
		String frontPhotoPath = frontPhotoChanged ?
			(frontPhotoFileItem != null ? getPhotoPath(frontPhotoFileItem, documentType, userId, webSiteType) : "")
			: null;

		String backPhotoPath = backPhotoChanged ?
			(backPhotoFileItem != null ? getPhotoPath(backPhotoFileItem, documentType, userId, webSiteType) : "")
			: null;

		String residenceImagePath = addressPhotoChanged ?
			(addressPhotoFileItem != null ? getPhotoPath(addressPhotoFileItem, documentType, userId, webSiteType) : "")
			: null;

		String oldFrontPhotoPath = accountDocument.getFrontImagePath();
		String oldBackPhotoPath = accountDocument.getBackImagePath();
		String oldResidenceImagePath = accountDocument.getResidenceImagePath();
		int updateCount;
		Connection conn = null;
		try {
			conn = DBPool.getWriteConnection();
			conn.setAutoCommit(false);

			updateCount = AccountDocumentDAO.updateRemarkOrImagePathOrExpiryDate(conn, accountDocumentId, userId,
				webSiteType,
				documentType,
				verificationRemark,
				frontPhotoPath, backPhotoPath, residenceImagePath,
				updater, expiryDate);

			LogUtils.accountDocument.info("update to account document table");
			conn.commit();

		} catch (Exception e) {
			DbUtils.rollback(conn);
			LogUtils.accountDocument.error(e.getMessage(), e);
			throw new RuntimeException("global.text.pleaseContactCustomerService");
		} finally {
			DbUtils.close(conn);
		}

		if (updateCount > 0) {
			List<AccountUpdateLog> accountUpdateLogList = new ArrayList<>();

			List<Object[]> updates = List.of(
				new Object[] {AccountUpdateType.DOCUMENT_FRONT_PHOTO, "Document Front Image",
					oldFrontPhotoPath, frontPhotoPath},
				new Object[] {AccountUpdateType.DOCUMENT_BACK_PHOTO, "Document Back Image",
					oldBackPhotoPath, backPhotoPath},
				new Object[] {AccountUpdateType.DOCUMENT_ADDRESS_PHOTO, "Residence Image",
					oldResidenceImagePath, residenceImagePath},
				new Object[] {AccountUpdateType.KYC_VERIFICATION_REMARK, "Verification Remark",
					accountDocument.getApprovedRemark(), verificationRemark},
				new Object[] {AccountUpdateType.DOCUMENT_EXPIRY_DATE, "Expiry Date",
					accountDocument.getExpiredDate(), expiryDate
				}
			);

			updates.forEach(update -> {
				AccountUpdateType type = (AccountUpdateType) update[0];
				String fieldName = (String) update[1];
				String before = update[2] != null ? Objects.toString(update[2]) : "-";
				String after = update[3] != null ? Objects.toString(update[3]) : null;

				if (after != null && !after.equals(before)) {
					accountUpdateLogList.add(AccountUtils.getAccountUpdateLog(
						accountDocument.getUserId(), accountDocument.getWebsiteType(), type,
						new UpdateRecord(before, after, "update" + fieldName + "."),
						updater, updaterIp, currencyId));
				}
			});

			if (!accountUpdateLogList.isEmpty()) {
				LogUtils.accountDocument.info("insert to account update log (update function)");
				AccountUpdateLogBO.batchInsert(accountUpdateLogList);
			}
		}
	}

	@Nullable
	private static String getPhotoPath(Part fileItem, DocumentType documentType, String userId,
		WebSiteType webSiteType) throws IOException {
		if (fileItem != null) {
			return AccountDocumentUtil.uploadDocumentImage(fileItem.getInputStream().readAllBytes(),
				getFileExtension(fileItem.getName()),
				documentType, userId, webSiteType);
		}
		return null;
	}

	@NotNull
	private static String getFileExtension(String fileName) {
		String fileExtension = "";
		int lastDotIndex = fileName.lastIndexOf('.');
		if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
			fileExtension = fileName.substring(lastDotIndex + 1).toLowerCase();
		}
		return fileExtension;
	}

	public static boolean updateKycStatus(String userId, WebSiteType webSiteType, DocumentType documentType,
		DocumentStatusType documentStatusType, String updater, String updaterIp, CurrencyType currencyType,
		AccountDocument accountDocument) {

		boolean result = updateStatus(
			accountDocument.getId(), userId, webSiteType, documentType, documentStatusType, updater
		);

		if (result) {
			AccountUpdateLog accountUpdateLog = AccountUtils.getAccountUpdateLog(userId,
				webSiteType.unique(), AccountUpdateType.KYC_VERIFICATION_STATUS,
				new UpdateRecord(String.valueOf(accountDocument.getKycDocumentStatus().unique()),
					String.valueOf(documentStatusType.unique()), "Update verification status"),
				updater, updaterIp, currencyType.unique());

			AccountUpdateLogBO.insert(accountUpdateLog);
			return true;
		}
		return false;
	}

	// TODO:: Enhance this function: To check which field is needed based on the configuration
	public static boolean checkKYCCompleteInformation(KycPersonalInfo kycPersonalInfo,
		AccountDocument accountDocument) {

		return StringUtils.isNotEmpty(accountDocument.getFrontImagePath()) &&
			StringUtils.isNotEmpty(accountDocument.getResidenceImagePath()) &&
			accountDocument.getExpiredDate() != null &&
			StringUtils.isNotEmpty(kycPersonalInfo.getFirstName()) &&
			// StringUtils.isNotEmpty(kycPersonalInfo.getLastName())
			StringUtils.isNotEmpty(kycPersonalInfo.getDocumentNo()) &&
			StringUtils.isNotEmpty(kycPersonalInfo.getStreet()) &&
			StringUtils.isNotEmpty(kycPersonalInfo.getCity()) &&
			StringUtils.isNotEmpty(kycPersonalInfo.getPostalCode());
	}

}
