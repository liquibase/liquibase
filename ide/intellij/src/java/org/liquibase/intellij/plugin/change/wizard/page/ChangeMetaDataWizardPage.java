package org.liquibase.intellij.plugin.change.wizard.page;

import com.intellij.ide.wizard.Step;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeFileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;

import javax.swing.*;
import java.util.Date;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import liquibase.util.StringUtils;
import org.liquibase.intellij.plugin.LiquibaseProjectComponent;

public class ChangeMetaDataWizardPage extends BaseRefactorWizardPage {
    private JTextArea commentsTextArea;
    private JTextField idTextField;
    private JTextField authorTextField;
    private JCheckBox runOnChangeCheckBox;
    private JCheckBox alwaysRunCheckBox;
    private JTextField contextsTextField;
    private JTextField dbmsTextField;
    private JPanel mainPanel;
    private JButton selectChangeLogButton;

    private Project project;


    public ChangeMetaDataWizardPage(Project project) {
        this.project = project;
    }

    public JComponent getComponent() {
        idTextField.setText(String.valueOf(new Date().getTime()));
        authorTextField.setText(StringUtils.trimToEmpty(System.getProperty("user.name")));

        selectChangeLogButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
//                TreeFileChooser.PsiFileFilter filter = new TreeFileChooser.PsiFileFilter() {
//
//                    public boolean accept(PsiFile psiFile) {
//                        return psiFile.getFileType().getDefaultExtension().equalsIgnoreCase("xml");
//                    }
//                };
//
//                TreeFileChooser chooser = TreeClassChooserFactory.getInstance(project).createFileChooser("Select Change Log File to Write To", null, StdFileTypes.XML, null);
//                PsiFile psiFile = chooser.getSelectedFile();
//                if (psiFile != null) {
//                    System.out.println("file : "+psiFile.toString());
//                }

                FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, true, false);
                FileChooserDialog dialog = FileChooserFactory.getInstance().createFileChooser(descriptor, project);
                VirtualFile[] files = dialog.choose(project.getWorkspaceFile(), project);
                if (files != null && files.length > 0) {
                    LiquibaseProjectComponent.getInstance().setChangeLogFile((XmlFile) PsiManager.getInstance(project).findFile(files[files.length-1]));
                }
            }
        });

        return mainPanel;
    }

    public String getId() {
        return idTextField.getText();
    }

    public String getAuthor() {
        return authorTextField.getText();
    }

    public boolean isAlwaysRun() {
        return alwaysRunCheckBox.isSelected();
    }

    public boolean isRunOnChange() {
        return runOnChangeCheckBox.isSelected();
    }

    public String getContext() {
        return contextsTextField.getText();
    }

    public String getDbms() {
        return dbmsTextField.getText();
    }

    public String getComments() {
        return commentsTextArea.getText();
    }
}
