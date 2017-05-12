package fr.woorib.backand.client.exception;

/**
 * Exception thrown when an error happens in the Backand Client.
 */
public class BackandException extends Exception {
  public BackandException(String s, Exception e) {
    super(s,e);
  }

  public BackandException(String s) {
    super(s);
  }
}
 
