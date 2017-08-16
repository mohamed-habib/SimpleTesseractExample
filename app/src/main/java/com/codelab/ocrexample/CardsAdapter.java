package com.codelab.ocrexample;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.codelab.ocrexample.data.model.Card;

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
    public void onBindViewHolder(CardsAdapter.CardViewHolder viewHolder, int i) {
        final Card selectedCard = mFilteredList.get(i);
        viewHolder.cardIV.setImageBitmap(selectedCard.getImgBitmap());
        viewHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(mContext);
                dialog.setContentView(R.layout.card_details);
                TextView text = (TextView) dialog.findViewById(R.id.card_details_data);
                text.setText(selectedCard.getImgText());
                ImageView image = (ImageView) dialog.findViewById(R.id.card_details_iv);
                image.setImageBitmap(selectedCard.getImgBitmap());

                dialog.show();

            }
        });
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
                        if (card.getImgText().toLowerCase().contains(charString) || card.getNotes().toLowerCase().contains(charString)) {
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

    public class CardViewHolder extends RecyclerView.ViewHolder {
        private ImageView cardIV;
        private View item;

        public CardViewHolder(View view) {
            super(view);

            cardIV = (ImageView) view.findViewById(R.id.card_iv);
            item = view;


        }
    }
}
