package com.olalekan.fashionstore;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.ABLE_TO_DELETE_BOOLEAN;
import static com.olalekan.fashionstore.Utility.AllRoundUseful.logoutIfUserIsNull;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.olalekan.fashionstore.Models.User;
import com.olalekan.fashionstore.Utility.AllRoundUseful;

public class ProfileActivity extends AppCompatActivity {

    //TODO:Remove

    EditText name, contactNumber;
    TextView emailAddress;
    Button done;
    ProgressBar mProgressBar;
    private User mUser;
    private DatabaseReference mReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        logoutIfUserIsNull(this);

        initialiseViews();
        Toolbar mainToolBar = (Toolbar) findViewById(R.id.includeProfileActionBar);
        setSupportActionBar(mainToolBar);

        AllRoundUseful.showProgressBarAndDisableTouch(ProfileActivity.this, mProgressBar);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mReference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.node_users))
                .child(firebaseUser.getUid());

        mReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AllRoundUseful.hideProgressBarAndEnableTouch(ProfileActivity.this, mProgressBar);
                mUser = snapshot.getValue(User.class);
                inputValues();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                AllRoundUseful.hideProgressBarAndEnableTouch(ProfileActivity.this,mProgressBar);
                AllRoundUseful.showErrorDialog(ProfileActivity.this, error.getMessage());
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUser.getName().equals(name.getText().toString())
                        && mUser.getContact_number().equals(contactNumber.getText().toString())){
                    startActivity(new Intent(ProfileActivity.this, CategoryListActivity.class));
                } else {
                    mUser.setName(name.getText().toString());
                    mUser.setContact_number(contactNumber.getText().toString());
                    changeValuesInDatabase();
                    onBackPressed();
                }
            }
        });
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

    private void initialiseViews() {
        name = (EditText) findViewById(R.id.editTextName);
        emailAddress = (TextView) findViewById(R.id.textViewEmailAddress);
        contactNumber = (EditText) findViewById(R.id.editTextContactNumber);
        done = (Button) findViewById(R.id.buttonProfileDone);
        mProgressBar = (ProgressBar) findViewById(R.id.profileProgressBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu_profile, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_profile_sign_out:
                signOut();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}