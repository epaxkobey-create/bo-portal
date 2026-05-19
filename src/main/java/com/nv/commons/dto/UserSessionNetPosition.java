package com.nv.commons.dto;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class  UserSessionNetPosition
{
//	@JsonProperty("userId")
	@JsonIgnore
	private String userId;

	@JsonProperty("loginSessionNetPosition")
	private BigDecimal loginSessionNetPosition;

	@JsonProperty("gameSessionNetPositions")
	private ConcurrentHashMap<Integer, BigDecimal> gameSessionNetPositions;

	public UserSessionNetPosition (String userId) {
		this.userId = userId;
		this.loginSessionNetPosition = BigDecimal.ZERO;
		this.gameSessionNetPositions = new ConcurrentHashMap<>();
	}

	//getter and setter
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public BigDecimal getLoginSessionNetPosition() {
		return loginSessionNetPosition;
	}
	public void setLoginSessionNetPosition(BigDecimal loginSessionNetPosition) {
		this.loginSessionNetPosition = loginSessionNetPosition;
	}
	public ConcurrentHashMap<Integer, BigDecimal> getGameSessionNetPositions() {
		return gameSessionNetPositions;
	}
	public void setGameSessionNetPositions(ConcurrentHashMap<Integer, BigDecimal> gameSessionNetPositions) {
		this.gameSessionNetPositions = gameSessionNetPositions;
	}
}
