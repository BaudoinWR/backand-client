/**
 * Paquet de définition
 **/
package fr.woorib.backand.client.exception;

/**
 * Exception thrown when an exception happens in the specifically client side of the call.
 */
public class BackandClientException extends BackandException {
  public BackandClientException(String s, Exception e) {
    super(s,e);
  }
}
 
