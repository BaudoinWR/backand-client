package fr.woorib.backand.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import fr.woorib.backand.client.api.BackandClient;
import fr.woorib.backand.client.api.BackandObject;
import fr.woorib.backand.client.exception.BackandClientException;
import fr.woorib.backand.client.exception.BackandException;
import fr.woorib.backand.client.tools.HttpHelper;
import fr.woorib.backand.client.tools.ProxyHelper;

/**
 * Using java.net api to connect to backand.com
 * Using google's Gson api to read json objects returned by backand.com
 */
public class BackandClientImpl implements BackandClient {
  private static Logger LOG = Logger.getLogger(BackandClient.class.getName());

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
    LOG.finest(token.toString());
    return true;
  }

  @Override
  public <T> T retrieveObjectById(String table, Integer id, Class<T> classOfT) throws BackandException {
    String callBackand = callBackand(BACKAND_API_ENDPOINT + table +"/"+id);
    LinkedTreeMap t = extractResponseObject(callBackand, LinkedTreeMap.class);
    T t1 = ProxyHelper.generateWrappedObject(classOfT, t, table);
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
  public <T> T[] retrieveObjects(String table, Class<T> classOfT) throws BackandException {
    String callBackand = callBackand(BACKAND_API_ENDPOINT + table);
    BackandResponseWrapper<T> backandResponseWrapper = extractResponseObject(callBackand, BackandResponseWrapper.class);
    backandResponseWrapper.wrapData(classOfT, table, null);
    return backandResponseWrapper.getData();
  }

  @Override
  public <T> T[] retrieveObjectDependence(String table, Integer id, String param, Class<T> classOfT) throws BackandException {
    return retrieveObjectDependence(table, id, param, classOfT, null);
  }

  @Override
  public <T> T[] retrieveObjectDependence(String table, Integer id, String param, Class<T> classOfT, String manyToManySide) throws BackandException {
    String callBackand = callBackand(BACKAND_API_ENDPOINT + table +"/"+id+"/"+param);
    BackandResponseWrapper<T> backandResponseWrapper = extractResponseObject(callBackand, BackandResponseWrapper.class);
    backandResponseWrapper.wrapData(classOfT, table, manyToManySide);
    return backandResponseWrapper.getData();
  }

  @Override
  public <T> T insertNewObject(T object) throws BackandException {
    BackandObject annotation = object.getClass().getAnnotation(BackandObject.class);
    if (annotation == null) {
      throw new BackandException("Class "+object.getClass()+" is missing @BackandObject annotation");
    }
    String table = annotation.table();
    String json = new Gson().toJson(object);
    String postBackand = postBackand(BACKAND_API_ENDPOINT + table + "?returnObject=true", json);
    LinkedTreeMap t = extractResponseObject(postBackand, LinkedTreeMap.class);
    T t1 = ProxyHelper.generateWrappedObject((Class<T>) object.getClass(), t, table);
    return t1;
  }

  /**
   * Transforms the http response from backand.com into an object of class classOfT.
   * @param response
   * @param classOfT
   * @param <T>
   * @return
   */
  private <T> T extractResponseObject(String response, Class<T> classOfT) {
    LOG.fine("Extracting " +response);
    Gson g = new Gson();
    return g.fromJson(response, classOfT);
  }

  /**
   * Sends a GET request to backand.com at the specified endpoint.
   * @param endpoint
   * @return
   * @throws BackandClientException
   */
  private String callBackand(String endpoint) throws BackandException {
    return callBackand(endpoint, null);
  }

  /**
   * Sends a GET request to backand.com at the specified endpoint with the parameters encoded in the request body.
   * @param endpoint
   * @param parameters
   * @return
   * @throws BackandClientException
   */
  private String callBackand(String endpoint, Map<String, String> parameters) throws BackandException {
    HttpURLConnection conn = null;
    String response;
    LOG.fine(endpoint);
    try {
      conn = HttpHelper.getHttpURLConnection(proxy, token, BACKAND_API_URL + endpoint, HttpHelper.GET);

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

  /**
   * Sends a POST request to backand.com at the specified endpoint using the json as body content.
   * @param endpoint
   * @param json
   * @return
   * @throws BackandClientException
   */
  private String postBackand(String endpoint, String json) throws BackandException {

    HttpURLConnection conn = null;
    String response;
    try {
      conn = HttpHelper.getHttpURLConnection(proxy, token, BACKAND_API_URL + endpoint, HttpHelper.POST);

      try (OutputStream output = conn.getOutputStream()) {
        output.write(json.getBytes());
      }

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
 
