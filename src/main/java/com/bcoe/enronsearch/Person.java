package com.bcoe.enronsearch;

public class Person {
    private String id;
    private String name;
    private String[] groups;

    public Person(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }

    public String[] getGroups() {
        return groups;
    }

}
