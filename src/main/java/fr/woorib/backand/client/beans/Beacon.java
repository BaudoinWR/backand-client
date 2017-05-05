package fr.woorib.backand.client.beans;

/**
 * Beacon bean used for personal testing with backand.com account
 */
public class Beacon {
  private Double latitude;
  private Double longitude;
  private String description;

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

  @Override
  public String toString() {
    return "Beacon{" +
      "latitude=" + latitude +
      ", longitude=" + longitude +
      ", description='" + description + '\'' +
      '}';
  }
}

