package com.nv.commons.dto;

import java.sql.Timestamp;

import com.nv.commons.annotation.Column;

public class MailTemplate {

    @Column(name = "website_type")
    private int websiteType;

    @Column(name = "marketing_group")
    private String marketingGroup;
    @Column(name = "currency_type")
    private int currencyType;

    @Column(name = "language_type")
    private int languageType;

    @Column(name = "template_type")
    private int templateType;

    @Column(name = "title")
    private String title;

    private String template;

    @Column(name = "update_file_name")
    private String updateFileName;

    @Column(name = "updater")
    private String updater;

    @Column(name = "file_updater")
    private String fileUpdater;

    @Column(name = "update_file_time")
    private Timestamp updateFileTime;

    @Column(name = "update_time")
    private Timestamp updateTime;

    @Column(name = "create_time")
    private Timestamp createTime;

    public int getWebsiteType() {
        return websiteType;
    }

    public void setWebsiteType(int websiteType) {
        this.websiteType = websiteType;
    }

    public String getMarketingGroup() {
        return marketingGroup;
    }

    public void setMarketingGroup(String marketingGroup) {
        this.marketingGroup = marketingGroup;
    }

    public int getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(int currencyType) {
        this.currencyType = currencyType;
    }

    public int getLanguageType() {
        return languageType;
    }

    public void setLanguageType(int languageType) {
        this.languageType = languageType;
    }

    public int getTemplateType() {
        return templateType;
    }

    public void setTemplateType(int templateType) {
        this.templateType = templateType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getUpdateFileName() {
        return updateFileName;
    }

    public void setUpdateFileName(String updateFileName) {
        this.updateFileName = updateFileName;
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public String getFileUpdater() {
        return fileUpdater;
    }

    public void setFileUpdater(String fileUpdater) {
        this.fileUpdater = fileUpdater;
    }

    public Timestamp getUpdateFileTime() {
        return updateFileTime;
    }

    public void setUpdateFileTime(Timestamp updateFileTime) {
        this.updateFileTime = updateFileTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

}
