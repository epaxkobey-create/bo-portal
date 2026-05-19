package com.nv.commons.message;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

public class LangMessage {

	protected final ResourceBundle resource;

	protected String languageResourceKey;

	public LangMessage(String propertyName) {
		this.languageResourceKey = propertyName.replace("message_", "");
		this.resource = ResourceBundle.getBundle(propertyName);
	}

	public String get(String key) {
		try {
			return this.resource.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public String get(String key, String[] args) {
		try {
			String result = this.resource.getString(key);
			if (args == null || args.length == 0) {
				return result;
			}
			MessageFormat form = new MessageFormat(result);
			return (form.format(args)).replace("\r\n", "");
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public String getLang() {
		return languageResourceKey;
	}


}
