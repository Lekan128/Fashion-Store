package com.olalekan.fashionstore;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.ABLE_TO_DELETE_BOOLEAN;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.CHOOSE_IMAGE_REQUEST_CODE;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.MB;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.MB_THRESHOLD;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.STORAGE_PERMISSION_REQUEST_CODE;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.closeSoftKeyboard;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.hideProgressBarAndEnableTouch;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.logoutIfUserIsNull;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showErrorDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.olalekan.fashionstore.Models.Categories;
import com.olalekan.fashionstore.Utility.AllRoundUseful;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddEditCategoryActivity extends AppCompatActivity {

    private ImageView mCategoryImage;
    private ImageButton mEditImage;
    private EditText mCategoryName;
    private Button mButtonAddEditCategory;
    private String[] mPermissions;
    private Uri mSelectedImageUri;
    private Bitmap mBitmap;

    private byte[] mBytes = null;
    private Categories mCategory;
    private double mProgress;
    private String mImageUri;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_category);

       logoutIfUserIsNull(this);

        Toolbar mainToolBar = (Toolbar) findViewById(R.id.includeAddEditCategoryToolbar);

        setSupportActionBar(mainToolBar);

        initialiseViews();

        onClickListeners();

    }

    private void onClickListeners(){
        mCategoryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkStoragePermission()){
                    Intent getImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getImageIntent.setType("image/*");
                    startActivityForResult(getImageIntent, CHOOSE_IMAGE_REQUEST_CODE);
                } else requestStoragePermission();
            }
        });

        mEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkStoragePermission()){
                    Intent getImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getImageIntent.setType("image/*");
                    startActivityForResult(getImageIntent, CHOOSE_IMAGE_REQUEST_CODE);
                } else requestStoragePermission();
            }
        });

        mButtonAddEditCategory.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //Close soft keyboard
                closeSoftKeyboard(AddEditCategoryActivity.this);

                if(mCategoryName.getText().toString().equals("") || mBytes==null){
                    new AlertDialog.Builder(AddEditCategoryActivity.this)
                            .setMessage("Please Input Image and Category Name")
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();

                } else {
                    AllRoundUseful.showProgressBarAndDisableTouch(AddEditCategoryActivity.this, mProgressBar);
                    executeImageUploadTask();
//                    if(mBytes != null) {
//                        executeImageUploadTask();
//                    }
                }

            }
        });
    }

    private void AddCategoryToDatabase() {
        mCategory = new Categories();

        mCategory.setCategoryName(mCategoryName.getText().toString());
        mCategory.setImageLink(mImageUri);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_node_categories)).child(mCategoryName.getText().toString());
        reference.setValue(mCategory).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mProgressBar.setVisibility(View.GONE);
                hideProgressBarAndEnableTouch(AddEditCategoryActivity.this, mProgressBar);
                Toast.makeText(AddEditCategoryActivity.this, "Service added", Toast.LENGTH_LONG).show();
                mBytes=null;
                mCategoryName.setText("");
                mCategoryImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_checkroom_24));
            }
        });
    }

    private void executeImageUploadTask() {
        Toast.makeText(AddEditCategoryActivity.this, "uploading image", Toast.LENGTH_LONG).show();
        resizeImage(mSelectedImageUri);

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child( getString(R.string.node_storage_image_category) + mCategoryName.getText().toString() +
                        getString(R.string.field_storage_category_image));

        if(mBytes.length/MB < MB_THRESHOLD) {


            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .setContentLanguage("en")
                    .build();

            UploadTask task = ref.putBytes(mBytes, metadata);

            task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();
                    downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mImageUri = uri.toString();
                            AddCategoryToDatabase();

                            Toast.makeText(AddEditCategoryActivity.this, "Image Upload successful", Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgressBarAndEnableTouch(AddEditCategoryActivity.this, mProgressBar);
                    showErrorDialog(AddEditCategoryActivity.this, e.getMessage());
//                    Toast.makeText(AddEditCategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double currentProgress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    if (currentProgress > (mProgress + 20)) {
                        mProgress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Toast.makeText(AddEditCategoryActivity.this, mProgress + "% of the image uploaded", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private void resizeImage(Uri imageUri) {
        BackgroundImageResize resize = new BackgroundImageResize();
        resize.execute(imageUri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CHOOSE_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Uri selectedImageUri = data.getData();

            if (!selectedImageUri.equals("")) {
                mSelectedImageUri = selectedImageUri;
//                newImageWasAdded = true;
//                mAddImage.setVisibility(View.INVISIBLE);
                //upload and image compression process starts when the user clicks the addNewStore button
            }
            if (mSelectedImageUri != null && !mSelectedImageUri.equals("")) {
//                uploadNewImage(mSelectedImageUri);
                if (Build.VERSION.SDK_INT < 28) {
                    try {
                        mBitmap = MediaStore.Images.Media.getBitmap(AddEditCategoryActivity.this.getContentResolver(), mSelectedImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    ImageDecoder.Source source = ImageDecoder.createSource(AddEditCategoryActivity.this.getContentResolver(), mSelectedImageUri);
                    try {
                        mBitmap = ImageDecoder.decodeBitmap(source);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mCategoryImage.setImageBitmap(mBitmap);


                mBytes = getBytesFromBitmap(mBitmap, 100);
            }
        }
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality){
        //quality is the quality of image remaining in percentage
        //if quality == 90% there has been a loss of 10% quality
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private boolean checkStoragePermission(){
        mPermissions = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if(ContextCompat.checkSelfPermission(AddEditCategoryActivity.this,
                mPermissions[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(AddEditCategoryActivity.this,
                        mPermissions[1])== PackageManager.PERMISSION_GRANTED){
            return true;
        } else
        return false;
    }

    private void requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(AddEditCategoryActivity.this,mPermissions[0])
            || ActivityCompat.shouldShowRequestPermissionRationale(AddEditCategoryActivity.this, mPermissions[1])){
            new AlertDialog.Builder(AddEditCategoryActivity.this)
                    .setMessage("The storage permission is required to get images from your pone into the application")
                    .setPositiveButton("Request Again", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which){
                            ActivityCompat.requestPermissions(AddEditCategoryActivity.this, mPermissions,STORAGE_PERMISSION_REQUEST_CODE);
                            dialogInterface.dismiss();
                        }
                    }).setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).create();
        } else {
            ActivityCompat.requestPermissions(AddEditCategoryActivity.this, mPermissions,STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    private void initialiseViews(){
        mCategoryImage = findViewById(R.id.imageAddEditCategory);
        mEditImage = findViewById(R.id.imageButtonAddEditCategoryImage);
        mCategoryName = findViewById(R.id.editTextAddEditCategoryName);
        mButtonAddEditCategory = findViewById(R.id.buttonAddEditCategory);
        mProgressBar = findViewById(R.id.progressBarAddEditCategory);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu_add_edit_category, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_add_product:
                startActivity(new Intent(AddEditCategoryActivity.this, AddEditProductsActivity.class));
                break;

            case R.id.menu_delete_category_product:
                Intent intent = new Intent(AddEditCategoryActivity.this, CategoryListActivity.class);
                intent.putExtra(ABLE_TO_DELETE_BOOLEAN, true);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {


        @Override
        protected byte[] doInBackground(Uri... uris) {

            for(int i = 1; i<11; ++i){
                if(i ==10){
                    Toast.makeText(AddEditCategoryActivity.this, "The image is too large",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                mBytes = getBytesFromBitmap(mBitmap, 100/i);
                if(mBytes.length/MB  < MB_THRESHOLD){
                    return mBytes;
                }
            }
            return mBytes;
        }
    }

}