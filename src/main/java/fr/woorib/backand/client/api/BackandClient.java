/**
 * Paquet de définition
 **/
package fr.woorib.backand.client.api;

import fr.woorib.backand.client.exception.BackandException;

/**
 * Java api to connect to backand.com serverless provider
 */
public interface BackandClient {
  /** Backand's public API url. **/
  static String BACKAND_API_URL = "https://api.backand.com";
  /** Backand's oAuth endpoint to get user token **/
  static String TOKEN_URI = "/token";

  /** Establish a connection to the backand.com api in order to retrive an access_token
   *
   * @param username backand.com username / email to connect to the service.
   * @param password password used on backand.com website.
   * @param appName Application Name to get an oAuth token for.
   * @return true if the connection was successfully established.
   * @throws BackandException when an issue happens while trying to retrieve the token.
   */
  public boolean establishConnection(String username, String password, String appName) throws BackandException;
}
 
