package com.olalekan.fashionstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.olalekan.fashionstore.Models.Product;
import com.olalekan.fashionstore.Utility.UniversalImageLoader;

public class AboutProductActivity extends AppCompatActivity {

    private String mCategoryName;
    private String mProductId;
    private Product mProduct;
    private ImageView mImageAboutProduct;
    private TextView mProductName;
    private TextView mPrice;
    private TextView mSize;
    private TextView mDescription;
    private TextView mFeatures;
    private Button mAddToCart;
    private ProgressBar mProgressBar;
    private DataSnapshot mCartSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_product);

        Toolbar toolbar = (Toolbar) findViewById(R.id.includeMainToolbar);
        setSupportActionBar(toolbar);

        CardView cardView = findViewById(R.id.include_about_product_bottom_sheet_dialog);


        initialiseViews();
//        initialiseImageLoader();

        ViewCompat.setTransitionName(mImageAboutProduct, VIEW_PRODUCT_IMAGE);
        ViewCompat.setTransitionName(mProductName, VIEW_PRODUCT_NAME);
        ViewCompat.setTransitionName(mPrice, VIEW_PRODUCT_PRICE);

        Intent intent = getIntent();
        mCategoryName = intent.getStringExtra(CATEGORY_NAME);
        mProductId = intent.getStringExtra(PRODUCT_ID);

        onClickListeners();


    }

    @Override
    protected void onResume() {
        super.onResume();
        getProductFromDatabase();

    }

    private void onClickListeners(){
        mAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FirebaseAuth.getInstance().getCurrentUser()!=null && mProduct != null){
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.node_users))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(getString(R.string.field_node_cart))
                            .child(mProduct.getProductId());

                    if(mCartSnapshot.hasChild(mProduct.getProductId())){ //Product is already in cart
                        //remove product from cart
                        reference.removeValue();
                    } else {
                        //add product to cart
                        reference.setValue(mProduct.getCategory());
                    }

                }
            }
        });

    }

    private void getProductFromDatabase() {
        showProgressBarAndDisableTouch(AboutProductActivity.this, mProgressBar);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.firebase_node_categories))
                        .child(mCategoryName).child(getString(R.string.field_node_products))
                        .child(mProductId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mProduct = snapshot.getValue(Product.class);
                getUsersCart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressBarAndEnableTouch(AboutProductActivity.this, mProgressBar);
                showErrorDialog(AboutProductActivity.this, error.getMessage());
            }
        });
    }

    private void getUsersCart() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_node_cart));

        //TODO: Change it to addListenerForSingleValueEvent if adding to cart causes problems
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mCartSnapshot = snapshot;
                if(snapshot.hasChild(mProductId)) mAddToCart.setText("Remove From Cart");
                inputProductValues();
                hideProgressBarAndEnableTouch(AboutProductActivity.this, mProgressBar);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressBarAndEnableTouch(AboutProductActivity.this, mProgressBar);
                showErrorDialog(AboutProductActivity.this, error.getMessage());
            }
        });
    }

    private void inputProductValues() {
        ImageLoader.getInstance().displayImage(mProduct.getImageLink(), mImageAboutProduct);
        mProductName.setText(mProduct.getProductName());
        mPrice.setText(mProduct.getPrice());
        mSize.setText(mProduct.getSize());
        mDescription.setText(mProduct.getDescription());
        mFeatures.setText(mProduct.getFeatures());
    }

    private void initialiseImageLoader() {
        UniversalImageLoader imageLoader = new UniversalImageLoader(AboutProductActivity.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }


    private void initialiseViews(){
        mImageAboutProduct = findViewById(R.id.imageViewAboutProduct);
        mProductName = findViewById(R.id.textViewAboutProductName);
        mPrice = findViewById(R.id.textViewAboutProductPrice);
        mSize = findViewById(R.id.textViewAboutProductSize);
        mDescription = findViewById(R.id.textViewAboutProductDetails);
        mFeatures = findViewById(R.id.textViewAboutProductFeatures);

        mAddToCart = findViewById(R.id.buttonAboutProductAddToCart);
        mProgressBar = findViewById(R.id.progressBarAboutProduct);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_about_product, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_my_cart:
                startActivity(new Intent(AboutProductActivity.this, CartActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}