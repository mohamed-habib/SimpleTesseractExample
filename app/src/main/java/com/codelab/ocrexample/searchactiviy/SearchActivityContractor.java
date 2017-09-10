package com.codelab.ocrexample.searchactiviy;

import java.util.UUID;

/**
 * Created by ahmed on 10/09/2017.
 */

public interface SearchActivityContractor {


    public interface Presenter {
        public void getCards(DataRetrieveListener retrieveListener);

        public void deleteCardData(UUID ID);

    }
}
