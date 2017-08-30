package com.codelab.ocrexample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codelab.ocrexample.data.model.Card;
import com.codelab.ocrexample.data.model.Card_Table;
import com.codelab.ocrexample.data.model.FieldDB;
import com.codelab.ocrexample.data.model.Item;
import com.codelab.ocrexample.data.model.Item_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mohamed Habib on 15/08/2017.
 */

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardViewHolder> implements Filterable {

    private List<Card> mArrayList;
    private List<Card> mFilteredList;
    private Context mContext;

    public CardsAdapter(Context context, List<Card> arrayList) {
        mArrayList = arrayList;
        mFilteredList = arrayList;
        mContext = context;
    }

    @Override
    public CardsAdapter.CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_row, viewGroup, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardsAdapter.CardViewHolder viewHolder, final int i) {
        final Card selectedCard = mFilteredList.get(i);
        viewHolder.cardIV.setImageBitmap(selectedCard.getImgBitmap());
        viewHolder.deleteIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Card card = SQLite.select().from(Card.class).where(Card_Table.id.eq(selectedCard.getId())).querySingle();
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
//                                SQLite.delete().from(Card.class).where(Card_Table.id.eq(selectedCard.getId())).query();
                                mFilteredList.remove(i);
                                notifyDataSetChanged();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                };
                builder.setMessage("Are you sure you want to delete this card ?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
        viewHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(mContext);
                dialog.setContentView(R.layout.card_details);
                TextView ocrMobileVisionTV = (TextView) dialog.findViewById(R.id.card_details_ocr_mv_data);
                ocrMobileVisionTV.setText(selectedCard.getImgTextMobileVision());
                TextView ocrGoogleCloudTV = (TextView) dialog.findViewById(R.id.card_details_ocr_gc_data);
                ocrGoogleCloudTV.setText(selectedCard.getImgTextGoogleCloud());
                TextView notesTV = (TextView) dialog.findViewById(R.id.card_details_notes);
                notesTV.setText(selectedCard.getNotes());
                ImageView image = (ImageView) dialog.findViewById(R.id.card_details_iv);
                image.setImageBitmap(selectedCard.getImgBitmap());

                LinearLayout fieldsContainerLL = (LinearLayout) dialog.findViewById(R.id.card_details_fields_container);
                for (FieldDB fieldDB : selectedCard.getFields()) {
                    for (Item item : fieldDB.getItems()) {
                        if (item.getData().length() > 0)
                            addRow(fieldsContainerLL, fieldDB.getType(), item.getData());

                    }
                }
//                for (String address : selectedCard.getAddresses()) {
//                    if (address.length() > 0)
//                        addRow(fieldsContainerLL, Address, address);
//                }
//                for (String email : selectedCard.getEmails()) {
//                    if (email.length() > 0)
//                        addRow(fieldsContainerLL, Email, email);
//                }
//
//                for (String job : selectedCard.getJobs()) {
//                    if (job.length() > 0)
//                        addRow(fieldsContainerLL, Job, job);
//                }
//
//                for (String name : selectedCard.getNames()) {
//                    if (name.length() > 0)
//                        addRow(fieldsContainerLL, Name, name);
//                }
//
//                for (String phone : selectedCard.getPhones()) {
//                    if (phone.length() > 0)
//                        addRow(fieldsContainerLL, Phone, phone);
//                }
//
//                for (String url : selectedCard.getUrls()) {
//                    if (url.length() > 0)
//                        addRow(fieldsContainerLL, URL, url);
//                }

                dialog.show();

            }
        });
    }

    private void addRow(LinearLayout fieldsContainerLL, String type, String line) {
        final LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        layout.addView(ViewsUtils.createTypeTV(mContext, type));
        layout.addView(ViewsUtils.createLineTV(mContext, line));

        fieldsContainerLL.addView(layout);
    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();

                if (charString.isEmpty()) {
                    mFilteredList = mArrayList;
                } else {
                    ArrayList<Card> filteredList = new ArrayList<>();

                    for (Card card : mArrayList) {
                        if (card.getImgTextGoogleCloud().toLowerCase().contains(charString) || card.getNotes().toLowerCase().contains(charString)) {
                            filteredList.add(card);
                        }
                    }
                    mFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList = (ArrayList<Card>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        private ImageView cardIV;
        private ImageButton deleteIB;
        private View item;

        CardViewHolder(View view) {
            super(view);

            cardIV = (ImageView) view.findViewById(R.id.card_iv);
            deleteIB = (ImageButton) view.findViewById(R.id.delete_ib);
            item = view;


        }
    }
}
