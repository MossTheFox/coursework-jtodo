package org.mainapp;

import org.mainapp.components.AboutDialog;
import org.mainapp.components.UpdateLogDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginWindow extends JFrame {
    private MainController mainControllerRef;
    private JPanel mainPanel;
    private JLabel bottomMessage;
    private JButton actionButton;
    private JButton resetButton;
    private JTextField bottomTextField;

    // 验证相关
    String redirectUrl;
    String qAuthObjectID;


    public LoginWindow(MainController mainControllerRef) {
        super();
        this.mainControllerRef = mainControllerRef;

        // 菜单栏
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("选择服务器");
        var indicator = new JMenuItem("不同服务器的数据不互通");
        indicator.setEnabled(false);
        menu.add(indicator);
        JSeparator separator = new JSeparator();
        separator.setOpaque(false);
        menu.add(separator);
        var server1 = new JRadioButtonMenuItem("MongoDB 后端");
        var server2 = new JRadioButtonMenuItem("MySQL 后端");
        var server3 = new JRadioButtonMenuItem("离线启动");
        server1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlobalConst.switchMainAPIServer(MainAPIServer.mongodb);
                server1.setSelected(true);
                server2.setSelected(false);
                server3.setSelected(false);
                openAndInit();
            }
        });
        server2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlobalConst.switchMainAPIServer(MainAPIServer.mysql);
                server1.setSelected(false);
                server2.setSelected(true);
                server3.setSelected(false);
                openAndInit();
            }
        });
        server3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlobalConst.switchMainAPIServer(MainAPIServer.offline);
                server1.setSelected(false);
                server2.setSelected(false);
                server3.setSelected(true);
                finishAuth();
            }
        });
        server1.setSelected(true); // default

        menu.add(server1);
        menu.add(server2);
        menu.add(server3);

        menuBar.add(menu);

        menu = new JMenu("关于");
        JMenuItem menuItem2 = new JMenuItem("关于 JToDo");
        JMenuItem menuItem3 = new JMenuItem("更新日志");
        menuItem2.addActionListener(e -> {
            AboutDialog dialog = new AboutDialog(this);
            dialog.pack();
            dialog.setVisible(true);
        });
        menuItem3.addActionListener(e -> {
            UpdateLogDialog dialog = new UpdateLogDialog(this);
            dialog.pack();
            dialog.setVisible(true);
        });
        menu.add(menuItem2);
        menu.add(menuItem3);
        menuBar.add(menu);

        this.setJMenuBar(menuBar);

        this.setTitle("JToDo 账户登录");
        // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        this.setLocationRelativeTo(null);

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAndInit();
            }
        });
    }

    void openAndInit() {
        this.bottomMessage.setText("正在获取验证凭据，请稍等一下...");

        this.actionButton.setText("QQ 授权登录");
        this.actionButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        this.actionButton.setEnabled(false);
        this.actionButton.removeActionListener(finishAuthAction);
        this.actionButton.removeActionListener(gotoAuthPageAction);

        this.resetButton.setVisible(false);
        this.bottomTextField.setVisible(false);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        var res = APIHandler.initOAuth();

        if (!res.code.equals("ok")) {
            this.bottomMessage.setText("获取验证凭据失败，请重试。");
            this.resetButton.setVisible(true);
            return;
        } else {
            // 初始化成功
            this.actionButton.setEnabled(true);
            this.bottomMessage.setText("初始化成功，点击授权按钮进行登录授权");
            this.redirectUrl = res.authUrl;
            this.qAuthObjectID = res.qAuthObjectID;

            actionButton.addActionListener(gotoAuthPageAction);
        }
    }

    ActionListener gotoAuthPageAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            goToAuthPage();
        }
    };

    void goToAuthPage() {
        if (redirectUrl == null) bottomMessage.setText("未获取到验证凭据，请重启程序。");

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(java.net.URI.create(redirectUrl));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        bottomMessage.setText("如果浏览器没有成功打开，你可以手动复制下方的链接、并进入完成验证");
        bottomTextField.setText(redirectUrl);
        bottomTextField.setEditable(true);
        bottomTextField.setVisible(true);


        actionButton.setForeground(new Color(84, 129, 66));
        actionButton.setText("已完成授权，点击继续");
        actionButton.setFont(new Font("微软雅黑", Font.BOLD, 14));

        actionButton.removeActionListener(gotoAuthPageAction);
        actionButton.addActionListener(finishAuthAction);

        resetButton.setVisible(true);

        this.pack();
        this.setLocationRelativeTo(null);
    }


    ActionListener finishAuthAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            finishAuth();
        }
    };

    void finishAuth() {
        // ok now begin
        this.bottomMessage.setText("正在验证授权，请稍等...");
        this.actionButton.setEnabled(false);

        if (GlobalConst.mainAPIServer == MainAPIServer.offline) {
            // 离线支持
            mainControllerRef.user = new UserInfo("OFFLINE MODE", "User", GlobalConst.defaultAvatarUrl);
            mainControllerRef.initUserData();
        } else {
            var res = APIHandler.finishAuthentication(qAuthObjectID);

            if (!res.code.equals("ok")) {
                this.bottomMessage.setText("验证遇到问题：" + res.message);
                this.actionButton.setEnabled(true);
                var dialog = new JDialog(this, "验证失败", true);
                dialog.setLayout(new BorderLayout());
                dialog.add(new JLabel("验证未完成，请重试。\n错误信息: " + res.message), BorderLayout.CENTER);
                dialog.setSize(300, 100);
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);
                return;
            }

            mainControllerRef.user = new UserInfo(res.token, res.username, res.avatarUrl);
            // ok
            this.bottomTextField.setVisible(false);
            this.resetButton.setVisible(false);
            this.bottomMessage.setText("授权成功，正在获取用户数据...");

            mainControllerRef.initUserData();
        }

    }

}
