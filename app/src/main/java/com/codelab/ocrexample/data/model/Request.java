
package com.codelab.ocrexample.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Request implements Serializable
{

    @SerializedName("image")
    @Expose
    private Image image;
    @SerializedName("features")
    @Expose
    private List<Feature> features = null;
    @SerializedName("imageContext")
    @Expose
    private ImageContext imageContext;

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public ImageContext getImageContext() {
        return imageContext;
    }

    public void setImageContext(ImageContext imageContext) {
        this.imageContext = imageContext;
    }

}
