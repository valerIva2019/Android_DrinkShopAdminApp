package com.ydkim2110.drinkshopadminapp.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.ydkim2110.drinkshopadminapp.Model.Category;
import com.ydkim2110.drinkshopadminapp.R;

import java.util.List;

/**
 * Created by Kim Yongdae on 2018-12-07
 * email : ydkim2110@gmail.com
 */
public class MenuAdapter extends RecyclerView.Adapter<MenuViewHolder> {

    private static final String TAG = "MenuAdapter";

    private Context mContext;
    private List<Category> mCategories;

    public MenuAdapter(Context context, List<Category> categories) {
        mContext = context;
        mCategories = categories;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View menuView = LayoutInflater.from(mContext).inflate(R.layout.menu_item_layout, parent, false);

        return new MenuViewHolder(menuView);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Picasso.with(mContext)
                .load(mCategories.get(position).Link)
                .into(holder.img_product);

        holder.txt_product.setText(mCategories.get(position).Name);
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }
}
