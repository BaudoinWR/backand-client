package fr.woorib.backand.client.api;

import fr.woorib.backand.client.exception.BackandClientException;
import fr.woorib.backand.client.exception.BackandException;

/**
 * Java api to connect to backand.com serverless provider
 */
public interface BackandClient {
  /** Backand's public API url. **/
  static String BACKAND_API_URL = "https://api.backand.com";
  /** Backand's oAuth endpoint to get user token **/
  static String TOKEN_ENDPOINT = "/token";

  /** Establish a connection to the backand.com api in order to retrive an access_token.
   *
   * @param username backand.com username / email to connect to the service.
   * @param password password used on backand.com website.
   * @param appName Application Name to get an oAuth token for.
   * @return true if the connection was successfully established.
   * @throws BackandException when an issue happens while trying to retrieve the token.
   */
  boolean establishConnection(String username, String password, String appName) throws BackandException;

  <T> T retrieveObjectById(String table, int id, Class<T> classOfT) throws BackandException;

  <T> T[] retrieveObjects(String table, Class<T> classOfT) throws BackandClientException;

  <T> T[] retrieveObjectDependence(String table, Object id, String param, Class<T> classOfT) throws BackandClientException;

}
 
