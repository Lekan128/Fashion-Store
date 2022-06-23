package com.olalekan.fashionstore.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.olalekan.fashionstore.CategoryListActivity;
import com.olalekan.fashionstore.Models.User;
import com.olalekan.fashionstore.ProfileActivity;
import com.olalekan.fashionstore.R;
import com.olalekan.fashionstore.Utility.AllRoundUseful;
import com.olalekan.fashionstore.Utility.DatabaseViewModel;

public class ProfileFragment extends Fragment {

    EditText name, contactNumber;
    TextView emailAddress;
    Button done;
    ProgressBar mProgressBar;
    private User mUser;
    private DatabaseReference mReference;

    public ProfileFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        initialiseViews(view);
        AllRoundUseful.showProgressBarAndDisableTouch(getActivity(), mProgressBar);

//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

//        mReference = FirebaseDatabase.getInstance().getReference()
//                .child(getString(R.string.node_users))
//                .child(firebaseUser.getUid());
//
//        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
//
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                AllRoundUseful.hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
//                mUser = snapshot.getValue(User.class);
//                inputValues();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                AllRoundUseful.hideProgressBarAndEnableTouch(getActivity(),mProgressBar);
//                AllRoundUseful.showErrorDialog(getContext(), error.getMessage());
//            }
//        });

        DatabaseViewModel databaseViewModel = new ViewModelProvider(this).get(DatabaseViewModel.class);

        final Observer<User> userObserver = new Observer<User>() {
            @Override
            public void onChanged(User user) {
                mUser = user;
                AllRoundUseful.hideProgressBarAndEnableTouch(getActivity(), mProgressBar);
                inputValues();
            }
        };

        databaseViewModel.getCurrentUser().observe(getViewLifecycleOwner(), userObserver);


        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUser.getName() == name.getText().toString()
                        && mUser.getContact_number() == contactNumber.getText().toString() ){
                    startActivity(new Intent(getContext(), CategoryListActivity.class));
                } else {
                    mUser.setName(name.getText().toString());
                    mUser.setContact_number(contactNumber.getText().toString());
                    changeValuesInDatabase();
                    getActivity().onBackPressed();
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void changeValuesInDatabase() {
        mReference.child(getString(R.string.field_name)).setValue(mUser.getName());
        mReference.child(getString(R.string.field_contact_number)).setValue(mUser.getContact_number());
    }

    private void inputValues() {
        name.setText(mUser.getName());
        emailAddress.setText(mUser.getEmail());
        contactNumber.setText(mUser.getContact_number());
    }

    private void initialiseViews(View view) {
        name = (EditText) view.findViewById(R.id.editTextName);
        emailAddress = (TextView) view.findViewById(R.id.textViewEmailAddress);
        contactNumber = (EditText) view.findViewById(R.id.editTextContactNumber);
        done = (Button) view.findViewById(R.id.buttonProfileDone);
        mProgressBar = (ProgressBar) view.findViewById(R.id.profileProgressBar);
    }
}
