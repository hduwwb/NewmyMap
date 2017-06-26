package com.exemple.newmymap;

import org.litepal.crud.DataSupport;

/**
 * Created by Jasper on 2017/3/22.
 */

public class Contacts extends DataSupport {
    private int id;
    private String name;
    private String number;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
