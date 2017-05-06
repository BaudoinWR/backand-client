/**
 * Paquet de définition
 **/
package fr.woorib.backand.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;
import fr.woorib.backand.client.api.BackandClient;
import fr.woorib.backand.client.beans.Beacon;
import fr.woorib.backand.client.beans.User;
import fr.woorib.backand.client.exception.BackandException;

/**
 * Main class used for testing of backand.com calls
 */
public class Main {

  public static void main(String[] args) throws BackandException {
    MainArgs getMainArgs = new MainArgs(args).invoke();
    String username = getMainArgs.getUsername();
    String password = getMainArgs.getPassword();
    String appName = getMainArgs.getAppName();
    BackandClient backandClient = BackandClientImpl.get();
    backandClient.establishConnection(username, password, appName);
    User users = backandClient.retrieveObjectById("users", 1, User.class);
    System.out.println(users);
    Collection<Beacon> seen_beacons = users.getSeen_beacons();
    System.out.println(seen_beacons);
    Object[] all = backandClient.retrieveObjects("users", User.class);
    Arrays.stream(all).forEach(System.out::println);
    Collection<Beacon> beacons = ((User) all[0]).getBeacons();
    beacons.forEach(b -> { System.out.println(b); System.out.println(b.getOwner());} );
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
 
