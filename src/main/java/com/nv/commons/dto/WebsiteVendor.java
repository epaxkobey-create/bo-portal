package com.nv.commons.dto;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nv.commons.annotation.Column;
import com.nv.commons.constants.GameType;
import com.nv.commons.utils.JSONUtils;
import com.nv.commons.utils.LogUtils;
import org.apache.commons.text.StringEscapeUtils;

public class WebsiteVendor {

	@Column(name = "website_type")
	private int websiteType;

	@Column(name = "vendor_id")
	private int vendorId;

	@Column(name = "website_provider_id")
	private int websiteProviderId;

	@Column(name = "display_name")
	private String displayName;

	private int status;

	@Column(name = "game_type")
	private int gameType;

	@Column(name = "maintenance_start")
	private Timestamp maintenanceStart;

	@Column(name = "maintenance_end")
	private Timestamp maintenanceEnd;

	@Column(name = "create_time")
	private Timestamp createTime;

	@Column(name = "update_time")
	private Timestamp updateTime;

	private String updater;

	private long positiontaking;

	// example "1,2,3,4,5"
	@Column(name = "CATEGORIES")
	private String categories;

	private String[] categoriesArray = null;

	@Column(name = "WEB_ICON")
	private String webIcon;

	private byte[] webIconByteArray;

	@Column(name = "H5_ICON")
	private String h5Icon;

	private byte[] h5IconByteArray;

	private final Map<String, String> webVendorIconMap = new HashMap<>();

	private final Map<String, String> h5VendorIconMap = new HashMap<>();

	private final Map<String , byte[]> vendorIconByteMap = new HashMap<>();

	private String title;

	private Map<String, String> titleMap = null;

	private String description;

	private Map<String, String> descMap = null;


	public void setCategories(String categories) {
		this.categories = categories;
		this.categoriesArray = null;
	}

	public void setWebIcon(String webIcon) {
		this.webIcon = webIcon;
		if (webIcon != null && !webIcon.isEmpty()) {
			webVendorIconMap.clear();
			if (checkNewIconDataFormat(webIcon)) {
				String[] icons = webIcon.split(",");
				for (String iconString : icons){
					String[] iconGames = iconString.split(":");
					// 0: game type, 1: icon file name
					webVendorIconMap.put(iconGames[0], iconGames[1]);
				}
			} else {
				List<GameType> gameTypeList = GameType.getGameTypes(this.gameType);
				for (GameType gameType:gameTypeList) {
					webVendorIconMap.put(gameType.getShortName(), webIcon);
				}
			}
		}
	}

	public void setH5Icon(String h5Icon) {
		this.h5Icon = h5Icon;
		h5VendorIconMap.clear();
		if (h5Icon != null && !h5Icon.isEmpty()) {
			if (checkNewIconDataFormat(h5Icon)) {
				String[] icons = h5Icon.split(",");
				for (String iconString : icons){
					String[] iconGames = iconString.split(":");
					// 0: game type, 1: icon file name
					h5VendorIconMap.put(iconGames[0], iconGames[1]);
				}
			} else {
				List<GameType> gameTypeList = GameType.getGameTypes(this.gameType);
				for (GameType gameType:gameTypeList) {
					h5VendorIconMap.put(gameType.getShortName(), h5Icon);
				}
			}
		}
	}

	private boolean checkNewIconDataFormat(String icon){
		String[] icons = icon.split(",");
		String[] vendorIcon = icons[0].split(":");

		try {
			if (vendorIcon.length != 2) {
				return false;
			} else {
				GameType.getInstance(vendorIcon[0]); //非game type
			}
		} catch (IllegalArgumentException e){
			LogUtils.SYS.error(e.getMessage(), e);
			return false;
		}

		return  true;
	}

	public String getTitle(String langKey) {
		try {
			if (titleMap == null) {
				titleMap = JSONUtils.jsonToMap(StringEscapeUtils.unescapeEcmaScript(title), String.class, String.class);
			}
			return titleMap.get(langKey);

		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
		return null;
	}

	public void setTitle(String title) {
		this.title = title;
		try {
			if (titleMap != null) {
				titleMap = JSONUtils.jsonToMap(StringEscapeUtils.unescapeEcmaScript(title), String.class, String.class);
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}


	public void setDescription(String description) {
		this.description = description;
		try {
			if (descMap != null) {
				descMap = JSONUtils.jsonToMap(StringEscapeUtils.unescapeEcmaScript(description), String.class, String.class);
			}
		} catch (Exception e) {
			LogUtils.SYS.error(e.getMessage(), e);
		}
	}

	public int getWebsiteType() {
		return websiteType;
	}

	public void setWebsiteType(int websiteType) {
		this.websiteType = websiteType;
	}

	public int getVendorId() {
		return vendorId;
	}

	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;
	}

	public int getWebsiteProviderId() {
		return websiteProviderId;
	}

	public void setWebsiteProviderId(int websiteProviderId) {
		this.websiteProviderId = websiteProviderId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public Timestamp getMaintenanceStart() {
		return maintenanceStart;
	}

	public void setMaintenanceStart(Timestamp maintenanceStart) {
		this.maintenanceStart = maintenanceStart;
	}

	public Timestamp getMaintenanceEnd() {
		return maintenanceEnd;
	}

	public void setMaintenanceEnd(Timestamp maintenanceEnd) {
		this.maintenanceEnd = maintenanceEnd;
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

	public String getUpdater() {
		return updater;
	}

	public void setUpdater(String updater) {
		this.updater = updater;
	}

	public long getPositiontaking() {
		return positiontaking;
	}

	public void setPositiontaking(long positiontaking) {
		this.positiontaking = positiontaking;
	}

	public String getCategories() {
		return categories;
	}

	public String getWebIcon() {
		return webIcon;
	}

	public byte[] getWebIconByteArray() {
		return webIconByteArray;
	}

	public void setWebIconByteArray(byte[] webIconByteArray) {
		this.webIconByteArray = webIconByteArray;
	}

	public String getH5Icon() {
		return h5Icon;
	}

	public byte[] getH5IconByteArray() {
		return h5IconByteArray;
	}

	public void setH5IconByteArray(byte[] h5IconByteArray) {
		this.h5IconByteArray = h5IconByteArray;
	}

	public String getTitle() {
		return title;
	}

	public Map<String, String> getTitleMap() {
		return titleMap;
	}

	public void setTitleMap(Map<String, String> titleMap) {
		this.titleMap = titleMap;
	}

	public String getDescription() {
		return description;
	}

	public Map<String, String> getDescMap() {
		return descMap;
	}

	public void setDescMap(Map<String, String> descMap) {
		this.descMap = descMap;
	}

}
