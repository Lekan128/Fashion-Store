package com.olalekan.fashionstore.Utility;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.ABLE_TO_DELETE_BOOLEAN;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.CATEGORY_NAME;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.PRODUCT_ID;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.VIEW_PRODUCT_IMAGE;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.VIEW_PRODUCT_NAME;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.VIEW_PRODUCT_PRICE;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showLoginRequiredDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.olalekan.fashionstore.AboutProductActivity;
import com.olalekan.fashionstore.LoginActivity;
import com.olalekan.fashionstore.Models.Product;
import com.olalekan.fashionstore.R;

import java.util.ArrayList;

public class ProductRecyclerAdapter extends RecyclerView.Adapter<ProductRecyclerAdapter.ProductViewHolder>{

    ArrayList<Product> mProducts;
    Context mContext;
    LayoutInflater mLayoutInflater;
    private DatabaseReference mReference;
    private DataSnapshot mCartSnapshot;
    private Activity mActivity;

    public ProductRecyclerAdapter(Context context, ArrayList<Product> products, DataSnapshot cartSnapshot) {
        mProducts = products;
        mContext = context;
        mActivity = (Activity) mContext;
        mCartSnapshot = cartSnapshot;
        mLayoutInflater = LayoutInflater.from(context);
        initialiseImageLoader();
    }


    private void initialiseImageLoader() {
        UniversalImageLoader imageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.single_product, parent,false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = mProducts.get(position);
        holder.productName.setText(product.getProductName());
        holder.price.setText(product.getPrice());

        ImageLoader.getInstance().displayImage(product.getImageLink(), holder.productImage);

        if(mCartSnapshot!=null){
            if(mCartSnapshot.hasChild(product.getProductId())){
                holder.cartImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_baseline_add_task_24));
            } else {
                holder.cartImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_baseline_add_shopping_cart_24));
            }
        }

    }

    @Override
    public int getItemCount() {
        if(mProducts!=null){
            return mProducts.size();

        }
        return 0;
    }

    public  class ProductViewHolder extends RecyclerView.ViewHolder{
        public final ImageView productImage;
        public final ImageView cartImage;
        public final TextView productName;
        public final TextView price;
        public final ImageButton mDeleteProduct;
        boolean canDelete = mActivity.getIntent().getBooleanExtra(ABLE_TO_DELETE_BOOLEAN, false);


        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imageViewSingleProduct);
            productName = itemView.findViewById(R.id.textViewSingleProductName);
            price = itemView.findViewById(R.id.textViewSingleProductPrice);
            cartImage = itemView.findViewById(R.id.imageViewSingleProductCart);
            mDeleteProduct = itemView.findViewById(R.id.imageButtonDeleteSingleProduct);

            if(mCartSnapshot==null) cartImage.setVisibility(View.INVISIBLE);
            else cartImage.setVisibility(View.VISIBLE);

            if(canDelete) mDeleteProduct.setVisibility(View.VISIBLE);
            else mDeleteProduct.setVisibility(View.GONE);

            onClickListeners();
        }

        private void onClickListeners() {

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Product product = mProducts.get(getAdapterPosition());

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user != null){
                        Intent intent = new Intent(mContext, AboutProductActivity.class);
                        intent.putExtra(CATEGORY_NAME, product.getCategory());
                        intent.putExtra(PRODUCT_ID, product.getProductId());

                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                mActivity,
                                new Pair<>(productImage, VIEW_PRODUCT_IMAGE),
                                new Pair<>(productName,VIEW_PRODUCT_NAME),
                                new Pair<>(price,VIEW_PRODUCT_PRICE)
                        );
                        ActivityCompat.startActivity(mContext, intent, optionsCompat.toBundle());
//                        mContext.startActivity(intent);
                    } else {
                        showLoginRequiredDialog(mContext);
                    }

                }
            };

            productImage.setOnClickListener(onClickListener);
            productName.setOnClickListener(onClickListener);
            price.setOnClickListener(onClickListener);
            itemView.setOnClickListener(onClickListener);

            cartImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                        Product product = mProducts.get(getAdapterPosition());

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                .child(mContext.getString(R.string.node_users))
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(mContext.getString(R.string.field_node_cart))
                                .child(product.getProductId());

                        if(mCartSnapshot.hasChild(product.getProductId())){ //Product is already in cart
                            //remove product from cart
                            reference.removeValue();
                        } else {
                            //add product to cart
                            reference.setValue(product.getCategory());
                        }

                    }


                }
            });

            mDeleteProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(canDelete){
                        Product product = mProducts.get(getAdapterPosition());
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                                .child( mContext.getString(R.string.node_storage_image_category) + product.getCategory() +
                                        "/" + mContext.getString(R.string.field_node_products) + "/" + product.getProductId() +
                                        mContext.getString(R.string.field_storage_product_image));
                        storageReference.delete();

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                                .child(mContext.getString(R.string.firebase_node_categories)).
                                        child(product.getCategory()).
                                        child(mContext.getString(R.string.field_node_products))
                                .child(product.getProductId());
                        databaseReference.removeValue();

                        notifyDataSetChanged();

                    }
                }
            });
        }


    }
}
