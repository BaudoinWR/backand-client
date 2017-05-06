package fr.woorib.backand.client;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.internal.LinkedTreeMap;
import fr.woorib.backand.client.api.BackandClient;
import fr.woorib.backand.client.api.BackandObject;
import fr.woorib.backand.client.exception.BackandClientException;
import fr.woorib.backand.client.exception.BackandException;
import net.sf.cglib.proxy.InvocationHandler;

/**
 * Invocation Handler tasked with hijacking method invocations in the proxy to either
 * use the real object to return basic types, or use the linkedtreemap returned from backand
 * to return only ids on Collection types.
 */
public class BackandInvocationHandler<T> implements InvocationHandler {

  /** Instance of the real object managed by this handler. */
  private T real;
  /** id returned by backand.com when the object was retrieved. */
  private final Integer backandId;
  /** backand.com table storing this object. */
  private String backandTableName;
  /** stores backandIds of objects linked to the proxied object for lazy retrieval */
  private Map<String, Integer> backandObjectsIds;

  BackandInvocationHandler(LinkedTreeMap<String, Object> object, T real, String backandTableName) {
    this.backandObjectsIds = new HashMap<>();
    this.backandId = castValue(object.get("id"), int.class);
    this.real = real;
    BackandObject annotation = real.getClass().getAnnotation(BackandObject.class);
    String realTable = backandTableName;
    if (annotation != null) {
      realTable = annotation.table();
    }
    this.backandTableName = realTable;

    for(Map.Entry<String, Object> entry : object.entrySet()) {
      String parameter = entry.getKey();
      Object parameterValue = entry.getValue();
      assignParameter(real, parameter, parameterValue);
    }

  }

  /**
   * Fill the real object with the parameter value using the setter for parameter.
   * @param real
   * @param parameter
   * @param parameterValue
   */
  private void assignParameter(T real, String parameter, Object parameterValue) {
    try {
      Field declaredField = real.getClass().getDeclaredField(parameter);
      Class<?> type = declaredField.getType();
      String methodName = "set" + parameter.substring(0, 1).toUpperCase() + parameter.substring(1);
      Method method = real.getClass().getMethod(methodName, type);
      if (isBackandClass(type)) {
        Object castedValue = castValue(parameterValue, type);
        method.invoke(real, castedValue);
      } else if(!Arrays.stream(type.getInterfaces()).anyMatch(Iterable.class::equals)) {
        backandObjectsIds.put(parameter, castValue(parameterValue, Integer.class));
      }
    }
    catch (NoSuchFieldException e) {
      System.err.println("Filed "+ e.getMessage() + " sent by backand not present in class " + real.getClass() );
    }
    catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    catch (NoSuchMethodException e) {
      System.err.println("Filed "+ e.getMessage() + " not present in class " + real.getClass() );
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  /**
   * Retrieve an object on backand.com based on it's type and id
   * @param id
   * @param type must be anotated with {@link BackandObject}
   * @return
   * @throws BackandException
   */
  private Object getBackandObjectFromId(Integer id, Class<?> type) throws BackandException {
    BackandObject annotation = type.getAnnotation(BackandObject.class);
    if (annotation == null) {
      return null;
    }
    String table = annotation.table();
    return BackandClientImpl.get().retrieveObjectById(table, id, type);
  }

  private boolean isBackandClass(Class<?> type) {
    return Arrays.stream(BackandClient.BACKAND_CLASSES).anyMatch(type::equals);
  }

  /**
   * As GSon uses doubles for all numbers we need a small hack to cast to int.
   * @param value
   * @param type
   * @return
   */
  private <T> T castValue(Object value, Class<T> type) {
    boolean wantInt = type == Integer.class || type == int.class;
    if ((value instanceof  Number) && wantInt) {
      return (T) new Integer(((Number) value).intValue());
    }
    if (value instanceof String && wantInt) {
      return (T) new Integer((String) value);
    }
    return type.cast(value);
  }

  /**
   * Will generally use the real object to invoke methods.
   * When trying to use a getter on a non Backand class, will use the backandId to retrieve the data directly from backand.com
   * @param proxy
   * @param method
   * @param args
   * @return
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   */
  public Object invoke(Object proxy, Method method, Object[] args)
    throws IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
    if (method.getName().startsWith("get")) {
      if (method.getReturnType() == Collection.class) {
        return getProxiedCollection(method);
      } else if (!isBackandClass(method.getReturnType())) {
        return getProxiedObject(method);
      }
    }
    return method.invoke(real, args);
  }

  /**
   * Retrieve the object requested from the method through a call to backand.com
   * @param method the method that was called on the proxied object.
   * @return
   * @throws InvocationTargetException
   */
  private Object getProxiedObject(Method method) throws InvocationTargetException {
    String param = getParamNameFromMethod(method);
    Integer id = backandObjectsIds.get(param);
    try {
      return getBackandObjectFromId(id, method.getReturnType());
    } catch (BackandException e) {
      e.printStackTrace();
      throw new InvocationTargetException(e, "Backand Access failed for " + param);
    }
  }

  /**
   * Retrieve the Collection requested from the method through a call to backand.com
   * @param method the method that was called on the proxied object.
   * @return
   * @throws InvocationTargetException
   */
  private Object getProxiedCollection(Method method) throws InvocationTargetException {
    String param = getParamNameFromMethod(method);
    try {
      Integer id = castValue(backandId, int.class);
      Class requiredClass = (Class) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
      Object[] data = BackandClientImpl.get().retrieveObjectDependence(backandTableName, id, param, requiredClass);
      return Arrays.asList(data);
    }
    catch (BackandClientException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      throw new InvocationTargetException(e, "Backand Access failed for " + param);
    }
  }

  private String getParamNameFromMethod(Method method) {
    String param = method.getName().replace("get", "");
    param = param.substring(0,1).toLowerCase() + param.substring(1);
    return param;
  }
}