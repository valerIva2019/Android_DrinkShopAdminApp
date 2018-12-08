package com.ydkim2110.drinkshopadminapp.Retrofit;

import com.ydkim2110.drinkshopadminapp.Model.Category;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by Kim Yongdae on 2018-12-07
 * email : ydkim2110@gmail.com
 */
public interface IDrinkShopAPI {

    @GET("getmenu.php")
    Observable<List<Category>> getMenu();

    @FormUrlEncoded
    @POST("admin/category/add_category.php")
    Observable<String> addNewCategory(@Field("name") String name, @Field("imgPath") String imgPath);

    @Multipart
    @POST("admin/category/upload_category_img.php")
    Call<String> uploadCategoryFile(@Part MultipartBody.Part file);


}
