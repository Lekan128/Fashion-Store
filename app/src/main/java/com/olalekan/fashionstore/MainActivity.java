package com.olalekan.fashionstore;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.CATEGORIES;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.GUEST_LOGIN_BOOLEAN;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.PRODUCTS;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.hideProgressBarAndEnableTouch;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.logoutIfUserIsNull;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showErrorDialog;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showProgressBarAndDisableTouch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.olalekan.fashionstore.Fragments.CartFragment;
import com.olalekan.fashionstore.Fragments.CategoryListFragment;
import com.olalekan.fashionstore.Fragments.ProductListFragment;
import com.olalekan.fashionstore.Fragments.ProfileFragment;
import com.olalekan.fashionstore.Models.Categories;
import com.olalekan.fashionstore.Models.Product;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ProductListFragment.productListFragmentListener {

    private FirebaseUser mUser;
    private ProgressBar mProgressBar;
    private ArrayList<Product> mProducts;

    private ArrayList<Categories> mCategories;
    private NavigationBarView mNavigationBarView;

    private ViewPager2 mViewPager;
    private static final int NUMBER_OF_PAGES = 4;

    private String mCategoryName;

    Bundle mSavedInstanceState;

    MenuItem previousMenuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSavedInstanceState = savedInstanceState;
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainerView);
//        if(fragment == null){
//            fragment = new ProductListFragment();
//            fragmentManager.beginTransaction()
//                    .add(R.id.fragmentContainerView, ProductListFragment.class, null)
//                    .commit();
//        }

        mNavigationBarView = findViewById(R.id.includeBottomNavigationViewMain);
        mProgressBar = findViewById(R.id.mainActivityProgressBar);
        mViewPager = findViewById(R.id.fragmentContainerViewPager);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();
//        mCategoryName = intent.getStringExtra(CATEGORY_NAME);

        boolean guestLogin = intent.getBooleanExtra(GUEST_LOGIN_BOOLEAN,false);

        if(!guestLogin && mUser == null){ //not a guest and user is null
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        }

//            mUser = FirebaseAuth.getInstance().getCurrentUser();


//        FragmentStateAdapter pageAdapter = new ScreenSlidePagerAdapter(this);
//        mViewPager.setAdapter(pageAdapter);

//        navigationBarViewListener();

//        if(mSavedInstanceState == null){
//
//            Bundle bundle = new Bundle();
////            bundle.putString(CATEGORY_NAME, mCategoryName);
//            bundle.putParcelableArrayList(PRODUCTS, mProducts);
//            bundle.putParcelableArrayList(CATEGORIES, mCategories);
//            getSupportFragmentManager().beginTransaction()
//                    .setReorderingAllowed(true)
//                    .replace(R.id.fragmentContainerView, ProductListFragment.class, bundle)
//                    .commit();
//        }
//

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                if(previousMenuItem != null){
                    previousMenuItem.setChecked(false);
                } else {
                    mNavigationBarView.getMenu().getItem(0).setChecked(false);
                }
                mNavigationBarView.getMenu().getItem(position).setChecked(true);
                previousMenuItem = mNavigationBarView.getMenu().getItem(position);
//                Bundle bundle = new Bundle();

