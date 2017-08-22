
package com.codelab.ocrexample.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ImageContext implements Serializable
{

    @SerializedName("languageHints")
    @Expose
    private List<String> languageHints = null;

    public List<String> getLanguageHints() {
        return languageHints;
    }

    public void setLanguageHints(List<String> languageHints) {
        this.languageHints = languageHints;
    }

}
