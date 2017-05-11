/**
 * Paquet de définition
 **/
package fr.woorib.backand.client.tools;

import java.lang.reflect.Method;

/**
 * Description: Merci de donner une description du service rendu par cette classe
 */
public class ReflectionHelper {
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
    if ((value instanceof  Number) && wantInt) {
      return (T) new Integer(((Number) value).intValue());
    }
    if (value instanceof String && wantInt) {
      return (T) new Integer((String) value);
    }
    return type.cast(value);
  }
}
 
