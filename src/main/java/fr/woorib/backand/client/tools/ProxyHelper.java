package fr.woorib.backand.client.tools;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import com.google.gson.internal.LinkedTreeMap;
import fr.woorib.backand.client.BackandInvocationHandler;
import fr.woorib.backand.client.api.BackandClient;
import net.sf.cglib.proxy.Enhancer;

/**
 * Description: Merci de donner une description du service rendu par cette classe
 */
public class ProxyHelper {
  /**
   * Provides a way to build a proxy object of class T using GLib Enhancer and fill it with the LinkedTreeMap retrieved from backand.com
   * @param classOfT class of the object expected
   * @param backandMap data retrieved from backand.com
   * @param backandTableName the table name on backand.com. Used to later retrieve relationship objects through backand.
   * @param <T>
   * @return an object of class T
   */
  public static <T> T generateWrappedObject(Class<T> classOfT, LinkedTreeMap backandMap, String backandTableName) {
    T o = null;
    try {
      o = (T) Enhancer.create(classOfT, new BackandInvocationHandler<>(backandMap, classOfT.newInstance(), backandTableName));
      setFieldValues(o, backandMap);
    }
    catch (InstantiationException e) {
      e.printStackTrace();
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return o;
  }

  /**
   * Fills the proxy with the values from backand in order for the proxy object to be a true representation
   * of the backand data. This is necessary for serialization of the proxy.
   * @param o
   * @param backandMap
   * @param <T>
   */
  private static <T> void setFieldValues(T o, LinkedTreeMap backandMap) {
    Field[] declaredFields = o.getClass().getSuperclass().getDeclaredFields();
    for (Field field : declaredFields) {
      if (!isBackandClass(field.getType())) {
        continue;
      }
      try {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        Object value = backandMap.get(field.getName());
        Object castedValue = ReflectionHelper.castValue(value, field.getType());
        field.set(o, castedValue);
        field.setAccessible(accessible);
      }
      catch (IllegalAccessException e) {
        System.err.println("Can't set field "+e);
      }
    }
  }

  /**
   * Creates a real object of classOfT from the proxy object.
   * @param object
   * @param <T>
   * @return
   * @throws IllegalAccessException
   * @throws InstantiationException
   */
  public static <T> T unProxify(Object object) throws IllegalAccessException, InstantiationException {
    Class<?> classOfT = object.getClass().getSuperclass();
    T unproxied = (T) classOfT.newInstance();
    Field[] declaredFields = classOfT.getDeclaredFields();
    for (Field field : declaredFields) {
      try {
        Method getterMethod = ReflectionHelper.getGetterMethod(field, object.getClass());
        Method setterMethod = ReflectionHelper.getSetterMethod(field, classOfT);
        Object gotten = getterMethod.invoke(object);
        setterMethod.invoke(unproxied, gotten);
      }
      catch (NoSuchMethodException e) {
        System.err.println("No method to deal with the field " + field + " in class " + classOfT);
      }
      catch (InvocationTargetException e) {
        System.err.println("Error while trying to deal with the field " + field + " in class " + classOfT);
        e.printStackTrace();
      }
    }
    return unproxied;
  }

  public static boolean isBackandClass(Class<?> type) {
    return Arrays.stream(BackandClient.BACKAND_CLASSES).anyMatch(type::equals);
  }
}
 
