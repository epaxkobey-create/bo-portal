package com.nv.commons.utils;

import java.sql.Timestamp;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.function.Supplier;

import com.ip2location.IPResult;
import com.nv.commons.cache.AccountPlayResponsiblySettingCache;
import com.nv.commons.constants.AccountPlayResponsiblyPeriodType;
import com.nv.commons.constants.AccountPlayResponsiblyType;
import com.nv.commons.constants.AccountStatusType;
import com.nv.commons.constants.AccountUpdateType;
import com.nv.commons.constants.SelfExclusionType;
import com.nv.commons.constants.SessionKeyConstants;
import com.nv.commons.constants.SystemConstants;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.AccountPlayResponsiblySetting;
import com.nv.commons.dto.AccountTracker;
import com.nv.commons.dto.AccountUpdateLog;
import com.nv.commons.dto.UpdateRecord;
import com.nv.commons.exceptions.AccountLockException;
import com.nv.commons.exceptions.Deviation;
import jakarta.servlet.http.HttpSession;

/**
 * Account 相關工具類
 *
 * @author user
 */
public final class AccountUtils {

	private AccountUtils() {
		throw new AssertionError();
	}

	public static void checkAllowLoginStatus(Account account) {
		if (account == null) {
			throw new Deviation().setI18N("msg.error.account.loginFailed");
		}
		isSelfExcluded(account);
		if (isLocked(account.getStatus())) {
			throw new AccountLockException().setI18N("msg.error.account.isLocked");
		}
		if (isInactived(account.getStatus())) {
			throw new Deviation().setI18N("msg.error.account.isInactived");
		}
		if (isSuspended(account.getStatus())) {
			//			throw new Deviation().setI18N("msg.error.account.blockSuspendedLogin");
		}
	}

	public static void checkCanForgotPwd(boolean unlockAccount, Account account) throws Deviation {
		if (isInactived(account.getStatus())) {
			throw new Deviation().setI18N("msg.error.account.isInactived");
		}
		if (isSuspended(account.getStatus())) {
			//			throw new Deviation().setI18N("msg.error.account.isSuspended");
		}
		if (isLocked(account.getStatus()) && !unlockAccount) {
			//			throw new AccountLockException().setI18N("msg.error.account.isLocked");
		}
	}

	public static boolean isInactived(int status) {
		return status == AccountStatusType.INACTIVE.unique();
	}

	public static boolean isActive(int status) {
		return status == AccountStatusType.ACTIVE.unique();
	}

	public static boolean isSuspended(int status) {
		return status == AccountStatusType.SUSPEND.unique();
	}

	// is password locked
	public static boolean isLocked(int status) {
		return status == AccountStatusType.LOCKED.unique();
	}

	public static void isSelfExcluded(Account account) {

		AccountPlayResponsiblySetting selfExclusion = AccountPlayResponsiblySettingCache.getInstance()
			.getPlayResponsiblyOrDefault(WebSiteType.getInstance(account.getWebsiteType()), account.getUserId(),
				AccountPlayResponsiblyType.SELF_EXCLUSION, AccountPlayResponsiblyPeriodType.DAILY);

		if (selfExclusion != null) {

			var currentSelfExclusionType = SelfExclusionType.getInstanceOf(selfExclusion.getCurrentValue());

			if (currentSelfExclusionType == SelfExclusionType.NO_EXCLUSION) {
				return;
			}

			if (currentSelfExclusionType != SelfExclusionType.INDEFINITE) {

				Timestamp effectiveEndTime = selfExclusion.getEffectiveEndTime();
				String timestamp = effectiveEndTime != null
					? String.valueOf(effectiveEndTime.getTime())
					: "";

				throw new Deviation().setI18N("msg.error.account.isSelfExcluded.definiteInfinite",
					timestamp);
			} else {
				throw new Deviation().setI18N("msg.error.account.isSelfExcluded.definiteInfinite", "");
			}
		}
	}

	public static String getUserKey(int webSiteType, String userID) {
		return getUserKey(WebSiteType.getInstance(webSiteType), userID);
	}

	public static String getUserKey(WebSiteType webSiteType, String userID) {

		return (webSiteType.unique() + "_" + userID).toLowerCase();
	}

	public static String[] parseUserKey(String key) {
		String[] userKeyPair = new String[2];
		StringTokenizer tokenizer = new StringTokenizer(key, "_");
		int i = 0;

		while (tokenizer.hasMoreTokens()) {
			if (i >= userKeyPair.length) {
				break;
			}
			userKeyPair[i] = tokenizer.nextToken();
			i++;
		}
		return userKeyPair;
	}
	public static String[] parseUserKeyEnhancement(String key) {
		int firstUnderscore = key.indexOf('_');
		if (firstUnderscore == -1) {
			throw new IllegalArgumentException("Invalid user key format: " + key);
		}
		return new String[] {
			key.substring(0, firstUnderscore),
			key.substring(firstUnderscore + 1)
		};
	}

	public static void logoutManager(HttpSession session) {
		session.setAttribute(SessionKeyConstants.ManagerRole, null);
		// 現在 Player Account 不放在 session 了 , 改放 userKey
		if (session.getAttribute(SessionKeyConstants.USER_KEY) == null) {
			session.invalidate();
		}
	}

