package fr.woorib.backand.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import fr.woorib.backand.client.api.BackandClient;
import fr.woorib.backand.client.beans.Beacon;
import fr.woorib.backand.client.beans.User;
import fr.woorib.backand.client.exception.BackandClientException;
import fr.woorib.backand.client.exception.BackandException;

/**
 * Using java.net api to connect to backand.com
 * Using google's Gson api to read json objects returned by backand.com
 */
public class BackandClientImpl implements BackandClient {
  private AccessToken token = null;
  private Proxy proxy;
  private String charset="UTF-8";
  private static BackandClient instance;

  private BackandClientImpl() {
    proxy = Proxy.NO_PROXY;
  }

  private BackandClientImpl(String proxyHost, Integer proxyPort) {
    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
  }

  public static BackandClient get() {
    if (instance == null) {
      instance = new BackandClientImpl();
    }
    return instance;
  }

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
  public <T> T retrieveObjectById(String table, int id, Class<T> classOfT) throws BackandException {
    String callBackand = callBackand("/1/objects/" + table +"/"+id, null);
    LinkedTreeMap t = extractResponseObject(callBackand, LinkedTreeMap.class);
    T t1 = BackandResponseWrapper.generateWrappedObject(classOfT, t, table);
    return t1;
//    return extractResponseObject(callBackand, classOfT);
  }

  @Override
  public <T> T[] retrieveObjects(String table, Class<T> classOfT) throws BackandClientException {
    String callBackand = callBackand("/1/objects/"+table , null);
    BackandResponseWrapper<T> backandResponseWrapper = extractResponseObject(callBackand, BackandResponseWrapper.class);
    backandResponseWrapper.wrapData(classOfT, table);
    return backandResponseWrapper.getData();
  }

  @Override
  public <T> T[] retrieveObjectDependence(String table, Object id, String param, Class<T> classOfT) throws BackandClientException {
    String callBackand = callBackand("/1/objects/" + table +"/"+id+"/"+param, null);
    BackandResponseWrapper<T> backandResponseWrapper = extractResponseObject(callBackand, BackandResponseWrapper.class);
    backandResponseWrapper.wrapData(classOfT, table);
    return backandResponseWrapper.getData();
  }

  private <T> T extractResponseObject(String response, Class<T> classOfT) {
    System.out.println("Extracting " +response);
    Gson g = new Gson();
    return g.fromJson(response, classOfT);
  }

  private <T> T extractResponseObject(String response, Type classOfT) {
    System.out.println("Extracting " +response);
    Gson g = new Gson();
    return g.fromJson(response, classOfT);
  }

  private String callBackand(String endpoint, Map<String, String> parameters) throws BackandClientException {
    HttpURLConnection conn = null;
    String response = "";
    try {
      conn = getHttpURLConnection(endpoint);

      addBodyParameters(conn, parameters);

      response = getResponseAsString(conn);
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

  private String getResponseAsString(HttpURLConnection conn) throws IOException {
    String response;
    if (conn.getResponseCode() != 200) {
      throw new RuntimeException("Failed : HTTP error code : "
        + conn.getResponseCode());
    }

    StringBuilder responseBuilder = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(
      (conn.getInputStream())));

    String output;
    while ((output = br.readLine()) != null) {
      responseBuilder.append(output);
    }
    response = responseBuilder.toString();
    return response;
  }

  private HttpURLConnection getHttpURLConnection(String endpoint) throws IOException {
    URL apiUrl = new URL(BACKAND_API_URL+endpoint);
    HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection(proxy);
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Accept", "application/json");
    if (token != null) {
      conn.setRequestProperty("Authorization", "Bearer "+token.getAccess_token());
    }
    conn.setDoOutput(true);

    conn.setRequestProperty("Accept-Charset", charset);
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
    return conn;
  }

  private void addBodyParameters(HttpURLConnection conn, Map<String, String> parameters) throws IOException {
    if (parameters == null || parameters.isEmpty()) {
      return;
    }
    String query = encodeQueryParameters(parameters);

    try (OutputStream output = conn.getOutputStream()) {
      output.write(query.getBytes(charset));
    }
  }

  private String encodeQueryParameters(Map<String, String> parameters) throws UnsupportedEncodingException {
    StringBuilder builder = new StringBuilder();
    parameters.forEach((key, value) -> builder.append(key)
                                              .append("=")
                                              .append(encodeParam(value))
                                              .append("&") );

    String query = builder.toString();
    if (query.length() > 0) {
      query = query.substring(0, query.length()-1);
    }
    return query;
  }

  private String encodeParam(String value) {
    try {
      return URLEncoder.encode(value, charset);
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return "";
  }

  public static void main(String[] args) throws BackandException {
    String username;
    String password;
    String appName;
    MainArgs getMainArgs = new MainArgs(args).invoke();
    username = getMainArgs.getUsername();
    password = getMainArgs.getPassword();
    appName = getMainArgs.getAppName();
    BackandClient backandClient = BackandClientImpl.get("lyon.proxy.corp.sopra", 8080);
    backandClient.establishConnection(username, password, appName);
    User users = backandClient.retrieveObjectById("users", 1, User.class);
    System.out.println(users);
    Collection<Beacon> seen_beacons = users.getSeen_beacons();
    System.out.println(seen_beacons);
    Object[] all = backandClient.retrieveObjects("users", User.class);
    Arrays.stream(all).forEach(System.out::println);
    Collection<Beacon> beacons = ((User) all[0]).getBeacons();
    beacons.forEach(System.out::println);
  }


  private static class MainArgs {
    private String[] args;
    private String username;
    private String password;
    private String appName;

    public MainArgs(String... args) {
      this.args = args;
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }

    public String getAppName() {
      return appName;
    }

    public MainArgs invoke() {
      if (args != null && args.length == 3) {
        username = args[0];
        password = args[1];
        appName = args[2];
      }
      else {
        Scanner in = new Scanner(System.in);
        System.out.print("username: ");
        username = in.nextLine();
        System.out.print("password: ");
        password = in.nextLine();
        System.out.print("application name:");
        appName = in.nextLine();
      }
      return this;
    }
  }
}
 
