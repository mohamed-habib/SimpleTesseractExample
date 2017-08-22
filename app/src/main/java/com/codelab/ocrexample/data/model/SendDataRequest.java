
package com.codelab.ocrexample.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SendDataRequest implements Serializable
{

    @SerializedName("requests")
    @Expose
    private List<Request> requests = null;

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }

}
