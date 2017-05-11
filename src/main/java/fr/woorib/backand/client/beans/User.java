package fr.woorib.backand.client.beans;

import java.util.Collection;
import java.util.Collections;
import fr.woorib.backand.client.api.BackandManyToMany;
import fr.woorib.backand.client.api.BackandObject;

/**
 * User bean used for personal testing with backand.com account
 */
@BackandObject(table="users")
public class User {
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

  @BackandManyToMany(parameter = "beacon")
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

