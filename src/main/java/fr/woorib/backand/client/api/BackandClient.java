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

  /**
   * Use the backand.com /object/table/id endpoint to retrieve a specific object in the specified table
   * @param table backand table to lookup
   * @param id id of the object
   * @param classOfT class of the expected item
   * @param <T>
   * @return an Object of class T
   * @throws BackandException
   */
  <T> T retrieveObjectById(String table, int id, Class<T> classOfT) throws BackandException;

  /**
   * Use the backand.com /object/table endpoint to retrieve all objects in a table.
   * @param table the table to lookup
   * @param classOfT the class of the expected items
   * @param <T>
   * @return an array of objects of class T
   * @throws BackandClientException
   */
  <T> T[] retrieveObjects(String table, Class<T> classOfT) throws BackandClientException;

  /**
   * Use the backand.com /object/table/id/param to retrieve objects through many to one or many to many relationships
   * @param table the table of the base object
   * @param id the id of the base object
   * @param param the name of the relationship in the base table
   * @param classOfT the class of the expected objects in the relationship
   * @param <T>
   * @return an array of objects of class T
   * @throws BackandClientException
   */
  <T> T[] retrieveObjectDependence(String table, Integer id, String param, Class<T> classOfT) throws BackandClientException;

}
 
