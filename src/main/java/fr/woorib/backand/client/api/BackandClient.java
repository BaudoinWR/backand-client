package fr.woorib.backand.client.api;

import java.util.Date;
import fr.woorib.backand.client.exception.BackandClientException;
import fr.woorib.backand.client.exception.BackandException;

/**
 * Java api to connect to backand.com serverless provider
 */
public interface BackandClient {

  /** Classes returned by backand.com that should be directly inserted into beans. */
  Class[] BACKAND_CLASSES = new Class[] {
          Integer.class,
          int.class,
          Double.class,
          double.class,
          Float.class,
          float.class,
          Boolean.class,
          boolean.class,
          String.class,
          Date.class};
  /** Backand's public API url. **/
  String BACKAND_API_URL = "https://api.backand.com";
  /** Backand's oAuth endpoint to get user token. **/
  String TOKEN_ENDPOINT = "/token";
  /** Backand's public API endpoint. **/
  String BACKAND_API_ENDPOINT = "/1/objects/";

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
  <T> T retrieveObjectById(String table, Integer id, Class<T> classOfT) throws BackandException;

  /**
   * Retrieve an object on backand.com based on it's type and id
   * @param id
   * @param type must be anotated with {@link fr.woorib.backand.client.api.BackandObject}
   * @return
   * @throws BackandException
   */
  <T> T retrieveBackandObjectFromId(Integer id, Class<T> type) throws BackandException;

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
   * Use the backand.com /object/table/id/param to retrieve objects through many to one relationships
   * @param table the table of the base object
   * @param id the id of the base object
   * @param param the name of the relationship in the base table
   * @param classOfT the class of the expected objects in the relationship
   * @param <T>
   * @return an array of objects of class T
   * @throws BackandClientException
   */
  <T> T[] retrieveObjectDependence(String table, Integer id, String param, Class<T> classOfT) throws BackandClientException;

  /**
   * Use the backand.com /object/table/id/param to retrieve objects through many to many relationships
   * @param table the table of the base object
   * @param id the id of the base object
   * @param param the name of the relationship in the base table
   * @param classOfT the class of the expected objects in the relationship
   * @param manyToManySide the backand.com column in the manyToMany relationship
   * @param <T>
   * @return an array of objects of class T
   * @throws BackandClientException
   */
  <T> T[] retrieveObjectDependence(String table, Integer id, String param, Class<T> classOfT, String manyToManySide) throws BackandClientException;

  /**
   * Save a new object onto backand.com
   * @param object must have a BackandObject annotation.
   * @param <T> the object's class.
   * @return a proxy of the backand object created.
   */
  <T> T insertNewObject(T object) throws BackandException;
}
 
