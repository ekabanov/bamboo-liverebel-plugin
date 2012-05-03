/**
 *    Copyright (C) 2012 ZeroTurnaround LLC <support@zeroturnaround.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.zeroturnaround.liverebel.bamboo;

import java.io.Serializable;

/**
 *
 * @author Mirko Adari
 */
public class Server implements Serializable {

  private String title;
  private String id;
  private transient boolean connected;
  private transient boolean checked;

  public Server() {
  }

  public Server(String id, String title, boolean connected, boolean checked) {
    this.title = title;
    this.id = id;
    this.connected = connected;
    this.checked = checked;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title + " " + (connected ? "(online)" : "(offline)");
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the connected
   */
  public boolean isConnected() {
    return connected;
  }

  /**
   * @param connected the connected to set
   */
  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  /**
   * @return the checked
   */
  public boolean isChecked() {
    return checked;
  }

  /**
   * @param checked the checked to set
   */
  public void setChecked(boolean checked) {
    this.checked = checked;
  }
}
