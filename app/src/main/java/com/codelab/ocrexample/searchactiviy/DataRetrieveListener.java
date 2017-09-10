package com.codelab.ocrexample.searchactiviy;

import com.codelab.ocrexample.data.model.Card;

import java.util.List;

/**
 * Created by ahmed on 10/09/2017.
 */

public interface DataRetrieveListener {
    void onSuccess(List<Card> cards);

}
