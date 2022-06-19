package org.mainapp.components;

import org.mainapp.GlobalConst;

import javax.swing.*;
import java.awt.event.*;

public class UpdateLogDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel updateLogLabel;

    public UpdateLogDialog(JFrame parent) {
        super(parent, true);
        setContentPane(contentPane);
        // setModal(true);
        setTitle("更新日志");
        updateLogLabel.setText("<html>" + "更新日志<br>"
                + GlobalConst.updateLog.replaceAll("\n", "<br>")
        );
        this.pack();
        this.setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
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
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
