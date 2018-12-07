package com.ydkim2110.drinkshopadminapp.Utils;

import com.ydkim2110.drinkshopadminapp.Retrofit.IDrinkShopAPI;
import com.ydkim2110.drinkshopadminapp.Retrofit.RetrofitClient;

/**
 * Created by Kim Yongdae on 2018-12-07
 * email : ydkim2110@gmail.com
 */
public class Common {

    public static final String BASE_URL = "http://192.168.0.13/drinkshop/";
    public static IDrinkShopAPI getAPI() {
        return RetrofitClient.getClient(BASE_URL).create(IDrinkShopAPI.class);
    }

}
