/**
 * Paquet de définition
 **/
package fr.woorib.backand.client;

import java.util.*;

import fr.woorib.backand.client.api.BackandManyToMany;
import fr.woorib.backand.client.api.BackandObject;
import fr.woorib.backand.client.exception.BackandException;
import fr.woorib.backand.client.tools.ProxyHelper;

/**
 * Main class used for testing of backand.com calls
 */
public class Main {

  public static void main(String[] args) throws BackandException, InstantiationException, IllegalAccessException {
    MainArgs getMainArgs = new MainArgs(args).invoke();
    String username = getMainArgs.getUsername();
    String password = getMainArgs.getPassword();
    String appName = getMainArgs.getAppName();
    BackandClientImpl backandClient = (BackandClientImpl) BackandClientImpl.get();
    backandClient.establishConnection(username, password, appName);
    Beacon beacon = new Beacon();
    User users = backandClient.retrieveObjectById("users", 3, User.class);
    User user2 = backandClient.retrieveObjectById("users", 3, User.class);
    User user = ProxyHelper.unProxify(users);
    System.out.println("USER1:" + user);
    beacon.setDescription("test");
    beacon.setLatitude(33.0);
    beacon.setLongitude(36.0);
    beacon.setOwner(user);
    List<User> list = new ArrayList<>();
    list.add(user);
    list.add(user2);
    beacon.setTargets(list);
   // Beacon s = backandClient.insertNewObject(beacon);
    // System.out.println("OWNER: " +s.getOwner());
    System.out.println("USERS: " +users);
    Collection<Beacon> seen_beacons = users.getSeen_beacons();
    System.out.println("SEEN BEACONS: "+seen_beacons);
    Object[] all = backandClient.retrieveObjects("users", User.class);
/*
    Arrays.stream(all).forEach(System.out::println);
    Collection<Beacon> beacons = ((User) all[0]).getBeacons();
    beacons.forEach(b -> { System.out.println(b); System.out.println(b.getOwner());} );
    beacons = ((User) all[0]).getSeen_beacons();
    beacons.forEach(b -> { System.out.println(b); System.out.println(b.getTargets());} );
*/
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

  /**
   * Beacon bean used for personal testing with backand.com account
   */
  @BackandObject(table="beacons")
  public static class Beacon {
    private Double latitude;
    private Double longitude;
    private String description;
    private User owner;
    private Collection<User> targets;
    private int id;

    public Double getLatitude() {
      return latitude;
    }

    public void setLatitude(Double latitude) {
      this.latitude = latitude;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public Double getLongitude() {
      return longitude;
    }

    public void setLongitude(Double longitude) {
      this.longitude = longitude;
    }

    public User getOwner() {
      return owner;
    }

    public void setOwner(User owner) {
      this.owner = owner;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    @BackandManyToMany(parameter="target", reference="beacon")
    public Collection<User> getTargets() {
      return targets;
    }

    public void setTargets(Collection<User> targets) {
      this.targets = targets;
    }

    @Override
    public String toString() {
      return "Beacon{" +
        "latitude=" + latitude +
        ", longitude=" + longitude +
        ", description='" + description + '\'' +
        ", owner=" + owner +
        ", targets=" + targets +
        ", id=" + id +
        '}';
    }
  }

  /**
   * User bean used for personal testing with backand.com account
   */
  @BackandObject(table="users")
  public static class User {
    private String email = "";
    private Collection<Beacon> beacons = Collections.emptyList();
    private Collection<Beacon> seen_beacons = Collections.emptyList();
    private int id = -1;
    public User() {
      email = "";
      beacons = Collections.emptyList();
      seen_beacons = Collections.emptyList();
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public Collection<Beacon> getBeacons() {
      return beacons;
    }

    public void setBeacons(Collection<Beacon> beacons) {
      this.beacons = beacons;
    }

    @BackandManyToMany(parameter = "beacon", reference = "target")
    public Collection<Beacon> getSeen_beacons() {
      return seen_beacons;
    }

    public void setSeen_beacons(Collection<Beacon> seen_beacons) {
      this.seen_beacons = seen_beacons;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    @Override
    public String toString() {
      return "User{" +
        "email='" + email + '\'' +
        ", beacons=" + beacons +
        ", seen_beacons=" + seen_beacons +
        ", id=" + id +
        '}';
    }
  }
}
 