	public static void logoutPlayer(HttpSession session) {
		if (session == null) {
			return;
		}
		// 現在 Player Account 不放在 session 了 , 改放 userKey
		// MEMO: 不可在這邊移除 USER_KEY, 因為 sessionDestroyed() 裡面還需要取 player 執行 logout()
		//	session.setAttribute(SessionKeyConstants.USER_KEY, null);
		session.setAttribute(SessionKeyConstants.SERVER_ID, null);
		// Avoid to logout other roles. Only destroy session when no other roles login.
		if (session.getAttribute(SessionKeyConstants.ManagerRole) == null) {
			session.invalidate();
		}
	}

	public static String generatePlayerPassword() {

		int indexForNumber = new Random().nextInt(6);
		int indexForChar = new Random().nextInt(6);
		int indexForLowerChar = new Random().nextInt(6);

		while (indexForChar == indexForNumber || indexForChar == indexForLowerChar
			|| indexForLowerChar == indexForNumber) {
			indexForChar = new Random().nextInt(6);
			indexForLowerChar = new Random().nextInt(6);
		}

		Supplier<Object> simpleRandomUnit = () -> {
			int type = new Random().nextInt(3);
			if (0 == type) {
				return new Random().nextInt(10);
			}
			if (1 == type) {
				return (char) (new Random().nextInt(26) + 65);
			}
			return (char) (new Random().nextInt(26) + 97);
		};

		StringBuilder sb = new StringBuilder(6);

		for (int i = 0; i < 6; i++) {
			if (i == indexForNumber) {
				sb.append(new Random().nextInt(10));
			} else if (i == indexForChar) {
				sb.append((char) (new Random().nextInt(26) + 65));
			} else if (i == indexForLowerChar) {
				sb.append((char) (new Random().nextInt(26) + 97));
			} else {
				sb.append(simpleRandomUnit.get());
			}
		}
		return sb.toString();
	}

	public static String getNotActiveMessage(int accountStatus) {
		if (isSuspended(accountStatus)) {
			return "msg.error.account.isSuspended";
		} else if (isLocked(accountStatus)) {
			return "msg.error.account.isLocked";
		} else {
			return "msg.error.account.isInactived";
		}
	}

	public static AccountTracker prepareAccountTracker(String userAgent, WebSiteType webSiteType, Account account,
		String affiliate, String loginIp, String userCountry, String ipTracker, int loginTypeId, boolean isFirstLogin,
		String browserHash, String deviceHash, String cookieSessionHash) {

		AccountTracker accountTracker = new AccountTracker();
		accountTracker.setUserId(account.getUserId());
		accountTracker.setWebsiteType(webSiteType.unique());
		accountTracker.setIp(loginIp);
		// LOGIN_DATE
		accountTracker.setAffiliate(affiliate);
		accountTracker.setVipLevel(account.getVipLevel());
		accountTracker.setCountry(userCountry);

		/*
		 */
		int userAgentDbSize = 512;
		String userAgentStr = userAgent;
		if (userAgent.length() > userAgentDbSize) {
			userAgentStr = userAgent.substring(0, userAgentDbSize);
		}
		accountTracker.setUserAgent(userAgentStr);
		accountTracker.setUserAgentType(UserAgentUtils.getBrowserUserAgent(userAgent).getBrowserName());
		accountTracker.setIpTracker(ipTracker);
		accountTracker.setBrowserHash(browserHash);
		accountTracker.setDeviceHash(deviceHash);
		accountTracker.setCookieSessionHash(cookieSessionHash);

		IPResult ipResult = CountryLookup.getInstance().getIPResult(loginIp);
		if (null != ipResult) {
			accountTracker.setIsp(ipResult.getISP());
			accountTracker.setCity(ipResult.getCity());
			accountTracker.setState(ipResult.getRegion());
		}

		accountTracker.setDeviceType(account.getDeviceType());
		accountTracker.setPlatformType(account.getPlatformType());
		accountTracker.setLoginDate(account.getLoginTime());

		accountTracker.setLoginType(loginTypeId);
		accountTracker.setFirstLogin(isFirstLogin);

		return accountTracker;
	}

	public static AccountUpdateLog getAccountUpdateLog(String userId, int websiteType, AccountUpdateType logType,
		UpdateRecord updateRecord, String updater, String updaterIp, int currencyType) {
		AccountUpdateLog updateLog = new AccountUpdateLog();
		updateLog.setUserId(userId);
		updateLog.setWebsiteType(websiteType);
		updateLog.setLogType(logType.unique());
		updateLog.setRecords(JSONUtils.toJsonString(updateRecord));
		updateLog.setUpdater(updater);
		updateLog.setUpdaterIp(updaterIp);
		updateLog.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		updateLog.setCurrencyTypeId(currencyType);
		updateLog.setLogTypeStr(logType.getName());
		return updateLog;
	}

	public static String getNewFormatPhoneNumber(String phoneNumber, String callingCode) {
		if (phoneNumber != null) {
			String[] phoneNumberStrArr = phoneNumber.split("-");

			if (phoneNumberStrArr.length == 1) {
				phoneNumber = String.join("-", callingCode, phoneNumber);
			}
		}

		return phoneNumber;
	}

	public static String getPhoneNumberWithoutCallingCode(String phoneNumber) {
		if (phoneNumber != null) {
			String[] phoneNumberStrArr = phoneNumber.split("-");

			if (phoneNumberStrArr.length == 2) {
				return phoneNumberStrArr[1];
			}
		}

		return phoneNumber;
	}

	public static String getCallingCodeFromPhoneNumber(String phoneNumber, String defaultCallingCode) {
		if (phoneNumber != null) {
			String[] phoneNumberStrArr = phoneNumber.split("-");

			if (phoneNumberStrArr.length == 2) {
				return phoneNumberStrArr[0];
			}
		}

		return defaultCallingCode;
	}
}
