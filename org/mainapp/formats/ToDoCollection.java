package org.mainapp.formats;

public class ToDoCollection {
    public String uuid;
    public String name;
    public String description = "";

    //    Date createdAt;
    public ToDoCollection(String uuid, String name, String description) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
//        this.createdAt = createdAt;
    }

   public  ToDoCollection(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
//        this.createdAt = createdAt;
    }
}
