package fr.woorib.backand.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.gson.internal.LinkedTreeMap;
import fr.woorib.backand.client.exception.BackandClientException;
import fr.woorib.backand.client.exception.BackandException;
import net.sf.cglib.proxy.Enhancer;

/**
 * Wrapper for the response comming from backand.com.
 */
class BackandResponseWrapper<T> {
  private int totalRows;
  private T[] data;

  T[] getData() {
    return data;
  }

  /**
   * Transforms data from an array of LinkedTreeMap into an array of proxies of class T
   * @param classOfT the class of the objects wanted.
   * @param backandTableName the name of the backand.com table where the data was retrieved.
   * @param manyToManySide
   */
  void wrapData(Class<T> classOfT, String backandTableName, String manyToManySide) throws BackandClientException {
    List<T> list = new ArrayList<>();
    for (int i=0; i<data.length;i++) {
      LinkedTreeMap backandMap = (LinkedTreeMap) data[i];
      if (manyToManySide != null && manyToManySide.trim().length() > 0) {
        String id = (String) backandMap.get(manyToManySide);
        T t = null;
        try {
          t = BackandClientImpl.get().retrieveBackandObjectFromId(new Integer(id), classOfT);
        }
        catch (BackandException e) {
          throw new BackandClientException("Impossible to retrieve many to many object.",e);
        }
        list.add(t);
        continue;
      }
      T o = generateWrappedObject(classOfT, backandMap, backandTableName);
      if (o != null) {
        list.add(o);
      }

    }
    data = (T[]) list.toArray();
  }

  /**
   * Provides a way to build a proxy object of class T using GLib Enhancer and fill it with the LinkedTreeMap retrieved from backand.com
   * @param classOfT class of the object expected
   * @param backandMap data retrieved from backand.com
   * @param backandTableName the table name on backand.com. Used to later retrieve relationship objects through backand.
   * @param <T>
   * @return an object of class T
   */
  static <T> T generateWrappedObject(Class<T> classOfT, LinkedTreeMap backandMap, String backandTableName) {
    T o = null;
    try {
      o = (T) Enhancer.create(classOfT, new BackandInvocationHandler<>(backandMap, classOfT.newInstance(), backandTableName));
    }
    catch (InstantiationException e) {
      e.printStackTrace();
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return o;
  }

  @Override
  public String toString() {
    return "BackandResponseWrapper{" +
      "totalRows=" + totalRows +
      ", data=" + Arrays.toString(data) +
      '}';
  }
}