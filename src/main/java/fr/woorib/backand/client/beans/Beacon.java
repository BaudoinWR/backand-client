package fr.woorib.backand.client.beans;

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

  @Override
  public String toString() {
    return "Beacon{" +
            "latitude=" + latitude +
            ", longitude=" + longitude +
            ", description='" + description + '\'' +
            ", owner=" + owner +
            ", id=" + id +
            '}';
  }
}

