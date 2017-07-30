package fr.woorib.backand.client.tools;

import fr.woorib.backand.client.BackandInvocationHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Helper class to provide utility for Reflection
 */
public class ReflectionHelper {
  private static Logger LOG = Logger.getLogger(ReflectionHelper.class.getName());

  /**
   * Resolves the name of the parameter from a getter method
   * @param method a getter method.
   * @return the name of the parameter returned by the method.
   */
  public static String getParamNameFromMethod(Method method) {
    String param = method.getName().replaceFirst("get", "");
    param = param.substring(0,1).toLowerCase() + param.substring(1);
    return param;
  }

  /**
   * As GSon uses doubles for all numbers we need a small hack to cast to int.
   * @param value
   * @param type
   * @return
   */
  public static <T> T castValue(Object value, Class<T> type) {
    boolean wantInt = type == Integer.class || type == int.class;
    try {
      if ((value instanceof Number) && wantInt) {
        return (T) new Integer(((Number) value).intValue());
      }
      if (value instanceof String && wantInt) {
        return (T) new Integer((String) value);
      }
      return type.cast(value);
    } catch (Exception e) {
      LOG.severe("Error during casting: "+e.getClass()+ " " +e.getMessage());
      return null;
    }
  }

  /**
   * returns the setter method for a specific field in the class given.
   * @param field
   * @param clazz
   * @return
   * @throws NoSuchMethodException
   */
  public static Method getSetterMethod(Field field, Class<?> clazz) throws NoSuchMethodException {
    Class<?> type = field.getType();
    String methodName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
    Method method = clazz.getMethod(methodName, type);
    return method;
  }

  /**
   * returns the getter method for a specific field in the class given.
   * @param field
   * @param clazz
   * @return
   * @throws NoSuchMethodException
   */
  public static Method getGetterMethod(Field field, Class<?> clazz) throws NoSuchMethodException {
    Class<?> type = field.getType();
    String methodName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
    Method method = clazz.getMethod(methodName);
    return method;
  }
}
 
