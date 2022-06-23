package com.olalekan.fashionstore.Fragments;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.hideProgressBarAndEnableTouch;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.logoutIfUserIsNull;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.showErrorDialog;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.olalekan.fashionstore.Models.Product;
import com.olalekan.fashionstore.R;
import com.olalekan.fashionstore.Utility.DatabaseHelper;
import com.olalekan.fashionstore.Utility.DatabaseViewModel;
import com.olalekan.fashionstore.Utility.ProductRecyclerAdapter;

import java.util.ArrayList;
import java.util.Objects;


public class CartFragment extends Fragment {

    private ArrayList<String> mCategories;
    private ArrayList<String> mProductId;
    private ArrayList<Product> mProducts = new ArrayList<>();
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

        //TODO: REMOVE THIS
        logoutIfUserIsNull(getActivity());

        initialiseViews(view);



        DatabaseViewModel databaseViewModel = new ViewModelProvider(this).get(DatabaseViewModel.class);
        DatabaseHelper databaseHelper = new DatabaseHelper();

        final Observer<DataSnapshot> cartSnapshotObserver = new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
//                    mCartSnapshot = dataSnapshot;
                    ArrayList<DatabaseHelper.CategoryNameAndProductId> categoryNameAndProductIds
                            = databaseHelper.getCategoryAndIdFromSnapshot(dataSnapshot);

                    //AFTER cart snapshot changes the listener in database view model will
                    // first get the category name and id with database helper and then
                    // call get products from database which will reload the product in the
                    // product observer

                    final Observer<ArrayList<Product>> productsObserver = new Observer<ArrayList<Product>>() {
                        @Override
                        public void onChanged(ArrayList<Product> products) {
                            if(products.isEmpty()) {
                                mCartEmptyText.setVisibility(View.VISIBLE);
                                mCardSummary.setVisibility(View.INVISIBLE);
                            }
                            else {
                                mProducts = products;
                                mCartEmptyText.setVisibility(View.GONE);
                                setUpRecyclerView(products, dataSnapshot);
                                setUpSummary(products);
                                mCardSummary.setVisibility(View.VISIBLE);

                            }
                        }
                    };

                    databaseViewModel.getCartProducts(categoryNameAndProductIds).observe(getActivity(), productsObserver);

                }else {
                     mCartEmptyText.setVisibility(View.VISIBLE);
                     mProducts = new ArrayList<>();
                     mCardSummary.setVisibility(View.INVISIBLE);
                     setUpRecyclerView(mProducts, dataSnapshot);
                     hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
                }

            }
        };

        databaseViewModel.getCartSnapshot().observe(getActivity(), cartSnapshotObserver);
        databaseViewModel.getCurrentUser();

        mBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                DataSnapshot = current
//                ArrayList<DatabaseHelper.CategoryNameAndProductId> categoryNameAndProductIds =
//                        databaseHelper.getCategoryAndIdFromSnapshot(Objects.requireNonNull(databaseViewModel.getCartSnapshot().getValue()));
//                databaseViewModel.getCartProducts(databaseHelper.getCategoryAndIdFromSnapshot(Objects.requireNonNull(databaseViewModel.getCartSnapshot().getValue())));

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
//        if(mProducts.isEmpty()) mCartEmptyText.setVisibility(View.GONE);

//        if (mProducts.isEmpty()) mCardSummary.setVisibility(View.INVISIBLE);
//        getUsersCart();
    }

    private void setUpSummary(ArrayList<Product> products) {
        StringBuilder concatenatePrice = new StringBuilder(products.get(0).getPrice());
        double total = Double.parseDouble(products.get(0).getPrice());

        for(int i = 1; i<products.size(); ++i){
            concatenatePrice.append(" + ").append(products.get(i).getPrice());
            total = total + Double.parseDouble(products.get(i).getPrice());
        }
        String summary = concatenatePrice.toString();

        mTotalPrice.setText(String.valueOf(total));

        mTextCartSummary.setText(summary);
    }


    private void setUpRecyclerView(ArrayList<Product> products, DataSnapshot cartSnapshot) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        ProductRecyclerAdapter adapter = new ProductRecyclerAdapter(getActivity(), products, cartSnapshot);
        mCartRecyclerView.setLayoutManager(layoutManager);
        mCartRecyclerView.setAdapter(adapter);
    }
}
