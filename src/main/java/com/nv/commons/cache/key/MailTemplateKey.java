package com.nv.commons.cache.key;

import org.apache.commons.lang3.builder.CompareToBuilder;

public record MailTemplateKey(int websiteTypeId, String marketingGroup, int currencyTypeId, int languageTypeId,
							  int templateTypeId) implements Comparable<MailTemplateKey> {

	public int compareTo(MailTemplateKey o) {
		return new CompareToBuilder().append(this.websiteTypeId, o.websiteTypeId)
			.append(this.currencyTypeId, o.currencyTypeId).append(this.languageTypeId, o.languageTypeId)
			.append(this.templateTypeId, o.templateTypeId).toComparison();
	}

}
