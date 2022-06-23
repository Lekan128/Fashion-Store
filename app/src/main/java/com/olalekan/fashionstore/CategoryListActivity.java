package com.olalekan.fashionstore;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.GUEST_LOGIN_BOOLEAN;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.hideProgressBarAndEnableTouch;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showErrorDialog;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showProgressBarAndDisableTouch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.olalekan.fashionstore.Models.Categories;
import com.olalekan.fashionstore.Utility.CategoryRecyclerAdapter;

import java.util.ArrayList;

public class CategoryListActivity extends AppCompatActivity {

    //Todo: remove

    private RecyclerView mCategoryRecyclerView;
    private CategoryRecyclerAdapter mCategoryRecyclerAdapter;
    private GridLayoutManager mCategoryLayoutManager;
    private ArrayList<Categories> mCategories;
    private FirebaseUser mUser;
    private ProgressBar mProgressBar;
    private TextView mNoCategoryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        Toolbar mainToolBar = (Toolbar) findViewById(R.id.includeMainToolbar);

        setSupportActionBar(mainToolBar);

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setIcon(R.drawable.ic_baseline_checkroom_24);
//        actionBar.setDisplayUseLogoEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        boolean guestLogin = intent.getBooleanExtra(GUEST_LOGIN_BOOLEAN,false);
        if(!guestLogin && mUser == null){
            Intent loginIntent = new Intent(CategoryListActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        }


        mCategoryRecyclerView = findViewById(R.id.categoryRecyclerAdapter);
        mProgressBar = findViewById(R.id.progressBarMain);
        mNoCategoryText = findViewById(R.id.textViewMainNoCategories);

//        getCategoriesFromDatabase();


//        setUpRecyclerView(demoList);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mNoCategoryText.setVisibility(View.GONE);
        getCategoriesFromDatabase();
    }

    private void getCategoriesFromDatabase() {
        showProgressBarAndDisableTouch(CategoryListActivity.this, mProgressBar);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.firebase_node_categories));
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mCategories = new ArrayList<>();
//                if(snapshot == null){
//                    hideProgressBarAndEnableTouch(MainActivity.this, mProgressBar);
//                }
                for(DataSnapshot categoriesSnapshot : snapshot.getChildren()){
                    //snapshot gets the category with its name;
                    mCategories.add(categoriesSnapshot.getValue(Categories.class));
//                    for(DataSnapshot singleCategorySnapshot: categoriesSnapshot.getChildren()){
//                        mCategories.add(singleCategorySnapshot.getValue(Categories.class));
//                    }
                }
                if(mCategories.isEmpty()){
                    mNoCategoryText.setVisibility(View.VISIBLE);
                } else {
                    mNoCategoryText.setVisibility(View.GONE);
                    setUpRecyclerView();
                    hideProgressBarAndEnableTouch(CategoryListActivity.this, mProgressBar);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressBarAndEnableTouch(CategoryListActivity.this, mProgressBar);
                showErrorDialog(CategoryListActivity.this, error.getMessage());
            }


        });
    }

    private void setUpRecyclerView() {
        mCategoryLayoutManager = new GridLayoutManager(CategoryListActivity.this, 2);
        mCategoryRecyclerView.setLayoutManager(mCategoryLayoutManager);
        mCategoryRecyclerAdapter = new CategoryRecyclerAdapter(CategoryListActivity.this, mCategories);
        mCategoryRecyclerView.setAdapter(mCategoryRecyclerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem signOut = menu.findItem(R.id.menu_sign_out);
        MenuItem myProfile = menu.findItem(R.id.menu_my_profile);
        MenuItem cart = menu.findItem(R.id.menu_main_cart);
        if(mUser == null){
            signOut.setVisible(false);
            myProfile.setVisible(false);
            cart.setVisible(false);
        } else {
            signOut.setVisible(true);
            myProfile.setVisible(true);
            cart.setVisible(true);
        }

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_cart:
                startActivity(new Intent(CategoryListActivity.this, CartActivity.class));
                break;

            case R.id.menu_sign_out:
                if(mUser!=null){
                    signOut();
                }
                break;

            case R.id.menu_my_profile:
                startActivity(new Intent(CategoryListActivity.this, ProfileActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(CategoryListActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}