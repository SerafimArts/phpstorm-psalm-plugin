package com.jetbrains.php.tools.quality.psalm;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager;
import com.jetbrains.php.tools.quality.QualityToolAnnotator;
import com.jetbrains.php.tools.quality.QualityToolValidationGlobalInspection;
import com.jetbrains.php.tools.quality.QualityToolXmlMessageProcessor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.codeInspection.ex.EditInspectionToolsSettingsAction.editToolSettings;
import static com.intellij.notification.NotificationType.WARNING;
import static com.intellij.openapi.util.text.StringUtilRt.isEmpty;
import static com.jetbrains.php.tools.quality.QualityToolAnnotator.GROUP_ID;

public class PsalmGlobalInspection extends QualityToolValidationGlobalInspection {
  public String config = "";
  public boolean showInfo = false;
  public boolean findUnusedCode = false;
  public boolean findUnusedSuppress = false;
  public static final Key<List<QualityToolXmlMessageProcessor.ProblemDescription>> PSALM_ANNOTATOR_INFO = Key.create("ANNOTATOR_INFO_PSALM");

  @Override
  public JComponent createOptionsPanel() {
    final PsalmOptionsPanel optionsPanel = new PsalmOptionsPanel(this);
    optionsPanel.init();
    return optionsPanel.getOptionsPanel();
  }

  @Override
  protected void checkCmdOptions(@NotNull Project project) {
    if (!getCommandLineOptions(null).contains("-c") && !Files.exists(Paths.get(project.getBasePath(), "psalm.xml"))) {
      notifyAboutMissingConfig(project);
    }
  }

  public static void notifyAboutMissingConfig(@NotNull Project project) {
    final Notification notification =
      new Notification(GROUP_ID, PsalmQualityToolType.INSTANCE.getDisplayName(),
                       PsalmBundle.message("psalm.config.not.found", project.getBasePath()), WARNING);

    notification.addAction(new AnAction("Show inspection settings") {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        editToolSettings(project, ProjectInspectionProfileManager.getInstance(project).getCurrentProfile(),
                         PsalmQualityToolType.INSTANCE.getInspectionShortName(project));
      }
    });
    Notifications.Bus.notify(notification);
  }

  @Override
  public @Nullable LocalInspectionTool getSharedLocalInspectionTool() {
    return new PsalmValidationInspection();
  }

  @Override
  protected @NotNull QualityToolAnnotator getAnnotator() {
    return PsalmAnnotatorProxy.INSTANCE;
  }

  @Override
  protected Key<List<QualityToolXmlMessageProcessor.ProblemDescription>> getKey() {
    return PSALM_ANNOTATOR_INFO;
  }


  public List<String> getCommandLineOptions(@Nullable String filePath) {
    @NonNls ArrayList<String> options = new ArrayList<>();
    options.add("--output-format=checkstyle");
    if (!isEmpty(config)) {
      options.add("-c");
      options.add(config);
    }
    if (showInfo) {
      options.add("--show-info");
    }
    if (findUnusedCode) {
      options.add("--find-unused-code");
    }
    if (findUnusedSuppress) {
      options.add("--find-unused-psalm-suppress");
    }
    options.add("--monochrome");
    if (filePath != null) {
      options.add(filePath);
    }
    return options;
  }
}
