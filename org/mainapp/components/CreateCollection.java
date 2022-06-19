package org.mainapp.components;

import javax.swing.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class CreateCollection extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameInput;

    private Consumer<String> callback;


    public CreateCollection(JFrame parent, Consumer<String> callback) {
        this.callback = callback;

        setContentPane(contentPane);
        setModal(true);
        setTitle("新建列表");
        getRootPane().setDefaultButton(buttonOK);

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

        this.pack();
        this.setLocationRelativeTo(parent);
    }

    private void onOK() {
        // add your code here
        if (nameInput.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入名称");
            return;
        }
        callback.accept(nameInput.getText());
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
