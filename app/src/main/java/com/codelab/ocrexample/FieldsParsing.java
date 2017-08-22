package com.codelab.ocrexample;

import android.support.annotation.NonNull;
import android.util.Patterns;

import com.codelab.ocrexample.data.model.Field;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.codelab.ocrexample.data.model.Field.Email;
import static com.codelab.ocrexample.data.model.Field.Other;
import static com.codelab.ocrexample.data.model.Field.Phone;
import static com.codelab.ocrexample.data.model.Field.URL;

/**
 * Created by Mohamed Habib on 22/08/2017.
 */

public class FieldsParsing {

    public static boolean isValidURL(String URL) {
        String url = URL.trim().replaceAll("\\s+", "");
        String pattern = "(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]\\.[^\\s]{2,})";
        Pattern r = Pattern.compile(pattern);

        return r.matcher(url).matches();
    }

    public static boolean isValidEmail(String text) {
        String email = text.trim().replaceAll("\\s", "");
        String pattern = "^[A-Za-z0-9+_.-]+@(.+)$\n";
        Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static String getAccuString(String line) {
        return line.replaceAll("\\s+", "");
    }

    public static List<String> getPhoneNumbers(String text) {
        List<String> phoneNumbers = new ArrayList<>();

        for (PhoneNumberMatch temp : PhoneNumberUtil.getInstance().findNumbers(text, "EG")) {
            phoneNumbers.add(getAccuString(PhoneNumberUtil.getInstance().format(temp.number(), PhoneNumberUtil.PhoneNumberFormat.NATIONAL)));
        }

        return phoneNumbers;
    }

    @NonNull
    public static List<Field> parseOCRResult(String ocrResult) {
        List<Field> fieldList = new ArrayList<>();

        List<String> numbers = getPhoneNumbers(ocrResult);

        for (String number : numbers)
            fieldList.add(new Field(Phone, number));

        for (String line : ocrResult.split("[\\r\\n/]+")) {
            if (isValidEmail(line)) {
                fieldList.add(new Field(Email, line));
            } else if (isValidURL(line)) {
                fieldList.add(new Field(URL, line));
            } else if (!numbers.contains(line.trim())) {
                fieldList.add(new Field(Other, line));
            }
        }
        return fieldList;
    }
}
