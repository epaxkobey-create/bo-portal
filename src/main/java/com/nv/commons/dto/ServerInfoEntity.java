package com.nv.commons.dto;

import java.sql.Timestamp;

import com.nv.commons.annotation.Column;
import com.nv.commons.utils.HostAddressUtils;

/**
 * Title: com.neutec.nepal.common.entity.ServerInfoEntity<br>
 * Description: 各台server的資訊, 供系統之間互相通訊
 *
 */
public class ServerInfoEntity {

	/**
	 * sequence
	 */
	private long id;
	/**
	 * ip address
	 */
	private String ip;
	/**
	 * port number
	 */
	private int port;
	/**
	 * 1 : member, 2: affiliate, 4:manager, 8:CacheServer ?
	 */
	@Column(name = "SERVER_TYPE")
	private int serverType;
	/**
	 * 0:closed server 1:active server
	 * -- SETTER --
	 *  MEMO: 不能拿掉, 不然 DBQueryRunner 會 set 不到

	 */
	@Column(name = "IS_ACTIVE")
	private boolean isActive;
	/**
	 * 描述
	 */
	private String description;
	/**
	 * 新增時間
	 */
	@Column(name = "CREATE_TIME")
	private Timestamp createTime = null;
	/**
	 * 上次更新時間
	 */
	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime = null;

	//-- other info
	//ip 的最後一組數字
//	private int lastIPNumber = -1;

	public void setIp(String ip) {
		this.ip = ip;
//		if (ip != null) {
//			this.lastIPNumber = HostAddressUtils.getLastIPNumber(ip);
//		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getServerType() {
		return serverType;
	}

	public void setServerType(int serverType) {
		this.serverType = serverType;
	}

	public boolean isActive() {
		return isActive;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

//	public int getLastIPNumber() {
//		return lastIPNumber;
//	}
//
//	public void setLastIPNumber(int lastIPNumber) {
//		this.lastIPNumber = lastIPNumber;
//	}

	/**
	 * MEMO: 不能拿掉, 不然 DBQueryRunner 會 set 不到
	 */
	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}
}
