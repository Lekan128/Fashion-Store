package com.olalekan.fashionstore.Fragments;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.CATEGORIES;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.CATEGORY_NAME;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.PRODUCTS;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.hideProgressBarAndEnableTouch;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showErrorDialog;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showLoginRequiredDialog;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showProgressBarAndDisableTouch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.GnssAntennaInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.olalekan.fashionstore.CartActivity;
import com.olalekan.fashionstore.MainActivity;
import com.olalekan.fashionstore.Models.Categories;
import com.olalekan.fashionstore.Models.Product;
import com.olalekan.fashionstore.R;
import com.olalekan.fashionstore.Utility.ProductRecyclerAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class ProductListFragment extends Fragment {

    private productListFragmentListener mListener;
    private String mCategoryName;


    private FirebaseUser mUser;
    private ImageView mCartImage;
    private ImageView mSearchImage;
    private EditText mSearchText;
    private ProgressBar mProgressBar;
    private TextView mNoProductText;
    private RecyclerView mProductListRecyclerView;
    private ProductRecyclerAdapter mProductRecyclerAdapter;
    private GridLayoutManager mProductLayoutManager;
    private DataSnapshot mCartSnapshot = null;
    private ArrayList<Product> mProducts;


    private ArrayList<Categories> mCategories;
    private Spinner mProductListSpinner;
    private ArrayAdapter<String> mAdapter;
    Bundle mBundle;


    public ProductListFragment(){}
    public ProductListFragment(Bundle bundle){
        mBundle = bundle;
    }

    public ProductListFragment(ArrayList<Product> products){
        mProducts = products;
    }

//    public ProductListFragment(String categoryName){
//        mCategoryName = categoryName;
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.activity_product_list, container, false);
//        Toast.makeText(getContext(),getContext().toString(), Toast.LENGTH_LONG).show();
//        mListener.shown(true);
        if(mBundle != null){
            mCategoryName = mBundle.getString(CATEGORY_NAME);
            mCategories = mBundle.getParcelableArrayList(CATEGORIES);
            mProducts = mBundle.getParcelableArrayList(PRODUCTS);
        }else
        if(requireArguments().getParcelableArrayList(PRODUCTS) !=null){
            mCategoryName = requireArguments().getString(CATEGORY_NAME);
            mCategories = requireArguments().getParcelableArrayList(CATEGORIES);
            mProducts = requireArguments().getParcelableArrayList(PRODUCTS);

        }

//        BottomNavigationView bottomNavigationView = getView()f


        mUser = FirebaseAuth.getInstance().getCurrentUser();
        initialiseViews(view);

        setUpSpinner();
//        mProductListSpinner.setSelection();

        searchForProduct();

//     Todo:   if(!guestLogin && mUser == null){ //not a guest and user is null
//            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
//            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(loginIntent);
//        }

        onClickListeners();

        mProductListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                mProducts = getProductsFromCategory(adapterView.getItemAtPosition(position).toString());

                if(mProducts == null || mProducts.isEmpty()){
                    mNoProductText.setVisibility(View.VISIBLE);
                }else {
                    mNoProductText.setVisibility(View.GONE);
                }
                setUpRecyclerView();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        return view;
    }

    private void searchForProduct() {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        mNoProductText.setText(R.string.no_products);
        if(mProducts == null || mProducts.isEmpty()){
            mNoProductText.setVisibility(View.VISIBLE);
        } else {
            mNoProductText.setVisibility(View.GONE);

//            if(mUser != null) getUsersCart();

//            if (mCategoryName != null) {
//                ArrayList<Product> products = new ArrayList<>();
//                for(Product product : mProducts){
//                    if(product.getCategory().equals(mCategoryName)) products.add(product);
//                }
//                mProducts = products;
//            }

//            setUpRecyclerView();
        }



//        if (mCategoryName != null)getProductsFromDatabase();
//        else getAllProductsFromDatabase();
    }

    private ArrayList<Product> getProductsFromCategory(String categorySelected) {
        if(categorySelected.equals(getString(R.string.all))){
            return getAllTheProducts();
        }

        for(Categories categories : mCategories){
            if(categories.getCategoryName().equals(categorySelected)){
                return categories.getTheProducts();
            }
        }

        return null;
    }

    private void setUpSpinner() {
        ArrayList<String> categoryStringList = new ArrayList<>();
        categoryStringList.add(getString(R.string.all));
        for(Categories categories: mCategories){
            categoryStringList.add(categories.getCategoryName());
        }
        mAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item,categoryStringList );
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        mProductListSpinner.setAdapter(mAdapter);

    }


