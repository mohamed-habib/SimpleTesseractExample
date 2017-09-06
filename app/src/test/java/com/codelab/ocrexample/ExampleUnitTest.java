package com.codelab.ocrexample;

import android.util.Patterns;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        String[] urlTestCases = {
                "www.codelab.com",
                "www.codelab.co",
                "ww.codelab.com",
                "www.codelab.com ",
                "www.codelab. com",
                "www.codelab.com R",
        };
        for (String url : urlTestCases)
            System.out.println(isValidURL(url) ? url + " is valid" : url + " is not valid");

    }

    public static boolean isValidURL(String URL) {
//        String url = URL.trim().replaceAll("\\s+", "");
//        String pattern = "(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]\\.[^\\s]{2,})";
//        Pattern r = Pattern.compile(pattern);
        return Patterns.WEB_URL.matcher(URL).matches();
//        return r.matcher(url).matches();
    }


}