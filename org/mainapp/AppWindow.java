package org.mainapp;

import org.mainapp.components.*;
import org.mainapp.formats.ToDoCollection;
import org.mainapp.formats.ToDoItem;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class AppWindow extends JFrame {

    MainController mainControllerRef;

    ArrayList<JRadioButton> collectionRadioButtons;
    ArrayList<JCheckBox> itemCheckBoxes;
    ArrayList<JButton> itemActionButtons;

    String selectedCollectionUUID;
    boolean showFinishedItems = true;
    boolean showUnfinishedTaskCount = true;

    private JLabel userLabel;
    private JPanel rightPanel;
    private JPanel leftPanel;
    private JPanel bottomPanel;
    public JLabel bottomText;  // 外界可以访问这个 label
    private JPanel userLabelContainer;
    private JPanel mainPanel;
    private JPanel collectionOuterContainer;
    private JPanel addItemActionContainer;
    private JScrollPane itemOuterContainer;
    private JTextField newItemInput;
    private JButton addNewItemButton;
    private JPanel addCollectionActionPanel;
    private JButton createCollectionButton;
    private JPanel collectionContainer;
    private JPanel itemContainer;

    AppWindow(MainController mainController) {
        super();
        this.setTitle("JToDo App");
        this.setContentPane(mainPanel);
        mainControllerRef = mainController;
        // 由 mainController 负责调用初始化界面的方法，并打开

        // 菜单栏
        JMenuBar menuBar = new JMenuBar();

        // 用户菜单
        JMenu menu = new JMenu("用户");
        JMenuItem menuItem = new JMenuItem("从云端重新同步");
        menuItem.addActionListener(e -> {
            this.openAndInit();
        });
        menu.add(menuItem);
        menuItem = new JMenuItem("注销登录");
        menuItem.addActionListener(e -> {
            mainControllerRef.logout();
        });
        menu.add(menuItem);
        menuItem = new JMenuItem("退出程序");
        menuItem.addActionListener(e -> {
            System.exit(0);
        });
        menu.add(menuItem);

        menuBar.add(menu);

        // 编辑菜单
        menu = new JMenu("编辑");
        menuItem = new JMenuItem("编辑当前列表");
        menuItem.addActionListener(e -> {
            var dialog = new CollectionManager(this,
                    mainControllerRef.collections.stream().filter((collection) -> collection.uuid == selectedCollectionUUID).findFirst().get(),
                    mainControllerRef);
            dialog.setVisible(true);
        });
        menu.add(menuItem);

        JSeparator separator = new JSeparator();
        separator.setOpaque(false);
        menu.add(separator);

        final JCheckBoxMenuItem checkBoxMenuItem = new JCheckBoxMenuItem("显示已完成项目");
        checkBoxMenuItem.setSelected(showFinishedItems);
        checkBoxMenuItem.addActionListener(e -> {
            showFinishedItems = checkBoxMenuItem.isSelected();
            this.render();
        });
        menu.add(checkBoxMenuItem);

        final JCheckBoxMenuItem checkBoxMenuItem2 = new JCheckBoxMenuItem("显示未完成的任务数");
        checkBoxMenuItem2.setSelected(showUnfinishedTaskCount);
        checkBoxMenuItem2.addActionListener(e -> {
            showUnfinishedTaskCount = checkBoxMenuItem2.isSelected();
            this.render();
        });
        menu.add(checkBoxMenuItem2);

        final JCheckBoxMenuItem checkBoxMenuItem3 = new JCheckBoxMenuItem("启用音效");
        checkBoxMenuItem3.addActionListener(e -> {
            mainControllerRef.audioEnabled = checkBoxMenuItem3.isSelected();
        });
        checkBoxMenuItem3.setSelected(mainControllerRef.audioEnabled);
        menu.add(checkBoxMenuItem3);

        menuBar.add(menu);

        // 关于菜单
        JMenu menuAbout = new JMenu("关于");
        JMenuItem menuItem2 = new JMenuItem("关于 JToDo");
        JMenuItem menuItem3 = new JMenuItem("更新日志");
        menuItem2.addActionListener(e -> {
            AboutDialog dialog = new AboutDialog(this);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });
        menuItem3.addActionListener(e -> {
            UpdateLogDialog dialog = new UpdateLogDialog(this);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });
        menuAbout.add(menuItem2);
        menuAbout.add(menuItem3);
        menuBar.add(menuAbout);

        this.setJMenuBar(menuBar);


        this.createCollectionButton.addActionListener((e) -> {
            var dialog = new CreateCollection(this, s -> {
                this.addCollectionCallback(s);
            });
            dialog.setVisible(true);
        });
        this.addNewItemButton.addActionListener((e) -> {
            this.addNewItem();
        });

        this.newItemInput.addActionListener((e) -> {
            this.addNewItem();
        });
    }

    void openAndInit() {
        this.userLabel.setText(mainControllerRef.user.username);
        // System.out.println(mainControllerRef.user.username + " " + mainControllerRef.user.avatarUrl);
        try {
            var url = new URL(mainControllerRef.user.avatarUrl);
            var image = ImageIO.read(url).getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            userLabel.setIcon(new ImageIcon(image));
        } catch (Exception e) {
            System.out.println("用户头像下载出错");
            this.bottomText.setText("用户头像下载出错，错误信息: " + e.getMessage());
            e.printStackTrace();
        }

        // 获取列表
        var res = APIHandler.getFullData(mainControllerRef.user.token);
        if (!res.code.equals("ok")) {
            var jDialog = new JDialog(this, "错误", true);
            jDialog.setSize(300, 100);
            jDialog.setLocationRelativeTo(null);
            jDialog.setResizable(false);
            jDialog.setLayout(new FlowLayout());
            jDialog.add(new JLabel("获取列表失败，请重新启动应用。错误信息：" + res.message));
            jDialog.setVisible(true);
            return;
        }
        this.mainControllerRef.collections = res.collections;
        this.mainControllerRef.items = res.items;
        this.mainControllerRef.sortItems();
        if (res.collections.size() == 0) {
            // so, create a default collection
            this.addCollectionCallback("默认列表");
        }
        this.selectedCollectionUUID = res.collections.get(0).uuid;

        // this.bottomText.setText("就绪");

        this.render();
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    /**
     * 由 renderCollectionPanel 调用
     */
    private void generateCollectionRadioButtons() {
        this.collectionRadioButtons = new ArrayList<>();
        for (var collection : this.mainControllerRef.collections) {
            JRadioButton radioButton;
            if (showUnfinishedTaskCount) {
                int count = 0;
                for (var item : mainControllerRef.items) {
                    if (!item.checked && item.inCollection.equals(collection.uuid)) {
                        count++;
                    }
                }
                if (count == 0) {
                    radioButton = new JRadioButton(collection.name);
                } else {
                    radioButton = new JRadioButton(collection.name + " (" + count + ")");
                }
            } else {
                radioButton = new JRadioButton(collection.name);
            }
            radioButton.setActionCommand(collection.uuid);
            if (collection.uuid.equals(this.selectedCollectionUUID)) {
                radioButton.setSelected(true);
            } else {

                radioButton.addActionListener((e) -> {
                    this.selectedCollectionUUID = e.getActionCommand();
                    render();
                });
            }
            // style
            radioButton.setOpaque(false);
            this.collectionRadioButtons.add(radioButton);
        }
    }

    private void generateItemCheckBoxes() {
        this.itemCheckBoxes = new ArrayList<>();
        for (var item : this.mainControllerRef.items) {
            if (item.inCollection.equals(this.selectedCollectionUUID)) {
                var checkBox = new JCheckBox(item.name);
                checkBox.setActionCommand(item.uuid);
                checkBox.setSelected(item.checked);
                checkBox.addActionListener((e) -> {
                    var uuid = e.getActionCommand();
                    var itemRef = this.mainControllerRef.items.stream().filter(i -> i.uuid.equals(uuid)).findFirst().get();
                    itemRef.checked = !itemRef.checked;
                    // play sound when checked
                    // filename: "checked.wav" (to be placed in the resource folder)
                    if (itemRef.checked && mainControllerRef.audioEnabled) {
                        try {
                            Clip clip = AudioSystem.getClip();
                            clip.open(AudioSystem.getAudioInputStream(new File("org/resources/checked.wav")));
                            clip.start();
                        } catch (Exception e2) {
                            System.out.println("播放声音出错，错误信息：" + e2.getMessage());
                        }
                    }

                    this.mainControllerRef.updateItem(uuid, itemRef);
                    this.render();
                });
                // style
                checkBox.setOpaque(false);

                if (item.checked && !this.showFinishedItems) {
                    continue;
                }
                this.itemCheckBoxes.add(checkBox);
            }
        }
    }

    private void generateItemActionButtons() {
        this.itemActionButtons = new ArrayList<>();
        for (var item : this.mainControllerRef.items) {
            if (item.inCollection.equals(this.selectedCollectionUUID)) {
                var button = new JButton("操作");
                button.setActionCommand(item.uuid);
                button.addActionListener((e) -> {
                    var uuid = e.getActionCommand();
                    // this.mainControllerRef.deleteItem(uuid);
                    var dialog = new ItemManager(this, item, mainControllerRef);
                    dialog.setVisible(true);
                });

                if (item.checked && !this.showFinishedItems) {
                    continue;
                }
                this.itemActionButtons.add(button);
            }
        }
    }

    void render() {
        renderCollectionPanel();
        renderItemPanel();
        this.mainPanel.repaint();
    }

    private void renderCollectionPanel() {
        generateCollectionRadioButtons();
        this.collectionContainer.removeAll();
        this.collectionContainer.setLayout(new GridLayout(Math.max(this.collectionRadioButtons.size(), 9), 1, 0, 2));
        for (var radioButton : this.collectionRadioButtons) {
            var jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());
            jPanel.add(radioButton, BorderLayout.WEST);
            jPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            jPanel.setBorder(BorderFactory.createLineBorder(new Color(0xa4b4c6)));
            jPanel.setOpaque(false);
            this.collectionContainer.add(jPanel);
        }
        this.collectionContainer.revalidate();
        this.collectionOuterContainer.revalidate();
    }

    // 在 renderItemPanel 中，动态创建的 JPanel
    class ItemJPanel extends JPanel {
        ItemJPanel(JCheckBox checkBox, JButton actionButton, String subtitle) {
            super();
            this.setLayout(new BorderLayout());
            var panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new GridLayout(subtitle.length() == 0 ? 1 : 2, 1));
            panel.add(checkBox);
            if (subtitle.length() != 0) {
                var subtitleFont = new Font("微软雅黑", Font.PLAIN, 12);
                var subtitleLabel = new JLabel("-  " + (subtitle.length() > 24 ? subtitle.substring(0, 22) + "…" : subtitle));
                subtitleLabel.setFont(subtitleFont);
                subtitleLabel.setForeground(new Color(0x999999));
                panel.add(subtitleLabel);
            }

            this.add(panel, BorderLayout.CENTER);

            this.add(actionButton, BorderLayout.EAST);
            this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            this.setBorder(BorderFactory.createLineBorder(new Color(0xabc2cc)));
            this.setBackground(new Color(0xF7F9FF));
            this.revalidate();

        }
    }

    private void renderItemPanel() {
        generateItemCheckBoxes();
        generateItemActionButtons();
        this.itemContainer.removeAll();
        this.itemContainer.setLayout(new GridLayout(Math.max(this.itemCheckBoxes.size(), 6), 1, 0, 2));
        for (var checkBox : this.itemCheckBoxes) {
            var itemJPanel = new ItemJPanel(
                    checkBox,
                    this.itemActionButtons.get(this.itemCheckBoxes.indexOf(checkBox)),
                    this.mainControllerRef.items.stream().filter(i -> i.uuid.equals(checkBox.getActionCommand())).findFirst().get().description
            );
            this.itemContainer.add(itemJPanel);
        }
        this.itemContainer.revalidate();
        this.itemOuterContainer.revalidate();
    }

    void addCollectionCallback(String name) {
        var uuid = UUID.randomUUID().toString();
        var toDoCollection = new ToDoCollection(uuid, name);
        this.mainControllerRef.addCollection(toDoCollection);
        this.selectedCollectionUUID = uuid;
        render();
    }

    private void addNewItem() {
        if (this.newItemInput.getText().isEmpty() || selectedCollectionUUID == null) {
            return;
        }
        var item = new ToDoItem(this.selectedCollectionUUID, this.newItemInput.getText());
        this.mainControllerRef.addItem(item);
        this.newItemInput.setText("");
        render();
    }
}
