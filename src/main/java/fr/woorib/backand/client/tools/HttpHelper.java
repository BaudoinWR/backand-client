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
import fr.woorib.backand.client.exception.BackandException;

/**
 * Helper for http connection to the backand.com framework
 */
public class HttpHelper {

  public static final String GET = "GET";
  public static final String POST = "POST";
  public static final String DELETE = "DELETE";
  private static final String CHARSET = "UTF-8";

  /**
   * Reads the http response and returns it as a String.
   * @param conn
   * @return
   * @throws IOException if an error is raised while reading the response
   * @throws BackandException if the http response code is not 200
   */
  public static String getResponseAsString(HttpURLConnection conn) throws IOException, BackandException {
    String response;
    if (conn.getResponseCode() != 200) {
      throw new BackandException("Failed : HTTP error code : "
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

  /**
   * Creates an HTTP connection.
   * <ul>
   * <li>Sets the authorization header with backand.com AccessToken</li>
   * <li>Sets the accept property as application/json</li>
   * <li>Sets the Charset as UTF-8</li>
   * </ul>
   * @param proxy
   * @param token
   * @param endpoint
   * @param method
   * @return
   * @throws IOException
   */
  public static HttpURLConnection getHttpURLConnection(Proxy proxy, AccessToken token, String endpoint, String method) throws IOException {
    URL apiUrl = new URL(endpoint);
    HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection(proxy);
    conn.setRequestMethod(method);
    conn.setRequestProperty("Accept", "application/json");
    if (token != null) {
      conn.setRequestProperty("Authorization", "Bearer "+token.getAccess_token());
    }
    conn.setDoOutput(true);

    conn.setRequestProperty("Accept-Charset", CHARSET);
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET);
    return conn;
  }

  /**
   * Encodes the parameters into UTF-8 and sets them as query parameters.
   * @param conn
   * @param parameters
   * @throws IOException
   */
  public static void addBodyParameters(HttpURLConnection conn, Map<String, String> parameters) throws IOException {
    if (parameters == null || parameters.isEmpty()) {
      return;
    }
    String query = encodeQueryParameters(parameters);

    try (OutputStream output = conn.getOutputStream()) {
      output.write(query.getBytes(CHARSET));
    }
  }

  /**
   * Encodes query parameters as 'key=value' separated by an ampersand character.
   * @param parameters
   * @return
   * @throws UnsupportedEncodingException
   */
  private static String encodeQueryParameters(Map<String, String> parameters) throws UnsupportedEncodingException {
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

  private static String encodeParam(String value) {
    try {
      return URLEncoder.encode(value, CHARSET);
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return "";
  }

}
 
