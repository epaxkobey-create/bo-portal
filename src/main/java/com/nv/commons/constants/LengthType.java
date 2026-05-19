package com.nv.commons.constants;

import java.lang.reflect.Field;

import com.nv.commons.annotation.Column;
import com.nv.commons.dto.Account;
import com.nv.commons.dto.Manager;

public enum LengthType {

	AccountUserId(Account.class, "userId"),
	ManagerUserId(Manager.class, "userId"),
	;

	private final int length;

	LengthType(Class<?> clazz, String fieldName) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Column.class) &&
				field.getName().equalsIgnoreCase(fieldName)) {

				int maxLength = field.getAnnotation(Column.class).maxLength();
				if (maxLength <= 0) {
					throw new RuntimeException("Error Length Setting : LengthType." + this.name());
				}
				this.length = maxLength;
				return;
			}
		}
		throw new RuntimeException("Error Length Setting : LengthType." + this.name());
	}

	public int getLength() {
		return this.length;
	}
}
