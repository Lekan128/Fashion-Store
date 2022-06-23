package com.olalekan.fashionstore.Utility;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.ABLE_TO_DELETE_BOOLEAN;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.CATEGORIES;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.CATEGORY_NAME;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.GUEST_LOGIN_BOOLEAN;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.PRODUCTS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.olalekan.fashionstore.Fragments.ProductListFragment;
import com.olalekan.fashionstore.Models.Categories;
import com.olalekan.fashionstore.Models.Product;
import com.olalekan.fashionstore.ProductListActivity;
import com.olalekan.fashionstore.R;

import java.util.ArrayList;

public class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryRecyclerAdapter.CategoryViewHolder> {

    ArrayList<Categories> mCategories;
    Context mContext;
    LayoutInflater mLayoutInflater;
    private final Activity mActivity;

    public CategoryRecyclerAdapter(Context context, ArrayList<Categories> categories){
        mContext = context;
        mCategories = categories;
        mLayoutInflater = LayoutInflater.from(context);
        mActivity = (Activity) mContext;
        initialiseImageLoader();
    }

    private void initialiseImageLoader() {
        UniversalImageLoader imageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    @NonNull
    @Override
    public CategoryRecyclerAdapter.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.single_category, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryRecyclerAdapter.CategoryViewHolder holder, int position) {
        Categories category = mCategories.get(position);
        holder.categoryName.setText(category.getCategoryName());
        ImageLoader.getInstance().displayImage(category.getImageLink(), holder.categoryImage);
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder{
        public final TextView categoryName;
        public final ImageView categoryImage;
        public final CardView mSingleCategoryCard;
        public final ImageButton mDeleteCategory;
        boolean canDelete = mActivity.getIntent().getBooleanExtra(ABLE_TO_DELETE_BOOLEAN, false);

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.singleCategoryText);
            categoryImage = itemView.findViewById(R.id.singleCategoryImage);
            mSingleCategoryCard = itemView.findViewById(R.id.cardSingelCategory);
            mDeleteCategory = itemView.findViewById(R.id.imageButtonDeleteSingleCategory);

            if(canDelete){
                mDeleteCategory.setVisibility(View.VISIBLE);
            } else mDeleteCategory.setVisibility(View.GONE);

            onClickListeners();

        }

        private void onClickListeners() {

            View.OnClickListener onClickListener = new View.OnClickListener(){
                @Override
                public void onClick(View view){
//                    Bundle bundle = new Bundle();
//
//                    ArrayList<Product> products = new ArrayList<>();
//                    for(Categories categories: mCategories){
//                        if(categories.getCategoryName().equals(categoryName.getText().toString())){
//                            if(categories.getTheProducts() != null)products.addAll(categories.getTheProducts());
//                            break;
////                            products.addAll(categories.getTheProducts());
//                        }
//                    }
//
////                    bundle.putString(CATEGORY_NAME, categoryName.getText().toString());
//                    bundle.putParcelableArrayList(PRODUCTS, products);
////                    bundle.putParcelableArrayList(CATEGORIES, mCategories);
//                    ProductListFragment productListFragment = new ProductListFragment(products);
//                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
//                            .setReorderingAllowed(true)
//                            .replace(R.id.fragmentContainerView, productListFragment, "find")
//                            .commit();

                    Intent guestLoginIntent = mActivity.getIntent();
                    boolean guestMode = guestLoginIntent.getBooleanExtra(GUEST_LOGIN_BOOLEAN, false);

                    Intent intent = new Intent(mContext, ProductListActivity.class);
                    intent.putParcelableArrayListExtra(CATEGORIES, mCategories);
                    intent.putExtra(CATEGORY_NAME, categoryName.getText().toString());
                    intent.putExtra(GUEST_LOGIN_BOOLEAN, guestMode);
                    if(canDelete) intent.putExtra(ABLE_TO_DELETE_BOOLEAN, canDelete);
                    mContext.startActivity(intent);

                }
            };

            categoryName.setOnClickListener(onClickListener);
            categoryImage.setOnClickListener(onClickListener);
            mSingleCategoryCard.setOnClickListener(onClickListener);

            mDeleteCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(canDelete){
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                                .child( mContext.getString(R.string.node_storage_image_category) + categoryName.getText().toString() +
                                        mContext.getString(R.string.field_storage_category_image));
                        storageReference.delete();

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                                .child(mContext.getString(R.string.firebase_node_categories)).child(categoryName.getText().toString());
                        databaseReference.removeValue();

                        notifyDataSetChanged();

                        }
                }
            });

        }
    }
}
