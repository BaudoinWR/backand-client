package fr.woorib.backand.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.gson.internal.LinkedTreeMap;
import fr.woorib.backand.client.exception.BackandClientException;
import fr.woorib.backand.client.exception.BackandException;
import fr.woorib.backand.client.tools.ProxyHelper;

/**
 * Wrapper for the response comming from backand.com.
 */
class BackandResponseWrapper<T> {
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
        T t;
        try {
          t = BackandClientImpl.get().retrieveBackandObjectFromId(new Integer(id), classOfT);
        }
        catch (BackandException e) {
          throw new BackandClientException("Impossible to retrieve many to many object.",e);
        }
        list.add(t);
        continue;
      }
      T o = ProxyHelper.generateWrappedObject(classOfT, backandMap, backandTableName);
      if (o != null) {
        list.add(o);
      }

    }
    data = (T[]) list.toArray();
  }

  @Override
  public String toString() {
    return "BackandResponseWrapper{" +
      "data=" + Arrays.toString(data) +
      '}';
  }
}