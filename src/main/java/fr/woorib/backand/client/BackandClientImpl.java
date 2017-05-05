package fr.woorib.backand.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
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
    String callBackand = callBackand("/1/objects/" + table +"/"+id);
    LinkedTreeMap t = extractResponseObject(callBackand, LinkedTreeMap.class);
    T t1 = BackandResponseWrapper.generateWrappedObject(classOfT, t, table);
    return t1;
  }

  @Override
  public <T> T[] retrieveObjects(String table, Class<T> classOfT) throws BackandClientException {
    String callBackand = callBackand("/1/objects/"+table);
    BackandResponseWrapper<T> backandResponseWrapper = extractResponseObject(callBackand, BackandResponseWrapper.class);
    backandResponseWrapper.wrapData(classOfT, table);
    return backandResponseWrapper.getData();
  }

  @Override
  public <T> T[] retrieveObjectDependence(String table, Integer id, String param, Class<T> classOfT) throws BackandClientException {
    String callBackand = callBackand("/1/objects/" + table +"/"+id+"/"+param);
    BackandResponseWrapper<T> backandResponseWrapper = extractResponseObject(callBackand, BackandResponseWrapper.class);
    backandResponseWrapper.wrapData(classOfT, table);
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
    String response = "";
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
 
