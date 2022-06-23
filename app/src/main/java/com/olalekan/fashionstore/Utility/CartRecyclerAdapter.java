package com.olalekan.fashionstore.Utility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.olalekan.fashionstore.Models.Product;
import com.olalekan.fashionstore.R;

import java.util.ArrayList;

public class CartRecyclerAdapter extends RecyclerView.Adapter<CategoryRecyclerAdapter.CategoryViewHolder>{

    private Context mContext;
    private ArrayList<Product> mProducts;
    private LayoutInflater mLayoutInflater;


    public CartRecyclerAdapter(Context context, ArrayList<Product> products){
        mContext = context;
        mProducts = products;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public CategoryRecyclerAdapter.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.single_product, parent,false);
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryRecyclerAdapter.CategoryViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class CartViewHolder extends RecyclerView.ViewHolder{

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
