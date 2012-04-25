package org.zeroturnaround.liverebel.bamboo;

import com.atlassian.bamboo.task.*;

/**
 *
 * @author Mirko Adari
 */
public class DeployTask implements TaskType {

  @Override
  public TaskResult execute(TaskContext tc) throws TaskException {
    TaskResultBuilder result = TaskResultBuilder.create(tc);
    return result.build();
  }
}
