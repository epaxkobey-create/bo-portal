package com.nv.commons.model.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBProperty {
	private String name = null;
	private boolean checkDBTime = false;
	private Map<String, String> defaultSettings = new HashMap<>();
	private List<Map<String, String>> nodes = new ArrayList<>();
	
	public boolean isCheckDBTime() {
		return checkDBTime;
	}
	
	public void setCheckDBTime(boolean checkDBTime) {
		this.checkDBTime = checkDBTime;
	}

	
	public Map<String, String> getDefaultSettings() {
		return defaultSettings;
	}

	
	public void setDefaultSettings(Map<String, String> defaultSettings) {
		this.defaultSettings = defaultSettings;
	}

	
	public List<Map<String, String>> getNodes() {
		return nodes;
	}

	
	public void setNodes(List<Map<String, String>> nodes) {
		this.nodes = nodes;
	}

	
	public String getName() {
		return name;
	}

	
	public void setName(String name) {
		this.name = name;
	}
	
	
}
