package org.mainapp.components;

import org.mainapp.MainController;
import org.mainapp.formats.ToDoItem;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;

public class ItemManager extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel itemNameLabel;
    private JPanel actionContainer;
    private JTextField itemName;
    private JTextField itemDescription;
    private JButton deleteItemButton;
    private JSlider timeSliderMin;
    private JLabel fireAlertTimeIndicatorText;
    private JLabel dueAtText;
    private JSlider timeSliderHour;

    private ToDoItem currentItem;
    private MainController mainController;
    private Timer timer;

    public ItemManager(JFrame parent, ToDoItem item, MainController mainController) {
        super(parent, true);
        this.currentItem = item;
        this.mainController = mainController;

        setContentPane(contentPane);
        setTitle("编辑事件");
        getRootPane().setDefaultButton(buttonOK);
        itemNameLabel.setText("编辑: " + item.name);
        // 初始化
        itemName.setText(item.name);
        itemDescription.setText(item.description);

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

        deleteItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDelete();
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

        var existedTimerTask = mainController.alertController.checkTask(item.uuid);

        timeSliderMin.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                timeSliderChange();
            }
        });
        timeSliderHour.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                timeSliderChange();
            }
        });

        if (item.checked) {
            this.timeSliderHour.setEnabled(false);
            this.timeSliderMin.setEnabled(false);
            this.fireAlertTimeIndicatorText.setText("<html>当前任务已完成，<br>不再需要设置提醒。");
            this.dueAtText.setText("(任务已完成)");
            return;
        }

        // 提示任务相关
        if (existedTimerTask != null) {
            var min = ChronoUnit.MINUTES.between(LocalDateTime.now(), existedTimerTask.alertTime);
            var sec = ChronoUnit.SECONDS.between(LocalDateTime.now(), existedTimerTask.alertTime);
            if (sec > 0) {
                this.timeSliderMin.setValue((int) min % 60);
                this.timeSliderHour.setValue((int) min / 60);

                fireAlertTimeIndicatorText.setText("将在 " + min + " 分钟后发出提醒");
                var formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                dueAtText.setText(formatter.format(existedTimerTask.alertTime));

            }
        } else {
            timer = new Timer();
            timer.schedule(new RefreshLabelText(), 0, 1000);

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    timer.cancel();
                    timer.purge();
                }
            });
        }
    }

    private void timeSliderChange() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new RefreshLabelText(), 0, 1000);

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    timer.cancel();
                    timer.purge();
                }
            });
        }
        var value = timeSliderMin.getValue();
        var hour = timeSliderHour.getValue();
        value += hour * 60;
        if (value > 0) {
            fireAlertTimeIndicatorText.setText("将在 " + value + " 分钟后发出提醒");
            var time = LocalDateTime.now().plusMinutes(value);
            var formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            dueAtText.setText(formatter.format(time));
        } else {
            fireAlertTimeIndicatorText.setText("你可以设置两小时以内的提醒");
            dueAtText.setText("(未开启提醒)");
        }
    }

    class RefreshLabelText extends TimerTask {
        @Override
        public void run() {
            timeSliderChange();
        }
    }

    private void onOK() {
        if (itemName.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "名称不能为空");
            return;
        }
        currentItem.name = itemName.getText();
        currentItem.description = itemDescription.getText();
        mainController.updateItem(currentItem.uuid, currentItem);
        // implement timer task
        var value = timeSliderMin.getValue();
        var hour = timeSliderHour.getValue();
        value += hour * 60;
        mainController.alertController.removeOne(currentItem.uuid);
        if (value != 0) {
            mainController.alertController.addTask(currentItem.uuid, LocalDateTime.now().plusMinutes(value));
        }


        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void onDelete() {
        mainController.deleteItem(currentItem.uuid);
        mainController.alertController.removeOne(currentItem.uuid);
        dispose();
    }
}
