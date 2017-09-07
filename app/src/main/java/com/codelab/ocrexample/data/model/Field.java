package com.codelab.ocrexample.data.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Mohamed Habib on 22/08/2017.
 */

public class Field {
    public
    @Field.DataType
    int getType() {
        return type;
    }

    public String getTypeValue() {
        switch (type) {
            case Address:
                return "Address";
            case Email:
                return "Email";
            case Job:
                return "Job";
            case Name:
                return "Name";
            case Other:
                return "Other";
            case Phone:
                return "Phone";
            case URL:
                return "URL";
            default:
                return "Other";
        }
    }

    public String getLine() {
        return line;
    }

    // User Data-field types
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Address, Email, Job, Name, Other, Phone, URL})
    public @interface DataType {
    }

    public static final int Address = 0;
    public static final int Email = 1;
    public static final int Job = 2;
    public static final int Name = 3;
    public static final int Other = 4;
    public static final int Phone = 5;
    public static final int URL = 6;

    public void setType(int type) {
        this.type = type;
    }

    public void setLine(String line) {
        this.line = line;
    }

    private
    @Field.DataType
    int type;
    private String line;

    private Field() {
    }

    public Field(int type, String line) {
        this.type = type;
        this.line = line;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Field field = (Field) o;

        return type == field.type && (line != null ? line.equals(field.line) : field.line == null);

    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + (line != null ? line.hashCode() : 0);
        return result;
    }
}
