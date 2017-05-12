package fr.woorib.backand.client.beans;

import java.util.Collection;
import fr.woorib.backand.client.api.BackandManyToMany;
import fr.woorib.backand.client.api.BackandObject;

/**
 * Beacon bean used for personal testing with backand.com account
 */
@BackandObject(table="beacons")
public class Beacon {
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

