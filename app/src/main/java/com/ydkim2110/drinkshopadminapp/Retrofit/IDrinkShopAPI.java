package com.ydkim2110.drinkshopadminapp.Retrofit;

import com.ydkim2110.drinkshopadminapp.Model.Category;
import com.ydkim2110.drinkshopadminapp.Model.Drink;

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

    /*
    CATEGORY MANAGEMENT
     */
    @GET("getmenu.php")
    Observable<List<Category>> getMenu();

    @FormUrlEncoded
    @POST("admin/category/add_category.php")
    Observable<String> addNewCategory(@Field("name") String name, @Field("imgPath") String imgPath);

    @Multipart
    @POST("admin/category/upload_category_img.php")
    Call<String> uploadCategoryFile(@Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("admin/category/update_category.php")
    Observable<String> updateCategory(@Field("id") String id,
                                      @Field("name") String name,
                                      @Field("imgPath") String imgPath);

    @FormUrlEncoded
    @POST("admin/category/delete_category.php")
    Observable<String> deleteCategory(@Field("id") String id);

    /*
    DRINK MANAGEMENT
     */
    @FormUrlEncoded
    @POST("getdrink.php")
    Observable<List<Drink>> getDrink(@Field("menuid") String menuID);

    @FormUrlEncoded
    @POST("admin/product/add_drink.php")
    Observable<String> addNewProduct(@Field("name") String name,
                                     @Field("imgPath") String imgPath,
                                     @Field("price") String price,
                                     @Field("menuId") String menuId);

    @Multipart
    @POST("admin/product/upload_product_img.php")
    Call<String> uploadProductFile(@Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("admin/product/update_product.php")
    Observable<String> updateProduct(@Field("id") String id,
                                      @Field("name") String name,
                                      @Field("imgPath") String imgPath,
                                      @Field("price") String price,
                                      @Field("menuId") String menuId);

    @FormUrlEncoded
    @POST("admin/product/delete_product.php")
    Observable<String> deleteProduct(@Field("id") String id);

}
