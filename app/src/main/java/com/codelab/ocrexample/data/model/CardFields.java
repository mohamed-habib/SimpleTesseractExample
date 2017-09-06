package com.codelab.ocrexample.data.model;

import android.content.Context;

import com.codelab.ocrexample.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Mohamed Habib on 22/08/2017.
 */

public class CardFields {
    Context context;
    private HashMap<String, List<String>> fields = new HashMap<>();

    public CardFields(Context context) {
        this.context = context;
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
//    public void createField(String type, String line) {
//        switch (type) {
//            case "Address":
//                addresses.add(line);
//                break;
//            case "Email":
//                emails.add(line);
//                break;
//            case "Job":
//                jobs.add(line);
//                break;
//            case "Name":
//                names.add(line);
//                break;
//            case "Phone":
//                phones.add(line);
//                break;
//            case "Other":
//                others.add(line);
//                break;
//            case "URL":
//                urls.add(line);
//                break;
//            default:
//                otherss.add(Pair.create(type, line));
//                break;
//
//
//        }
//    }

    public int getTypeIndex(String type) {
        String types[] = context.getResources().getStringArray(R.array.data_types);
        for (int i = 0; i < types.length; i++) {
            if (types[i].equalsIgnoreCase(type))
                return i;
        }
        return -1;
    }
}
