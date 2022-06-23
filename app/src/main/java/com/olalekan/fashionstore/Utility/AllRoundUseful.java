package com.olalekan.fashionstore.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.olalekan.fashionstore.LoginActivity;
import com.olalekan.fashionstore.R;

public class AllRoundUseful {
    //PERMISSION REQUEST CODE
    public static final int STORAGE_PERMISSION_REQUEST_CODE = 999;

    //Intent Extra
    public static final String CATEGORY_NAME = "com.olalekan.fashionstore.categoryName";
    public static final String PRODUCT_ID = "com.olalekan.fashionstore.productId";
    public static final String GUEST_LOGIN_BOOLEAN = "com.olalekan.fashionstore.guestLogin";
    public static final String ABLE_TO_DELETE_BOOLEAN = "com.olalekan.fashionstore.ableToDeleteBoolean";

    //PARCELABLE KEY
    public static final String CATEGORIES = "com.olalekan.fashionstore.categories";
    public static final String PRODUCTS = "com.olalekan.fashionstore.products";


    //Transition Name
    public static final String VIEW_PRODUCT_IMAGE = "product:detail:image";
    public static final String VIEW_PRODUCT_NAME = "product:detail:name";
    public static final String VIEW_PRODUCT_PRICE = "product:detail:price";




    public static final int CHOOSE_IMAGE_REQUEST_CODE = 000;

    //mb
    public static final double MB_THRESHOLD = 5.0;
    public static final double MB = 1000000.0;

    public static void hideProgressBarAndEnableTouch(Activity activity, ProgressBar progressBar){
        if(activity!=null){
            progressBar.setVisibility(View.GONE);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        }
    }

    public static void showProgressBarAndDisableTouch(Activity activity, ProgressBar progressBar){
        if (activity!= null){
            progressBar.setVisibility(View.VISIBLE);
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        }

    }

    public static void closeSoftKeyboard(Activity activity){
        if(activity.getCurrentFocus()!=null){
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),0);
        }

    }

    public static void logoutIfUserIsNull(Activity activity){
        FirebaseUser User = FirebaseAuth.getInstance().getCurrentUser();

        if(User == null){
//            AllRoundUseful.showErrorDialog(activity, "You have to be logged in");

            Intent loginIntent = new Intent(activity, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(loginIntent);
            activity.finish();

        }

    }

    public static void showLoginRequiredDialog(Context context) {
        new AlertDialog.Builder(context).setMessage("You need to be logged in to continue")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent loginIntent = new Intent(context, LoginActivity.class);
                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Activity activity = (Activity) context;
                        activity.startActivity(loginIntent);
                    }
                }).show();
    }

    public static void showErrorDialog(Context context, String message){
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }


}
