package com.nv.commons.provider.dto;

import java.util.List;
public class ProviderGameRs {

	private List<Game> webGame;

	private List<Game> h5Game;

	public static class Game{

		private boolean isExist = true;

		private String gameCode;

		private String gameName;

		private String gameNameEn;

		private String gameType;

		private String gameDetail;

		private String deviceType;

		private String gameImgUrl;

		private String extraData;

		public boolean isExist() {
			return isExist;
		}

		public void setExist(boolean exist) {
			isExist = exist;
		}

		public String getGameCode() {
			return gameCode;
		}

		public void setGameCode(String gameCode) {
			this.gameCode = gameCode;
		}

		public String getGameName() {
			return gameName;
		}

		public void setGameName(String gameName) {
			this.gameName = gameName;
		}

		public String getGameType() {
			return gameType;
		}

		public void setGameType(String gameType) {
			this.gameType = gameType;
		}

		public String getGameDetail() {
			return gameDetail;
		}

		public void setGameDetail(String gameDetail) {
			this.gameDetail = gameDetail;
		}

		public String getDeviceType() {
			return deviceType;
		}

		public void setDeviceType(String deviceType) {
			this.deviceType = deviceType;
		}

		public String getGameImgUrl() {
			return gameImgUrl;
		}

		public void setGameImgUrl(String gameImgUrl) {
			this.gameImgUrl = gameImgUrl;
		}

		public String getGameNameEn() {
			return gameNameEn;
		}

		public void setGameNameEn(String gameNameEn) {
			this.gameNameEn = gameNameEn;
		}

		public String getExtraData() {
			return extraData;
		}

		public void setExtraData(String extraData) {
			this.extraData = extraData;
		}
	}

	public List<Game> getWebGame() {
		return webGame;
	}

	public void setWebGame(List<Game> webGame) {
		this.webGame = webGame;
	}

	public List<Game> getH5Game() {
		return h5Game;
	}

	public void setH5Game(List<Game> h5Game) {
		this.h5Game = h5Game;
	}
}
