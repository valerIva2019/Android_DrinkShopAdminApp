package com.ydkim2110.drinkshopadminapp.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.ydkim2110.drinkshopadminapp.Adapter.ViewHolder.DrinkListViewHolder;
import com.ydkim2110.drinkshopadminapp.Interface.IItemClickListener;
import com.ydkim2110.drinkshopadminapp.Model.Drink;
import com.ydkim2110.drinkshopadminapp.R;

import java.util.List;

public class DrinkListAdapter extends RecyclerView.Adapter<DrinkListViewHolder> {

    private static final String TAG = "DrinkListAdapter";

    private Context mContext;
    private List<Drink> mDrinks;

    public DrinkListAdapter(Context mContext, List<Drink> mDrinks) {
        this.mContext = mContext;
        this.mDrinks = mDrinks;
    }

    @NonNull
    @Override
    public DrinkListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called");

        View drinkView = LayoutInflater.from(mContext)
                .inflate(R.layout.drink_item_layout, parent, false);
        return new DrinkListViewHolder(drinkView);
    }

    @Override
    public void onBindViewHolder(@NonNull DrinkListViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called");

        Picasso.with(mContext)
                .load(mDrinks.get(position).Link)
                .into(holder.mImageProduct);

        holder.mDrinkName.setText(new StringBuilder("$").append(mDrinks.get(position).Name).toString());
        holder.mPrice.setText(mDrinks.get(position).Price);

        // event - anti crash null item click
        holder.setiItemClickListener(new IItemClickListener() {
            @Override
            public void onClick(View view, boolean isLongClick) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mDrinks.size();
    }
}
