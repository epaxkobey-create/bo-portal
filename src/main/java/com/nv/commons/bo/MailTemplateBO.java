package com.nv.commons.bo;

import java.io.File;

import com.nv.commons.constants.CurrencyType;
import com.nv.commons.constants.LanguageType;

public class MailTemplateBO {

	public static String getFolderPath(CurrencyType currencyType,
		LanguageType languageType) {

		return "mail/mt" + File.separator + currencyType.getName()
			.toLowerCase() + File.separator + languageType.getLanguageResourceKey();
	}

}
