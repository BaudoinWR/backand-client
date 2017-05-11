package fr.woorib.backand.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import fr.woorib.backand.client.api.BackandClient;
import fr.woorib.backand.client.api.BackandObject;
import fr.woorib.backand.client.exception.BackandClientException;
import fr.woorib.backand.client.exception.BackandException;
import fr.woorib.backand.client.tools.HttpHelper;

/**
 * Using java.net api to connect to backand.com
 * Using google's Gson api to read json objects returned by backand.com
 */
public class BackandClientImpl implements BackandClient {
  private AccessToken token = null;
  private final Proxy proxy;
  private static BackandClient instance;

  private BackandClientImpl() {
    proxy = Proxy.NO_PROXY;
  }

  private BackandClientImpl(String proxyHost, Integer proxyPort) {
    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
  }

  /**
   * @return the current instance if it exists, otherwise create an instance with no proxy.
   */
  public static BackandClient get() {
    if (instance == null) {
      instance = new BackandClientImpl();
    }
    return instance;
  }

  /**
   * @param proxyHost
   * @param proxyPort
   * @return the current instance if it exists, otherwise create an instance with the specified proxy.
   */
  public static BackandClient get(String proxyHost, Integer proxyPort) {
    if (instance == null) {
      instance = new BackandClientImpl(proxyHost, proxyPort);
    }
    return instance;
  }

  @Override
  public boolean establishConnection(String username, String password, String appName) throws BackandException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("username", username);
    parameters.put("password", password);
    parameters.put("appName", appName);
    parameters.put("grant_type", "password");
    String tokenEndpoint = TOKEN_ENDPOINT;

    String response = callBackand(tokenEndpoint, parameters);
    token = extractResponseObject(response, AccessToken.class);
    System.out.println(token);
    return true;
  }

  @Override
  public <T> T retrieveObjectById(String table, Integer id, Class<T> classOfT) throws BackandException {
    String callBackand = callBackand("/1/objects/" + table +"/"+id);
    LinkedTreeMap t = extractResponseObject(callBackand, LinkedTreeMap.class);
    T t1 = BackandResponseWrapper.generateWrappedObject(classOfT, t, table);
    return t1;
  }

  @Override
  public <T> T retrieveBackandObjectFromId(Integer id, Class<T> type) throws BackandException {
    BackandObject annotation = type.getAnnotation(BackandObject.class);
    if (annotation == null) {
      return null;
    }
    String table = annotation.table();
    return BackandClientImpl.get().retrieveObjectById(table, id, type);
  }

  @Override
  public <T> T[] retrieveObjects(String table, Class<T> classOfT) throws BackandClientException {
    String callBackand = callBackand("/1/objects/" + table);
    BackandResponseWrapper<T> backandResponseWrapper = extractResponseObject(callBackand, BackandResponseWrapper.class);
    backandResponseWrapper.wrapData(classOfT, table, null);
    return backandResponseWrapper.getData();
  }

  @Override
  public <T> T[] retrieveObjectDependence(String table, Integer id, String param, Class<T> classOfT) throws BackandClientException {
    return retrieveObjectDependence(table, id, param, classOfT, null);
  }

  @Override
  public <T> T[] retrieveObjectDependence(String table, Integer id, String param, Class<T> classOfT, String manyToManySide) throws BackandClientException {
    String callBackand = callBackand("/1/objects/" + table +"/"+id+"/"+param);
    BackandResponseWrapper<T> backandResponseWrapper = extractResponseObject(callBackand, BackandResponseWrapper.class);
    backandResponseWrapper.wrapData(classOfT, table, manyToManySide);
    return backandResponseWrapper.getData();
  }

  private <T> T extractResponseObject(String response, Class<T> classOfT) {
    System.out.println("Extracting " +response);
    Gson g = new Gson();
    return g.fromJson(response, classOfT);
  }

  private String callBackand(String endpoint) throws BackandClientException {
    return callBackand(endpoint, null);
  }

  private String callBackand(String endpoint, Map<String, String> parameters) throws BackandClientException {
    HttpURLConnection conn = null;
    String response;
    System.out.println(endpoint);
    try {
      conn = HttpHelper.getHttpURLConnection(proxy, token, BACKAND_API_URL + endpoint);

      HttpHelper.addBodyParameters(conn, parameters);

      response = HttpHelper.getResponseAsString(conn);
    } catch (MalformedURLException e) {
      throw new BackandClientException("Error in the backand client URL setup", e);
    } catch (ProtocolException e) {
      throw new BackandClientException("Error in the backand client protocol", e);
    } catch (IOException e) {
      throw new BackandClientException("IOException when trying to connect to Backand", e);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
    return response;
  }

}
 
