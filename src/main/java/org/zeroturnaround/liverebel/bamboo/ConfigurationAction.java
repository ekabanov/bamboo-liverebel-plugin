/**
 *    Copyright (C) 2012 ZeroTurnaround OÜ <support@zeroturnaround.com>
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

import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.zeroturnaround.liverebel.api.CommandCenterFactory;
import com.zeroturnaround.liverebel.api.ConnectException;
import com.zeroturnaround.liverebel.api.Forbidden;
import java.net.MalformedURLException;
import java.net.URL;
import org.codehaus.plexus.util.StringUtils;

/**
 * 
 * @author Mirko Adari
 */
public class ConfigurationAction extends BambooActionSupport {

  private String URL;
  private String token;
  private String method;
  private PluginSettingsFactory pluginSettingsFactory;
  private String message;

  @Override
  public void validate() {
    clearErrorsAndMessages();
    if (StringUtils.isBlank(token)) {
      addFieldError("token", "Please specify a token.");
    }
    if (StringUtils.isBlank(URL)) {
      addFieldError("URL", "Please specify a URL of an LiveRebel Command Center.");
    }
    else {
      try {
        new URL(URL);
      }
      catch (MalformedURLException mue) {
        addFieldError("url", "Please specify a valid URL of an LiveRebel Command Center.");
      }
    }
  }

  public String doEdit() throws Exception {
    PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

    String URL = (String) settings.get("liverebel.url");
    if (URL != null)
      setURL(URL);
    else
      setURL("https://127.0.0.1:9001");
    String token = (String) settings.get("liverebel.token");
    if (token != null)
      setToken(token);

    if (getMessage() == "success") {
      addActionMessage(getMessage());
    }
    return "input";
  }

  public String doSave() throws Exception {
    if (isTestConnection()) {
      testConnection();
      return "input";
    }
    PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
    settings.put("liverebel.url", getURL());
    settings.put("liverebel.token", getToken());

    return "success";
  }

  /**
   * @return the URL
   */
  public String getURL() {
    return URL;
  }

  /**
   * @param URL the URL to set
   */
  public void setURL(String URL) {
    this.URL = URL;
  }

  /**
   * @return the token
   */
  public String getToken() {
    return token;
  }

  /**
   * @param token the token to set
   */
  public void setToken(String token) {
    this.token = token;
  }

  /**
   * @return the method
   */
  public String getMethod() {
    return method;
  }

  /**
   * @param method the method to set
   */
  public void setMethod(String method) {
    this.method = method;
  }

  private boolean isTestConnection() {
    return "Test connection".equals(getMethod());
  }

  private void testConnection() {
    try {
      new CommandCenterFactory().setUrl(URL).setVerbose(false).authenticate(token).newCommandCenter();
      addActionMessage("Connection successful.");
    }
    catch (Forbidden e) {
      addActionError("Please, provide correct authentication token!");
    }
    catch (ConnectException e) {
      addActionError("Could not connect to LiveRebel at (" + e.getURL() + ")");
    }
    catch (Exception e) {
      addActionError(e.getMessage());
    }
  }

  /**
   * @return the factory
   */
  public PluginSettingsFactory getPluginSettingsFactory() {
    return pluginSettingsFactory;
  }

  /**
   * @param pluginSettingsFactory the factory to set
   */
  public void setPluginSettingsFactory(PluginSettingsFactory pluginSettingsFactory) {
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }
}
