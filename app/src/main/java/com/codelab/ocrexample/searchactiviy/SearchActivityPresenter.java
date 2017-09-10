package com.codelab.ocrexample.searchactiviy;

import android.support.annotation.NonNull;

import com.codelab.ocrexample.data.model.Card;
import com.codelab.ocrexample.data.model.Card_Table;
import com.codelab.ocrexample.data.model.FieldDB;
import com.codelab.ocrexample.data.model.Item;
import com.codelab.ocrexample.data.model.Item_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * Created by ahmed on 10/09/2017.
 */

public class SearchActivityPresenter implements SearchActivityContractor.Presenter {

    @Override
    public void getCards(final DataRetrieveListener retrieveListener) {


        SQLite.select().from(Card.class).async().queryListResultCallback(new QueryTransaction.QueryResultListCallback<Card>() {
            @Override
            public void onListQueryResult(QueryTransaction transaction, @NonNull List<Card> tResult) {
                retrieveListener.onSuccess(tResult);
            }
        });
    }

    @Override
    public void deleteCardData(UUID ID) {
        Card card = SQLite.select().from(Card.class).where(Card_Table.id.eq(ID)).querySingle();
        if (card != null) {
            for (final FieldDB fieldDB : card.getFields()) {
                SQLite.delete().from(Item.class).where(Item_Table.FieldID.eq(fieldDB.getID())).async().success(new Transaction.Success() {
                    @Override
                    public void onSuccess(@NonNull Transaction transaction) {
                        fieldDB.delete();
                    }
                }).execute();

            }
            card.delete();
        }
    }

}
