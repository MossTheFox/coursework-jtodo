package org.mainapp.components;

import org.mainapp.MainController;
import org.mainapp.formats.ToDoItem;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class TimerAlert extends JDialog {
    private JPanel contentPane;
    private JButton finishedButton;
    private JButton notFinishedButton;
    private JLabel mainText;
    private JLabel imageLabel;
    private JLabel questionLabel;

    JFrame parent;
    ToDoItem linkedItem;
    MainController mainController;

    public TimerAlert(JFrame parent, MainController mainController, ToDoItem item) {
        super(parent, true);
        this.parent = parent;
        this.linkedItem = item;
        this.mainController = mainController;
        this.setTitle("定时任务截止提醒");

        mainText.setText("任务 [" + item.name + "] 已到达提醒时间！");
        // read Image
        if (mainController.audioEnabled) {
            try {
                var img = ImageIO.read(new File("org/resources/alert.png"));
                // var width = img.getHeight() / 200.0 * img.getWidth();
                var resized = img.getScaledInstance(-1, 200, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(resized));
            } catch (Exception e) {
                imageLabel.setText("*图片读取失败*");
            }
        }

        setContentPane(contentPane);
        getRootPane().setDefaultButton(finishedButton);

        this.pack();
        this.setLocationRelativeTo(parent);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                Clip clip = null;
                try {
                    clip = AudioSystem.getClip();
                    clip.open(AudioSystem.getAudioInputStream(new File("org/resources/alert.wav")));
                    clip.start();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        finishedButton.addActionListener(firstOK);
        notFinishedButton.addActionListener(firstBad);

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

    boolean okFlag = false;     // 在任务完成的二次确认完成之前关闭窗口，会按照这个去完成同步

    private ActionListener firstOK = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            finishedButton.removeActionListener(this);
            notFinishedButton.setVisible(false);
//            notFinishedButton.getParent().remove(notFinishedButton);
            try {
                var img = ImageIO.read(new File("org/resources/good.png"));
                // var width = img.getHeight() / 200.0 * img.getWidth();
                var resized = img.getScaledInstance(-1, 200, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(resized));
            } catch (Exception err) {
                imageLabel.setText("*图片读取失败*");
            }
            // SFX
            if (mainController.audioEnabled) {
                try {
                    Clip clip = AudioSystem.getClip();
                    clip.open(AudioSystem.getAudioInputStream(new File("org/resources/checked.wav")));
                    clip.start();
                } catch (Exception e2) {
                    System.out.println("播放声音出错，错误信息：" + e2.getMessage());
                }
            }

            mainText.setText("可喜可贺");
            questionLabel.setText("请再接再厉！");
            finishedButton.setForeground(Color.black);
            finishedButton.setText("完成");
            TimerAlert.this.revalidate();
            TimerAlert.this.pack();
            TimerAlert.this.setLocationRelativeTo(parent);

            okFlag = true;

            finishedButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mainController.checkItem(linkedItem.uuid, true);
                    dispose();
                }
            });
        }
    };

    private ActionListener firstBad = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            finishedButton.setVisible(false);
//            finishedButton.getParent().remove(finishedButton);
            notFinishedButton.removeActionListener(this);

            try {
                var img = ImageIO.read(new File("org/resources/bad.png"));
                // var width = img.getHeight() / 200.0 * img.getWidth();
                var resized = img.getScaledInstance(-1, 200, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(resized));
            } catch (Exception err) {
                imageLabel.setText("*图片读取失败*");
            }
            mainText.setText("你不行啊");
            questionLabel.setText("鉴定为：懒狗");
            notFinishedButton.setText("关闭");
            TimerAlert.this.revalidate();
            TimerAlert.this.pack();
            TimerAlert.this.setLocationRelativeTo(parent);

            notFinishedButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onCancel();
                }
            });
        }
    };

    private void onCancel() {
        if (okFlag) {
            mainController.checkItem(linkedItem.uuid, true);
        }
        dispose();
    }
}
