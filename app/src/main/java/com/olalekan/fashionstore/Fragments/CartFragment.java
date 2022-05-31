package com.olalekan.fashionstore.Fragments;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.hideProgressBarAndEnableTouch;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.logoutIfUserIsNull;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showErrorDialog;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showProgressBarAndDisableTouch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.olalekan.fashionstore.CartActivity;
import com.olalekan.fashionstore.Models.Product;
import com.olalekan.fashionstore.R;
import com.olalekan.fashionstore.Utility.ProductRecyclerAdapter;

import java.util.ArrayList;

public class CartFragment extends Fragment {

    private ArrayList<String> mCategories;
    private ArrayList<String> mProductId;
    private ArrayList<Product> mProducts;
    private RecyclerView mCartRecyclerView;
    private DataSnapshot mCartSnapshot;
    private ProgressBar mProgressBar;
    private Button mBuy;
    private TextView mCartEmptyText;
    private TextView mTextCartSummary;
    private CardView mCardSummary;
    private TextView mTotalPrice;

    public CartFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_cart, container, false);

        logoutIfUserIsNull(getActivity());

        initialiseViews(view);

        mBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mProducts.isEmpty()){

                }
            }
        });

        return view;
    }

    private void initialiseViews(View view) {
        mCartRecyclerView = view.findViewById(R.id.cartRecyclerView);
        mProgressBar = view.findViewById(R.id.progressBarCart);
        mBuy = view.findViewById(R.id.buttonCartBuy);
        mCartEmptyText = view.findViewById(R.id.textViewCartEmpty);
        mTextCartSummary = view.findViewById(R.id.textViewCartSummary);
        mTotalPrice = view.findViewById(R.id.textViewCartTotalPrice);
        mCardSummary = view.findViewById(R.id.cardViewCartSummary);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCartEmptyText.setVisibility(View.GONE);
        mCardSummary.setVisibility(View.INVISIBLE);
        getUsersCart();
    }

    private void getUsersCart() {
        showProgressBarAndDisableTouch(getActivity(), mProgressBar);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_node_cart));

        //TODO: Change it to addListenerForSingleValueEvent if adding to cart causes problems
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    mCategories = new ArrayList<>();
                    mProductId = new ArrayList<>();
                    mCartSnapshot = snapshot;
//                setUpRecyclerView();
                    for(DataSnapshot cartSnapshot : snapshot.getChildren()){
                        mCategories.add(cartSnapshot.getValue(String.class));
                        mProductId.add(cartSnapshot.getKey());
                    }

                    getProductsFromDatabase();

                }else {
                    mCartEmptyText.setVisibility(View.VISIBLE);
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

    private void getProductsFromDatabase() {
        mProducts = new ArrayList<>();
        for(int i =0; i<mCategories.size(); i++){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.firebase_node_categories)).
                    child(mCategories.get(i)).child(getString(R.string.field_node_products))
                    .child(mProductId.get(i));

            int counter = i;
            int lastCount = mCategories.size()-1;
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) mProducts.add(snapshot.getValue(Product.class));
                    else removeProductFromDatabase(mProductId.get(counter));

                    if(counter == lastCount){
                        if(mProducts.isEmpty()) {
                            mCartEmptyText.setVisibility(View.VISIBLE);
                            mCardSummary.setVisibility(View.INVISIBLE);
                        }
                        else {
                            mCartEmptyText.setVisibility(View.GONE);
                            setUpRecyclerView();
                            setUpSummary();
                            mCardSummary.setVisibility(View.VISIBLE);

                        }
                    }
                    hideProgressBarAndEnableTouch(getActivity(), mProgressBar);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
                    showErrorDialog(getContext(),error.getMessage());
                }
            });

        }

    }

    private void setUpSummary() {
        StringBuilder concatenatePrice = new StringBuilder(mProducts.get(0).getPrice());
        double total = Double.parseDouble(mProducts.get(0).getPrice());

        for(int i = 1; i<mProducts.size(); ++i){
            concatenatePrice.append(" + ").append(mProducts.get(i).getPrice());
            total = total + Double.parseDouble(mProducts.get(i).getPrice());
        }
        String summary = concatenatePrice.toString();

        mTotalPrice.setText(String.valueOf(total));

        mTextCartSummary.setText(summary);
    }

    private void removeProductFromDatabase(String productId) {
        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_node_cart)).child(productId).removeValue();
    }

    private void setUpRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        ProductRecyclerAdapter adapter = new ProductRecyclerAdapter(getActivity(), mProducts, mCartSnapshot);
        mCartRecyclerView.setLayoutManager(layoutManager);
        mCartRecyclerView.setAdapter(adapter);
    }
}
