package com.nv.commons.model.database;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.nv.commons.annotation.Column;
import com.nv.commons.utils.LogUtils;

/**
 * 改寫自org.apache.commons.dbutils.BeanProcessor
 * 1. 調整執行效能
 * 2. 新增支援lambda的調用
 * 3. 新增cache，提高效能
 * <p>
 * Alan
 */
public class BeanConverter {

	private static final Map<Class<?>, Object> primitiveDefaults = new HashMap<Class<?>, Object>();

	static {
		primitiveDefaults.put(Integer.TYPE, Integer.valueOf(0));
		primitiveDefaults.put(Short.TYPE, Short.valueOf((short) 0));
		primitiveDefaults.put(Byte.TYPE, Byte.valueOf((byte) 0));
		primitiveDefaults.put(Float.TYPE, Float.valueOf(0f));
		primitiveDefaults.put(Double.TYPE, Double.valueOf(0d));
		primitiveDefaults.put(Long.TYPE, Long.valueOf(0L));
		primitiveDefaults.put(Boolean.TYPE, Boolean.FALSE);
		primitiveDefaults.put(Character.TYPE, Character.valueOf((char) 0));
	}

	// key : class name, DB Columns name to Object Field name
	private static Map<String, Map<String, String>> dbColumnNameMap = new ConcurrentHashMap<>();

	// key : class name, Object Field name to DB Columns name
	private static Map<String, Map<String, String>> objectFieldNameMap = new ConcurrentHashMap<>();

	private static Map<String, Map<String, PropertyDescriptor>> classPropertyDescriptorsMap = new ConcurrentHashMap<>();

	public static <T> void updateBean(ResultSet rs, T bean) throws SQLException {
		PropertyDescriptor[] columnToProperty = mapColumnsToProperties(rs.getMetaData(), bean.getClass());
		createBean(rs, bean, columnToProperty);
	}

