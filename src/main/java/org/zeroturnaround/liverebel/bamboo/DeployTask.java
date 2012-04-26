package org.zeroturnaround.liverebel.bamboo;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.zeroturnaround.liverebel.api.ApplicationInfo;
import com.zeroturnaround.liverebel.api.DuplicationException;
import com.zeroturnaround.liverebel.api.UploadInfo;
import com.zeroturnaround.liverebel.api.diff.DiffResult;
import com.zeroturnaround.liverebel.api.Error;
import com.zeroturnaround.liverebel.api.ParseException;
import com.zeroturnaround.liverebel.api.diff.Level;
import com.zeroturnaround.liverebel.api.update.ConfigurableUpdate;
import com.zeroturnaround.liverebel.util.LiveApplicationUtil;
import com.zeroturnaround.liverebel.util.LiveRebelXml;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipException;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Mirko Adari
 */
public class DeployTask implements TaskType {

  public static final String ARTIFACT_DEPLOYED_AND_UPDATED = "SUCCESS. Artifact deployed and activated in all %s servers: %s\n";
  private BuildLogger logger;
  private LiveRebelManager liverebel;
  private Strategy strategy;
  private boolean useFallbackIfCompatibleWithWarnings;

  public void setLiverebel(LiveRebelManager liverebel) {
    this.liverebel = liverebel;
  }

  @Override
  public TaskResult execute(TaskContext tc) throws TaskException {
    TaskResultBuilder result = TaskResultBuilder.create(tc);
    logger = tc.getBuildLogger();
    Map<String, Object> configuration = DeployTaskConfigurator.fromConfiguration(tc.getConfigurationMap());
    final String warFilePath = (String) configuration.get("warFilePath");
    final File warFile = new File(tc.getRootDirectory(), warFilePath);

    if (!warFile.exists()) {
      logger.addErrorLogEntry("Could not find any artifact to deploy. Please, specify it in task configuration.");
      return result.failedWithError().build();
    }
    List<String> deployableServers = (List<String>) configuration.get("servers");
    if (deployableServers.isEmpty()) {
      logger.addErrorLogEntry("No servers specified in LiveRebel configuration.");
      return result.failedWithError().build();
    }

    this.strategy = Strategy.values()[Integer.valueOf((String) configuration.get("updateMode")).intValue()];
    this.useFallbackIfCompatibleWithWarnings = Boolean.valueOf((String) configuration.get("fallback"));
    try {
      logger.addBuildLogEntry(String.format("Processing artifact: %s\n", warFile));
      LiveRebelXml lrXml = getLiveRebelXml(warFile);
      ApplicationInfo applicationInfo = liverebel.getConnection().getApplication(lrXml.getApplicationId());
      uploadIfNeeded(applicationInfo, lrXml.getVersionId(), warFile);
      update(lrXml, applicationInfo, warFile, deployableServers);
      logger.addBuildLogEntry(String.format(ARTIFACT_DEPLOYED_AND_UPDATED, deployableServers, warFile));
    }
    catch (IllegalArgumentException e) {
      logger.addErrorLogEntry("ERROR!", e);
      return result.failedWithError().build();
    }
    catch (Error e) {
      logger.addErrorLogEntry("ERROR! Unexpected error received from server.");
      logger.addErrorLogEntry("URL: " + e.getURL());
      logger.addErrorLogEntry("Status code: " + e.getStatus());
      logger.addErrorLogEntry("Message: " + e.getMessage());
      return result.failedWithError().build();
    }
    catch (ParseException e) {
      logger.addErrorLogEntry("ERROR! Unable to read server response.");
      logger.addErrorLogEntry("Response: " + e.getResponse());
      logger.addErrorLogEntry("Reason: " + e.getMessage());
      return result.failedWithError().build();
    }
    catch (RuntimeException e) {
      if (e.getCause() instanceof ZipException) {
        logger.addErrorLogEntry(String.format(
            "ERROR! Unable to read artifact (%s). The file you trying to deploy is not an artifact or may be corrupted.\n",
            warFile));
      }
      else {
        logger.addErrorLogEntry("ERROR! Unexpected error occured:", e);
      }
      return result.failedWithError().build();
    }
    catch (Throwable t) {
      logger.addErrorLogEntry("ERROR! Unexpected error occured:", t);
      return result.failedWithError().build();
    }
    return result.build();
  }

  boolean isFirstRelease(ApplicationInfo applicationInfo) {
    return applicationInfo == null;
  }

  void update(LiveRebelXml lrXml, ApplicationInfo applicationInfo, File warFile, List<String> selectedServers) throws IOException,
      InterruptedException {
    logger.addBuildLogEntry("Starting updating application on servers:");
    Set<String> deployServers = getDeployServers(applicationInfo, selectedServers);
    if (!deployServers.isEmpty()) {
      deploy(lrXml, warFile, deployServers);
    }

    if (deployServers.size() != selectedServers.size()) {
      Set<String> activateServers = new HashSet<String>(selectedServers);
      activateServers.removeAll(deployServers);

      Level diffLevel = getMaxDifferenceLevel(applicationInfo, lrXml, activateServers);

      activate(lrXml, warFile, activateServers, diffLevel);
    }
  }

