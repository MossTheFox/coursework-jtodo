package org.mainapp.formats;

import java.time.LocalDateTime;

public class AlertTask {
    public String associatedItemUuid;
    public String message = "任务提醒";      // Unused
    public LocalDateTime alertTime;

    public AlertTask(String itemUUID, LocalDateTime alertAt) {
        associatedItemUuid = itemUUID;
        alertTime = alertAt;
    }

    public AlertTask(String itemUUID, int minutesFromNow) {
        associatedItemUuid = itemUUID;
        var date = LocalDateTime.now();
        date.plusMinutes(minutesFromNow);
        alertTime = date;
    }
}
