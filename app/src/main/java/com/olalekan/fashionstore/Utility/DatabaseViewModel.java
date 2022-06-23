package com.olalekan.fashionstore.Utility;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.olalekan.fashionstore.Models.Categories;
import com.olalekan.fashionstore.Models.Product;
import com.olalekan.fashionstore.Models.User;
import com.olalekan.fashionstore.R;

import java.util.ArrayList;

public class DatabaseViewModel extends AndroidViewModel {
    static Application mApplication;
    DatabaseViewModelListener mListener;



    private static MutableLiveData<ArrayList<Categories>> currentCategories;
    private static MutableLiveData<DataSnapshot> mCartSnapshot;
    private static MutableLiveData<ArrayList<Product>> mCartProducts;
    private static MutableLiveData<User> mUser;

//    private static DatabaseViewModel instance;
//
//    public static DatabaseViewModel getInstance(){
//        if (instance == null){
//            instance = new DatabaseViewModel(instance.getApplication());
//        }
//        return instance;
//    }

    public DatabaseViewModel(@NonNull Application application) {
        super(application);
        mApplication = application;
    }

    public MutableLiveData<ArrayList<Categories>> getCurrentCategories(){
        if(currentCategories == null){
            currentCategories = new MutableLiveData<>();
            getAllCategoriesFromDatabase();

        }
        return currentCategories;
    }

    public MutableLiveData<DataSnapshot> getCartSnapshot(){
        if(mCartSnapshot == null){
            mCartSnapshot = new MutableLiveData<>();
            getUsersCart();
        }
        return mCartSnapshot;
    }

    public MutableLiveData<ArrayList<Product>> getCartProducts(ArrayList<DatabaseHelper.CategoryNameAndProductId> categoryNameAndProductIds){
        if(mCartProducts == null){
            mCartProducts = new MutableLiveData<>();
            getCartProductsFromDatabase(categoryNameAndProductIds);
        }
        return mCartProducts;
    }

    public MutableLiveData<User> getCurrentUser(){
        if(mUser == null){
            mUser = new MutableLiveData<>();
            getUserWithDetailsFromDatabase();
        }
        return mUser;
    }


    public void getUserWithDetailsFromDatabase(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(mApplication.getApplicationContext().getString(R.string.node_users))
                .child(new DatabaseHelper().getUser().getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                AllRoundUseful.hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
//                mUser = snapshot.getValue(User.class);
//                inputValues();
                getCurrentUser().setValue(snapshot.getValue(User.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                AllRoundUseful.hideProgressBarAndEnableTouch(getActivity(),mProgressBar);
//                AllRoundUseful.showErrorDialog(getContext(), error.getMessage());
                mListener.showErrorDialog(error.getMessage());
            }
        });
    }

