package org.liquibase.ide.common.change.wizard.page;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.util.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class AddTableWizardPageImpl implements AddTableWizardPage {
    private JTable columnTable;
    private JTextField tableNameTextField;
    private JTextField columnNameTextField;
    private JTextField dataTypeTextField;
    private JButton addColumnButton;
    private JPanel mainPanel;
    private JCheckBox primaryKeyCheckBox;
    private JCheckBox autoIncrementCheckBox;
    private JCheckBox nullableCheckBox;
    private JTextField defaultValueTextField;


    private static final int COLUMN_NAME_COLUMN = 0;
    private static final int DATA_TYPE_COLUMN = 1;
    private static final int IS_NULLABLE_COLUMN = 2;
    private static final int IS_PRIMARY_KEY_COLUMN = 3;
    private static final int IS_AUTO_INCREMENT_COLUMN = 4;
    private static final int DEFAULT_VALUE_COLUMN = 5;

    public JPanel getMainPanel() {
        DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Column Name", "Data Type", "Nullable", "PK", "Auto-Increment", "Default Value"}, 0);
        columnTable.setModel(tableModel);

        addColumnButton.addActionListener(new AddColumnActionListener());

        return mainPanel;
    }


    public void init() {
        
    }

    public JComponent getComponent() {
        return getMainPanel();
    }

    public String getTableName() {
        return tableNameTextField.getText();
    }

    public ColumnConfig[] getColumns() {
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        TableModel columnTableModel = columnTable.getModel();
        for (int row = 0; row < columnTableModel.getRowCount(); row++) {
            String name = columnTableModel.getValueAt(row, COLUMN_NAME_COLUMN).toString();
            String dataType = columnTableModel.getValueAt(row, DATA_TYPE_COLUMN).toString();
            Boolean isNullable = (Boolean) columnTableModel.getValueAt(row, IS_NULLABLE_COLUMN);
            Boolean isPrimaryKey = (Boolean) columnTableModel.getValueAt(row, IS_PRIMARY_KEY_COLUMN);
            Boolean isAutoIncrement = (Boolean) columnTableModel.getValueAt(row, IS_AUTO_INCREMENT_COLUMN);
            String defaultValue = StringUtils.trimToNull(columnTableModel.getValueAt(row, DEFAULT_VALUE_COLUMN).toString());

            ColumnConfig columnConfig = new ColumnConfig();
            columnConfig.setName(name);
            columnConfig.setType(dataType);
            columnConfig.setDefaultValue(defaultValue);
            columnConfig.setAutoIncrement(isAutoIncrement);

            ConstraintsConfig constraints = new ConstraintsConfig();
            constraints.setNullable(isNullable);
            constraints.setPrimaryKey(isPrimaryKey);

            columnConfig.setConstraints(constraints);

            columns.add(columnConfig);
        }

        return columns.toArray(new ColumnConfig[columns.size()]);

    }

    private class AddColumnActionListener implements ActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            DefaultTableModel tableModel = ((DefaultTableModel) columnTable.getModel());
            if (StringUtils.trimToNull(columnNameTextField.getText()) != null) {
                tableModel.addRow(new Object[]{
                        columnNameTextField.getText(),
                        dataTypeTextField.getText(),
                        nullableCheckBox.isSelected(),
                        primaryKeyCheckBox.isSelected(),
                        autoIncrementCheckBox.isSelected(),
                        defaultValueTextField.getText(),
                });

                columnNameTextField.setText("");
                dataTypeTextField.setText("");
                nullableCheckBox.setSelected(false);
                primaryKeyCheckBox.setSelected(false);
                autoIncrementCheckBox.setSelected(false);
                defaultValueTextField.setText("");
            }

        }
    }
}
