package com.olalekan.fashionstore.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Categories implements Parcelable {
    private String imageLink = "";
    private String categoryName = "";
    private ArrayList<Product> mProducts;

    public Categories() {
    }

    protected Categories(Parcel in) {
        imageLink = in.readString();
        categoryName = in.readString();
        mProducts = in.createTypedArrayList(Product.CREATOR);
    }

    public static final Creator<Categories> CREATOR = new Creator<Categories>() {
        @Override
        public Categories createFromParcel(Parcel in) {
            return new Categories(in);
        }

        @Override
        public Categories[] newArray(int size) {
            return new Categories[size];
        }
    };

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public ArrayList<Product> getTheProducts() {
        return mProducts;
    }

    public void setTheProducts(ArrayList<Product> products) {
        mProducts = products;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imageLink);
        parcel.writeString(categoryName);
        parcel.writeTypedList(mProducts);
    }
}
