/**
 * Paquet de définition
 **/
package fr.woorib.backand.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import fr.woorib.backand.client.api.BackandClient;
import fr.woorib.backand.client.exception.BackandClientException;
import fr.woorib.backand.client.exception.BackandException;

/**
 * Using java.net api to connect to backand.com
 * Using google's Gson api to read json objects returned by backand.com
 */
public class BackandClientImpl implements BackandClient {
  private Proxy proxy;
  private String charset="UTF-8";

  public BackandClientImpl() {
    proxy = Proxy.NO_PROXY;
  }

  public BackandClientImpl(String proxyHost, Integer proxyPort) {
    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
  }

  @Override
  public boolean establishConnection(String username, String password, String appName) throws BackandException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("username", username);
    parameters.put("password", password);
    parameters.put("appName", appName);
    parameters.put("grant_type", "password");
    String tokenUrl = BackandClient.BACKAND_API_URL + BackandClient.TOKEN_URI;

    String response = callBackand(tokenUrl, parameters);
    Object o = extractResponseObject(response);
    System.out.println(o);
    return true;
  }

  private Object extractResponseObject(String response) {
    Gson g = new Gson();
    return g.fromJson(response, LinkedTreeMap.class);
  }

  private String callBackand(String tokenUrl, Map<String, String> parameters) throws BackandClientException {
    HttpURLConnection conn = null;
    String response = "";
    try {
      conn = getHttpURLConnection(tokenUrl);

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

  private HttpURLConnection getHttpURLConnection(String tokenUrl) throws IOException {
    URL apiUrl = new URL(tokenUrl);
    HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection(proxy);
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Accept", "application/json");
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
    Scanner in = new Scanner(System.in);
    System.out.print("username: ");
    String username = in.nextLine();
    System.out.print("password: ");
    String password = in.nextLine();
    System.out.print("application name:");
    String appName = in.nextLine();
    new BackandClientImpl().establishConnection(username,password,appName);
  }
}
 
