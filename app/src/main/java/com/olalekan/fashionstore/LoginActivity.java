package com.olalekan.fashionstore;

import static com.olalekan.fashionstore.Utility.AllRoundUseful.GUEST_LOGIN_BOOLEAN;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

//import com.example.mystore.Dialogs.PasswordResetDialog;
//import com.example.mystore.Dialogs.ResendVerificationEmailDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.olalekan.fashionstore.Dialog.PasswordResetDialog;
import com.olalekan.fashionstore.Dialog.ResendVerificationEmailDialog;
import com.olalekan.fashionstore.Utility.AllRoundUseful;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private EditText mEmail;
    private EditText mPassword;
    private Button mSignInButton;
    private TextView mRegisterText, mForgotPasswordText, mResendVerificationEmailText;
    private ProgressBar mProgressBar;
    private TextView mGuestLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initialiseViews();

        setupFirebaseAuth();

        onClicks();
    }

    private void onClicks() {
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if fields are empty
                if(!isEmpty(mEmail.getText().toString())
                        && !isEmpty(mPassword.getText().toString())){
                    showProgressBar();

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(
                            mEmail.getText().toString(), mPassword.getText().toString()
                    ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            hideProgressBar();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            AllRoundUseful.showErrorDialog(LoginActivity.this, e.getMessage());
                            hideProgressBar();
                        }
                    });
                }
                else {
                    AllRoundUseful.showErrorDialog(LoginActivity.this, "Please fill all the fields.");
                }
            }
        });

        mRegisterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,
                        RegisterActivity.class));
            }
        });

        mForgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PasswordResetDialog dialog = new PasswordResetDialog();
                dialog.show(getSupportFragmentManager(), "dialog_password_reset");
            }
        });

        mResendVerificationEmailText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResendVerificationEmailDialog dialog = new ResendVerificationEmailDialog();
                dialog.show(getSupportFragmentManager(), "dialog_resend_verification_email");
            }
        });

        mGuestLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra(GUEST_LOGIN_BOOLEAN, true);
                startActivity(intent);
            }
        });
    }

    private void showTheUser(String message){
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }
    private void hideProgressBar(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void setupFirebaseAuth() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){

                    if(user.isEmailVerified()){
                        showTheUser("Authenticated with" + user.getEmail());

                        Intent intent = new Intent(LoginActivity.this,
                                CategoryListActivity.class);
                        //clear all tasks in the back stack
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);
                        finish();
                    }

                    else {
                        AllRoundUseful.showErrorDialog(LoginActivity.this,
                                "Your Email has not been verified\n" +
                                        "Please check your inbox to verify your email.\nThank you!");
                    }
                }
                else {
                    showTheUser("User signed out");
                }
            }
        };
    }

    private void initialiseViews() {
        mEmail = (EditText) findViewById(R.id.login_email_address);
        mPassword = (EditText) findViewById(R.id.login_password);
        mSignInButton = (Button) findViewById(R.id.login_signin_button);
        mRegisterText = (TextView) findViewById(R.id.login_register_text_view);
        mForgotPasswordText = (TextView) findViewById(R.id.login_forgot_password_text_view);
        mResendVerificationEmailText = (TextView)
                findViewById(R.id.login_send_verification_email_text_view);
        mProgressBar = (ProgressBar) findViewById(R.id.login_progressBar);

        mGuestLogin = findViewById(R.id.textViewGuestLogin);
    }

    private boolean isEmpty(String string){
        return string.equals("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthStateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        }
    }
}