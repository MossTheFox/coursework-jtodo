package org.mainapp;

import org.mainapp.components.TimerAlert;
import org.mainapp.formats.AlertTask;
import org.mainapp.formats.ToDoItem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

public class AlertController {
    MainController mainControllerRef;

    CopyOnWriteArrayList<AlertTask> tasks;
    Timer timer;
    // 作为定时任务运行：https://stackoverflow.com/questions/11361332/how-to-call-a-method-on-specific-time-in-java
    // 呃，不过这里图省事，采用固定时长间隔来做到类似效果，顺便移除已经无效了的定时任务

    public AlertController(MainController mainController) {
        this.mainControllerRef = mainController;
    }

    /**
     * 清空任务列表，并等待插入新的任务
     */
    void init() {
        tasks = new CopyOnWriteArrayList<>();
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = new Timer();
        timer.schedule(new Task(), 1000, 2000); // 2s
    }

    public void addTask(String belongToItemUUID, LocalDateTime fireAt) {
        if (tasks != null) {
            tasks.add(new AlertTask(belongToItemUUID, fireAt));
        }
    }

    public AlertTask checkTask(String itemUUID) {
        for (var i : tasks) {
            if (i.associatedItemUuid == itemUUID) return i;
        }
        return null;
    }

    public void removeOne(String uuid) {
        try {
            var target = tasks.stream().filter((e) -> e.associatedItemUuid == uuid).findFirst().get();
            tasks.remove(target);

        } catch (NoSuchElementException e) {
            // yeah, pass
        }
    }

    class Task extends TimerTask {
        @Override
        public void run() {
            // 在发起之前，先检查对应 UUID 的 item 是否存在、且是未完成状态
            for (var it = tasks.iterator(); it.hasNext(); ) {
                var task = it.next();
                ToDoItem associated = null;
                try {
                    associated =  mainControllerRef.items.stream()
                            .filter((e) -> e.uuid == task.associatedItemUuid).findFirst().get();
                } catch (NoSuchElementException e) {
                    tasks.remove(task);
                    continue;
                }
                if (associated.checked) {
                    tasks.remove(task);
                    continue;
                }

                if (task.alertTime.isBefore(LocalDateTime.now())) {
                    // fire dialog and remove the task
                    if (mainControllerRef.appWindow.isVisible()) {
                        var dialog = new TimerAlert(mainControllerRef.appWindow, mainControllerRef, associated);
                        dialog.setVisible(true);
                        mainControllerRef.appWindow.toFront();
                        mainControllerRef.appWindow.requestFocus();
                        tasks.remove(task);
                    }
                }
            }
        }
    }
}
