package com.nv.commons.constants;

import com.nv.commons.exceptions.Deviation;

public enum APIResponseType {
	/*
	 * 1. 功能分類
	 * 遊戲, 交易, 系統, 帳戶, 結算
	 * 2. 系統分類
	 * 金額相關, 帳密相關, 檢核相關,
	 * 3. 站台分類 XXXX 會共用function不行
	 * BO/OP/ADMIN, Affiliate, FE, SYS
	 * <p>
	 * E: Error 一般錯誤, 未知錯誤, E9999, E9998
	 * S: System相關: 系統,IP,Domain,維護,參數,操作,權限
	 * T: transaction相關: 交易,轉帳,存,提,等等。跟金額有關
	 * G: Game相關 : 遊戲維護,類型,餘額,權限,Provider,Vendor
	 * F: Field 欄位
	 * U: User相關 : 帳號,密碼,年齡,信箱,手機檢核,個人資訊
	 * C: 其他類型: 日期檢核, 查無資料, 幣別檢核, 語言檢核, 其他檢核...
	 * //Bank, payment 相關放哪邊比較好, 或是另開一個類型
	 * BW: BonusWallet 相關
	 * PC: Promotion Code 相關
	 */

	//E: Error 一般錯誤, 未知錯誤, E9999, E9998
	UNKNOWN_ERROR("E9999", "msg.api.response.unknownError"), //系統錯誤，請聯繫客服。//使用在未知錯誤,以及不應該走到的邏輯,需要查詢發生原因
	ERROR("E9998", "msg.api.response.error"), //系統錯誤，請聯繫客服。//使用在已知錯誤,但未定義在APIResponseType

	//S: System相關: 系統,IP,Domain,維護,參數,操作,權限
	API_VALIDATE_ERROR("S0001", "msg.api.response"), //api 驗證錯誤 : {0}
	MUST_BE_LOGIN("S0002", "msg.error.account.mustBeLogin"), //must be login
	ACCOUNT_IS_LOCK("S0003", "msg.error.account.isLocked"), //isLocked
	ACCOUNT_IS_INACTIVED("S0004", "msg.error.account.isInactived"), //isInactived
	ACCOUNT_IS_NOT_EXISTS("S0005", "msg.api.response.accountIsNotExists"), //IsNotExists
	MOBILE_FRONTEND_FRAMEWORK_CLOSE("S0006", "msg.api.response.frontend.mobileFrameworkClose"), //websiteSystemSettingType 51 is close
	WEB_FRONTEND_FRAMEWORK_CLOSE("S0007", "msg.api.response.frontend.webFrameworkClose"), //websiteSystemSettingType 52 is close
	ACCOUNT_IS_SUSPENDED("S0008", "msg.error.account.isSuspended"), //isSuspended
	ACCOUNT_IS_SESSION_EXPIRED("S0009", "msg.error.account.isSessionExpired"), //isSessionExpired
	ACCOUNT_IS_SELF_EXCLUDED("S0010", "msg.error.account.isSelfExcluded."), //isSelfExcluded

	//P: Payment相關: 交易,轉帳,存,提,等等。跟金額有關
	DEPOSIT_ERROR("P0002", "msg.error.deposit."), //Deporsit error
	
	//G: Game相關 : 遊戲維護,類型,餘額,權限,Provider,Vendor
	GAME_PLAY_BUSY("G0001", "msg.error.game.play.busy"),
	GAME_PLAY_BUSY_RETRY("G0002", "msg.error.game.play.busy"),
	GAME_PLAY_INACTIVE("G0003", "msg.error.game.play.inactive"),
	VENDOR_NOT_FOUND("G0003", "msg.error.validation.notFindVendor"),

