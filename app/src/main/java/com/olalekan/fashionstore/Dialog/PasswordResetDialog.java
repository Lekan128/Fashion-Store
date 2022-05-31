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
import com.google.firebase.auth.FirebaseAuth;
import com.olalekan.fashionstore.R;

public class PasswordResetDialog extends DialogFragment {

    private EditText mEmail;

    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_password_reset, container, false);
        mEmail = (EditText) view.findViewById(R.id.reset_password_dialog_email);
        mContext = getActivity();

        TextView confirmDialog = (TextView) view.findViewById(R.id.reset_password_dialod_confirm);
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEmpty(mEmail.getText().toString())){
                    sendPasswordResetEmail(mEmail.getText().toString());
                }
            }
        });

        return view;
    }

    private void sendPasswordResetEmail(final String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            showTheUser("Password Reset Link Sent To " + email);
                            getDialog().dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showTheUser(e.getMessage());
                    }
                });
    }

    private void showTheUser(String message){
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isEmpty(String string){
        return string.equals("");
    }
}
