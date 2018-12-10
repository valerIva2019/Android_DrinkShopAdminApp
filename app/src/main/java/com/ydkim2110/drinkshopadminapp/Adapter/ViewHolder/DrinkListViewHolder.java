package com.ydkim2110.drinkshopadminapp.Adapter.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ydkim2110.drinkshopadminapp.Interface.IItemClickListener;
import com.ydkim2110.drinkshopadminapp.R;

public class DrinkListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private static final String TAG = "DrinkListViewHolder";

    public ImageView mImageProduct;
    public TextView mDrinkName, mPrice;

    public IItemClickListener iItemClickListener;

    public void setiItemClickListener(IItemClickListener iItemClickListener) {
        this.iItemClickListener = iItemClickListener;
    }

    public DrinkListViewHolder(@NonNull View itemView) {
        super(itemView);

        mImageProduct = itemView.findViewById(R.id.img_product);
        mDrinkName = itemView.findViewById(R.id.txt_drink_name);
        mPrice = itemView.findViewById(R.id.txt_price);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        iItemClickListener.onClick(v, false);
    }
}
