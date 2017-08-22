package com.codelab.ocrexample.data.model;

import java.util.ArrayList;
import java.util.List;

import static com.codelab.ocrexample.data.model.Field.Address;
import static com.codelab.ocrexample.data.model.Field.Email;
import static com.codelab.ocrexample.data.model.Field.Job;
import static com.codelab.ocrexample.data.model.Field.Name;
import static com.codelab.ocrexample.data.model.Field.Other;
import static com.codelab.ocrexample.data.model.Field.Phone;
import static com.codelab.ocrexample.data.model.Field.URL;

/**
 * Created by Mohamed Habib on 22/08/2017.
 */

public class CardFields {
    List<String> addresses = new ArrayList<>();
    List<String> emails = new ArrayList<>();
    List<String> jobs = new ArrayList<>();
    List<String> names = new ArrayList<>();
    List<String> phones = new ArrayList<>();
    List<String> urls = new ArrayList<>();
    List<String> others = new ArrayList<>();

    public List<String> getAddresses() {
        return addresses;
    }

    public List<String> getEmails() {
        return emails;
    }

    public List<String> getJobs() {
        return jobs;
    }

    public List<String> getNames() {
        return names;
    }

    public List<String> getPhones() {
        return phones;
    }

    public List<String> getUrls() {
        return urls;
    }

    public List<String> getOthers() {
        return others;
    }

    public void createField(int type, String line) {
        switch (type) {
            case Address:
                addresses.add(line);
                break;
            case Email:
                emails.add(line);
                break;
            case Job:
                jobs.add(line);
                break;
            case Name:
                names.add(line);
                break;
            case Phone:
                phones.add(line);
                break;
            case Other:
                others.add(line);
                break;
            case URL:
                urls.add(line);
                break;
        }
    }
}
