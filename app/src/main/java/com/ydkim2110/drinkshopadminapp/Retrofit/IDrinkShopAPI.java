package com.ydkim2110.drinkshopadminapp.Retrofit;

import com.ydkim2110.drinkshopadminapp.Model.Category;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * Created by Kim Yongdae on 2018-12-07
 * email : ydkim2110@gmail.com
 */
public interface IDrinkShopAPI {

    @GET("getmenu.php")
    Observable<List<Category>> getMenu();

}