//                switch (position){
//                    case 0:
//                        mNavigationBarView.getMenu().getItem(position).setChecked(true);
//                        break;
//
//                    case 1:
//                        mNavigationBarView.getMenu().getItem(position).setChecked(true);
//                        break;
//
//                    case 2:
//                        if(mUser==null){
//                            logoutIfUserIsNull(MainActivity.this);
//                            finish();
//                        } else{
//                            mNavigationBarView.getMenu().getItem(position).setChecked(true);
//                            previousMenuItem = mNavigationBarView.getMenu().getItem(position);
//                            break;
//                        }
//
//                    case 3:
//                        if(mUser==null){
//                            logoutIfUserIsNull(MainActivity.this);
//                            finish();
//                        }else {
//                            mNavigationBarView.getMenu().getItem(position).setChecked(true);
//                            break;
//                        }
//
//                    default:
//                        bundle.putParcelableArrayList(PRODUCTS, mProducts);
//                        bundle.putParcelableArrayList(CATEGORIES, mCategories);
//                        break;
//
//                }

                super.onPageSelected(position);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        showProgressBarAndDisableTouch(this, mProgressBar);
        getAllCategoriesFromDatabase();
    }

    private void getAllCategoriesFromDatabase() {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_node_categories));

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mCategories = new ArrayList<>();
                for(DataSnapshot categorySnapshot: snapshot.getChildren()){
                    Categories category = new Categories();
//                    category.setCategoryName(categorySnapshot.getKey());
                    category = categorySnapshot.getValue(Categories.class);
                    DataSnapshot productSnapshot = categorySnapshot.child(getString(R.string.field_node_products));
                    ArrayList<Product> products = new ArrayList<>();
                    for(DataSnapshot singleProductSnapshot: productSnapshot.getChildren()){
                        products.add(singleProductSnapshot.getValue(Product.class));
                    }
                    category.setTheProducts(products);
                    mCategories.add(category);

                }
                getAllTheProducts();

//                if(mUser != null) getUsersCart();
//                else {
//                    navigationBarViewListener();
//                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressBarAndEnableTouch(MainActivity.this, mProgressBar);
                showErrorDialog(MainActivity.this, error.getMessage());
            }
        });
    }

//    private void getUsersCart() {
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
//                .child(getString(R.string.node_users))
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .child(getString(R.string.field_node_cart));
//
//        //TODO: Change it to addListenerForSingleValueEvent if adding to cart causes problems
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                mCartSnapshot = snapshot;
////                setUpRecyclerView();
//                if(mProducts.isEmpty()){
//                    mNoProductText.setVisibility(View.VISIBLE);
//                }
//                hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
//                showErrorDialog(getContext(), error.getMessage());
//            }
//        });
//    }

    private void getAllTheProducts() {
        mProducts = new ArrayList<>();
        for(Categories categories: mCategories){
            mProducts.addAll(categories.getTheProducts());
//            for(Product product: categories.getTheProducts()){
//                mProducts.add(product);
//            }
        }

//        setUpRecyclerView();
        hideProgressBarAndEnableTouch(this, mProgressBar);

        FragmentStateAdapter pageAdapter = new ScreenSlidePagerAdapter(this);
        mViewPager.setAdapter(pageAdapter);

        navigationBarViewListener();
    }


    private void navigationBarViewListener() {
//        if(mSavedInstanceState == null){
//
//            Bundle bundle = new Bundle();
////            bundle.putString(CATEGORY_NAME, mCategoryName);
//            bundle.putParcelableArrayList(PRODUCTS, mProducts);
//            bundle.putParcelableArrayList(CATEGORIES, mCategories);
//            getSupportFragmentManager().beginTransaction()
//                    .setReorderingAllowed(true)
//                    .replace(R.id.fragmentContainerViewPager, ProductListFragment.class, bundle)
//                    .commit();
//        }


        mNavigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Bundle bundle = new Bundle();

                switch (item.getItemId()){
                    case R.id.menu_main_home:
//                        bundle.putString("Category Name", "Gowns");
//                        bundle.putParcelableArrayList(PRODUCTS, mProducts);
//                        bundle.putParcelableArrayList(CATEGORIES, mCategories);
//                        getSupportFragmentManager().beginTransaction()
//                                .setCustomAnimations(R.anim.slide_in,
//                                        R.anim.slide_out,
//                                        R.anim.slide_in,
//                                        R.anim.slide_out)
//                                .setReorderingAllowed(true)
//                                .replace(R.id.fragmentContainerViewPager, ProductListFragment.class, bundle)
//                                .commit();
//                        break;
                        mViewPager.setCurrentItem(0);
                        break;

                    case R.id.menu_main_category:
//                        bundle.putParcelableArrayList(CATEGORIES, mCategories);
//                        getSupportFragmentManager().beginTransaction()
//                                .setCustomAnimations(R.anim.slide_in,
//                                        R.anim.slide_out,
//                                        R.anim.slide_in,
//                                        R.anim.slide_out)
//                                .setReorderingAllowed(true)
//                                .replace(R.id.fragmentContainerViewPager, CategoryListFragment.class, bundle)
//                                .commit();
                        mViewPager.setCurrentItem(1);
                        break;

                    case R.id.menu_main_cart:
                        if(mUser==null){
                            logoutIfUserIsNull(MainActivity.this);
                            finish();
                        } else{
                            mViewPager.setCurrentItem(2);
                        }

                        break;

                    case R.id.menu_main_profile:
                        if(mUser==null){
                            logoutIfUserIsNull(MainActivity.this);
                            finish();
                        }else {
                            mViewPager.setCurrentItem(3);
                        }
                        break;


                }

                return true;
            }
        });

        mNavigationBarView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {

            }
        });
    }

