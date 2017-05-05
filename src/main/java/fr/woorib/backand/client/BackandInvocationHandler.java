package fr.woorib.backand.client;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import com.google.gson.internal.LinkedTreeMap;
import fr.woorib.backand.client.exception.BackandClientException;
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

  BackandInvocationHandler(LinkedTreeMap<String, Object> object, T real, String backandTableName) {
    this.backandId = castValue(object.get("id"), int.class);
    this.real = real;
    this.backandTableName = backandTableName;

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
      if (Collection.class != type) {
        String methodName = "set" + parameter.substring(0, 1).toUpperCase() + parameter.substring(1);
        Object castedValue = castValue(parameterValue, type);
        real.getClass().getMethod(methodName, type).invoke(real, castedValue);
      }
    }
    catch (NoSuchFieldException e) {
      System.err.println("Filed "+ e.getMessage() + " sent by backand not present in class " + real.getClass() );
    }
    catch (InvocationTargetException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    catch (NoSuchMethodException e) {
      System.err.println("Filed "+ e.getMessage() + " not present in class " + real.getClass() );
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  /**
   * As GSon uses doubles for all numbers we need a small hack to cast to int.
   * @param parameterValue
   * @param type
   * @return
   */
  private <T> T castValue(Object parameterValue, Class<T> type) {
    if ((parameterValue instanceof  Number) && ((type == Integer.class || type == int.class))) {
      return (T) new Integer(((Number) parameterValue) .intValue());
    }
    return type.cast(parameterValue);
  }

  /**
   * Will generally use the real object to invoke methods.
   * When trying to use a getter on a collection, will use the backandId to retrieve the data directly from backand.com
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
    Object invoke;
    if (method.getName().startsWith("get") && method.getReturnType() == Collection.class) {
      String param = method.getName().replace("get", "");
      param = param.substring(0,1).toLowerCase() + param.substring(1);
      try {
        Integer id = castValue(backandId, int.class);
        Object[] data = BackandClientImpl.get().retrieveObjectDependence(backandTableName, id, param, (Class) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0]);
        invoke = Arrays.asList(data);
      }
      catch (BackandClientException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        throw new InvocationTargetException(e, "Backand Access failed for " + param);
      }
      //invoke = Collections.singletonList(backandMap.get(param));
    } else {
      invoke = method.invoke(real, args);
    }
    return invoke;
  }
}