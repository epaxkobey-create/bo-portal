package com.nv.commons.dto;


import com.nv.commons.annotation.Column;
import com.nv.commons.constants.BinaryStatusType;

import java.sql.Timestamp;
import java.util.Objects;

public class AccountContactInfo {

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "WEBSITE_TYPE")
    private int websiteType;

    
    @Column(name = "CONTACT_TYPE")
    private int contactType;

    
    private String content;
    
	// 0 or 1
    
    @Column(name = "VERIFIED_TYPE")
    private int verifiedType;

    
    @Column(name = "CONTENT_NO")
    private int contentNo;

    private String creator;

    @Column(name = "CREATE_TIME")
    private Timestamp createTime;

    private String updater;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    @Column(name = "IS_DELETED")
    private int isDeleted;

    public AccountContactInfo() {
        this.contentNo = 1;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getWebsiteType() {
        return websiteType;
    }

    public void setWebsiteType(int websiteType) {
        this.websiteType = websiteType;
    }

    public int getContactType() {
        return contactType;
    }

    public void setContactType(int contactType) {
        this.contactType = contactType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getVerifiedType() {
        return verifiedType;
    }

    public void setVerifiedType(int verifiedType) {
        this.verifiedType = verifiedType;
    }

    public int getContentNo() {
        return contentNo;
    }

    public void setContentNo(int contentNo) {
        this.contentNo = contentNo;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }


    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
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

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public boolean isVerified() {
        return this.verifiedType == BinaryStatusType.ACTIVE.unique();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AccountContactInfo that = (AccountContactInfo) o;
        return websiteType == that.websiteType && contactType == that.contactType && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(websiteType, contactType, content);
    }

    public AccountContactInfoVerification toAccountContactInfoVerification() {
        AccountContactInfoVerification accountContactInfoVerification = new AccountContactInfoVerification();
        accountContactInfoVerification.setUserId(this.userId);
        accountContactInfoVerification.setWebsiteType(this.websiteType);
        accountContactInfoVerification.setContactType(this.contactType);
        accountContactInfoVerification.setContent(this.content);

        return accountContactInfoVerification;
    }
}
