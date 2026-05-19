package com.nv.commons.constants;

public enum DBQueryType {
	UNLOCK() {
		public String getSqlString() {
			return "";
		}
	},
	LOCK_FOR_UPDATE() {
		public String getSqlString() {
			return "FOR UPDATE";
		}
	},
	LOCK_FOR_UPDATE_NO_WAIT() {
		public String getSqlString() {
			return "FOR UPDATE NOWAIT";
		}
	},
	SKIP_LOCKED_FOR_UPDATE() {
		public String getSqlString() {
			return "FOR UPDATE SKIP LOCKED";
		}
	},
	LOCK_FOR_UPDATE_WAIT5() {
		public String getSqlString() {
			return " FOR UPDATE WAIT 5";
		}
	};

	public abstract String getSqlString();
}
