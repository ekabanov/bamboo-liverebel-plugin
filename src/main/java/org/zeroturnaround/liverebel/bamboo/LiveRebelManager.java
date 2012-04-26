package org.zeroturnaround.liverebel.bamboo;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.zeroturnaround.liverebel.api.CommandCenter;
import com.zeroturnaround.liverebel.api.CommandCenterFactory;
import com.zeroturnaround.liverebel.api.ConnectException;
import com.zeroturnaround.liverebel.api.Forbidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mirko Adari
 */
public class LiveRebelManager {

  private static final Logger log = LoggerFactory.getLogger(LiveRebelManager.class);
  private PluginSettingsFactory pluginSettingsFactory;

  public CommandCenter getConnection() {
    if (getURL() == null || getToken() == null) {
      log.warn("Please, navigate to LiveRebel Configuration to specify running LiveRebel Url and Authentication Token.");
      return null;
    }

    try {
      CommandCenter commandCenter = new CommandCenterFactory().setUrl(getURL()).setVerbose(true).authenticate(getToken()).newCommandCenter();
      return commandCenter;
    }
    catch (Forbidden e) {
      log.warn("ERROR! Access denied. Please, navigate to LiveRebel Configuration to specify LiveRebel Authentication Token.");
    }
    catch (ConnectException e) {
      log.warn("ERROR! Unable to connect to server.");
      log.warn( "URL: " + e.getURL());
      if (e.getURL().equals("https://")) {
        log.warn("Please, navigate to LiveRebel Configuration to specify running LiveRebel Url.");
      }
      else {
        log.warn( "Reason: " + e.getMessage());
      }
    }
    return null;
  }

  public void setPluginSettingsFactory(PluginSettingsFactory pluginSettingsFactory) {
    this.pluginSettingsFactory = pluginSettingsFactory;
  }

  public String getURL() {
    return (String) pluginSettingsFactory.createGlobalSettings().get("liverebel.url");
  }

  public String getToken() {
    return (String) pluginSettingsFactory.createGlobalSettings().get("liverebel.token");
  }
}
