package fr.woorib.backand.client;

import com.google.gson.internal.LinkedTreeMap;
import fr.woorib.backand.client.api.BackandManyToMany;
import fr.woorib.backand.client.api.BackandObject;
import fr.woorib.backand.client.exception.BackandException;
import fr.woorib.backand.client.tools.ProxyHelper;
import fr.woorib.backand.client.tools.ReflectionHelper;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Invocation Handler tasked with hijacking method invocations in the proxy to either
 * use the real object to return basic types, or use the linkedtreemap returned from backand
 * to return only ids on Collection types.
 */
public class BackandInvocationHandler<T> implements InvocationHandler {
  private static Logger LOG = Logger.getLogger(BackandInvocationHandler.class.getName());

  /** Instance of the real object managed by this handler. */
  private T real;
  /** id returned by backand.com when the object was retrieved. */
  private final Integer backandId;
  /** backand.com table storing this object. */
  private String backandTableName;
  /** stores backandIds of objects linked to the proxied object for lazy retrieval */
  private Map<String, Integer> backandObjectsIds;

  /**
   * Constructor will initiate the real object with the contents of the object parameter which is backand.com data.
   * It also initiates backandTableName and backandId.
   * @param object
   * @param real
   * @param backandTableName
   */
  public BackandInvocationHandler(LinkedTreeMap<String, Object> object, T real, String backandTableName) {
    this.backandObjectsIds = new HashMap<>();
    this.backandId = ReflectionHelper.castValue(object.get("id"), int.class);
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
      assignParameter(parameter, parameterValue);
    }

  }

  /**
   * Fill the real object with the parameter value using the setter for parameter.
   * @param parameter
   * @param parameterValue
   */
  private void assignParameter(String parameter, Object parameterValue) {
    try {
      Field declaredField = this.real.getClass().getDeclaredField(parameter);
      Method method = ReflectionHelper.getSetterMethod(declaredField, real.getClass());
      Class<?> type = declaredField.getType();
      if (ProxyHelper.isBackandClass(type)) {
        Object castedValue = ReflectionHelper.castValue(parameterValue, type);
        method.invoke(this.real, castedValue);
      } else if(!isIterable(type)) {
        //If the type is not a collection, backand.com will have returned the ID of the object.
        backandObjectsIds.put(parameter, ReflectionHelper.castValue(parameterValue, Integer.class));
      }
    }
    catch (NoSuchFieldException e) {
      LOG.fine("Field "+ e.getMessage() + " sent by backand not present in class " + real.getClass() );
    }
    catch (InvocationTargetException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
    }
    catch (NoSuchMethodException e) {
      LOG.fine("Method "+ e.getMessage() + " not present in class " + real.getClass() );
    }
    catch (IllegalAccessException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private boolean isIterable(Class<?> type) {
    for (Class clazz : type.getInterfaces()) {
      if (clazz.equals(Iterable.class)) {
        return true;
      }
    }
    return false;
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
      } else if (!ProxyHelper.isBackandClass(method.getReturnType())) {
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
    String param = ReflectionHelper.getParamNameFromMethod(method);
    Integer id = backandObjectsIds.get(param);
    try {
      return BackandClientImpl.get().retrieveBackandObjectFromId(id, method.getReturnType());
    } catch (BackandException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
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
    String param = ReflectionHelper.getParamNameFromMethod(method);
    try {
      Integer id = ReflectionHelper.castValue(backandId, int.class);
      Class requiredClass = (Class) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
      BackandManyToMany annotation = method.getAnnotation(BackandManyToMany.class);
      Object[] data;
      if (annotation != null) {
        String manyToManySide = annotation.parameter();
        data = BackandClientImpl.get().retrieveObjectDependence(backandTableName, id, param, requiredClass, manyToManySide);
      } else {
        data = BackandClientImpl.get().retrieveObjectDependence(backandTableName, id, param, requiredClass);
      }
      return Arrays.asList(data);
    }
    catch (BackandException e) {
      throw new InvocationTargetException(e, "Backand Access failed for " + param);
    }
  }

}