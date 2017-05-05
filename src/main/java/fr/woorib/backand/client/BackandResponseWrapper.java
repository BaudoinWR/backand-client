package fr.woorib.backand.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.gson.internal.LinkedTreeMap;
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

  void wrapData(Class<T> classOfT, String backandTableName) {
    List<T> list = new ArrayList<>();
    for (int i=0; i<data.length;i++) {
      LinkedTreeMap backandMap = (LinkedTreeMap) data[i];
      T o = generateWrappedObject(classOfT, backandMap, backandTableName);
      if (o != null) {
        list.add(o);
      }

    }
    data = (T[]) list.toArray();
  }

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