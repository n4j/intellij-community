/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zmlx.hg4idea.action.mq;

import com.intellij.openapi.progress.util.BackgroundTaskUtil;
import com.intellij.openapi.project.Project;
import com.intellij.vcs.log.Hash;
import org.jetbrains.annotations.NotNull;
import org.zmlx.hg4idea.HgBundle;
import org.zmlx.hg4idea.action.HgCommandResultNotifier;
import org.zmlx.hg4idea.execution.HgCommandExecutor;
import org.zmlx.hg4idea.execution.HgCommandResult;
import org.zmlx.hg4idea.repo.HgRepository;
import org.zmlx.hg4idea.util.HgErrorUtil;

import static java.util.Collections.singletonList;
import static org.zmlx.hg4idea.HgNotificationIdsHolder.QFINISH_ERROR;

public class HgQFinishFromLogAction extends HgMqAppliedPatchAction {
  @Override
  protected void actionPerformed(@NotNull HgRepository repository, @NotNull Hash commit) {
    String revision = commit.asString();
    Project project = repository.getProject();
    BackgroundTaskUtil.executeOnPooledThread(repository, () -> {
      HgCommandExecutor executor = new HgCommandExecutor(project);
      HgCommandResult result = executor.executeInCurrentThread(repository.getRoot(), "qfinish", singletonList("qbase:" + revision));
      if (HgErrorUtil.hasErrorsInCommandExecution(result)) {
        new HgCommandResultNotifier(project).notifyError(QFINISH_ERROR,
                                                         result,
                                                         HgBundle.message("action.hg4idea.QFinish.error"),
                                                         HgBundle.message("action.hg4idea.QFinish.error.msg"));
      }
      repository.update();
    });
  }
}