//    private void getAllProductsFromDatabase() {
//        Query query = FirebaseDatabase.getInstance().getReference()
//                .child(getString(R.string.firebase_node_categories));
//
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                mCategories = new ArrayList<>();
//                for(DataSnapshot categorySnapshot: snapshot.getChildren()){
//                    Categories category = new Categories();
//                    category.setCategoryName(categorySnapshot.getKey());
//                    DataSnapshot productSnapshot = categorySnapshot.child(getString(R.string.field_node_products));
//                    ArrayList<Product> products = new ArrayList<>();
//                    for(DataSnapshot singleProductSnapshot: productSnapshot.getChildren()){
//                        products.add(singleProductSnapshot.getValue(Product.class));
//                    }
//                    category.setTheProducts(products);
//                    mCategories.add(category);
//
//                }
//                getAllTheProducts();
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
//                showErrorDialog(getContext(), error.getMessage());
//            }
//        });
//    }

    private ArrayList<Product> getAllTheProducts() {
        ArrayList<Product> allProducts = new ArrayList<>();
        for(Categories categories: mCategories){
            allProducts.addAll(categories.getTheProducts());
//            for(Product product: categories.getTheProducts()){
//                mProducts.add(product);
//            }
        }
//        if(allProducts != null){
//            mProducts = allProducts;
//
//            setUpRecyclerView();
//        }

        return allProducts;

    }

    private void onClickListeners() {
        mCartImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    startActivity(new Intent(getContext(), CartActivity.class));
                } else {
                    showLoginRequiredDialog(getContext());
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

            mProductRecyclerAdapter = new ProductRecyclerAdapter(getContext(), searchedProducts, mCartSnapshot);
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
                hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
                mProductRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
                showErrorDialog(getContext(), error.getMessage());
            }
        });
    }

//    private void getProductsFromDatabase() {
//        showProgressBarAndDisableTouch(getActivity(), mProgressBar );
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//        Query query = reference.child(getString(R.string.firebase_node_categories)).
//                child(mCategoryName).child(getString(R.string.field_node_products));
//
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                mProducts = new ArrayList<>();
//
//                for(DataSnapshot productSnapshot : snapshot.getChildren()){
//                    mProducts.add(productSnapshot.getValue(Product.class));
//                }
//                if(mUser != null) getUsersCart();
//                else {
//                    setUpRecyclerView();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
//                showErrorDialog(getContext(), error.getMessage());
//            }
//        });
//    }

    private void setUpRecyclerView() {
        hideProgressBarAndEnableTouch(getActivity(), mProgressBar);

        mProductLayoutManager = new GridLayoutManager(getContext(), 2);
        mProductListRecyclerView.setLayoutManager(mProductLayoutManager);
        mProductRecyclerAdapter = new ProductRecyclerAdapter(getContext(), mProducts, mCartSnapshot);
        mProductListRecyclerView.setAdapter(mProductRecyclerAdapter);
    }


    private void initialiseViews(@NonNull View view) {
        mNoProductText = view.findViewById(R.id.textViewProductListNoProduct);
        mSearchText = view.findViewById(R.id.editTextSearchProductList);
        mSearchImage = view.findViewById(R.id.searchImageProductList);
        mCartImage = view.findViewById(R.id.imageViewCart);
        mProgressBar = view.findViewById(R.id.progressBarProductList);

        mProductListRecyclerView = view.findViewById(R.id.recyclerViewProductList);
        mProductListSpinner = view .findViewById(R.id.spinnerCategoriesProductList);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (productListFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement NewCategoryDialogListener");
        }
    }

    public interface productListFragmentListener{
        void shown(boolean isShown);
    }
}
