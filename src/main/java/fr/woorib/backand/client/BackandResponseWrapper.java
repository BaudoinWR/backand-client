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

  public int getTotalRows() {
    return totalRows;
  }

  public void setTotalRows(int totalRows) {
    this.totalRows = totalRows;
  }

  public T[] getData() {
    return data;
  }

  public void setData(T[] data) {
    this.data = data;
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

  public static <T> T generateWrappedObject(Class<T> classOfT, LinkedTreeMap backandMap, String backandTableName) {
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

  public class BackandData {
    private Object __metadata;
    private T data;

    public void put(String key, Object value) {
      System.out.println("trying put "+key);
    }

    public void set__metadata(Object __metadata) {
      // not supported
    }

    public T getData() {
      return data;
    }

    public void setData(T data) {
      this.data = data;
    }

    @Override
    public String toString() {
      return "BackandData{" +
        "__metadata=" + __metadata +
        ", data=" + data +
        '}';
    }
  }

  @Override
  public String toString() {
    return "BackandResponseWrapper{" +
      "totalRows=" + totalRows +
      ", data=" + Arrays.toString(data) +
      '}';
  }
}