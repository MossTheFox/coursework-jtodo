package org.mainapp;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mainapp.formats.ToDoCollection;
import org.mainapp.formats.ToDoItem;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

class UserInfo {
    String token;
    String username;
    String avatarUrl;

    UserInfo(String token, String username, String avatarUrl) {
        this.token = token;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }
}

/**
 * API 响应格式
 */
class APIResponse {
    String code = "";
    String message = "unknown";
    JSONObject data;

    APIResponse(String code) {
        this.code = code;
    }

    APIResponse(String code, JSONObject data) {
        this.code = code;
        this.message = "";
        this.data = data;
    }

    APIResponse(String code, String message) {
        this.code = code;
        this.message = message;
        this.data = null;
    }

    APIResponse(String code, String message, JSONObject data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}

/**
 * note:
 * all object constructors are expected to accept these:
 * {
 * code: "ok",
 * data: JSONObject
 * }
 * into: constructor(code, data)
 */

class InitOAuthResponse extends APIResponse {
    String qAuthObjectID;   // 从服务端获取验证状态使用
    String authUrl;         // 重定向链接

    InitOAuthResponse(String code, JSONObject data) {
        super(code, data);
        qAuthObjectID = data.getString("qqAuth");
        authUrl = data.getString("redirectLink");
    }

    // err res
    InitOAuthResponse(String errCode, String message) {
        super(errCode, message);
    }
}

class AuthenticationPassedResponse extends APIResponse {
    String username;
    String avatarUrl;
    String registeredAt;    // Date String like "2022-01-01T00:00:00.000Z"
    String token;           // JWT

    AuthenticationPassedResponse(String code, JSONObject data) {
        super(code, data);
        username = data.getString("username");
        avatarUrl = data.getString("avatarUrl");
        registeredAt = data.getString("registeredAt");
        token = data.getString("token");
    }

    // err res
    AuthenticationPassedResponse(String errCode, String message) {
        super(errCode, message);
    }
}

class GetFullDataResponse extends APIResponse {
    ArrayList<ToDoCollection> collections;
    ArrayList<ToDoItem> items;

    GetFullDataResponse(String code, String message) {
        // usually error
        super(code, message);
    }

    GetFullDataResponse(String code, JSONObject data) {
        // now begins
        super(code, data);
        var collections = data.getJSONArray("collections");
        var items = data.getJSONArray("items");
        this.collections = new ArrayList<>();
        this.items = new ArrayList<>();
        for (int i = 0; i < collections.length(); i++) {
            var collection = collections.getJSONObject(i);
            var uuid = collection.getString("uuid");
            var name = collection.getString("name");
            var description = collection.getString("description");
//            var createdAt = collection.getString("createdAt");
            this.collections.add(new ToDoCollection(uuid, name, description));
        }
        for (int i = 0; i < items.length(); i++) {
            var item = items.getJSONObject(i);
            var uuid = item.getString("uuid");
            var inCollection = item.getString("inCollection");
            var name = item.getString("name");
            var description = item.getString("description");
//            var createdAt = item.getString("createdAt");
            var checked = item.getBoolean("checked");
            this.items.add(new ToDoItem(uuid, inCollection, name, description, checked));
        }
    }
}

/* ===== 同步相关 =====  */

class SyncActionPayload {
    public JSONObject jsonObject;
    String getJSONString() {
        return jsonObject.toString();
    }
}

class SyncCreateCollectionPayload extends SyncActionPayload {
    SyncCreateCollectionPayload(String uuid, String name, String description) {
        jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
        jsonObject.put("name", name);
        jsonObject.put("description", description);
    }

    SyncCreateCollectionPayload(String uuid, String name) {
        jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
        jsonObject.put("name", name);
    }
}

class SyncUpdateCollectionPayload extends SyncActionPayload {
    SyncUpdateCollectionPayload(String uuid, String name, String description) {
        jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
        jsonObject.put("name", name);
        jsonObject.put("description", description);
    }

    SyncUpdateCollectionPayload(String uuid, String name) {
        jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
        jsonObject.put("name", name);
    }
}

class SyncDeleteCollectionPayload extends SyncActionPayload {
    SyncDeleteCollectionPayload(String uuid) {
        jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
    }
}

class SyncCreateItemPayload extends SyncActionPayload {
    SyncCreateItemPayload(String uuid, String inCollection, String name, String description) {
        jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
        jsonObject.put("inCollection", inCollection);
        jsonObject.put("name", name);
        jsonObject.put("description", description);
    }

    SyncCreateItemPayload(String uuid, String inCollection, String name) {
        jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
        jsonObject.put("inCollection", inCollection);
        jsonObject.put("name", name);
    }
}

class SyncUpdateItemPayload extends SyncActionPayload {
    SyncUpdateItemPayload(String uuid, String inCollection, String name, String description, boolean checked) {
        jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
        jsonObject.put("inCollection", inCollection);
        jsonObject.put("name", name);
        jsonObject.put("description", description);
        jsonObject.put("checked", checked);
    }

    SyncUpdateItemPayload(String uuid, String inCollection, String name, boolean checked) {
        jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
        jsonObject.put("inCollection", inCollection);
        jsonObject.put("name", name);
        jsonObject.put("checked", checked);
    }

    SyncUpdateItemPayload(String uuid, boolean checked) {
        jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
        jsonObject.put("checked", checked);
    }
}

class SyncDeleteItemPayload extends SyncActionPayload {
    SyncDeleteItemPayload(String uuid) {
        jsonObject = new JSONObject();
        jsonObject.put("uuid", uuid);
    }
}

class ActionType {
    public static String CREATE_COLLECTION = "createCollection";
    public static String UPDATE_COLLECTION = "updateCollection";
    public static String DELETE_COLLECTION = "deleteCollection";
    public static String CREATE_ITEM = "createItem";
    public static String UPDATE_ITEM = "updateItem";
    public static String DELETE_ITEM = "deleteItem";
}

class SyncAction {
    String actionType;
    SyncActionPayload payload;

    SyncAction(String actionType, SyncActionPayload payload) {
        this.actionType = actionType;
        this.payload = payload;
    }
}

class SyncRequestBody {
    ArrayList<SyncAction> actions;
    JSONArray jsonArray;
    SyncRequestBody(CopyOnWriteArrayList<SyncAction> actions) {
        this.jsonArray = new JSONArray();
        for (var e : actions) {
            var json = new JSONObject();
            /* 下面是前后端交接的格式，别整错了 */
            json.put("type", e.actionType);
            json.put("payload", e.payload.jsonObject);
            this.jsonArray.put(json);
        }

    }

    String getJSONString() {
        var json = new JSONObject();
        json.put("actions", jsonArray);
        return json.toString();
    }
}