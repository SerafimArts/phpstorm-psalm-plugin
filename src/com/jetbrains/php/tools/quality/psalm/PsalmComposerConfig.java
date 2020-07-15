package com.jetbrains.php.tools.quality.psalm;

import com.google.gson.JsonElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.composer.ComposerDataService;
import com.jetbrains.php.composer.actions.log.ComposerLogMessageBuilder;
import com.jetbrains.php.tools.quality.QualityToolConfigurationManager;
import com.jetbrains.php.tools.quality.QualityToolsComposerConfig;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.openapi.util.text.StringUtil.*;
import static com.jetbrains.php.tools.quality.psalm.PsalmOpenSettingsProvider.PSALM_OPEN_SETTINGS_PROVIDER;

public class PsalmComposerConfig extends QualityToolsComposerConfig<PsalmConfiguration, PsalmValidationInspection> {
  @NonNls private static final String PACKAGE = "vimeo/psalm";
  @NonNls private static final String RELATIVE_PATH = "bin/psalm" + (SystemInfo.isWindows ? ".bat" : "");
  @NonNls private static final String PSALM_XML = "psalm.xml";
  private static final PsalmValidationInspection PSALM_VALIDATION_INSPECTION = new PsalmValidationInspection();


  public PsalmComposerConfig() {
    super(PACKAGE, RELATIVE_PATH);
  }

  @Override
  protected ComposerLogMessageBuilder.Settings getQualityToolsInspectionSettings() {
    return null;
  }

  @Override
  public PsalmValidationInspection getQualityInspection() {
    return PSALM_VALIDATION_INSPECTION;
  }

  @Override
  protected boolean applyRulesetFromComposer(@NotNull Project project, PsalmConfiguration configuration) {
    final String configPath = ComposerDataService.getInstance(project).getConfigPath();
    final VirtualFile config = LocalFileSystem.getInstance().refreshAndFindFileByPath(configPath);
    if (config == null) return false;

    final String ruleset = getRuleset(config);
    if (ruleset == null) return false;
    final VirtualFile customRulesetFile = detectCustomRulesetFile(config.getParent(), ruleset);
    if (customRulesetFile != null) {
      return modifyRulesetInspectionSetting(project, tool -> applyRuleset(tool, customRulesetFile.getPath()));
    }
    return false;
  }

  @Override
  protected void checkComposerScriptsLeaves(JsonElement element, Ref<String> result) {
    final String string = element.getAsString();
    if (string != null && string.contains("psalm")) {
      final List<String> split = split(string, " ");
      for (String arg: split) {
        final String prefix = "--config=";
        if (startsWith(arg, prefix)) {
          result.set(trimStart(arg, prefix));
          return;
        }
        final int index = split.indexOf(arg);
        if (StringUtil.equals(arg, "-c") && index < split.size() - 1) {
          result.set(split.get(index + 1));
          return;
        }
      }
    }
  }
  
  @Override
  protected boolean applyRulesetFromRoot(@NotNull Project project) {
    VirtualFile customRulesetFile = detectCustomRulesetFile(project.getBaseDir(), PSALM_XML);
    if(customRulesetFile == null){
      customRulesetFile = detectCustomRulesetFile(project.getBaseDir(), PSALM_XML + ".dist");
    }

    if (customRulesetFile != null) {
      final String path = customRulesetFile.getPath();
      return modifyRulesetInspectionSetting(project, tool -> applyRuleset(tool, path));
    }
    return false;
  }

  @Override
  public @Nullable ComposerLogMessageBuilder.Settings getSettings() {
    return PSALM_OPEN_SETTINGS_PROVIDER;
  }

  private static void applyRuleset(PsalmValidationInspection tool, String customRuleset) {
    tool.config = customRuleset;
  }

  @NotNull
  @Override
  protected QualityToolConfigurationManager<PsalmConfiguration> getConfigurationManager(@NotNull Project project) {
    return PsalmConfigurationManager.getInstance(project);
  }
}