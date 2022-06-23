package com.olalekan.fashionstore.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private String productId;
    private String productName;
    private String category;
    private String imageLink ="";
    private String price;
    private String description;
    private String size;
    private String features;

    public Product() {
    }

    protected Product(Parcel in) {
        productId = in.readString();
        productName = in.readString();
        category = in.readString();
        imageLink = in.readString();
        price = in.readString();
        description = in.readString();
        size = in.readString();
        features = in.readString();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(productId);
        parcel.writeString(productName);
        parcel.writeString(category);
        parcel.writeString(imageLink);
        parcel.writeString(price);
        parcel.writeString(description);
        parcel.writeString(size);
        parcel.writeString(features);
    }
}
