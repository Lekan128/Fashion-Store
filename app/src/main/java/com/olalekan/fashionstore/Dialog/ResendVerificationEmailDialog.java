package com.olalekan.fashionstore.Dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.olalekan.fashionstore.R;

public class ResendVerificationEmailDialog extends DialogFragment {

    private Context mContext;
    private View mView;
    private EditText mEmail;
    private EditText mPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.dialog_resend_verification_email, container, false);
        mEmail = (EditText) mView.findViewById(R.id.resend_email_dialog_confirm_email);
        mPassword = (EditText) mView.findViewById(R.id.resend_email_dialog_confirm_password);
        mContext = getContext();

        onClicks();

        return mView;
    }

    private void onClicks() {
        TextView confirm = (TextView) mView.findViewById(R.id.resend_email_dialog_verification_confirm);
        TextView cancel = (TextView) mView.findViewById(R.id.resend_email_dialog_verification_cancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEmpty(mEmail.getText().toString())
                        && !isEmpty(mPassword.getText().toString())){

                    authenticateAndResendEmail(mEmail.getText().toString(), mPassword.getText().toString());
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }

    private void authenticateAndResendEmail(String email, String password) {
        AuthCredential credential = EmailAuthProvider
                .getCredential(email, password);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    sendVerificationEmail();
                    FirebaseAuth.getInstance().signOut();
                    if(getDialog() != null) getDialog().dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showTheUser(e.getMessage());
                if(getDialog() != null) getDialog().dismiss();
            }
        });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                showTheUser("Email verification sent");
                            }
                            else {
                                showTheUser("email was not sent");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showTheUser(e.getMessage());
                }
            });
        }
    }

    private Boolean isEmpty(String string){
        return string.equals("");
    }

    private void showTheUser(String message){
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }
}
