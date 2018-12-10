package com.ydkim2110.drinkshopadminapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.ydkim2110.drinkshopadminapp.Adapter.DrinkListAdapter;
import com.ydkim2110.drinkshopadminapp.Model.Drink;
import com.ydkim2110.drinkshopadminapp.Retrofit.IDrinkShopAPI;
import com.ydkim2110.drinkshopadminapp.Utils.Common;

import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DrinkListActivity extends AppCompatActivity {

    private static final String TAG = "DrinkListActivity";

    private IDrinkShopAPI mService;
    private RecyclerView mRecyclerViewDrinks;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_list);
        Log.d(TAG, "onCreate: started");

        mService = Common.getAPI();

        mRecyclerViewDrinks = findViewById(R.id.recycler_drink);
        mRecyclerViewDrinks.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerViewDrinks.setHasFixedSize(true);

        loadListDrink(Common.currentCategory.getID());
    }

    private void loadListDrink(String id) {
        Log.d(TAG, "loadListDrink: called");

        mCompositeDisposable.add(mService.getDrink(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<Drink>>() {
                    @Override
                    public void accept(List<Drink> drinks) throws Exception {
                        displayDrinkList(drinks);
                    }
                }));
    }

    private void displayDrinkList(List<Drink> drinks) {
        Log.d(TAG, "displayDrinkList: called");

        DrinkListAdapter adapter = new DrinkListAdapter(this, drinks);
        mRecyclerViewDrinks.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        loadListDrink(Common.currentCategory.getID());
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        mCompositeDisposable.clear();
        super.onStop();
    }
}
