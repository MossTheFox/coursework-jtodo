package org.mainapp.formats;

import java.util.UUID;

public class ToDoItem {
    public String uuid;
    public String inCollection;
    public String name;
    public String description = "";
    //    Date createdAt = new Date();
    public boolean checked = false;

    public ToDoItem(String uuid, String inCollection, String name, String description, boolean checked) {
        this.uuid = uuid;
        this.inCollection = inCollection;
        this.name = name;
        this.description = description;
//        this.createdAt = createdAt;
        this.checked = checked;
    }

    public ToDoItem(String uuid, String inCollection, String name, boolean checked) {
        this.uuid = uuid;
        this.inCollection = inCollection;
        this.name = name;
//        this.createdAt = createdAt;
        this.checked = checked;
    }

    public ToDoItem(String inCollection, String name) {
        this.uuid = UUID.randomUUID().toString();
        this.inCollection = inCollection;
        this.name = name;
    }

    public ToDoItem(String inCollection, String name, String description) {
        this.uuid = UUID.randomUUID().toString();
        this.inCollection = inCollection;
        this.name = name;
        this.description = description;
    }
}
