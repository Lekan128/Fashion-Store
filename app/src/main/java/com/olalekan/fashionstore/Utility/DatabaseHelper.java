package com.olalekan.fashionstore.Utility;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.hideProgressBarAndEnableTouch;

import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.olalekan.fashionstore.Models.Categories;
import com.olalekan.fashionstore.Models.Product;

import java.util.ArrayList;
import java.util.Objects;

public class DatabaseHelper {
    public ArrayList<Product> getAllTheProducts(ArrayList<Categories> categories) {
        ArrayList<Product> products = new ArrayList<>();
        for(Categories category: categories){
            products.addAll(category.getTheProducts());
        }
        return products;
    }

    public FirebaseUser getUser () {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public boolean thereIsAUser(){
        return getUser() != null;
    }


    public static class CategoryNameAndProductId{
        public String categoryName;
        public String  productId;


        CategoryNameAndProductId(String categoryName, String productId){
            this.categoryName = categoryName;
            this.productId = productId;
        }


    }

    public ArrayList<CategoryNameAndProductId> getCategoryAndIdFromSnapshot(DataSnapshot cartSnapshot){


        ArrayList<CategoryNameAndProductId> categoryNameAndProductIds = new ArrayList<>();
//            mCartSnapshot = snapshot;
//                setUpRecyclerView();
        for(DataSnapshot snapshot : cartSnapshot.getChildren()){
            final String categoryName = snapshot.getValue(String.class);
            final String productId = snapshot.getKey();

            categoryNameAndProductIds.add(new CategoryNameAndProductId(categoryName, productId));
        }

//            getProductsFromDatabase(categoryNameAndProductIds);

        return categoryNameAndProductIds;

    }
//
//    public ArrayList<Product> getCartProducts(){
//        DatabaseViewModel databaseViewModel = new DatabaseViewModel();
//        getCategoryAndIdFromSnapshot(Objects.requireNonNull(databaseViewModel.getCartSnapshot().getValue()));
//        databaseViewModel.getCartProducts(getCategoryAndIdFromSnapshot(Objects.requireNonNull(databaseViewModel.getCartSnapshot().getValue())))
//
//    }


}
