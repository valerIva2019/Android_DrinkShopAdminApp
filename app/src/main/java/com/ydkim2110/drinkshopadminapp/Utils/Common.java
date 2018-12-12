package com.ydkim2110.drinkshopadminapp.Utils;

import com.ydkim2110.drinkshopadminapp.Model.Category;
import com.ydkim2110.drinkshopadminapp.Model.Drink;
import com.ydkim2110.drinkshopadminapp.Retrofit.IDrinkShopAPI;
import com.ydkim2110.drinkshopadminapp.Retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim Yongdae on 2018-12-07
 * email : ydkim2110@gmail.com
 */
public class Common {

    public static Category currentCategory;
    public static Drink currentDrink;
    public static List<Category> menuList = new ArrayList<>();

    public static final String BASE_URL = "http://192.168.0.13/drinkshop/";
    public static IDrinkShopAPI getAPI() {
        return RetrofitClient.getClient(BASE_URL).create(IDrinkShopAPI.class);
    }

}
