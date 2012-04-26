package org.zeroturnaround.liverebel.bamboo;

/**
 *
 * @author Mirko Adari
 */
public enum Strategy {

  OFFLINE("Full restart (offline)"),
  ROLLING("Rolling restarts (online, session drain)");
  private final String title;

  public String getTitle() {
    return title;
  }

  public String getValue() {
    return String.valueOf(this.ordinal());
  }

  Strategy(String title) {
    this.title = title;
  }
}