    private void getAllCategoriesFromDatabase() {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(mApplication.getApplicationContext().getString(R.string.firebase_node_categories));

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Categories> categories = new ArrayList<>();
                for(DataSnapshot categorySnapshot: snapshot.getChildren()){
//                    category.setCategoryName(categorySnapshot.getKey());
                    Categories category = categorySnapshot.getValue(Categories.class);
                    DataSnapshot productSnapshot = categorySnapshot.child(mApplication.getApplicationContext().getString(R.string.field_node_products));
                    ArrayList<Product> products = new ArrayList<>();
                    for(DataSnapshot singleProductSnapshot: productSnapshot.getChildren()){
                        products.add(singleProductSnapshot.getValue(Product.class));
                    }
                    assert category != null;
                    category.setTheProducts(products);
                    categories.add(category);


                }
                getCurrentCategories().setValue(categories);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //TODO: show that there is an error
                mListener.showErrorDialog(error.getMessage());
//                hideProgressBarAndEnableTouch(MainActivity.this, mProgressBar);
//                showErrorDialog(MainActivity.this, error.getMessage());
            }
        });
    }


    public interface DatabaseViewModelListener{
        void showErrorDialog(String error);
    }


    private void getCartProductsFromDatabase(ArrayList<DatabaseHelper.CategoryNameAndProductId> categoryNameAndProductIds) {
        ArrayList<Product> mProducts = new ArrayList<>();
        for(DatabaseHelper.CategoryNameAndProductId categoryNameAndProductId : categoryNameAndProductIds){

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(mApplication.getApplicationContext().getString(R.string.firebase_node_categories)).
                    child(categoryNameAndProductId.categoryName).child(mApplication.getApplicationContext().getString(R.string.field_node_products))
                    .child(categoryNameAndProductId.productId);

//            int counter = i;
//            int lastCount = mCategories.size()-1;
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) mProducts.add(snapshot.getValue(Product.class));
                    else removeProductFromDatabase(categoryNameAndProductId.productId);

//                    if(counter == lastCount){
//                    if(mProducts.isEmpty()) {
//                        mCartEmptyText.setVisibility(View.VISIBLE);
//                        mCardSummary.setVisibility(View.INVISIBLE);
//                    }
//                    else {
//                        mCartEmptyText.setVisibility(View.GONE);
//                        setUpRecyclerView();
//                        setUpSummary();
//                        mCardSummary.setVisibility(View.VISIBLE);
//
//                    }
//                    }

                    getCartProducts(categoryNameAndProductIds).setValue(mProducts);


//                    hideProgressBarAndEnableTouch(getActivity(), mProgressBar);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
//                    hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
//                    showErrorDialog(getContext(),error.getMessage());
                    mListener.showErrorDialog(error.getMessage());
                }
            });
        }

//
//        for(int i =0; i<mCategories.size(); i++){
//            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//            Query query = reference.child(getString(R.string.firebase_node_categories)).
//                    child(mCategories.get(i)).child(getString(R.string.field_node_products))
//                    .child(mProductId.get(i));
//
//            int counter = i;
//            int lastCount = mCategories.size()-1;
//            query.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if(snapshot.exists()) mProducts.add(snapshot.getValue(Product.class));
//                    else removeProductFromDatabase(mProductId.get(counter));
//
//                    if(counter == lastCount){
//                        if(mProducts.isEmpty()) {
//                            mCartEmptyText.setVisibility(View.VISIBLE);
//                            mCardSummary.setVisibility(View.INVISIBLE);
//                        }
//                        else {
//                            mCartEmptyText.setVisibility(View.GONE);
//                            setUpRecyclerView();
//                            setUpSummary();
//                            mCardSummary.setVisibility(View.VISIBLE);
//
//                        }
//                    }
//                    hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
//
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
//                    showErrorDialog(getContext(),error.getMessage());
//                }
//            });
//
//        }

    }

    private void removeProductFromDatabase(String productId) {
        FirebaseDatabase.getInstance().getReference()
                .child(mApplication.getApplicationContext().getString(R.string.node_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mApplication.getApplicationContext().getString(R.string.field_node_cart)).child(productId).removeValue();
    }




    private void getUsersCart() {
//        showProgressBarAndDisableTouch(getActivity(), mProgressBar);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(mApplication.getApplicationContext().getString(R.string.node_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mApplication.getApplicationContext().getString(R.string.field_node_cart));

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                mCartSnapshot = new MutableLiveData<>(snapshot);
                getCartSnapshot().setValue(snapshot);

                DatabaseHelper databaseHelper = new DatabaseHelper();
                ArrayList<DatabaseHelper.CategoryNameAndProductId> categoryNameAndProductIds
                        = databaseHelper.getCategoryAndIdFromSnapshot(snapshot);

                getCartProductsFromDatabase(categoryNameAndProductIds);

//                if(snapshot.exists()){
//                    mCategories = new ArrayList<>();
//                    mProductId = new ArrayList<>();
////                setUpRecyclerView();
//                    for(DataSnapshot cartSnapshot : snapshot.getChildren()){
//                        mCategories.add(cartSnapshot.getValue(String.class));
//                        mProductId.add(cartSnapshot.getKey());
//                    }
//
//                    getProductsFromDatabase();
//
//                }else {
//                    mCartEmptyText.setVisibility(View.VISIBLE);
//                    hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
//                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
//                showErrorDialog(getContext(), error.getMessage());
                mListener.showErrorDialog(error.getMessage());
            }
        });
    }


}
