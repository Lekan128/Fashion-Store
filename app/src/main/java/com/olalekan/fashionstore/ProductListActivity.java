package com.olalekan.fashionstore;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.CATEGORIES;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.CATEGORY_NAME;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.GUEST_LOGIN_BOOLEAN;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.hideProgressBarAndEnableTouch;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showErrorDialog;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showLoginRequiredDialog;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showProgressBarAndDisableTouch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.olalekan.fashionstore.Models.Categories;
import com.olalekan.fashionstore.Models.Product;
import com.olalekan.fashionstore.Utility.ProductRecyclerAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class ProductListActivity extends AppCompatActivity {

    private String mCategoryName;
    private ArrayList<Product> mProducts;
    private RecyclerView mProductListRecyclerView;
    private GridLayoutManager mProductLayoutManager;
    private ProductRecyclerAdapter mProductRecyclerAdapter;
    private DataSnapshot mCartSnapshot = null;
    private FirebaseUser mUser;
    private ImageView mCartImage;
    private ImageView mSearchImage;
    private EditText mSearchText;
    private ProgressBar mProgressBar;
    private TextView mNoProductText;
    private ArrayList<Categories> mCategories;
    private Spinner mProductListSpinner;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

//        Toolbar toolbar = findViewById(R.id.includeMainToolbar);
//        setSupportActionBar(toolbar);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        initialiseViews();
        Intent intent = getIntent();
        mCategoryName = intent.getStringExtra(CATEGORY_NAME);
        mCategories = intent.getParcelableArrayListExtra(CATEGORIES);
        boolean guestLogin = intent.getBooleanExtra(GUEST_LOGIN_BOOLEAN,false);



        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchForProductWithKeyword(editable.toString());
            }
        });





        if(!guestLogin && mUser == null){ //not a guest and user is null
            Intent loginIntent = new Intent(ProductListActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        }

        onClickListeners();

//        BottomNavigationView bottomNavigationView = findViewById(R.id.includeBottomNavProductList);

        mProductListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                mProducts = getProductsFromCategory(adapterView.getItemAtPosition(position).toString());

                if(mProducts == null || mProducts.isEmpty()){
                    mNoProductText.setVisibility(View.VISIBLE);
                }else {
                    mNoProductText.setVisibility(View.GONE);
//                    setUpRecyclerView();
                }
                setUpRecyclerView();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        mNoProductText.setText(R.string.no_products);
        mNoProductText.setVisibility(View.GONE);

        if (mCategoryName != null)getProductsFromDatabase();
        else getAllProductsFromDatabase();
    }

    private ArrayList<Product> getProductsFromCategory(String categorySelected) {
        for(Categories categories : mCategories){
            if(categories.getCategoryName().equals(categorySelected)){
                return categories.getTheProducts();
            }
        }
        return null;
    }

    private void setUpSpinner() {
        ArrayList<String> categoryStringList = new ArrayList<>();
        for(Categories categories: mCategories){
            categoryStringList.add(categories.getCategoryName());
        }
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,categoryStringList );
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        mProductListSpinner.setAdapter(mAdapter);

    }

    private void initialiseViews() {
        mNoProductText = findViewById(R.id.textViewProductListNoProduct);
        mSearchText = findViewById(R.id.editTextSearchProductList);
        mSearchImage = findViewById(R.id.searchImageProductList);
        mCartImage = findViewById(R.id.imageViewCart);
        mProgressBar = findViewById(R.id.progressBarProductList);

        mProductListRecyclerView = findViewById(R.id.recyclerViewProductList);
        mProductListSpinner = findViewById(R.id.spinnerCategoriesProductList);
    }


    private void getAllProductsFromDatabase() {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_node_categories));

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mCategories = new ArrayList<>();
                for(DataSnapshot categorySnapshot: snapshot.getChildren()){
                    Categories category = new Categories();
                    category.setCategoryName(categorySnapshot.getKey());
                    DataSnapshot productSnapshot = categorySnapshot.child(getString(R.string.field_node_products));
                    ArrayList<Product> products = new ArrayList<>();
                    for(DataSnapshot singleProductSnapshot: productSnapshot.getChildren()){
                        products.add(singleProductSnapshot.getValue(Product.class));
                    }
                    category.setTheProducts(products);
                    mCategories.add(category);

                }
                getAllTheProducts();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressBarAndEnableTouch(ProductListActivity.this, mProgressBar);
                showErrorDialog(ProductListActivity.this, error.getMessage());
            }
        });
    }

    private void getAllTheProducts() {
        mProducts = new ArrayList<>();
        for(Categories categories: mCategories){
            mProducts.addAll(categories.getTheProducts());
//            for(Product product: categories.getTheProducts()){
//                mProducts.add(product);
//            }
        }

        setUpRecyclerView();
    }

    private void onClickListeners() {
        mCartImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    startActivity(new Intent(ProductListActivity.this, CartActivity.class));
                } else {
                    showLoginRequiredDialog(ProductListActivity.this);
                }
            }
        });

        mSearchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keyWord = mSearchText.getText().toString();
                if(!keyWord.equals("") && mProducts != null){
                    searchForProductWithKeyword(keyWord);
                }
            }
        });
    }


    private void searchForProductWithKeyword(String keyWord) {
        keyWord = keyWord.toLowerCase(Locale.ROOT);
        if(keyWord.equals("")){
            setUpRecyclerView();
            if(mProducts.isEmpty()){
                mNoProductText.setText(R.string.no_products);
                mNoProductText.setVisibility(View.VISIBLE);
            }else {
                mNoProductText.setVisibility(View.GONE);
            }
        }else {
            ArrayList<Product> searchedProducts = new ArrayList<>();
            for(Product product : mProducts){
                if(product.getProductName().toLowerCase(Locale.ROOT).contains(keyWord)){
                    searchedProducts.add(product);
                }
            }

            mProductRecyclerAdapter = new ProductRecyclerAdapter(ProductListActivity.this, searchedProducts, mCartSnapshot);
            mProductListRecyclerView.setAdapter(mProductRecyclerAdapter);

            if(searchedProducts.isEmpty()){
                mNoProductText.setText(R.string.no_searched_product);
                mNoProductText.setVisibility(View.VISIBLE);
            } else {
                mNoProductText.setVisibility(View.GONE);

            }

        }


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
//                setUpRecyclerView();
                if(mProducts.isEmpty()){
                    mNoProductText.setVisibility(View.VISIBLE);
                }
                setUpSpinner();
                mProductListSpinner.setSelection(mAdapter.getPosition(mCategoryName));
                hideProgressBarAndEnableTouch(ProductListActivity.this, mProgressBar);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressBarAndEnableTouch(ProductListActivity.this, mProgressBar);
                showErrorDialog(ProductListActivity.this, error.getMessage());
            }
        });
    }

    private void getProductsFromDatabase() {
        showProgressBarAndDisableTouch(ProductListActivity.this, mProgressBar );
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.firebase_node_categories)).
                child(mCategoryName).child(getString(R.string.field_node_products));

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mProducts = new ArrayList<>();

                for(DataSnapshot productSnapshot : snapshot.getChildren()){
                    mProducts.add(productSnapshot.getValue(Product.class));
                }
                if(mUser != null) getUsersCart();
                else {
                    setUpSpinner();
                    mProductListSpinner.setSelection(mAdapter.getPosition(mCategoryName));
                    setUpRecyclerView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressBarAndEnableTouch(ProductListActivity.this, mProgressBar);
                showErrorDialog(ProductListActivity.this, error.getMessage());
            }
        });
    }


    private void setUpRecyclerView() {
        hideProgressBarAndEnableTouch(ProductListActivity.this, mProgressBar);

        mProductLayoutManager = new GridLayoutManager(ProductListActivity.this, 2);
        mProductListRecyclerView.setLayoutManager(mProductLayoutManager);
        mProductRecyclerAdapter = new ProductRecyclerAdapter(ProductListActivity.this, mProducts, mCartSnapshot);
        mProductListRecyclerView.setAdapter(mProductRecyclerAdapter);
    }

}