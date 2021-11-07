package cn.xyz.commons.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.bson.types.ObjectId;

import com.alibaba.fastjson.JSON;

/**
 * 反射工具类
 * 
 * @author luorc
 * 
 */
@SuppressWarnings("unchecked")
public final class ReflectionUtils {

	public static <T> T deepCopy(Object obj, Class<?> clazz) throws Exception {
		Object newInstance = clazz.newInstance();

		for (Field field : obj.getClass().getDeclaredFields()) {
			String propName = getPropName(field);
			Object value = getValue(obj, propName);
			if (null != value)
				setValue(newInstance, field, propName, value);
		}

		return (T) newInstance;
	}

	public static String getPropName(Field field) {
		return new StringBuffer().append(field.getName().substring(0, 1).toUpperCase()).append(field.getName().substring(1)).toString();
	}

	public static Object getValue(Object obj, String propName) {
		Object value = null;
		String name = new StringBuffer().append("get").append(propName).toString();

		try {
			Method method = obj.getClass().getDeclaredMethod(name, new Class<?>[] {});
			value = method.invoke(obj, new Object[] {});
		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;
	}

	public static <T> T mapToBean(Map<String, String> mapSrc, Class<?> clazz) {
		try {
			Object objTo = clazz.newInstance();
			Iterator<String> keySet = mapSrc.keySet().iterator();

			while (keySet.hasNext()) {
				String key = keySet.next();
				String value = mapSrc.get(key);

				setValue(objTo, clazz, ("_id".equals(key) ? "userId" : key), value);
			}

			return (T) objTo;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void main(String[] args) {
		/*JobExample example = new JobExample();
		Field[] fields = example.getClass().getDeclaredFields();
		for (Field field : fields) {
			// System.out.println(field.getType().getTypeName());
			if (field.getType().getTypeName().equals("java.util.List")) {
				// System.out.println(field.getGenericType().getTypeName());
				ParameterizedType pt = (ParameterizedType) field.getGenericType();
				System.out.println(pt.getActualTypeArguments()[0].getTypeName());
			}
		}*/

	}

	public static <T> T parse(HttpServletRequest request, Class<?> cls) {
		Object obj = null;

		try {
			obj = cls.newInstance();
			for (Field field : cls.getDeclaredFields()) {
				String s = request.getParameter(field.getName());
				if (StringUtil.isEmpty(s)) {

				} else {
					Object value = null;
					switch (field.getType().getTypeName()) {
					case "java.lang.Object":
						break;
					case "int":
					case "java.lang.Integer":
						value = Integer.parseInt(s);
						break;
					case "long":
					case "java.lang.Long":
						value = Long.parseLong(s);
						break;
					case "java.lang.String":
						value = s;
						break;
					case "org.bson.types.ObjectId":
						value = new ObjectId(s);
						break;
					case "java.util.List":
						if (field.getGenericType() instanceof ParameterizedType) {
							ParameterizedType pt = (ParameterizedType) field.getGenericType();
							Class<?> clazz = (Class<?>) pt.getActualTypeArguments()[0];
							value = JSON.parseArray(s, clazz);
						}
						break;
					}
					if (null != value)
						setValue(cls, obj, field, value);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("参数解析失败");
		}

		return null == obj ? null : (T) obj;
	}

	public static void setValue(Class<?> cls, Object obj, Field field, Object value) throws Exception {
		String name = MessageFormat.format("set{0}{1}", field.getName().substring(0, 1).toUpperCase(), field.getName().substring(1));
		Method method = cls.getDeclaredMethod(name, field.getType());
		method.invoke(obj, value);
	}

	public static void setValue(Object obj, Class<?> clazz, String key, String value) {
		try {
			Field field = clazz.getDeclaredField(key);
			field.setAccessible(true);

			switch (field.getType().getTypeName()) {
			case "java.lang.Byte":
				field.set(obj, Byte.parseByte(value));
				break;
			case "java.lang.Integer":
				field.set(obj, Integer.parseInt(value));
				break;
			case "java.util.Date":
				// field.set(obj, DateUtils.getDate(value));
				break;
			default:
				field.set(obj, value);
				break;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setValue(Object obj, Field field, String propName, Object value) {
		String name = new StringBuffer().append("set").append(propName).toString();

		try {
			Method method = obj.getClass().getDeclaredMethod(name, new Class<?>[] { field.getType() });

			method.invoke(obj, new Object[] { value });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// public static Object getValueFromMap(BasicDBObject mapSrc, Field field) {
	// String key = field.getName();
	// Object value = null;
	//
	// if (mapSrc.containsField(key)) {
	// String typeName = field.getType().getTypeName();
	//
	// switch (typeName) {
	// case "java.lang.Byte":
	// value = Byte.parseByte(mapSrc.getString(key));
	// break;
	// case "java.lang.Integer":
	// value = mapSrc.getInt(key);
	// break;
	// case "java.lang.Long":
	// value = mapSrc.getLong(key);
	// break;
	// case "java.lang.String":
	// value = mapSrc.getString(key);
	// break;
	// default:
	// break;
	// }
	// }
	//
	// return value;
	// }
	//
	// @SuppressWarnings("unchecked")
	// public static <T> T transfer(BasicDBObject mapSrc, Class<?> clazz) {
	// try {
	// Object obj = clazz.newInstance();
	// Field[] fields = clazz.getDeclaredFields();
	//
	// for (Field field : fields) {
	// String key = field.getName();
	// Object value = null;
	//
	// if (mapSrc.containsField(key)) {
	// switch (field.getType().getTypeName()) {
	// case "java.lang.Byte":
	// value = Byte.parseByte(mapSrc.getString(key));
	// break;
	// case "java.lang.Integer":
	// value = mapSrc.getInt(key);
	// break;
	// case "java.lang.Long":
	// value = mapSrc.getLong(key);
	// break;
	// case "java.lang.String":
	// value = mapSrc.getString(key);
	// break;
	// default:
	// break;
	// }
	// }
	//
	// if (value != null) {
	// String name = Joiner.on("").join("set", field.getName().substring(0,
	// 1).toUpperCase(), field.getName().substring(1));
	// Method method = clazz.getDeclaredMethod(name, new Class<?>[] {
	// field.getType() });
	//
	// method.invoke(obj, new Object[] { value });
	// }
	// }
	//
	// return (T) obj;
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// return null;
	// }

}