	//F: Field 欄位
	CAPTCHA_TIMEOUT_ERROR("F0001", "msg.error.captcha."),
	CAPTCHA_VALIDATION_FAIL("F0002", "fs.parameter.validation.captcha"), // FE Customization i18N
	ACCOUNT_USERID_ERROR("F0003","msg.error.account.userId."),
	ACCOUNT_NAME_ERROR("F0004", "msg.error.account.name."),
	PASSWORD_ERROR("F0005", "msg.error.password."),
	CONFIRM_PASSWORD_ERROR("F0006","msg.error.confirmPassword."),
	CURRENCY_ERROR("F0007", "msg.error.currency."),
	CALLING_CODE_ERROR("F0008", "msg.error.callingCode."),
	ACCOUNT_PHONE_ERROR("F0009", "msg.error.account.phone."),
	ACCOUNT_EMAIL_ERROR("F0010", "msg.error.account.email."),
	ACCOUNT_REFERRER_CODE_ERROR("F0011", "msg.error.account.referrer."),
	Date_ERROR("F0012","msg.error.date.isNotValidated"),
	ACCOUNT_BIRTHDAY_ERROR("F0012", "msg.error.account.birthday."),
	ACCOUNT_QQID_ERROR("F0013", "msg.error.account.qqId."),
	ACCOUNT_WECHATID_ERROR("F0014", "msg.error.account.wechatId."),
	ACCOUNT_BANK_ERROR("F0015", "msg.error.account.bank."),
	ACCOUNT_BANK_ACCOUNT_NUMBER_ERROR("F0016", "msg.error.account.bank.accountNumber."),
	DAILY_WAGER_LIMIT_ERROR("F0017", "msg.error.account.playResponsibly.limit.daily.wager."),
	WEEKLY_WAGER_LIMIT_ERROR("F0018", "msg.error.account.playResponsibly.limit.weekly.wager."),
	MONTHLY_WAGER_LIMIT_ERROR("F0019", "msg.error.account.playResponsibly.limit.monthly.wager."),
	DAILY_LOSS_LIMIT_ERROR("F0020", "msg.error.account.playResponsibly.limit.daily.loss."),
	WEEKLY_LOSS_LIMIT_ERROR("F0021", "msg.error.account.playResponsibly.limit.weekly.loss."),
	MONTHLY_LOSS_LIMIT_ERROR("F0022", "msg.error.account.playResponsibly.limit.monthly.loss."),
	DAILY_DEPOSIT_LIMIT_ERROR("F0023", "msg.error.account.playResponsibly.limit.daily.deposit."),
	WEEKLY_DEPOSIT_LIMIT_ERROR("F0024", "msg.error.account.playResponsibly.limit.weekly.deposit."),
	MONTHLY_DEPOSIT_LIMIT_ERROR("F0025", "msg.error.account.playResponsibly.limit.monthly.deposit."),
	DUPLICATED_BANK_ACCOUNT("F0026", "msg.error.bank.account.duplicated"),
	//U: User相關 : 帳號,密碼,年齡,信箱,手機檢核,個人資訊
	LOGIN_STATUS_ERROR("U0001", "msg.login.loginStatusType."), //用戶狀態錯誤
	ACCOUNT_ERROR("U0002", "msg.error.account."), //帳戶類型錯誤
	//	PASSWORD_ERROR("U0003", "msg.error.password."), //密碼類型錯誤
	//	CONFIRMPASSWORD_ERROR("U0004", "msg.error.confirmPassword.incorrect"), //確認密碼錯誤
	PASSWORD_SAME_AS_OLD_PASSWORD("U0006", "msg.error.validation.newPassSameAsOldPass"), //password same as old password
	//	EMAIL_ALREADY_USED("U0007", "msg.error.account.email.alreadyUsed"), // email already used
	//	Validation_CAPTCHA_ERROR("U0008", "fs.parameter.validation.captcha"), // Captcha is not valid

	//C: 其他檢核: 日期檢核, 查無資料, 幣別檢核, 語言檢核, 其他檢核...
	VALIDATION_ERROR("C0001", "msg.error.validation."), //驗證類型錯誤
	//	CURRENCY_IS_NOT_VALIDATED("C0002", "msg.error.currency.isNotValidated"),//幣別不正确
	DATE_IS_NOT_VALIDATED("C0003", "msg.error.date.isNotValidated"),//日期格式错误

	//新的FEAPi 用的I18N Key
	PARAMETER_VALIDATION_ERROR("FS9999", "fs.parameter.validation"),
	PARAMETER_NOT_FOUND_ERROR("FS9998", "fs.parameter.notfound"),
	PARAMETER_ALREADY_USED("FS9997", "fs.parameter.alreadyUsed"),
	ERROR_WITH_MESSAGE("FS9996","fs.error.message"),
	LOGIN_ERROR_FAIL_COUNT("FS9995","fs.error.account.failCount"),
	IP_BLOCK_BAN_COUNTRY("FS9994","fs.error.banCountry"),
	MUST_RESET_PASSWORD("FS9993","fs.api.response.changePassword"),

	//使用在特殊情境: 需要利用Exception 控管回傳的Response, 直接在Message組好整個回傳Json時使用, Ex: throw new Deviation().set
	MESSAGE_JSON_RESPONSE("FS0000","fs.api.response.message"), //FS0000 不會真的回傳, 會依照 Exception 的 Message整個回傳
	//以下兩個已佔用
	//	FS0001 : Brand Currency Maintain
	//	FS0002 : System Maintain (Only Return From Maintain Tomcat)

	//Other Status Code:
	//000000  //Success
	/*
		MESSAGE_JSON_RESPONSE:
		FS0001 : Brand Currency Maintain
		FS0002 : System Maintain (Only Return From Maintain Tomcat)
	 */
	;

	APIResponseType(String code, String i18nKey) {
		this.code = code;
		this.i18nKey = i18nKey;
	}

	private final String code;
	private final String i18nKey;

	public String getCode() {
		return code;
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public static APIResponseType getInstanceByException(Exception e) {
		if (e instanceof Deviation || e.getCause() instanceof Deviation) {
			Deviation deviation = (Deviation) (e instanceof Deviation ? e : e.getCause());
			String i18nKey = deviation.getMessage();

			// check special rules, if over 3 item, collect to function
			if("Error_ReferCode_Currency".equals(i18nKey)) {
				i18nKey = "msg.error.account.referrer.currency.isNotValidated";
				deviation.setI18N(i18nKey);
			}

			//相等優先, 再看分類
			for (APIResponseType type : APIResponseType.values()) {
				if (type.i18nKey.equals(i18nKey)) {
					return type;
				}
			}
			for (APIResponseType type : APIResponseType.values()) {
				if (i18nKey.startsWith(type.i18nKey)) {
					return type;
				}
			}
		} else {
			return UNKNOWN_ERROR;
		}
		return UNKNOWN_ERROR;
	}
}
