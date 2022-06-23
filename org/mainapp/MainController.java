package org.mainapp;

import org.mainapp.formats.ToDoCollection;
import org.mainapp.formats.ToDoItem;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.CopyOnWriteArrayList;


public class MainController {

    /* JFrame */
    LoginWindow loginWindow;
    AppWindow appWindow;

    /* 全局状态 */
    UserInfo user;
    ArrayList<ToDoCollection> collections;
    ArrayList<ToDoItem> items;
    public boolean audioEnabled = true;


    /* 定时任务相关 */
    CopyOnWriteArrayList<SyncAction> syncTaskQueue = new CopyOnWriteArrayList<>();
    SyncController syncController;
    public AlertController alertController;

    void syncControllerMessageCallback(String message) {
        // show at AppWindow bottomMessage
        setCurrentMessage(message);
    }

    private void setCurrentMessage(String message) {
        this.appWindow.bottomText.setText(message);
        this.appWindow.bottomText.repaint();
    }

    void sortItems() {
        // 已完成的项目放在后面
        items.sort((a, b) -> {
            if (a.checked && !b.checked) {
                return 1;
            } else if (!a.checked && b.checked) {
                return -1;
            } else {
                // 先加入的会排在后面
                return items.indexOf(b) - items.indexOf(a);
//                return a.name.compareTo(b.name);
            }
        });
    }

    public void addCollection(ToDoCollection collection) {
        collections.add(collection);
        var action = new SyncAction(ActionType.CREATE_COLLECTION,
                new SyncCreateCollectionPayload(
                        collection.uuid,
                        collection.name
                ));
        syncTaskQueue.add(action);
        // 每次有修改都重绘一下 AppWindow
        appWindow.render();
    }

    public void updateCollection(String uuid, ToDoCollection collection) {
        for (var c : collections) {
            if (c.uuid.equals(uuid)) {
                c.name = collection.name;
                c.description = collection.description;
                break;
            }
        }
        var action = new SyncAction(ActionType.UPDATE_COLLECTION,
                new SyncUpdateCollectionPayload(
                        uuid,
                        collection.name,
                        collection.description
                ));
        syncTaskQueue.add(action);
        // 每次有修改都重绘一下 AppWindow
        appWindow.render();
    }

    public void addItem(ToDoItem item) {
        items.add(item);
        sortItems();
        var action = new SyncAction(ActionType.CREATE_ITEM,
                new SyncCreateItemPayload(
                        item.uuid,
                        item.inCollection,          // UUID 和 inCollection (collection uuid) 别弄反了
                        item.name,
                        item.description
                ));
        syncTaskQueue.add(action);
        // 每次有修改都重绘一下 AppWindow
        appWindow.render();
    }

    public void updateItem(String target, ToDoItem item) {
        for (var i : items) {
            if (i.uuid.equals(target)) {
                i.name = item.name;
                i.description = item.description;
                i.checked = item.checked;
                break;
            }
        }
        sortItems();
        var action = new SyncAction(ActionType.UPDATE_ITEM,
                new SyncUpdateItemPayload(
                        target,
                        item.inCollection,
                        item.name,
                        item.description,
                        item.checked
                ));
        syncTaskQueue.add(action);
        // 每次有修改都重绘一下 AppWindow
        appWindow.render();
    }


    /**
     * 方便直接更新状态，不用拿到 ToDoItem 对象本身
     * @param target item UUID
     * @param checked 更新的目标状态 (勾选与否)
     */
    public void checkItem(String target, boolean checked) {
        ToDoItem item = null;
        for (var i : items) {
            if (i.uuid.equals(target)) {
                i.checked = checked;
                item = i;
                break;
            }
        }
        if (item == null) return;   // 不清楚会不会出现这种情况

        sortItems();
        var action = new SyncAction(ActionType.UPDATE_ITEM,
                new SyncUpdateItemPayload(
                        target,
                        item.inCollection,
                        item.name,
                        item.description,
                        item.checked
                ));
        syncTaskQueue.add(action);
        // 每次有修改都重绘一下 AppWindow
        appWindow.render();
    }

    public void deleteItem(String target) {
        for (var i : items) {
            if (i.uuid.equals(target)) {
                items.remove(i);
                break;
            }
        }
        var action = new SyncAction(ActionType.DELETE_ITEM,
                new SyncDeleteItemPayload(
                        target
                ));
        syncTaskQueue.add(action);
        // 每次有修改都重绘一下 AppWindow
        appWindow.render();
    }

    public void deleteCollection(String target) {
        for (var i : collections) {
            if (i.uuid.equals(target)) {
                collections.remove(i);
                break;
            }
        }
        // remove all items in this collection
        items = items.stream()
                .filter(item -> !item.inCollection.equals(target))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        var action = new SyncAction(ActionType.DELETE_COLLECTION,
                new SyncDeleteCollectionPayload(
                        target
                ));
        syncTaskQueue.add(action);
        // 每次有修改都重绘一下 AppWindow
        appWindow.render();
    }

    public void initUserData() {
        // 在 user 对象被初始化完成后，由 LoginWindow 实例调用
        collections = new ArrayList<ToDoCollection>();
        items = new ArrayList<ToDoItem>();

        appWindow.openAndInit();
        loginWindow.dispose();
        // now create Sync object
        if (GlobalConst.mainAPIServer != MainAPIServer.offline) {
            syncController = new SyncController(this);
            syncController.initAndRun();
        } else {
            setCurrentMessage("Offline mode.");
        }
        alertController = new AlertController(this);
        alertController.init();
    }

    void logout() {
        this.loginWindow.openAndInit();
        this.appWindow.setVisible(false);
        this.syncTaskQueue.clear();
        this.syncController.stop();
        this.items.clear();
        this.collections.clear();
        this.user = null;
    }

    public static void main(String[] args) {
        // 将 UI 设置为 Windows 风格、并设置默认字体
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            Enumeration keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof javax.swing.plaf.FontUIResource) {
                    UIManager.put(key, new javax.swing.plaf.FontUIResource("Microsoft YaHei", 0, 12));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 主控类
        MainController mainController = new MainController();
        mainController.loginWindow = new LoginWindow(mainController);
        mainController.appWindow = new AppWindow(mainController);

        // production ////////////////////////////////////
        mainController.loginWindow.openAndInit();
        //////////////////////////////////////////////////

        // end program when window closed
        var shut = new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        };
        mainController.loginWindow.addWindowListener(shut);
        mainController.appWindow.addWindowListener(shut);

        // to close the frame: whatever.dispose();
    }
}