  void deploy(LiveRebelXml lrXml, File warfile, Set<String> serverIds) {
    logger.addBuildLogEntry(String.format("Deploying new application on %s.\n", serverIds));
    liverebel.getConnection().deploy(lrXml.getApplicationId(), lrXml.getVersionId(), null, serverIds);
    logger.addBuildLogEntry(String.format("SUCCESS: Application deployed to %s.\n", serverIds));
  }

  void activate(LiveRebelXml lrXml, File warfile, Set<String> serverIds, Level diffLevel) throws IOException,
      InterruptedException {
    ConfigurableUpdate update = liverebel.getConnection().update(lrXml.getApplicationId(), lrXml.getVersionId());
    if (diffLevel == Level.ERROR || diffLevel == Level.WARNING && useFallbackIfCompatibleWithWarnings) {
      if (strategy == Strategy.OFFLINE)
        update.enableOffline();
      else if (strategy == Strategy.ROLLING)
        update.enableRolling();
    }
    update.on(serverIds);
    update.execute();
  }

  void uploadIfNeeded(ApplicationInfo applicationInfo, String currentVersion, File warFile) throws IOException,
      InterruptedException {
    if (applicationInfo != null && applicationInfo.getVersions().contains(currentVersion)) {
      logger.addBuildLogEntry("Current version of application is already uploaded. Skipping upload.");
    }
    else {
      uploadArtifact(warFile);
      logger.addBuildLogEntry(String.format("Artifact uploaded: %s\n", warFile));
    }
  }

  boolean uploadArtifact(File artifact) throws IOException, InterruptedException {
    try {
      UploadInfo upload = liverebel.getConnection().upload(artifact);
      logger.addBuildLogEntry(String.format("SUCCESS: %s %s was uploaded.\n", upload.getApplicationId(), upload.getVersionId()));
      return true;
    }
    catch (DuplicationException e) {
      logger.addBuildLogEntry(e.getMessage());
      return false;
    }
  }

  LiveRebelXml getLiveRebelXml(File warFile) throws IOException, InterruptedException {
    LiveRebelXml lrXml = LiveApplicationUtil.findLiveRebelXml(warFile);
    if (lrXml != null) {
      logger.addBuildLogEntry(String.format("Found LiveRebel xml. Current application is: %s %s.\n", lrXml.getApplicationId(), lrXml.getVersionId()));
      if (lrXml.getApplicationId() == null) {
        throw new RuntimeException("application name is not set in liverebel.xml");
      }
      if (lrXml.getVersionId() == null) {
        throw new RuntimeException("application version is not set in liverebel.xml");
      }
      return lrXml;
    }
    else {
      throw new RuntimeException("Didn't find liverebel.xml");
    }
  }

  Set<String> getDeployServers(ApplicationInfo applicationInfo, List<String> selectedServers) {
    Set<String> deployServers = new HashSet<String>();

    if (isFirstRelease(applicationInfo)) {
      deployServers.addAll(selectedServers);
      return deployServers;
    }

    Map<String, String> activeVersions = applicationInfo.getActiveVersionPerServer();

    for (String server : selectedServers) {
      if (!activeVersions.containsKey(server))
        deployServers.add(server);
    }
    return deployServers;
  }

  private Level getMaxDifferenceLevel(ApplicationInfo applicationInfo, LiveRebelXml lrXml, Set<String> serversToUpdate) {
    Map<String, String> activeVersions = applicationInfo.getActiveVersionPerServer();
    Level diffLevel = Level.NOP;
    String versionToUpdateTo = lrXml.getVersionId();
    int serversWithSameVersion = 0;
    for (Entry<String, String> entry : activeVersions.entrySet()) {
      String server = entry.getKey();
      if (!serversToUpdate.contains(server)) {
        continue;
      }
      String versionInServer = entry.getValue();
      if (StringUtils.equals(versionToUpdateTo, versionInServer)) {
        serversWithSameVersion++;
        serversToUpdate.remove(server);
        logger.addBuildLogEntry(
            "Server " + server + " already contains active version " + lrXml.getVersionId() + " of application "
            + lrXml.getApplicationId());
      }
      else {
        DiffResult differences = getDifferences(lrXml, versionInServer);
        Level maxLevel = differences.getMaxLevel();
        if (maxLevel.compareTo(diffLevel) > 0) {
          diffLevel = maxLevel;
        }
      }
    }
    if (serversWithSameVersion > 0) {
      String msg = "Cancelling update - version " + lrXml.getVersionId() + " of application "
          + lrXml.getApplicationId() + " is already deployed to " + serversWithSameVersion + " servers";
      if (!serversToUpdate.isEmpty()) {
        msg += " out of " + (serversToUpdate.size() + serversWithSameVersion) + " servers.";
      }
      throw new RuntimeException(msg);
    }
    return diffLevel;
  }

  DiffResult getDifferences(LiveRebelXml lrXml, String activeVersion) {
    DiffResult diffResult = liverebel.getConnection().compare(lrXml.getApplicationId(), activeVersion, lrXml.getVersionId(), false);
    return diffResult;
  }
}
