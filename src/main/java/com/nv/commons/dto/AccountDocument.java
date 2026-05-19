package com.nv.commons.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.nv.commons.annotation.Column;
import com.nv.commons.constants.DocumentStatusType;
import com.nv.commons.constants.DocumentType;
import com.nv.commons.constants.KycDocumentStatusType;
import com.nv.commons.utils.FormatUtils;
import org.apache.commons.lang3.StringUtils;

public class AccountDocument {

//	public AccountDocument() {
//	}

//	public AccountDocument(long id, int websiteType, String userId, int currencyTypeId, int documentType, int documentIndex, String documentNo, String frontImagePath, String frontImageExtension, String backImagePath, String backImageExtension, String addressImageExtension, int status, String executor, String creator, Timestamp createTime, String createTimeStr, String updater, Timestamp updateTime, String updateTimeStr, String approvedUserid, Timestamp approvedTime, String approvedTimeStr, String approvedRemark, Timestamp expiredDate, String expiredDateStr, boolean openUpdateBtn, byte[] originalFrontImage, byte[] originalBackImage, byte[] originalAddressImage, String bankAccNumber, int groupType, int isDeleted, String residenceImagePath) {
//		this.id = id;
//		this.websiteType = websiteType;
//		this.userId = userId;
//		this.currencyTypeId = currencyTypeId;
//		this.documentType = documentType;
//		this.documentIndex = documentIndex;
//		this.documentNo = documentNo;
//		this.frontImagePath = frontImagePath;
//		this.frontImageExtension = frontImageExtension;
//		this.backImagePath = backImagePath;
//		this.backImageExtension = backImageExtension;
//		this.addressImageExtension = addressImageExtension;
//		this.status = status;
//		this.executor = executor;
//		this.creator = creator;
//		this.createTime = createTime;
//		this.createTimeStr = createTimeStr;
//		this.updater = updater;
//		this.updateTime = updateTime;
//		this.updateTimeStr = updateTimeStr;
//		this.approvedUserid = approvedUserid;
//		this.approvedTime = approvedTime;
//		this.approvedTimeStr = approvedTimeStr;
//		this.approvedRemark = approvedRemark;
//		this.expiredDate = expiredDate;
//		this.expiredDateStr = expiredDateStr;
//		this.openUpdateBtn = openUpdateBtn;
//		this.originalFrontImage = originalFrontImage;
//		this.originalBackImage = originalBackImage;
//		this.originalAddressImage = originalAddressImage;
//		this.bankAccNumber = bankAccNumber;
//		this.groupType = groupType;
//		this.isDeleted = isDeleted;
//		this.residenceImagePath = residenceImagePath;
//	}

	
	@Column(name = "id")
	private long id;

	@Column(name = "website_type")
	private int websiteType;

	@Column(name = "user_id")
	private String userId;

	// TODO: why need this field?
	@Column(name = "currency_type_id")
	private int currencyTypeId;

	
	@Column(name = "document_type")
	private int documentType;

	@Column(name = "document_index")
	private int documentIndex;

	
	@Column(name = "document_no")
	private String documentNo;

	
	@Column(name = "front_image_path")
	private String frontImagePath;

	//	@Column(name = "front_image_data")
	//	private String frontImageData;

	// not used to save db, is for save image to file system
	private String frontImageExtension;

	
	@Column(name = "back_image_path")
	private String backImagePath;

	//	@Column(name = "back_image_data")
	//	private String backImageData;

	// not used to save db, is for save image to file system
	private String backImageExtension;

	// not used to save db, is for save image to file system
	private String addressImageExtension;

	
	@Column(name = "status")
	private int status;

	@Column(name = "executor")
	private String executor;

	@Column(name = "creator")
	private String creator;

	
	@Column(name = "create_time")
	private Timestamp createTime;

	
	private String createTimeStr;

	
	@Column(name = "updater")
	private String updater;

	
	@Column(name = "update_time")
	private Timestamp updateTime;

	
	private String updateTimeStr;

	
	@Column(name = "approved_userid")
	private String approvedUserid;

	
	@Column(name = "approved_time")
	private Timestamp approvedTime;

	
	private String approvedTimeStr;

	
	@Column(name = "approved_remark")
	private String approvedRemark;

	
	@Column(name = "expired_date")
	private Timestamp expiredDate;

	
	private String expiredDateStr;

	private boolean openUpdateBtn;

	
	private byte[] originalFrontImage;

	
	private byte[] originalBackImage;

	
	private byte[] originalAddressImage;

	@Column(name = "bank_acc_number")
	private String bankAccNumber;

	@Column(name = "group_type")
	private int groupType;

	@Column(name = "IS_DELETED")
	private int isDeleted;

	
	@Column(name = "residence_image_path")
	private String residenceImagePath;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getCurrencyTypeId() {
		return currencyTypeId;
	}

	public void setCurrencyTypeId(int currencyTypeId) {
		this.currencyTypeId = currencyTypeId;
	}

	public int getDocumentType() {
		return documentType;
	}

	public int getDocumentIndex() {
		return documentIndex;
	}

	public void setDocumentIndex(int documentIndex) {
		this.documentIndex = documentIndex;
	}

	public String getDocumentNo() {
		return documentNo;
	}

	public void setDocumentNo(String documentNo) {
		this.documentNo = documentNo;
	}

	public String getFrontImagePath() {
		return frontImagePath;
	}

