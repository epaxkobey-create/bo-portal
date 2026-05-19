package com.nv.commons.constants;

public enum MessageType {

	TOP_BANNER(0) {
		public String getName() {
			return "Announcement";
		}

	},
	RUNNING(1) {
		public String getName() {
			return "Running";
		}

	},
	MEMBER(2) {
		public String getName() {
			return "Member";
		}

	},
	POP(3) {
		public String getName() {
			return "Pop up";
		}

	},
	SYSTEM(4) {
		public String getName() {
			return "System";
		}

	},
	PROMOTION(5) {
		public String getName() {
			return "Promotion";
		}

	},
	SLOT_SLIDER(6) {
		public String getName() {
			return "Slot Slider";
		}

	},
	SLOT_TOP_BANNER(7) {
		public String getName() {
			return "Slot Announcement";
		}

	},
	EMAIL(8) {
		public String getName() {
			return "E-mail";
		}

	},
	SMS(9) {
		public String getName() {
			return "SMS";
		}

	},
	FEATURE_GAME(10) {
		public String getName() {
			return "Feature Game";
		}

	},
	RANK_WINNER(11) {
		public String getName() {
			return "Ranking Winner";
		}

	},
	SPORT_SLIDER(12) {
		public String getName() {
			return "Sport Slider";
		}

	},
	CASINO_TOP_BANNER(13) {
		public String getName() {
			return "Casino Announcement";
		}

	},
	PROMOTION_TOP_BANNER(14) {
		public String getName() {
			return "Promotion Announcement";
		}

	},
	TABLE_TOP_BANNER(15) {
		public String getName() {
			return "Table Announcement";
		}

	},
	REGISTER_SLIDER(16) {
		public String getName() {
			return "Register Announcement";
		}

	},
	REGISTER_SUCCESS_SLIDER(17) {
		public String getName() {
			return "Register Success Announcement";
		}

	},
	SOCIAL_MEDIA(18) {
		public String getName() {
			return "Social Media";
		}

	},

	RANK_BONUS(20) {
		@Override
		public String getName() {
			return "Ranking Bonus";
		}

	},
	RANK_PAYMENT(21) {
		@Override
		public String getName() {
			return "Ranking Payment";
		}

	},
	REFERRAL_TEAMS_AND_CONDITIONS(22) {
		public String getName() {
			return "Terms & Conditions";
		}

	},
	SPORT_TOP_BANNER(23) {
		public String getName() {
			return "Sport Announcement";
		}

	},
	FISH_TOP_BANNER(24) {
		public String getName() {
			return "Fish Announcement";
		}

	},
	ARCADE_TOP_BANNER(25) {
		public String getName() {
			return "Arcade Announcement";
		}

	},
	FOOTER_FLOAT_BANNER(26) {
		public String getName() {
			return "Footer Float Banner";
		}

	},
	LOTTERY_TOP_BANNER(27) {
		public String getName() {
			return "Lottery Announcement";
		}

	},
	SIDE_MENU_BANNER(28) {
		public String getName() {
			return "Side Menu Banner";
		}

	},
	REFERRAL_RANKING_LIST(29) {
		public String getName() {
			return "Referral Ranking List";
		}

	},
	;

	public static final MessageType[] VALUES = MessageType.values();

	public static MessageType getInstanceOf(int value) {
		for (MessageType e : VALUES) {
			if (e.value == value) {
				return e;
			}
		}
		return null;
	}

	private final int value;

	MessageType(int value) {
		this.value = value;
	}

	public int unique() {
		return value;
	}

	public abstract String getName();

}