//    public static void selectNavigation


    @Override
    public void shown(boolean isShown) {
        if(isShown){
            mNavigationBarView.setSelectedItemId(R.id.menu_main_home);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter{

        public ScreenSlidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Bundle bundle = new Bundle();

            switch (position){
                case 0:
//                        bundle.putString("Category Name", "Gowns");
                    bundle.putParcelableArrayList(PRODUCTS, mProducts);
                    bundle.putParcelableArrayList(CATEGORIES, mCategories);
//                    mNavigationBarView.getMenu().getItem(0).setChecked(true);
//                    if(previousMenuItem != null){
//                        previousMenuItem.setChecked(false);
//                    } else {
//                        mNavigationBarView.getMenu().getItem(0).setChecked(false);
//                    }
//                    mNavigationBarView.getMenu().getItem(position).setChecked(true);
//                    previousMenuItem = mNavigationBarView.getMenu().getItem(position);

                    return new ProductListFragment(bundle);

                case 1:
                    bundle.putParcelableArrayList(CATEGORIES, mCategories);
//                    mNavigationBarView.setSelectedItemId(R.id.menu_main_category);
//                    mNavigationBarView.getMenu().getItem(1).setChecked(true);
//                    if(previousMenuItem != null){
//                        previousMenuItem.setChecked(false);
//                    } else {
//                        mNavigationBarView.getMenu().getItem(0).setChecked(false);
//                    }
//                    mNavigationBarView.getMenu().getItem(position).setChecked(true);
//                    previousMenuItem = mNavigationBarView.getMenu().getItem(position);
                    return new CategoryListFragment(bundle);

                case 2:
                    if(mUser==null){
                        logoutIfUserIsNull(MainActivity.this);
                        finish();
                    } else{
//                        mNavigationBarView.setSelectedItemId(R.id.menu_main_cart);
//                        mNavigationBarView.getMenu().getItem(2).setChecked(true);
//                        if(previousMenuItem != null){
//                            previousMenuItem.setChecked(false);
//                        } else {
//                            mNavigationBarView.getMenu().getItem(0).setChecked(false);
//                        }
//                        mNavigationBarView.getMenu().getItem(position).setChecked(true);
//                        previousMenuItem = mNavigationBarView.getMenu().getItem(position);
                        return new CartFragment();
                    }

                case 3:
                    if(mUser==null){
                        logoutIfUserIsNull(MainActivity.this);
                        finish();
                    }else {
//                        mNavigationBarView.setSelectedItemId(R.id.menu_main_profile);
//                        mNavigationBarView.getMenu().getItem(3).setChecked(true);
//                        if(previousMenuItem != null){
//                            previousMenuItem.setChecked(false);
//                        } else {
//                            mNavigationBarView.getMenu().getItem(0).setChecked(false);
//                        }
//                        mNavigationBarView.getMenu().getItem(position).setChecked(true);
//                        previousMenuItem = mNavigationBarView.getMenu().getItem(position);
                        return new ProfileFragment();
                    }

                default:
                    bundle.putParcelableArrayList(PRODUCTS, mProducts);
                    bundle.putParcelableArrayList(CATEGORIES, mCategories);
                    return new ProductListFragment(bundle);

            }
        }

        @Override
        public int getItemCount() {
            return NUMBER_OF_PAGES;
        }
    }
}