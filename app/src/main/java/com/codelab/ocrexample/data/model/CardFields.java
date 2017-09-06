package com.codelab.ocrexample.data.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Mohamed Habib on 22/08/2017.
 */

public class CardFields {
    private HashMap<String, List<String>> fields = new HashMap<>();

    public CardFields() {
    }

    public void createField(String type, String line) {
        if (fields.containsKey(type)) {
            List<String> items = fields.get(type);
            items.add(line);
            fields.put(type, items);
        } else {
            List<String> items = new ArrayList<>();
            items.add(line);
            fields.put(type, items);

        }
    }

    public Set<String> getKeys() {
        return fields.keySet();
    }

    public List<String> getValues(String type) {
        return fields.get(type);
    }

}