	public void setFrontImagePath(String frontImagePath) {
		this.frontImagePath = frontImagePath;
	}

	public String getFrontImageExtension() {
		return frontImageExtension;
	}

	public void setFrontImageExtension(String frontImageExtension) {
		this.frontImageExtension = frontImageExtension;
	}

	public String getBackImagePath() {
		return backImagePath;
	}

	public void setBackImagePath(String backImagePath) {
		this.backImagePath = backImagePath;
	}

	public String getBackImageExtension() {
		return backImageExtension;
	}

	public void setBackImageExtension(String backImageExtension) {
		this.backImageExtension = backImageExtension;
	}

	public String getAddressImageExtension() {
		return addressImageExtension;
	}

	public void setAddressImageExtension(String addressImageExtension) {
		this.addressImageExtension = addressImageExtension;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getExecutor() {
		return executor;
	}

	public void setExecutor(String executor) {
		this.executor = executor;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public String getCreateTimeStr() {
		return createTimeStr;
	}

	public void setCreateTimeStr(String createTimeStr) {
		this.createTimeStr = createTimeStr;
	}

	public String getUpdater() {
		return updater;
	}

	public void setUpdater(String updater) {
		this.updater = updater;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public String getUpdateTimeStr() {
		return updateTimeStr;
	}

	public void setUpdateTimeStr(String updateTimeStr) {
		this.updateTimeStr = updateTimeStr;
	}

	public String getApprovedUserid() {
		return approvedUserid;
	}

	public void setApprovedUserid(String approvedUserid) {
		this.approvedUserid = approvedUserid;
	}

	public Timestamp getApprovedTime() {
		return approvedTime;
	}

	public String getApprovedTimeStr() {
		return approvedTimeStr;
	}

	public void setApprovedTimeStr(String approvedTimeStr) {
		this.approvedTimeStr = approvedTimeStr;
	}

	public Timestamp getExpiredDate() {
		return expiredDate;
	}

	public String getExpiredDateStr() {
		return expiredDateStr;
	}

	public void setExpiredDateStr(String expiredDateStr) {
		this.expiredDateStr = expiredDateStr;
	}

	public boolean isOpenUpdateBtn() {
		return openUpdateBtn;
	}

	public void setOpenUpdateBtn(boolean openUpdateBtn) {
		this.openUpdateBtn = openUpdateBtn;
	}

	public byte[] getOriginalFrontImage() {
		return originalFrontImage;
	}

	public void setOriginalFrontImage(byte[] originalFrontImage) {
		this.originalFrontImage = originalFrontImage;
	}

	public byte[] getOriginalBackImage() {
		return originalBackImage;
	}

	public void setOriginalBackImage(byte[] originalBackImage) {
		this.originalBackImage = originalBackImage;
	}

	public byte[] getOriginalAddressImage() {
		return originalAddressImage;
	}

	public void setOriginalAddressImage(byte[] originalAddressImage) {
		this.originalAddressImage = originalAddressImage;
	}

	public String getBankAccNumber() {
		return bankAccNumber;
	}

	public void setBankAccNumber(String bankAccNumber) {
		this.bankAccNumber = bankAccNumber;
	}

	public int getGroupType() {
		return groupType;
	}

	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}

	public int getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getResidenceImagePath() {
		return residenceImagePath;
	}

	public void setResidenceImagePath(String residenceImagePath) {
		this.residenceImagePath = residenceImagePath;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
		this.createTimeStr = FormatUtils.dateFormat(createTime);
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
		this.updateTimeStr = FormatUtils.dateFormat(updateTime);
	}

	public void setApprovedTime(Timestamp approvedTime) {
		this.approvedTime = approvedTime;
		if (null != approvedTime) {
			this.approvedTimeStr = FormatUtils.dateFormat(approvedTime);
		}
	}

	public String getApprovedRemark() {
		return StringUtils.isBlank(this.approvedRemark) ? "" : this.approvedRemark;
	}

	public void setApprovedRemark(String approvedRemark) {
		this.approvedRemark = approvedRemark;
	}

	public String getPassword() {
		return websiteType + userId + documentType;
	}

	public void setExpiredDate(Timestamp expiredDate) {
		this.expiredDate = expiredDate;
		if (expiredDate != null) {
			expiredDateStr = FormatUtils.dateFormat(expiredDate, FormatUtils.DATE_PATTERN_SLASH_yyyyMMdd);
		}
	}

	public void setDocumentType(int documentType) {
		this.documentType = documentType;
		this.setGroupType(DocumentType.getInstance(documentType).getGroupType());
	}

	@JsonIgnore
	public KycDocumentStatusType getKycDocumentStatus() {

		return switch (DocumentStatusType.getInstance(this.status)) {
			case NO_RECORD, REMOVED, CREATED -> KycDocumentStatusType.UNVERIFIED;
			case PENDING, ON_HOLD -> KycDocumentStatusType.VERIFYING;
			case APPROVED -> KycDocumentStatusType.VERIFIED;
			case REJECTED -> KycDocumentStatusType.FAILED;
			default -> KycDocumentStatusType.UNVERIFIED;
		};
	}

	@Override
	public String toString() {
		return "AccountDocument{" +
				"id=" + id +
				", websiteType=" + websiteType +
				", userId='" + userId + '\'' +
				", documentType=" + documentType +
				", documentNo='" + documentNo + '\'' +
				", status=" + status +
				", createTime=" + createTime +
				", updateTime=" + updateTime +
				'}';
	}
}
