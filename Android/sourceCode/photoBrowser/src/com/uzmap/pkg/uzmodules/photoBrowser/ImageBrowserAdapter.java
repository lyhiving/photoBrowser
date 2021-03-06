/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.photoBrowser;

import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.photoBrowser.view.largeImage.LargeImageView;
import com.uzmap.pkg.uzmodules.photoBrowser.view.largeImage.factory.FileBitmapDecoderFactory;
import com.uzmap.pkg.uzmodules.photoBrowser.view.largeImage.factory.InputStreamBitmapDecoderFactory;

public class ImageBrowserAdapter extends PagerAdapter{
	
	private ArrayList<String> mImagePaths;
	private Context mContext;

	private UZModuleContext mUZContext;
	
	private ViewGroup mViewContainer;
	
	private boolean zoomEnable = true;
	
	public void setZoomEnable(boolean zoomable){
		this.zoomEnable = zoomable;
	}
	
	private String mPlaceholdImg;
	
	public void setPlaceholdImg(String path){
		this.mPlaceholdImg = path;
	}

	public ImageBrowserAdapter(Context context, UZModuleContext uzContext, ArrayList<String> imagePaths, ImageLoader imageLoader) {
		this.mImagePaths = imagePaths;
		this.mContext = context;
		this.mUZContext = uzContext;
	}

	@Override
	public int getCount() {
		return mImagePaths.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	public ViewGroup getViewContainer(){
		return this.mViewContainer;
	}
	
	@SuppressLint("NewApi")
	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		
		mViewContainer = container;
		
		int item_view_id = UZResourcesIDFinder.getResLayoutID("photo_browser_item_layout");
		View itemView = View.inflate(mContext, item_view_id, null);
		
		itemView.setTag(position);
		
		int photo_view_id = UZResourcesIDFinder.getResIdID("photoView");
		final LargeImageView imageView = (LargeImageView)itemView.findViewById(photo_view_id);
		
		imageView.setCanZoom(this.zoomEnable);
		
		int load_progress_id = UZResourcesIDFinder.getResIdID("loadProgress");
		final ProgressBar progress = (ProgressBar)itemView.findViewById(load_progress_id);
		progress.setTag(position);
		
		String imagePath = mImagePaths.get(position);
		
		
		if(!TextUtils.isEmpty(imagePath)){
			if(imagePath.startsWith("http")){
				new ImageDownLoader(new ImageDownLoader.DownLoadListener() {
					
					@Override
					public void onStart() {
						progress.setVisibility(View.VISIBLE);
						
						if(!TextUtils.isEmpty(mPlaceholdImg)){
							if(mPlaceholdImg.startsWith("file://")){
								try {
									imageView.setImage(new InputStreamBitmapDecoderFactory(mContext.getAssets().open(mPlaceholdImg.replace("file:///android_asset/", ""))));
									Log.i("debug", "== 1" + mPlaceholdImg.replace("file:///android_asset/", ""));
								} catch (IOException e) {
									e.printStackTrace();
									
									imageView.setImage(new FileBitmapDecoderFactory(mPlaceholdImg.replaceAll("file://", "")));
									Log.i("debug", "== 2" + mPlaceholdImg.replaceAll(".+widget", "widget"));
									
								}
							} else {
								imageView.setImage(new FileBitmapDecoderFactory(mPlaceholdImg));
							}
							
						}
					}
					
					@Override
					public void onFailed(){
						PhotoBrowser.callback(mUZContext, PhotoBrowser.EVENT_TYPE_LOADFAILED, (Integer)progress.getTag());
					}
					
					@Override
					public void onFinish(String savePath) {
						imageView.setImage(new FileBitmapDecoderFactory(savePath));
						progress.setVisibility(View.GONE);
						
						PhotoBrowser.callback(mUZContext, PhotoBrowser.EVENT_TYPE_LOADSUCCESSED, (Integer)progress.getTag());
					}
					
					@Override
					public void onCancel() {
						progress.setVisibility(View.GONE);
					}
				}).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imagePath);
				
			} else {
				try{
					imageView.setImage(new FileBitmapDecoderFactory(imagePath));
					progress.setVisibility(View.GONE);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		imageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				PhotoBrowser.callback(mUZContext, PhotoBrowser.EVENT_TYPE_CLICK, position);
			}
		});
		
		imageView.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				PhotoBrowser.callback(mUZContext, PhotoBrowser.EVENT_TYPE_LONG_CLICK, position);
				return false;
			}
		});
		container.addView(itemView);
		return itemView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}
	
	public ArrayList<String> getDatas(){
		return mImagePaths;
	}
	
}
