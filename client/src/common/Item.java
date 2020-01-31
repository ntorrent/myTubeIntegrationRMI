package common;

import java.io.Serializable;

public class Item implements Serializable {

    private Integer key;
    private String name;
    private String description;
    private Integer owner; //user_id
    private String path;
    //private TubeInterface targetServerRef;
    private Integer server; //server_id

    public Item(Integer key, String name, String description, Integer owner, String path,  Integer server_id) {
    //public Item(Integer key, String name, String description, Integer owner, String path, TubeInterface targetServerRef, Integer server_id) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.path = path;
        //this.targetServerRef = targetServerRef;
        this.server = server;
    }


    // And some getters

    public long getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getOwner() {
        return owner;
    }

    public String getPath() {
        return path;
    }

    public Integer getServer_id() {
        return server;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }
}
