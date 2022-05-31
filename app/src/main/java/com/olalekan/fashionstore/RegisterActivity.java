package com.olalekan.fashionstore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.olalekan.fashionstore.Models.User;
import com.olalekan.fashionstore.Utility.AllRoundUseful;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText name, emailAddress, contactNumber, password, confirmPassword;
    Button register;
    ProgressBar progressBar;
//    FirebaseDatabase mFirebaseDatabase;
//    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        initialiseViews();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(allFieldsFilled()) {
//                    if(isValid(emailAddress.getText().toString())){ //check email address validity
//                        if(passwordsAreEqual()){//are the passwords equal
//
//                            RegisterNewEmail(emailAddress.getText().toString(),
//                                    password.getText().toString());
//
//
//                        }else showTheUser("Passwords do not match");
//
//                    }else showTheUser("Input a valid email address");
//
//                }else showTheUser("Fill everything up");

                if(!allFieldsFilled()){
                    showTheUser("Fill everything up");
                    return;
                }
                if(!isValid(emailAddress.getText().toString())){ //check email address validity
                    showTheUser("Input a valid email address");
                    return;
                }
                if(!passwordsAreEqual()){ //password are not equal
                    showTheUser("Passwords do not match");
                    return;
                }
                RegisterNewEmail(emailAddress.getText().toString(),
                        password.getText().toString());


            }
        });
    }


    private void RegisterNewEmail(final String Email, String password) {

        closeSoftKeyboard();
        showProgressBar();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(Email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            sendVerificationEmail();

                            User user = new User();
                            user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            user.setName(name.getText().toString());
                            user.setEmail(emailAddress.getText().toString());
                            user.setContact_number(contactNumber.getText().toString());

                            FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.node_users))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            startActivity(new Intent(RegisterActivity.this,
                                                    MainActivity.class));
                                            //TODO: continue from here
                                        }
                                    });
                        }
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AllRoundUseful.showErrorDialog(RegisterActivity.this, e.getMessage());
            }
        });
        hideProgressBar();
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                showTheUser("a email has been sent to" +
                                        emailAddress.getText().toString());
                            }
                            else showTheUser("unable to send the email");
                        }
                    });
        }
    }

    private void initialiseViews() {
        name = (EditText) findViewById(R.id.editTextRegisterName);
        emailAddress = (EditText) findViewById(R.id.editTexRegisterEmailAddress);
        contactNumber = (EditText) findViewById(R.id.editTextRegisterContactNumber);
        password = (EditText) findViewById(R.id.editTextRegisterPassword);
        confirmPassword = (EditText) findViewById(R.id.editTextConfirmRegisterPassword);
        register = (Button) findViewById(R.id.buttonRegister);
        progressBar = (ProgressBar) findViewById(R.id.registerProgressBar);
    }

    public static boolean isValid(String email) //returns true if email is valid
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    private boolean isNotEmpty(String string){
        return !string.equals("");
        //if the string is the same as "" return flase
        //ie isNotEmpty is false it is a empty string
    }

    private boolean allFieldsFilled(){
        if(isNotEmpty(name.getText().toString())
                && isNotEmpty(emailAddress.getText().toString())
                && isNotEmpty(contactNumber.getText().toString())
                && isNotEmpty(password.getText().toString())
                && isNotEmpty(confirmPassword.getText().toString())){
            return true;
        }
        else return false;
    }

    private boolean editTextStringAreSame(EditText editText1, EditText editText2){
        String string1 =editText1.getText().toString();
        String string2 =editText2.getText().toString();
        if(string1.equals(string2)){
            return true;
        }
        else return false;
    }

    private boolean passwordsAreEqual(){
        if(editTextStringAreSame(password, confirmPassword)){
            return true;
        }
        else return false;
    }

    private void showTheUser(String message){
        Toast.makeText(RegisterActivity.this, message,
                Toast.LENGTH_LONG).show();
    }

    private void closeSoftKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}