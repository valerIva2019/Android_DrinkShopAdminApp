package com.ydkim2110.drinkshopadminapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.ydkim2110.drinkshopadminapp.Adapter.MenuAdapter;
import com.ydkim2110.drinkshopadminapp.Model.Category;
import com.ydkim2110.drinkshopadminapp.Retrofit.IDrinkShopAPI;
import com.ydkim2110.drinkshopadminapp.Utils.Common;
import com.ydkim2110.drinkshopadminapp.Utils.ProgressRequestBody;
import com.ydkim2110.drinkshopadminapp.Utils.UploadCallBack;

import java.io.File;
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

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, UploadCallBack {

    private static final String TAG = "HomeActivity";
    private static final int REQUEST_PERMISSION_CODE = 1111;
    private static final int PICK_FILE_REQUEST = 2222;

    private RecyclerView mRecyclerViewMenu;
    private IDrinkShopAPI mService;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private EditText edt_name;
    private ImageView img_browser;

    private Uri selected_uri=null;
    private String uploaded_img_path="";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: started");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddCategoryDialog();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // view
        mRecyclerViewMenu = findViewById(R.id.recycler_menu);
        mRecyclerViewMenu.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerViewMenu.setHasFixedSize(true);

        mService = Common.getAPI();

        getMenu();
    }

    private void showAddCategoryDialog() {
        Log.d(TAG, "showAddCategoryDialog: called");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Category");

        View view = LayoutInflater.from(this).inflate(R.layout.add_category_layout, null);

        edt_name = view.findViewById(R.id.edt_name);
        img_browser = view.findViewById(R.id.img_browser);

        // event
        img_browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Intent.createChooser(FileUtils.createGetContentIntent(),
                        "Select a File"), PICK_FILE_REQUEST);
            }
        });

        // set view
        builder.setView(view);
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                uploaded_img_path = "";
                selected_uri = null;
            }
        });
        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (edt_name.getText().toString().isEmpty()) {
                    Toast.makeText(HomeActivity.this, "Please enter name of category",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if(uploaded_img_path.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "Please select image of category",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                mCompositeDisposable.add(mService.addNewCategory(edt_name.getText().toString(), uploaded_img_path)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                Toast.makeText(HomeActivity.this, s, Toast.LENGTH_SHORT).show();

                                getMenu();

                                uploaded_img_path = "";
                                selected_uri = null;
                            }
                        }));
            }
        });
        builder.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data != null) {
                    selected_uri = data.getData();
                    if (selected_uri != null && !selected_uri.getPath().isEmpty()) {
                        img_browser.setImageURI(selected_uri);
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

        if (selected_uri != null) {
            Log.d(TAG, "uploadFileToServer: selected_uri is not null");
            File file = FileUtils.getFile(this, selected_uri);

            String fileName = new StringBuilder(UUID.randomUUID().toString())
                    .append(FileUtils.getExtension(file.toString())).toString();

            ProgressRequestBody requestFile = new ProgressRequestBody(file, this);

            final MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", fileName, requestFile);

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
                                    Toast.makeText(HomeActivity.this, t.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).start();
        }
    }

    private void getMenu() {
        Log.d(TAG, "getMenu: called");

        mCompositeDisposable.add(mService.getMenu()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Category>>() {
                    @Override
                    public void accept(List<Category> categories) throws Exception {
                        displayMenuList(categories);
                    }
                }));


    }

    private void displayMenuList(List<Category> categories) {
        Log.d(TAG, "displayMenuList: called");
        MenuAdapter adapter =new MenuAdapter(this, categories);
        mRecyclerViewMenu.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMenu();
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onProgressUpdate(int percentage) {

    }
}
