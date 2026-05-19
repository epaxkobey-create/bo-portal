package com.nv.commons.utils.secret;

import java.security.Provider;
import java.security.Security;
import java.util.List;

import com.nv.commons.utils.LogUtils;


public class SecurityBase {

	private static final List<String> PROVIDER_CLASS_NAME = List.of("org.bouncycastle.jce.provider.BouncyCastleProvider");

	public static void enableAESProvider() {

		for (String providerName : PROVIDER_CLASS_NAME) {
			try {
				Provider provider = (Provider) Class.forName(providerName).getDeclaredConstructor().newInstance();
				Security.addProvider(provider);
			} catch (Throwable e) {
				LogUtils.SYS.error(e.getMessage(), e);
			}
		}
	}
}
