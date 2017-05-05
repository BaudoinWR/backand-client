package fr.woorib.backand.client.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import fr.woorib.backand.client.AccessToken;

/**
 * Helper for http connection to the backand.com framework
 */
public class HttpHelper {

  private static final String CHARSET = "UTF-8";

  public static String getResponseAsString(HttpURLConnection conn) throws IOException {
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

  public static HttpURLConnection getHttpURLConnection(Proxy proxy, AccessToken token, String endpoint) throws IOException {
    URL apiUrl = new URL(endpoint);
    HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection(proxy);
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Accept", "application/json");
    if (token != null) {
      conn.setRequestProperty("Authorization", "Bearer "+token.getAccess_token());
    }
    conn.setDoOutput(true);

    conn.setRequestProperty("Accept-Charset", CHARSET);
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET);
    return conn;
  }

  public static void addBodyParameters(HttpURLConnection conn, Map<String, String> parameters) throws IOException {
    if (parameters == null || parameters.isEmpty()) {
      return;
    }
    String query = encodeQueryParameters(parameters);

    try (OutputStream output = conn.getOutputStream()) {
      output.write(query.getBytes(CHARSET));
    }
  }

  public static String encodeQueryParameters(Map<String, String> parameters) throws UnsupportedEncodingException {
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

  public static String encodeParam(String value) {
    try {
      return URLEncoder.encode(value, CHARSET);
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return "";
  }

}
 
