package com.ydkim2110.drinkshopadminapp.Adapter.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ydkim2110.drinkshopadminapp.Interface.IItemClickListener;
import com.ydkim2110.drinkshopadminapp.R;

/**
 * Created by Kim Yongdae on 2018-12-07
 * email : ydkim2110@gmail.com
 */
public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView img_product;
    public TextView txt_product;

    IItemClickListener iItemClickListener;

    public void setIItemClickListener(IItemClickListener iItemClickListener) {
        this.iItemClickListener = iItemClickListener;
    }

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);

        img_product = itemView.findViewById(R.id.img_product);
        txt_product = itemView.findViewById(R.id.txt_menu_name);

        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        iItemClickListener.onClick(view);
    }

}
