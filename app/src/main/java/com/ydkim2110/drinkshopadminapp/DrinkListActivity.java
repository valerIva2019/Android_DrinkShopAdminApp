package com.ydkim2110.drinkshopadminapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.ydkim2110.drinkshopadminapp.Adapter.DrinkListAdapter;
import com.ydkim2110.drinkshopadminapp.Model.Drink;
import com.ydkim2110.drinkshopadminapp.Retrofit.IDrinkShopAPI;
import com.ydkim2110.drinkshopadminapp.Utils.Common;
import com.ydkim2110.drinkshopadminapp.Utils.ProgressRequestBody;
import com.ydkim2110.drinkshopadminapp.Utils.UploadCallBack;

import java.io.File;
import java.util.List;
import java.util.UUID;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DrinkListActivity extends AppCompatActivity implements UploadCallBack {

    private static final String TAG = "DrinkListActivity";
    private static final int PICK_FILE_REQUEST = 1111;

    private IDrinkShopAPI mService;
    private RecyclerView mRecyclerViewDrinks;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private FloatingActionButton mFab;
    private ImageView mImageBrowser;
    private EditText mDrinkName, mDrinkPrice;

    private Uri selectedUri;
    private String uploadedImagePath="";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_OK)
        {
            if(requestCode==PICK_FILE_REQUEST)
            {
                if(data!=null)
                {
                    selectedUri=data.getData();
                    if(selectedUri!=null && !selectedUri.getPath().isEmpty())
                    {
                        mImageBrowser.setImageURI(selectedUri);
                        uploadFileToServer();
                    }else {
                        Toast.makeText(this, "Cannot Upload File to Server",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_list);
        Log.d(TAG, "onCreate: started");

        mService = Common.getAPI();

        mFab = findViewById(R.id.btn_add);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDrinkDialog();
            }
        });

        mRecyclerViewDrinks = findViewById(R.id.recycler_drink);
        mRecyclerViewDrinks.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerViewDrinks.setHasFixedSize(true);

        loadListDrink(Common.currentCategory.getID());
    }

    private void showAddDrinkDialog() {
        Log.d(TAG, "showAddDrinkDialog: called");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Product");

        View view = LayoutInflater.from(this).inflate(R.layout.add_new_procut_layout, null);

        mDrinkName = view.findViewById(R.id.edt_drink_name);
        mDrinkPrice = view.findViewById(R.id.edt_drink_price);
        mImageBrowser = view.findViewById(R.id.img_browser);
        
        // event
        mImageBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(Intent.createChooser(FileUtils.createGetContentIntent(), "Select a file"), 
                        PICK_FILE_REQUEST);
            }
        });

        builder.setView(view);
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                uploadedImagePath="";
                selectedUri=null;

            }
        }).setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mDrinkName.getText().toString().isEmpty())
                {
                    Toast.makeText(DrinkListActivity.this, "Please Enter Name Of Product", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mDrinkPrice.getText().toString().isEmpty())
                {
                    Toast.makeText(DrinkListActivity.this, "Please Enter Price Of Product", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(uploadedImagePath.isEmpty())
                {
                    Toast.makeText(DrinkListActivity.this, "Please Select Image Of Product", Toast.LENGTH_SHORT).show();
                    return;
                }
                mCompositeDisposable.add(mService.addNewProduct(mDrinkName.getText().toString(),
                        uploadedImagePath, mDrinkPrice.getText().toString(), Common.currentCategory.ID)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                Toast.makeText(DrinkListActivity.this, s,
                                        Toast.LENGTH_SHORT).show();
                                loadListDrink(Common.currentCategory.getID());

                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Toast.makeText(DrinkListActivity.this, "internal",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }));

            }

        }).show();
    }

    private void uploadFileToServer() {
        Log.d(TAG, "uploadFileToServer: called");

        if(selectedUri!=null)
        {
            File file=FileUtils.getFile(this,selectedUri);

            String fileName=new StringBuilder(UUID.randomUUID().toString())
                    .append(FileUtils.getExtension(file.toString())).toString();

            ProgressRequestBody requestFile=new ProgressRequestBody(file,this);

            final MultipartBody.Part body=MultipartBody.Part
                    .createFormData("uploaded_file",fileName,requestFile);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mService.uploadProductFile(body)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    uploadedImagePath=new StringBuilder(Common.BASE_URL)
                                            .append("admin/product/product_img/")
                                            .append(response.body())
                                            .toString();
                                    Log.d("IMGPath",uploadedImagePath);
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(DrinkListActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).start();
        }
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

    @Override
    public void onProgressUpdate(int percentage) {

    }
}
