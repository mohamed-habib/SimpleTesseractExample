package com.codelab.ocrexample.data.model;

import android.graphics.Bitmap;

import com.codelab.ocrexample.data.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Arrays;
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
    //// TODO: extract into a separate table and make a one to many rel.
    @Column
    String addresses;
    @Column
    String emails;
    @Column
    String jobs;
    @Column
    String names;
    @Column
    String phones;
    @Column
    String urls;
    @Column
    String others;


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

    public void setAddresses(List<String> addresses) {
        this.addresses = convertToString(addresses);
    }

    public void setEmails(List<String> emails) {
        this.emails = convertToString(emails);
    }

    public void setJobs(List<String> jobs) {
        this.jobs = convertToString(jobs);
    }

    public void setNames(List<String> names) {
        this.names = convertToString(names);
    }

    public void setPhones(List<String> phones) {
        this.phones = convertToString(phones);
    }

    public void setUrls(List<String> urls) {
        this.urls = convertToString(urls);
    }

    public void setOthers(List<String> others) {
        this.others = convertToString(others);
    }


    private List<String> convertToList(String list) {
        return Arrays.asList(list.split("&&&"));
    }

    public List<String> getAddresses() {
        return convertToList(addresses);
    }

    public List<String> getEmails() {
        return convertToList(emails);
    }

    public List<String> getJobs() {
        return convertToList(jobs);
    }

    public List<String> getNames() {
        return convertToList(names);
    }

    public List<String> getPhones() {
        return convertToList(phones);
    }

    public List<String> getUrls() {
        return convertToList(urls);
    }

    public List<String> getOthers() {
        return convertToList(others);
    }

    private String convertToString(List<String> list) {
        String string = "";
        for (String s : list) {
            string = string + "&&&" + s;
        }
        return string;
    }

    public Card(String imgPath, String imgTextMobileVision, String imgTextGoogleCloud, String notes, UUID id, List<String> addresses,
                List<String> emails, List<String> jobs, List<String> names, List<String> phones, List<String> urls, List<String> others) {
        this.imgPath = imgPath;
        this.imgTextMobileVision = imgTextMobileVision;
        this.imgTextGoogleCloud = imgTextGoogleCloud;
        this.notes = notes;
        this.id = id;
        setAddresses(addresses);
        setEmails(emails);
        setJobs(jobs);
        setNames(names);
        setPhones(phones);
        setUrls(urls);
        setOthers(others);
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
