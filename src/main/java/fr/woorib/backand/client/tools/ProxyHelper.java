package fr.woorib.backand.client.tools;

import com.google.gson.internal.LinkedTreeMap;
import fr.woorib.backand.client.BackandInvocationHandler;
import fr.woorib.backand.client.api.BackandClient;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.android.AndroidClassLoadingStrategy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Description: Merci de donner une description du service rendu par cette classe
 */
public class ProxyHelper {
  private static Logger LOG = Logger.getLogger(ProxyHelper.class.getName());

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
      //o = (T) Enhancer.create(classOfT, new BackandInvocationHandler<>(backandMap, classOfT.newInstance(), backandTableName));
      o = getProxy(classOfT, backandMap, backandTableName);
      setFieldValues(o, backandMap);
    }
    catch (InstantiationException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
    }
    catch (IllegalAccessException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
    }
    return o;
  }

  private static <T> T getProxy(Class<T> classOfT, LinkedTreeMap backandMap, String backandTableName) throws IllegalAccessException, InstantiationException {
    Class<? extends T> dynamicType = new ByteBuddy(ClassFileVersion.JAVA_V6)
            .subclass(classOfT)
            .method(ElementMatchers.<MethodDescription>any())
            .intercept(InvocationHandlerAdapter.of(new BackandInvocationHandler<T>(backandMap, classOfT.newInstance(), backandTableName)))
            .make()
            .load(ProxyHelper.class.getClassLoader(), new AndroidClassLoadingStrategy.Wrapping(new File(System.getProperty("buddy.folder"))))
            .getLoaded();
    return dynamicType.newInstance();
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
        LOG.severe("Can't set field "+e);
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
        LOG.severe("No method to deal with the field " + field + " in class " + classOfT);
      }
      catch (InvocationTargetException e) {
        LOG.severe("Error while trying to deal with the field " + field + " in class " + classOfT);
        LOG.log(Level.SEVERE, e.getMessage(), e);
      }
    }
    return unproxied;
  }

  public static boolean isBackandClass(Class<?> type) {
    for (Class clazz : BackandClient.BACKAND_CLASSES) {
      if (clazz.equals(type)) {
        return true;
      }
    }
    return false;
  }
}
 
