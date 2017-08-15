package com.codelab.ocrexample.data.model;

import android.graphics.Bitmap;

import com.codelab.ocrexample.data.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.UUID;

/**
 * Created by Mohamed Habib on 15/08/2017.
 */
@Table(database = AppDatabase.class)
public class Card extends BaseModel {
    @Column
    String imgPath;
    @Column
    String imgText;
    @Column
    String notes;
    @PrimaryKey // at least one primary key required
            UUID id;

    public Bitmap getImgBitmap() {
        return imgBitmap;
    }

    public void setImgBitmap(Bitmap imgBitmap) {
        this.imgBitmap = imgBitmap;
    }

    Bitmap imgBitmap;

    public Card() {
    }

    public String getNotes() {
        return notes;
    }

    public Card(String imgPath, String imgText, String notes, UUID id) {
        this.imgPath = imgPath;
        this.imgText = imgText;
        this.notes = notes;
        this.id = id;
    }

    public String getImgPath() {
        return imgPath;
    }

    public String getImgText() {
        return imgText;
    }

    public UUID getId() {
        return id;
    }
}
