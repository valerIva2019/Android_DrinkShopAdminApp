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
import com.squareup.picasso.Picasso;
import com.ydkim2110.drinkshopadminapp.Retrofit.IDrinkShopAPI;
import com.ydkim2110.drinkshopadminapp.Utils.Common;
import com.ydkim2110.drinkshopadminapp.Utils.ProgressRequestBody;
import com.ydkim2110.drinkshopadminapp.Utils.UploadCallBack;

import java.io.File;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateCategoryActivity extends AppCompatActivity implements UploadCallBack {

    private static final String TAG = "UpdateCategoryActivity";
    private static final int PICK_FILE_REQUEST = 1111;

    private ImageView mImageBrowser;
    private EditText mName;
    private Button mUpdateBtn, mDeleteBtn;

    private IDrinkShopAPI mService;
    private CompositeDisposable mCompositeDisposable;

    private Uri selectedUri = null;
    private String uploaded_img_path = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_category);
        Log.d(TAG, "onCreate: started");

        // view
        mDeleteBtn = findViewById(R.id.btn_delete);
        mUpdateBtn = findViewById(R.id.btn_update);
        mName = findViewById(R.id.edt_name);
        mImageBrowser = findViewById(R.id.img_browser);

        // api
        mService = Common.getAPI();

        // Rxjava
        mCompositeDisposable = new CompositeDisposable();

        displayData();

        // event
        mImageBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Intent.createChooser(FileUtils.createGetContentIntent(),
                        "Select a File"), PICK_FILE_REQUEST);
            }
        });

        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCategory();
            }
        });

        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCategory();
            }
        });
    }

    private void deleteCategory() {
        Log.d(TAG, "deleteCategory: called");

        mCompositeDisposable.add(mService.deleteCategory(Common.currentCategory.getID())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Toast.makeText(UpdateCategoryActivity.this, s, Toast.LENGTH_SHORT).show();
                        uploaded_img_path = "";
                        selectedUri = null;

                        Common.currentCategory = null;

                        finish();
                    }
                }));
    }

    private void updateCategory() {
        Log.d(TAG, "updateCategory: called");

        if (!mName.getText().toString().isEmpty()) {
            mCompositeDisposable.add(mService.updateCategory(Common.currentCategory.getID(),
                    mName.getText().toString(), uploaded_img_path)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {
                            Toast.makeText(UpdateCategoryActivity.this, s, Toast.LENGTH_SHORT).show();
                            uploaded_img_path = "";
                            selectedUri = null;

                            Common.currentCategory = null;

                            finish();
                        }
                    }));
        }
    }

    private void displayData() {
        Log.d(TAG, "displayData: called");
        if (Common.currentCategory != null) {
            Picasso.with(this)
                    .load(Common.currentCategory.getLink())
                    .into(mImageBrowser);

            mName.setText(Common.currentCategory.getName());

            uploaded_img_path = Common.currentCategory.getLink();
        }
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
                    }
                    else {
                        Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void uploadFileToServer() {
        Log.d(TAG, "uploadFileToServer: called");

        if (selectedUri != null) {
            Log.d(TAG, "uploadFileToServer: selected_uri is not null");
            File file = FileUtils.getFile(this, selectedUri);

            String fileName = new StringBuilder(UUID.randomUUID().toString())
                    .append(FileUtils.getExtension(file.toString())).toString();

            ProgressRequestBody requestFile = new ProgressRequestBody(file, this);

            final MultipartBody.Part body = MultipartBody.Part.
                    createFormData("uploaded_file", fileName, requestFile);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mService.uploadCategoryFile(body)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    // after uploaded we will get file name and return string contains link of image
                                    uploaded_img_path = new StringBuilder(Common.BASE_URL)
                                            .append("admin/category/category_img/")
                                            .append(response.body().toString())
                                            .toString();

                                    Log.d(TAG, "onResponse: ImgPath: "+response.body());
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Log.d(TAG, "onFailure: runable failure!");
                                    Toast.makeText(UpdateCategoryActivity.this, t.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).start();
        }
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

    @Override
    public void onProgressUpdate(int percentage) {

    }
}
