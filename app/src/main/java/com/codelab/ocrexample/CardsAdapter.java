package com.codelab.ocrexample;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

import com.codelab.ocrexample.data.model.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mohamed Habib on 15/08/2017.
 */

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardViewHolder> implements Filterable {

    private List<Card> mArrayList;
    private List<Card> mFilteredList;

    public CardsAdapter(List<Card> arrayList) {
        mArrayList = arrayList;
        mFilteredList = arrayList;
    }

    @Override
    public CardsAdapter.CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_row, viewGroup, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardsAdapter.CardViewHolder viewHolder, int i) {
//        viewHolder.cardIV.setImageBitmap(Utils.getBitmap(mFilteredList.get(i).getImgPath()));
        viewHolder.cardIV.setImageBitmap(mFilteredList.get(i).getImgBitmap());
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

        public CardViewHolder(View view) {
            super(view);

            cardIV = (ImageView) view.findViewById(R.id.card_iv);


        }
    }
}
