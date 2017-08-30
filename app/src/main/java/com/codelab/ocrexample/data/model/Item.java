package com.codelab.ocrexample.data.model;

import com.codelab.ocrexample.data.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.UUID;

/**
 * Created by ahmed on 28/08/2017.
 */

@Table(database = AppDatabase.class)
public class Item extends BaseModel {

    @Column
    String data;

    @PrimaryKey()
    UUID ID;

    @Column
    UUID FieldID;

    public Item(String data, UUID fieldID) {
        this.data = data;
        FieldID = fieldID;
        ID = UUID.randomUUID();
    }

    public Item() {
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public UUID getID() {
        return ID;
    }

    public void setID(UUID ID) {
        this.ID = ID;
    }

    public UUID getFieldID() {
        return FieldID;
    }

    public void setFieldID(UUID fieldID) {
        FieldID = fieldID;
    }
}
