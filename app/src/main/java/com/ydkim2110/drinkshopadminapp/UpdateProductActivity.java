package com.ydkim2110.drinkshopadminapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;
import com.ydkim2110.drinkshopadminapp.Model.Category;
import com.ydkim2110.drinkshopadminapp.Retrofit.IDrinkShopAPI;
import com.ydkim2110.drinkshopadminapp.Utils.Common;
import com.ydkim2110.drinkshopadminapp.Utils.ProgressRequestBody;
import com.ydkim2110.drinkshopadminapp.Utils.UploadCallBack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProductActivity extends AppCompatActivity implements UploadCallBack {

    private static final String TAG = "UpdateProductActivity";
    private static final int PICK_FILE_REQUEST = 1111;

    private MaterialSpinner mSpinnerMenu;
    private HashMap<String, String> menu_data_for_get_key = new HashMap<>();
    private HashMap<String, String> menu_data_for_get_value = new HashMap<>();
    private List<String> menu_data = new ArrayList<>();

    private ImageView mImageBrowser;
    private EditText mName, mPrice;
    private Button mUpdateBtn, mDeleteBtn;

    private IDrinkShopAPI mService;
    private CompositeDisposable mCompositeDisposable;

    private Uri selectedUri = null;
    private String uploadedImagePath = "", selectedCategory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_product);
        Log.d(TAG, "onCreate: started");

        if (Common.currentDrink != null) {
            uploadedImagePath = Common.currentDrink.Link;
            selectedCategory = Common.currentDrink.MenuId;
        }

        mService = Common.getAPI();
        mCompositeDisposable = new CompositeDisposable();

        selectedCategory = Common.currentDrink.MenuId;

        mDeleteBtn = findViewById(R.id.btn_delete);
        mUpdateBtn = findViewById(R.id.btn_update);
        mName = findViewById(R.id.edt_drink_name);
        mPrice = findViewById(R.id.edt_drink_price);
        mImageBrowser = findViewById(R.id.img_browser);
        mSpinnerMenu = findViewById(R.id.spinner_menu_id);

        // event
        mImageBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(Intent.createChooser(FileUtils.createGetContentIntent(),
                        "Select a File"), PICK_FILE_REQUEST);
            }
        });

        mSpinnerMenu.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                selectedCategory = menu_data_for_get_key.get(menu_data.get(position));
            }
        });

        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProduct();
            }
        });

        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProduct();
            }
        });

        setSpinnerMenu();
        setProductInfo();
    }

    private void setProductInfo() {
        Log.d(TAG, "setProductInfo: called");

        if (Common.currentDrink != null) {
            mName.setText(Common.currentDrink.Name);
            mPrice.setText(Common.currentDrink.Price);
            Picasso.with(this).load(Common.currentDrink.Link).into(mImageBrowser);
            mSpinnerMenu.setSelectedIndex(menu_data.indexOf(
                    menu_data_for_get_value.get(Common.currentCategory.getID())));
        }
    }

    private void deleteProduct() {
        Log.d(TAG, "deleteProduct: called");

        mCompositeDisposable.add(mService.deleteProduct(Common.currentDrink.ID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Toast.makeText(UpdateProductActivity.this, s,
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(UpdateProductActivity.this, throwable.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }));
    }

    private void updateProduct() {
        Log.d(TAG, "updateProduct: called");

        mCompositeDisposable.add(mService.updateProduct(Common.currentDrink.ID,
                mName.getText().toString(),
                uploadedImagePath,
                mPrice.getText().toString(),
                selectedCategory)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Toast.makeText(UpdateProductActivity.this, s,
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(UpdateProductActivity.this, throwable.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data != null) {
                    selectedUri = data.getData();
                    if (selectedUri != null && !selectedUri.getPath().isEmpty()) {
                        mImageBrowser.setImageURI(selectedUri);
                        uploadFileToServer();
                    } else {
                        Toast.makeText(this, "Cannot Upload File to Server",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void uploadFileToServer() {
        Log.d(TAG, "uploadFileToServer: called");

        if (selectedUri != null) {
            File file = FileUtils.getFile(this, selectedUri);

            String fileName = new StringBuilder(UUID.randomUUID().toString())
                    .append(FileUtils.getExtension(file.toString())).toString();

            ProgressRequestBody requestFile = new ProgressRequestBody(file, this);

            final MultipartBody.Part body = MultipartBody.Part
                    .createFormData("uploaded_file", fileName, requestFile);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mService.uploadProductFile(body)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    uploadedImagePath = new StringBuilder(Common.BASE_URL)
                                            .append("admin/product/product_img/")
                                            .append(response.body())
                                            .toString();
                                    Log.d("IMGPath", uploadedImagePath);
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(UpdateProductActivity.this, t.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).start();
        }
    }

    private void setSpinnerMenu() {
        Log.d(TAG, "setSpinnerMenu: called");

        for (Category category : Common.menuList) {
            menu_data_for_get_key.put(category.getName(), category.getID());
            menu_data_for_get_value.put(category.getID(), category.getName());

            menu_data.add(category.getName());
        }

        mSpinnerMenu.setItems(menu_data);
    }

    @Override
    public void onProgressUpdate(int percentage) {

    }
}