	public static <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
		PropertyDescriptor[] columnToProperty = mapColumnsToProperties(rs.getMetaData(), type);
		return createBean(rs, type, columnToProperty);
	}

	public static <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {

		if (!rs.next()) {
			return Collections.emptyList();
		}

		List<T> results = new ArrayList<T>();
		PropertyDescriptor[] columnToProperty = mapColumnsToProperties(rs.getMetaData(), type);
		do {
			T bean = createBean(rs, type, columnToProperty);
			results.add(bean);
		} while (rs.next());

		return results;
	}

	public static <T> LinkedList<T> toBeanLinkedList(ResultSet rs, Class<T> type) throws SQLException {

		if (!rs.next()) {
			return null;
		}

		LinkedList<T> results = new LinkedList<>();
		PropertyDescriptor[] columnToProperty = mapColumnsToProperties(rs.getMetaData(), type);
		do {
			T bean = createBean(rs, type, columnToProperty);
			results.add(bean);
		} while (rs.next());

		return results;
	}

	public static <T> void processBeanResult(ResultSet rs, Class<T> type, DataBeanProcessor<T> dataBeanProcessor)
		throws SQLException {

		if (!rs.next()) {
			return;
		}

		PropertyDescriptor[] columnToProperty = mapColumnsToProperties(rs.getMetaData(), type);
		do {
			T bean = createBean(rs, type, columnToProperty);
			dataBeanProcessor.process(rs, bean);
		} while (rs.next());
	}

	private static <T> T createBean(ResultSet rs, T bean, PropertyDescriptor[] columnToProperty)
		throws SQLException {

		for (int i = 1; i < columnToProperty.length; i++) {

			if (columnToProperty[i] == null) {
				continue;
			}

			PropertyDescriptor prop = columnToProperty[i];

			Class<?> propType = prop.getPropertyType();

			Object value = null;
			if (propType != null) {
				value = processColumn(rs, i, propType);

				if (value == null && propType.isPrimitive()) {
					value = primitiveDefaults.get(propType);
				}
			}

			callSetter(bean, prop, value);
		}

		return bean;
	}

	private static <T> T createBean(ResultSet rs, Class<T> type, PropertyDescriptor[] columnToProperty)
		throws SQLException {

		return createBean(rs, newInstance(type), columnToProperty);
	}

	private static void callSetter(Object target, PropertyDescriptor prop, Object value) throws SQLException {

		Method setter = prop.getWriteMethod();

		if (setter == null) {
			return;
		}

		Class<?>[] params = setter.getParameterTypes();
		try {
			// convert types for some popular ones
			if (value instanceof java.util.Date) {
				final String targetType = params[0].getName();
				if ("java.sql.Date".equals(targetType)) {
					value = new java.sql.Date(((java.util.Date) value).getTime());
				} else if ("java.sql.Time".equals(targetType)) {
					value = new java.sql.Time(((java.util.Date) value).getTime());
				} else if ("java.sql.Timestamp".equals(targetType)) {
					Timestamp tsValue = (Timestamp) value;
					int nanos = tsValue.getNanos();
					value = new Timestamp(tsValue.getTime());
					((Timestamp) value).setNanos(nanos);
				}
			} else if (value instanceof String && params[0].isEnum()) {
				value = Enum.valueOf(params[0].asSubclass(Enum.class), (String) value);
			}

			// Don't call setter if the value object isn't the right type
			if (isCompatibleType(value, params[0])) {
				setter.invoke(target, new Object[] {value});
			} else {
				throw new SQLException("Cannot set " + prop.getName() + ": incompatible types, cannot convert "
									   + value.getClass().getName() + " to " + params[0].getName());
				// value cannot be null here because isCompatibleType allows
				// null
			}
		} catch (IllegalArgumentException e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new SQLException("Cannot set " + prop.getName() + ": " + e.getMessage());

		} catch (IllegalAccessException e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new SQLException("Cannot set " + prop.getName() + ": " + e.getMessage());

		} catch (InvocationTargetException e) {
			LogUtils.SYS.error(e.getMessage(), e);
			throw new SQLException("Cannot set " + prop.getName() + ": " + e.getMessage());
		}
	}

	private static boolean isCompatibleType(Object value, Class<?> type) {
		// Do object check first, then primitives
		if (value == null || type.isInstance(value)) {
			return true;

		} else if (type.equals(Integer.TYPE) && value instanceof Integer) {
			return true;

		} else if (type.equals(Long.TYPE) && value instanceof Long) {
			return true;

		} else if (type.equals(Double.TYPE) && value instanceof Double) {
			return true;

		} else if (type.equals(Float.TYPE) && value instanceof Float) {
			return true;

		} else if (type.equals(Short.TYPE) && value instanceof Short) {
			return true;

		} else if (type.equals(Byte.TYPE) && value instanceof Byte) {
			return true;

		} else if (type.equals(Character.TYPE) && value instanceof Character) {
			return true;

		} else if (type.equals(Boolean.TYPE) && value instanceof Boolean) {
			return true;

		}
		return false;

	}

	private static <T> T newInstance(Class<T> c) throws SQLException {
		try {
			return c.getDeclaredConstructor().newInstance();
		} catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
				 IllegalAccessException e) {
			throw new SQLException("Cannot create " + c.getName() + ": " + e.getMessage());
		}
	}

	private static <T> PropertyDescriptor[] mapColumnsToProperties(ResultSetMetaData rsmd, Class<T> type)
		throws SQLException {

		Map<String, String> columnToPropertyOverrides = getColumnProperties(type);
		Map<String, PropertyDescriptor> props = getPropertyDescriptors(type);

		int cols = rsmd.getColumnCount();

		PropertyDescriptor[] columnToProperty = new PropertyDescriptor[cols + 1];

		Arrays.fill(columnToProperty, null);

		for (int i = 1; i <= cols; i++) {
			String columnName = rsmd.getColumnLabel(i);
			if (null == columnName || 0 == columnName.length()) {
				columnName = rsmd.getColumnName(i);
			}

			String propertyName = columnToPropertyOverrides.get(columnName.toUpperCase());
			if (propertyName == null) {
				propertyName = columnName;
			}


			columnToProperty[i] = props.get(propertyName.toUpperCase());
		}

		return columnToProperty;
	}

	private static Object processColumn(ResultSet rs, int index, Class<?> propType) throws SQLException {

		if (!propType.isPrimitive() && rs.getObject(index) == null) {
			return null;
		}

		if (propType.equals(String.class)) {
			return rs.getString(index);

		} else if (propType.equals(Integer.TYPE) || propType.equals(Integer.class)) {
			return Integer.valueOf(rs.getInt(index));

		} else if (propType.equals(Boolean.TYPE) || propType.equals(Boolean.class)) {
			return Boolean.valueOf(rs.getBoolean(index));

		} else if (propType.equals(Long.TYPE) || propType.equals(Long.class)) {
			return Long.valueOf(rs.getLong(index));

		} else if (propType.equals(Double.TYPE) || propType.equals(Double.class)) {
			return Double.valueOf(rs.getDouble(index));

		} else if (propType.equals(Float.TYPE) || propType.equals(Float.class)) {
			return Float.valueOf(rs.getFloat(index));

		} else if (propType.equals(Short.TYPE) || propType.equals(Short.class)) {
			return Short.valueOf(rs.getShort(index));

		} else if (propType.equals(Byte.TYPE) || propType.equals(Byte.class)) {
			return Byte.valueOf(rs.getByte(index));

		} else if (propType.equals(Timestamp.class)) {
			return rs.getTimestamp(index);

		} else if (propType.equals(SQLXML.class)) {
			return rs.getSQLXML(index);

		} else if (propType.equals(byte[].class)) {
			return rs.getBytes(index);

		} else {
			return rs.getObject(index);
		}

	}

	private static Map<String, PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) throws SQLException {
		String className = clazz.getName();
		Map<String, PropertyDescriptor> propertyDescriptors = classPropertyDescriptorsMap.get(className);
		if (propertyDescriptors == null) {
			try {
				propertyDescriptors = new HashMap<>();
				PropertyDescriptor[] propertyArray = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
				for (int i = 0; i < propertyArray.length; i++) {
					propertyDescriptors.put(propertyArray[i].getName().toUpperCase(), propertyArray[i]);
				}
			} catch (IntrospectionException e) {
				throw new SQLException("Bean introspection failed: " + e.getMessage());
			}
			classPropertyDescriptorsMap.put(className, propertyDescriptors);
		}
		return propertyDescriptors;
	}

	private static Map<String, String> getColumnProperties(Class<?> clazz) {
		String className = clazz.getName();

		Map<String, String> properties = dbColumnNameMap.get(className);

		if (properties == null) {
			properties = new HashMap<>();

			final Class<?> superclass = clazz.getSuperclass();

			if (superclass != null && !superclass.equals(Object.class)) {
				final Map<String, String> propertiesOfParent = getColumnProperties(superclass);
				properties.putAll(propertiesOfParent);
			}

			for (Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(Column.class)) {
					Column column = field.getAnnotation(Column.class);
					properties.put(column.name().toUpperCase(), field.getName());
				}

			}
			dbColumnNameMap.put(className, properties);
		}
		return properties;
	}

	private static Map<String, String> getFieldProperties(Class<?> clazz) {
		String className = clazz.getName();

		Map<String, String> properties = objectFieldNameMap.get(className);
		if (properties == null) {
			properties = new HashMap<>();
			for (Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(Column.class)) {
					Column column = field.getAnnotation(Column.class);
					properties.put(field.getName().toUpperCase(), column.name());
				} else {
					properties.put(field.getName().toUpperCase(), field.getName());
				}
			}
			objectFieldNameMap.put(className, properties);
		}
		return properties;
	}

	public static String getDBColumnName(Class<?> clazz, String fieldName) {
		if (fieldName == null) {
			return null;
		}
		return getFieldProperties(clazz).get(fieldName.toUpperCase());
	}
}
