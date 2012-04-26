/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zeroturnaround.liverebel.bamboo;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.zeroturnaround.liverebel.api.CommandCenter;
import com.zeroturnaround.liverebel.api.ServerInfo;
import java.util.*;

/**
 *
 * @author Mirko Adari
 */
public class DeployTaskConfigurator extends AbstractTaskConfigurator {

  private LiveRebelManager liverebel;

  public void setLiveRebelManager(LiveRebelManager liverebel) {
    this.liverebel = liverebel;
  }

  private Map<String, Server> getServers() {
    Map<String, Server> servers = new HashMap<String, Server>();
    CommandCenter connection = liverebel.getConnection();
    if (connection != null) {
      for (ServerInfo server : connection.getServers().values()) {
        servers.put(server.getId(), new Server(server.getId(), server.getName(), server.isConnected(), false));
      }
    }
    return servers;
  }

  public void populateContextForAll(Map<String, Object> context, TaskDefinition td, boolean viewOnly) {
    Map<String, Object> configuration = null;;
    if (td != null)
      configuration = fromConfiguration(td.getConfiguration());

    context.put("modes", Arrays.asList(new Strategy[]{Strategy.OFFLINE, Strategy.ROLLING}));

    Map<String, Server> servers = getServers();
    if (td != null) {
      List<String> checked = (List<String>) configuration.get("servers");

      if (viewOnly)
        servers.keySet().retainAll(checked);

      context.put("checked", checked);
    }
    else {
      context.put("checked", new ArrayList<String>());
    }
    context.put("servers", servers.values());
    context.put("updateMode", Strategy.OFFLINE.getValue());
    if (td != null) {
      context.put("warFilePath", configuration.get("warFilePath")); // artifact
      context.put("fallback", configuration.get("fallback")); // compatible with warnings
      context.put("updateMode", configuration.get("updateMode"));
    }
  }

  @Override
  public void populateContextForView(Map<String, Object> context, TaskDefinition td) {
    populateContextForAll(context, td, true);
  }

  @Override
  public void populateContextForEdit(Map<String, Object> context, TaskDefinition td) {
    populateContextForAll(context, td, false);
  }

  @Override
  public void populateContextForCreate(Map<String, Object> context) {
    populateContextForAll(context, null, false);
  }

  @Override
  public Map<String, String> generateTaskConfigMap(ActionParametersMap apm, TaskDefinition td) {
    Map<String, String> taskConfig = new HashMap<String, String>();
    for (String key : apm.keySet()) {
      if (key.equals("server")) {
        String[] servers = apm.getStringArray("server");
        for (int i = 0; i < servers.length; i++)
          taskConfig.put("server." + i, servers[i]);
      }
      else {
        taskConfig.put(key, apm.getString(key));
      }
    }
    return taskConfig;
  }

  public static Map<String, Object> fromConfiguration(Map<String, String> configuration) {
    Map<String, Object> result = new HashMap<String, Object>();
    List<String> servers = new ArrayList<String>();
    for (Map.Entry<String, String> entry : configuration.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if (key.startsWith("server.")) {
        servers.add(value);
      }
      else {
        result.put(key, value);
      }
    }
    result.put("servers", servers);
    return result;
  }
}
