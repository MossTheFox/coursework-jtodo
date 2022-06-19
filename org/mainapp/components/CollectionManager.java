package org.mainapp.components;

import org.mainapp.MainController;
import org.mainapp.formats.ToDoCollection;

import javax.swing.*;
import java.awt.event.*;

public class CollectionManager extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel collectionNameLabel;
    private JButton deleteButton;
    private JTextField nameInput;
    private JTextArea descriptionInput;

    private ToDoCollection collection;
    private MainController mainController;

    public CollectionManager(JFrame parent, ToDoCollection collection, MainController controller) {
        super(parent, true);
        this.collection = collection;
        this.mainController = controller;

        collectionNameLabel.setText("编辑列表: " + collection.name);
        nameInput.setText(collection.name);
        descriptionInput.setText(collection.description);

        setContentPane(contentPane);
        setTitle("编辑当前列表");
        getRootPane().setDefaultButton(buttonOK);

        pack();
        setLocationRelativeTo(parent);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onDelete();
            }
        });
    }

    private void onOK() {
        if (nameInput.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入列表名称", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        collection.name = nameInput.getText();
        collection.description = descriptionInput.getText();
        mainController.updateCollection(collection.uuid, collection);
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void onDelete() {
        mainController.deleteCollection(collection.uuid);
        dispose();
    }
}
