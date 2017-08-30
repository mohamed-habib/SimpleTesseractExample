package com.codelab.ocrexample.data.model;


import com.codelab.ocrexample.data.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;
import java.util.UUID;

/**
 * Created by ahmed on 28/08/2017.
 */

@Table(database = AppDatabase.class)
public class FieldDB extends BaseModel {
    @Column
    String type;
    @PrimaryKey
    UUID ID;
    @Column
    UUID cardID;


    List<Item> items;


    public FieldDB(String type, UUID cardID) {
        this.type = type;
        this.cardID = cardID;
        ID = UUID.randomUUID();
    }

    public FieldDB() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getID() {
        return ID;
    }

    public void setID(UUID ID) {
        this.ID = ID;
    }

    public UUID getCardID() {
        return cardID;
    }

    public void setCardID(UUID cardID) {
        this.cardID = cardID;
    }

    @OneToMany(methods = OneToMany.Method.LOAD, variableName = "items")
    public List<Item> getItems() {
        if (items == null) {
            items = SQLite.select()
                    .from(Item.class)
                    .where(Item_Table.FieldID.eq(this.ID))
                    .queryList();
        }
        return items;
    }

    public void setItems(List<Item> items) {

    }

}

//
//public class FieldDB {
//    public
//    @FieldDB.DataType
//    int getTypeIndex() {
//        return type;
//    }
//
//    public String getLine() {
//        return line;
//    }
//
//    // User Data-field types
//    @Retention(RetentionPolicy.SOURCE)
//    @IntDef({Address, Email, Job, Name, Other, Phone, URL})
//    public @interface DataType {
//    }
//
//    public static final int Address = 0;
//    public static final int Email = 1;
//    public static final int Job = 2;
//    public static final int Name = 3;
//    public static final int Other = 4;
//    public static final int Phone = 5;
//    public static final int URL = 6;
//
//    public void setType(int type) {
//        this.type = type;
//    }
//
//    public void setLine(String line) {
//        this.line = line;
//    }
//
//    private
//    @FieldDB.DataType
//    int type;
//    private String line;
//
//    private FieldDB() {
//    }
//
//    public FieldDB(int type, String line) {
//        this.type = type;
//        this.line = line;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        FieldDB field = (FieldDB) o;
//
//        return type == field.type && (line != null ? line.equals(field.line) : field.line == null);
//
//    }
//
//    @Override
//    public int hashCode() {
//        int result = type;
//        result = 31 * result + (line != null ? line.hashCode() : 0);
//        return result;
//    }

