package com.olalekan.fashionstore;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.CHOOSE_IMAGE_REQUEST_CODE;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.MB;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.MB_THRESHOLD;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.STORAGE_PERMISSION_REQUEST_CODE;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.closeSoftKeyboard;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.hideProgressBarAndEnableTouch;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.logoutIfUserIsNull;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showErrorDialog;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showProgressBarAndDisableTouch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.olalekan.fashionstore.Models.Categories;
import com.olalekan.fashionstore.Models.Product;
import com.olalekan.fashionstore.Utility.AllRoundUseful;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AddEditProductsActivity extends AppCompatActivity {

    private ImageView mProductImage;
    private ImageButton mEditProductImage;
    private EditText mProductName;
    private EditText mPrice;
    private EditText mSize;
    private EditText mDescription;
    private EditText mFeatures;
    private Button mAddProduct;

    private String[] mPermissions;
    private Uri mSelectedImageUri;
    private Bitmap mBitmap;

    private byte[] mBytes = null;
    private Product mProduct;
    private Categories mCategories;
    private double mProgress;
    private String mImageUri;
    private ArrayList<String> mStringListOfCategories;
    private Spinner mCategoriesSpinner;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_products);

        logoutIfUserIsNull(this);


        initializeViews();
        onClickListeners();

    }

    @Override
    protected void onResume() {
        super.onResume();
        getCategoriesFromDatabase();
    }

    private void onClickListeners(){
        mProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkStoragePermission()){
                    Intent getImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getImageIntent.setType("image/*");
                    startActivityForResult(getImageIntent, CHOOSE_IMAGE_REQUEST_CODE);
                } else requestStoragePermission();
            }
        });

        mEditProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkStoragePermission()){
                    Intent getImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getImageIntent.setType("image/*");
                    startActivityForResult(getImageIntent, CHOOSE_IMAGE_REQUEST_CODE);
                } else requestStoragePermission();
            }
        });

        mAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeSoftKeyboard(AddEditProductsActivity.this);

                if(AllFieldsFilled()){
                    showProgressBarAndDisableTouch(AddEditProductsActivity.this, mProgressBar);
                    inputProductValues();
                    AddProductToDatabase();
                } else{
                    Toast.makeText(AddEditProductsActivity.this, "Fill all fields", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private boolean AllFieldsFilled() {
        return mBytes !=null &&
            !mProductName.equals("") &&
            !mPrice.equals("") &&
            !mDescription.equals("") &&
            !mSize.equals("")&&
            !mFeatures.equals("");

    }

    private void inputProductValues() {
        mProduct = new Product();
        mProduct.setProductName(mProductName.getText().toString());
        mProduct.setCategory(mCategoriesSpinner.getSelectedItem().toString());
        mProduct.setPrice(mPrice.getText().toString());
        mProduct.setDescription(mDescription.getText().toString());
        mProduct.setSize(mSize.getText().toString());
        mProduct.setFeatures(mFeatures.getText().toString());
    }


    private void AddProductToDatabase() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_node_categories)).
                child(mCategoriesSpinner.getSelectedItem().toString()).
                child(getString(R.string.field_node_products));
        String productId = reference.push().getKey();
        mProduct.setProductId(productId);

        reference.child(productId).setValue(mProduct)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        executeImageUploadTask();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                       hideProgressBarAndEnableTouch(AddEditProductsActivity.this, mProgressBar);
                       showErrorDialog(AddEditProductsActivity.this, e.getMessage());
                    }
                });
    }


    private void getCategoriesFromDatabase() {
        mStringListOfCategories = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.firebase_node_categories));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot categorySnapshot : snapshot.getChildren()){
                    mStringListOfCategories.add(categorySnapshot.getKey());
                    setUpSpinner();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setUpSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddEditProductsActivity.this, android.R.layout.simple_spinner_item, mStringListOfCategories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategoriesSpinner.setAdapter(adapter);
    }

    private void executeImageUploadTask() {
        Toast.makeText(AddEditProductsActivity.this, "uploading image", Toast.LENGTH_LONG).show();
        resizeImage(mSelectedImageUri);

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child( getString(R.string.node_storage_image_category) + mProduct.getCategory() +
                        "/" + getString(R.string.field_node_products) + "/" + mProduct.getProductId() +
                        getString(R.string.field_storage_product_image));
        //images/category/categoryName/products/productId/product_image_jpg

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
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                                    reference.child(getString(R.string.firebase_node_categories)).
                                            child(mCategoriesSpinner.getSelectedItem().toString()).
                                            child(getString(R.string.field_node_products)).
                                            child(mProduct.getProductId()).
                                            child(getString(R.string.field_image_link))
                                    .setValue(mImageUri);

                            Toast.makeText(AddEditProductsActivity.this, "Image Upload successful", Toast.LENGTH_SHORT).show();
                            hideProgressBarAndEnableTouch(AddEditProductsActivity.this, mProgressBar);
                            clearAllSetValues();

                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddEditProductsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double currentProgress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    if (currentProgress > (mProgress + 20)) {
                        mProgress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Toast.makeText(AddEditProductsActivity.this, mProgress + "% of the image uploaded", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private void clearAllSetValues(){
        mBytes = null;
        mProductImage.setImageDrawable(getDrawable(R.drawable.ic_baseline_checkroom_24));
        mProductName.setText("");
        mCategoriesSpinner.setSelection(0);
        mPrice.setText("");
        mDescription.setText("");
        mSize.setText("");
        mFeatures.setText("");
    }

    private void resizeImage(Uri imageUri) {
        BackgroundImageResize resize = new BackgroundImageResize();
        resize.execute(imageUri);
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

        if(ContextCompat.checkSelfPermission(AddEditProductsActivity.this,
                mPermissions[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(AddEditProductsActivity.this,
                        mPermissions[1])== PackageManager.PERMISSION_GRANTED){
            return true;
        } else
            return false;
    }

    private void requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(AddEditProductsActivity.this,mPermissions[0])
                || ActivityCompat.shouldShowRequestPermissionRationale(AddEditProductsActivity.this, mPermissions[1])){
            new AlertDialog.Builder(AddEditProductsActivity.this)
                    .setMessage("The storage permission is required to get images from your pone into the application")
                    .setPositiveButton("Request Again", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which){
                            ActivityCompat.requestPermissions(AddEditProductsActivity.this, mPermissions,STORAGE_PERMISSION_REQUEST_CODE);
                            dialogInterface.dismiss();
                        }
                    }).setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).create();
        } else {
            ActivityCompat.requestPermissions(AddEditProductsActivity.this, mPermissions,STORAGE_PERMISSION_REQUEST_CODE);
        }
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
                        mBitmap = MediaStore.Images.Media.getBitmap(AddEditProductsActivity.this.getContentResolver(), mSelectedImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    ImageDecoder.Source source = ImageDecoder.createSource(AddEditProductsActivity.this.getContentResolver(), mSelectedImageUri);
                    try {
                        mBitmap = ImageDecoder.decodeBitmap(source);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mProductImage.setImageBitmap(mBitmap);


                mBytes = getBytesFromBitmap(mBitmap, 100);
            }
        }
    }

    private void initializeViews(){
        mProductImage = findViewById(R.id.imageViewAddEditProducts);
        mEditProductImage = findViewById(R.id.imageButtonAddEditProducts);

        mProductName = findViewById(R.id.editTextAddEditProductsProductName);
        mPrice = findViewById(R.id.editTextAddEditProductsPrice);
        mSize = findViewById(R.id.editTextAddEditProductsSize);
        mDescription = findViewById(R.id.editTextAddEditProductsDescription);
        mFeatures = findViewById(R.id.editTextAddEditProductsFeatures);
        mAddProduct = findViewById(R.id.buttonAddEditProducts);

        mCategoriesSpinner = findViewById(R.id.spinnerAddEditProductCategories);
        mProgressBar = findViewById(R.id.progressBarAddEditProducts);
    }

    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {


        @Override
        protected byte[] doInBackground(Uri... uris) {

            for(int i = 1; i<11; ++i){
                if(i ==10){
                    Toast.makeText(AddEditProductsActivity.this, "The image is too large",
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