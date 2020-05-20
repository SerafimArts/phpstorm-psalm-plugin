package com.jetbrains.php.tools.quality.psalm;

import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBCheckBox;
import com.jetbrains.php.config.interpreters.PhpTextFieldWithSdkBasedBrowse;
import com.jetbrains.php.tools.quality.QualityToolsOptionsPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

public class PsalmOptionsPanel extends QualityToolsOptionsPanel {
  private JPanel myOptionsPanel;
  private PhpTextFieldWithSdkBasedBrowse myConfigPathTextField;
  private JBCheckBox myShowInfoJBCheckBox;
  private JBCheckBox myFindUnusedCheckbox;
  private JBCheckBox myFindUnusedSuppressCheckbox;
  private final PsalmValidationInspection myInspection;

  public PsalmOptionsPanel(PsalmValidationInspection inspection) {
    myInspection = inspection;

    final Project project = getCurrentProject();
    myConfigPathTextField.setText(inspection.config);
    myConfigPathTextField
      .init(project, getSdkAdditionalData(project), PsalmBundle.message("psalm.configuration.file"), true, false);
    myConfigPathTextField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        myInspection.config = myConfigPathTextField.getText();
      }
    });

    myShowInfoJBCheckBox.setSelected(inspection.showInfo);
    myShowInfoJBCheckBox.addActionListener(event -> myInspection.showInfo = myShowInfoJBCheckBox.isSelected());
    
    myFindUnusedCheckbox.setSelected(inspection.findUnusedCode);
    myFindUnusedCheckbox.addActionListener(event -> myInspection.findUnusedCode = myFindUnusedCheckbox.isSelected());
    
    myFindUnusedSuppressCheckbox.setSelected(inspection.findUnusedSuppress);
    myFindUnusedSuppressCheckbox.addActionListener(event -> myInspection.findUnusedSuppress = myFindUnusedSuppressCheckbox.isSelected());
  }
  
  private void createUIComponents(){
  }

  @Override
  public JPanel getOptionsPanel() {
    return myOptionsPanel;
  }

  public void init() {
  }
}