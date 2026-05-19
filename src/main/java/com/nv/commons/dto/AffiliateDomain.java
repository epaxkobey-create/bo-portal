package com.nv.commons.dto;

import com.nv.commons.annotation.Column;

import java.sql.Timestamp;
public class AffiliateDomain {

	private long id;

	@Column(name = "WEBSITE_TYPE")
	private int websiteType;

	@Column(name = "CATEGORY_TYPE")
	private long categoryType;

	private int status;

	@Column(name = "DEFAULT_AFFILIATE_ID")
	private long defaultAffiliateId;

	private String domain;

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	@Column(name = "CREATE_TIME")
	private Timestamp createTime;

	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	//affiliate user_id
	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "MARKETING_GROUP")
	private String marketingGroup;

	// default 0
	@Column(name = "IS_MARKETING_DEFAULT")
	private int isMarketingDefault;

}
