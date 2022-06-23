package com.olalekan.fashionstore.Fragments;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.CATEGORIES;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.CATEGORY_NAME;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.GUEST_LOGIN_BOOLEAN;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.PRODUCTS;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.hideProgressBarAndEnableTouch;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showErrorDialog;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showProgressBarAndDisableTouch;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.olalekan.fashionstore.CartActivity;
import com.olalekan.fashionstore.CategoryListActivity;
import com.olalekan.fashionstore.LoginActivity;
import com.olalekan.fashionstore.MainActivity;
import com.olalekan.fashionstore.Models.Categories;
import com.olalekan.fashionstore.ProfileActivity;
import com.olalekan.fashionstore.R;
import com.olalekan.fashionstore.Utility.CategoryRecyclerAdapter;
import com.olalekan.fashionstore.Utility.DatabaseHelper;
import com.olalekan.fashionstore.Utility.DatabaseViewModel;

import java.util.ArrayList;

public class CategoryListFragment extends Fragment {

    private RecyclerView mCategoryRecyclerView;
    private CategoryRecyclerAdapter mCategoryRecyclerAdapter;
    private GridLayoutManager mCategoryLayoutManager;
    private ArrayList<Categories> mCategories;
    private FirebaseUser mUser;
    private ProgressBar mProgressBar;
    private TextView mNoCategoryText;
    private Bundle mBundle;

    public CategoryListFragment(){}
    public CategoryListFragment(Bundle bundle){
        mBundle = bundle;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_category_list, container, false);

//        if(mBundle!=null){
//            mCategories = mBundle.getParcelableArrayList(CATEGORIES);
//
//        }else {
////            mCategoryName = requireArguments().getString(CATEGORY_NAME);
//            mCategories = requireArguments().getParcelableArrayList(CATEGORIES);
////        mProducts = requireArguments().getParcelableArrayList(PRODUCTS);
//        }

        DatabaseViewModel mDatabaseViewModel = new ViewModelProvider(this).get(DatabaseViewModel.class);
        DatabaseHelper mDatabaseHelper = new DatabaseHelper();

        final Observer<ArrayList<Categories>> categoriesObserver = new Observer<ArrayList<Categories>>() {
            @Override
            public void onChanged(ArrayList<Categories> categories) {
                if(categories != null){
                    mCategories = categories;
                    setUpRecyclerView();
//                    mProducts = mDatabaseHelper.getAllTheProducts(categories);
//                    hideProgressBarAndEnableTouch(MainActivity.this, mProgressBar);

//                    FragmentStateAdapter pageAdapter = new MainActivity.ScreenSlidePagerAdapter(MainActivity.this);
//                    mViewPager.setAdapter(pageAdapter);

//                    navigationBarViewListener();
                }
            }
        };

        mDatabaseViewModel.getCurrentCategories().observe(getViewLifecycleOwner(), categoriesObserver);



        mUser = mDatabaseHelper.getUser();


//     TODO:   Intent intent = getIntent();
//        boolean guestLogin = intent.getBooleanExtra(GUEST_LOGIN_BOOLEAN,false);
//        if(!guestLogin && mUser == null){
//            Intent loginIntent = new Intent(CategoryListActivity.this, LoginActivity.class);
//            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(loginIntent);
//        }


        mCategoryRecyclerView = view.findViewById(R.id.categoryRecyclerAdapter);
        mProgressBar = view.findViewById(R.id.progressBarMain);
        mNoCategoryText = view.findViewById(R.id.textViewMainNoCategories);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mNoCategoryText.setVisibility(View.GONE);
        if(mCategories == null || mCategories.isEmpty()){
            mNoCategoryText.setVisibility(View.VISIBLE);
            hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
        }
        else{
//            setUpRecyclerView();
        }
//        getCategoriesFromDatabase();
    }

    private void getCategoriesFromDatabase() {
        showProgressBarAndDisableTouch(getActivity(), mProgressBar);
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
                    hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
                showErrorDialog(getContext(), error.getMessage());
            }


        });
    }

    private void setUpRecyclerView() {
        hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
        mCategoryLayoutManager = new GridLayoutManager(getActivity(), 2);
        mCategoryRecyclerView.setLayoutManager(mCategoryLayoutManager);
        mCategoryRecyclerAdapter = new CategoryRecyclerAdapter(getActivity(), mCategories);
        mCategoryRecyclerView.setAdapter(mCategoryRecyclerAdapter);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getActivity().getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//
//        MenuItem signOut = menu.findItem(R.id.menu_sign_out);
//        MenuItem myProfile = menu.findItem(R.id.menu_my_profile);
//        MenuItem cart = menu.findItem(R.id.menu_main_cart);
//        if(mUser == null){
//            signOut.setVisible(false);
//            myProfile.setVisible(false);
//            cart.setVisible(false);
//        } else {
//            signOut.setVisible(true);
//            myProfile.setVisible(true);
//            cart.setVisible(true);
//        }
//
//        return true;
//
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_cart:
                startActivity(new Intent(getActivity(), CartActivity.class));
                break;

            case R.id.menu_sign_out:
                if(mUser!=null){
                    signOut();
                }
                break;

            case R.id.menu_my_profile:
                startActivity(new Intent(getActivity(), ProfileActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}


