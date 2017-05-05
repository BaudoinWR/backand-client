package fr.woorib.backand.client;

/**
 * AccessToken holder for oAuth token returned by backand.com
 */
class AccessToken {
  private String access_token;
  private String token_type;
  private int expires_in;
  private String appName;
  private String username;
  private String role;
  private String firstName;
  private String lastName;
  private String fullName;
  private int regId;
  private int userId;

  public String getAccess_token() {
    return access_token;
  }

  public void setAccess_token(String access_token) {
    this.access_token = access_token;
  }

  public String getToken_type() {
    return token_type;
  }

  public void setToken_type(String token_type) {
    this.token_type = token_type;
  }

  public float getExpires_in() {
    return expires_in;
  }

  public void setExpires_in(int expires_in) {
    this.expires_in = expires_in;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public float getRegId() {
    return regId;
  }

  public void setRegId(int regId) {
    this.regId = regId;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  @Override
  public String toString() {
    return "AccessToken{" +
      "access_token='" + access_token + '\'' +
      ", token_type='" + token_type + '\'' +
      ", expires_in=" + expires_in +
      ", appName='" + appName + '\'' +
      ", username='" + username + '\'' +
      ", role='" + role + '\'' +
      ", firstName='" + firstName + '\'' +
      ", lastName='" + lastName + '\'' +
      ", fullName='" + fullName + '\'' +
      ", regId=" + regId +
      ", userId=" + userId +
      '}';
  }
}
 
