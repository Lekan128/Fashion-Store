package com.olalekan.fashionstore.Utility;

import android.content.Context;

import com.olalekan.fashionstore.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class UniversalImageLoader {
    private final String TAG = "UNIVERSAL_IMAGE_LOADER";
    private static final int loadingImage = R.drawable.loading;
        private static final int failLoadingImage = R.drawable.ic_baseline_priority_high_24;
    private static final int defaultImage = R.drawable.ic_baseline_checkroom_24;
    private Context mContext;

    public UniversalImageLoader(Context context){
        this.mContext = context;
    }

    public ImageLoaderConfiguration getConfig(){
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(loadingImage)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(failLoadingImage)
                .cacheOnDisk(true).cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer((new FadeInBitmapDisplayer(300))).build();


        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 * 1024 * 1024)
                .build();

        return configuration;
    }
}
