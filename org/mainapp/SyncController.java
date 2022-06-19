package org.mainapp;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class SyncController {
    MainController mainControllerRef;

    public SyncController(MainController mainControllerRef) {
        this.mainControllerRef = mainControllerRef;
    }

    Timer timer;

    void initAndRun() {
        if (timer != null) {
            // already running, do nothing
            return;
        }
        timer = new Timer();
        timer.schedule(new SyncTask(), 5000, 5000);
    }

    void stop() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }


    class SyncTask extends TimerTask {
        CompletableFuture<Object> runningFuture;

        SyncTask() {}

        @Override
        public void run() {
            boolean hasNewTask = mainControllerRef.syncTaskQueue.size() > 0;
            System.out.println("[" + LocalDateTime.now().toString() + "] Sync task run, tasks: " + mainControllerRef.syncTaskQueue.size());
            try {
                if (runningFuture != null) {
                    // stop current running task
                    runningFuture.cancel(true);
                    runningFuture = null;
                }
                if (hasNewTask) {
                    mainControllerRef.syncControllerMessageCallback("正在同步...");

                    var syncRequestBody = new SyncRequestBody(mainControllerRef.syncTaskQueue); // here
                    // fire sync
                    final var syncTaskCount = mainControllerRef.syncTaskQueue.size();

                    Consumer<String> onSuccess = (str) -> {
                        var successCount = syncTaskCount;
                        while (successCount > 0 && mainControllerRef.syncTaskQueue.size() > 0) {
                            mainControllerRef.syncTaskQueue.remove(0);
                            successCount--;
                        }
                        mainControllerRef.syncControllerMessageCallback("同步完成，共同步了 " + syncTaskCount + " 个任务");
                        return;
                    };

                    Consumer<String> onError = (str) -> {
                        System.out.println("Sync error: " + str);
                        mainControllerRef.syncControllerMessageCallback("同步出错，错误信息: " + str);
                        return;
                    };

                    runningFuture = APIHandler.fireSyncData(
                            mainControllerRef.user.token,
                            syncRequestBody.getJSONString(),
                            onSuccess,
                            onError);

                }
            } catch (Exception e) {
                e.printStackTrace();
                mainControllerRef.syncControllerMessageCallback("同步遇到问题，将在稍后重试。错误信息: " + e.getMessage());
            }
        }
    }
}
