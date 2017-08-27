package com.codelab.ocrexample;

import android.support.annotation.NonNull;
import android.util.Patterns;

import com.codelab.ocrexample.data.model.Field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codelab.ocrexample.data.model.Field.Email;
import static com.codelab.ocrexample.data.model.Field.Job;
import static com.codelab.ocrexample.data.model.Field.Name;
import static com.codelab.ocrexample.data.model.Field.Other;
import static com.codelab.ocrexample.data.model.Field.Phone;
import static com.codelab.ocrexample.data.model.Field.URL;

/**
 * Created by Mohamed Habib on 22/08/2017.
 */

public class FieldsParsing {

    static List<String> job_titles = Arrays.asList("Manager", "HR ", "Technical", "Co-Founder", "CEO"
            , "Financial", "Administrator", "Admin", "Developer"
            , "Engineer", "Programmer", "Sales", "Analyst", "Architect "
            , "Leader", "President", "Chief", "Officer", "Designer"
            , "Senior", "Junior", "Project", "Supervisor", "Specialist");

    static List<String> unWantedKeywords = Arrays.asList("Mobile:", "Mobile", "Fax:", "Fax",
            "Phone:", "Phone", "E-mail:", "E-mail", "Mail", "Mail:","Email:", "Email",  "mail:",
             "Mob\\.", "Mob:","Mob", "\\.:", "M:", "M\\.", "E:", "E\\.", "Tel:", "Tel\\.", "Tel");

    public static boolean isValidURL(String URL) {
        String url = URL.trim().replaceAll("\\s+", "");
//        String pattern = "(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]\\.[^\\s]{2,})";
//        Pattern r = Pattern.compile(pattern);
        return Patterns.WEB_URL.matcher(url).matches();
    }

    public static boolean isValidEmail(String text) {
        String email = text.trim().replaceAll("\\s", "");
//        String pattern = "^[A-Za-z0-9+_.-]+@(.+)$\n";
//        Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static String getAccuString(String line) {
//        return line.replaceAll("\\s+", "");
        return line;
    }

    public static List<String> getPhoneNumbers(String text) {
        List<String> phoneNumbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("^[0-9 ()+-]{5,20}+$");
        for (String line : text.split("[\\r\\n/]+")) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                phoneNumbers.add(line);
            }
        }

//        for (PhoneNumberMatch temp : PhoneNumberUtil.getInstance().findNumbers(text, null)) {
//            phoneNumbers.add(getAccuString(PhoneNumberUtil.getInstance().format(temp.number(), PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)));
//        }
        return phoneNumbers;
    }

    public static String getPhoneNumber(String text) {
        Pattern pattern = Pattern.compile("\\d{3}-\\d{3}-\\d{4}");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }


//    private static boolean isValidPhoneNumber(String number) {
//        Pattern pattern = Pattern.compile("\\d{3}-\\d{3}-\\d{4}");
//        Matcher matcher = pattern.matcher(number);
//        return matcher.find();
//        return PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber);
//    }

    public static boolean containsAKeyword(String subString, List<String> keywords) {
        for (String keyword : keywords) {
            if (subString.contains(keyword)) {
                return true;
            }
        }
        return false; // Never found match.
    }

    @NonNull
    public static List<Field> parseOCRResult(String ocrResult) {
        List<Field> fieldList = new ArrayList<>();

        //remove unwanted keywords
        for (String keyword : unWantedKeywords)
            ocrResult = ocrResult.replaceAll(keyword, "");

        List<String> numbers = getPhoneNumbers(ocrResult);
        // remove numbers
        for (String number : numbers)
            ocrResult = ocrResult.replace(number, "");

        for (String number : numbers)
            fieldList.add(new Field(Phone, number));

        for (String line : ocrResult.split("[\\r\\n/]+")) {
//            if (getKeyWords(line) != null) {
//
//            }
            if (isValidEmail(line)) {
                fieldList.add(new Field(Email, line));
            } else if (isValidURL(line)) {
                fieldList.add(new Field(URL, line));
            } else if (containsAKeyword(line, job_titles)) {
                fieldList.add(new Field(Job, line));
            } else if (isValidText(line)) {
                fieldList.add(new Field(Other, line));
            }
//            else if(Pattern.compile("[^A-Za-z0-9]").matcher(line).matches()){
//                fieldList.add(new Field(Other, line));
//            }
        }
        return fieldList;
    }

    private static boolean hasKeyWords(String line) {
        return false;
    }

    private static boolean isValidText(String line) {

        return !Pattern.compile("[\\s@&,.?$+-]+").matcher(line).matches();
    }

    @NonNull
    public static List<Field> parseQRCode(String cardData) {
        List<Field> fieldList = new ArrayList<>();
        String data[] = cardData.split("[\\r?\\n]+"); // split result lines
        for (String s : data) {
            if (s.startsWith("N:"))
                fieldList.add(new Field(Name, s.substring(s.indexOf(":") + 1)));
            else if (s.startsWith("TEL:"))
                fieldList.add(new Field(Phone, s.substring(s.indexOf(":") + 1)));
            else if (s.startsWith("ORG:"))
                fieldList.add(new Field(Other, s.substring(s.indexOf(":") + 1)));
            else if (s.startsWith("EMAIL:"))
                fieldList.add(new Field(Email, s.substring(s.indexOf(":") + 1)));
        }
        return fieldList;
    }

}
