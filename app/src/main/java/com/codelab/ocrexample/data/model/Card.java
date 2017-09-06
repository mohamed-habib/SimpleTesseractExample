package com.codelab.ocrexample.data.model;

import android.graphics.Bitmap;

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
 * Created by Mohamed Habib on 15/08/2017.
 */
@Table(database = AppDatabase.class)
public class Card extends BaseModel {
    @Column
    String imgPath;
    @Column
    String imgTextMobileVision;
    @Column
    String imgTextGoogleCloud;
    @Column
    String notes;
    @PrimaryKey // at least one primary key required
            UUID id;

    // get fields linked to this card
    List<FieldDB> fieldsDBList;
    Bitmap imgBitmap;

    public Card() {
    }

    public Card(String imgPath, String imgTextMobileVision, String imgTextGoogleCloud, String notes) {
        this.imgPath = imgPath;
        this.imgTextMobileVision = imgTextMobileVision;
        this.imgTextGoogleCloud = imgTextGoogleCloud;
        this.notes = notes;
        this.id = UUID.randomUUID();
    }

    @OneToMany(methods = OneToMany.Method.LOAD, variableName = "fieldsDBList")
    public List<FieldDB> getFields() {
        if (fieldsDBList == null) {
            fieldsDBList = SQLite.select()
                    .from(FieldDB.class)
                    .where(FieldDB_Table.cardID.eq(this.id))
                    .queryList();
        }
        return fieldsDBList;
    }

    public Bitmap getImgBitmap() {
        return imgBitmap;
    }

    public void setImgBitmap(Bitmap imgBitmap) {
        this.imgBitmap = imgBitmap;
    }

    public String getNotes() {
        return notes;
    }

    public String getImgPath() {
        return imgPath;
    }

    public String getImgTextGoogleCloud() {
        return imgTextGoogleCloud;
    }

    public String getImgTextMobileVision() {
        return imgTextMobileVision;
    }

    public UUID getId() {
        return id;
    }
}
