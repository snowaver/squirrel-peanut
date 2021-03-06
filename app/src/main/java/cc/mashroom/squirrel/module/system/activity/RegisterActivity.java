/*
 * Copyright 2019 snowaver.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.mashroom.squirrel.module.system.activity;

import  android.content.Intent;
import  android.net.Uri;
import  android.os.Bundle;

import  com.facebook.drawee.view.SimpleDraweeView;
import  com.google.android.material.appbar.AppBarLayout;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.View;
import  android.widget.Button;

import  com.aries.ui.widget.BasisDialog;
import  com.aries.ui.widget.action.sheet.UIActionSheetDialog;
import  com.fasterxml.jackson.core.type.TypeReference;
import  com.irozon.sneaker.Sneaker;

import  net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import  net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import  java.io.File;
import  java.util.List;

import  androidx.core.content.res.ResourcesCompat;

import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.util.StyleUnifier;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.http.Hint;
import  cc.mashroom.squirrel.http.ResponseRetrofit2Callback;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.http.ServiceRegistry;
import  cc.mashroom.hedgehog.module.common.activity.AlbumMediaMultichoiceActivity;
import  cc.mashroom.hedgehog.module.common.activity.CamcorderActivity;
import  cc.mashroom.hedgehog.module.common.activity.ImageCropingActivity;
import  cc.mashroom.squirrel.module.common.services.UserService;
import  cc.mashroom.hedgehog.system.Media;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;

import  okhttp3.MediaType;
import  okhttp3.MultipartBody;
import  okhttp3.RequestBody;

public  class  RegisterActivity   extends  AbstractActivity  implements  View.OnClickListener,UIActionSheetDialog.OnItemClickListener,KeyboardVisibilityEventListener
{
	protected  void  onCreate(        Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		super.setContentView( R.layout.activity_register );

		ObjectUtils.cast(super.findViewById(R.id.portrait_input),SimpleDraweeView.class).setOnClickListener( (view) -> StyleUnifier.unify(new  UIActionSheetDialog.ListIOSBuilder(this).setBackgroundRadius(15).addItem(R.string.camera_take_photo).addItem(R.string.album).setItemsTextSize(18).setCancel(R.string.close).setCancelTextColorResource(R.color.red).setCancelTextSize(18).setItemsMinHeight(DensityUtils.px(this,50)).setPadding(DensityUtils.px(this,10)).setCanceledOnTouchOutside(true).setOnItemClickListener(this).create(),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).show() );

		KeyboardVisibilityEvent.setEventListener(   this , this );

		ObjectUtils.cast(super.findViewById(R.id.register_button),Button.class).setOnClickListener( this );
	}

	protected  File     portrait;

	public  void  onClick(View  registerBtn )
	{
		if( StringUtils.isNoneBlank(ObjectUtils.cast(findViewById(R.id.username),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(findViewById(R.id.nickname),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(findViewById(R.id.password),StyleableEditView.class).getText().toString().trim()) && ObjectUtils.cast(findViewById(R.id.password),StyleableEditView.class).getText().toString().trim().equals(ObjectUtils.cast(findViewById(R.id.password_confirm),StyleableEditView.class).getText().toString().trim()) )
		{
			ServiceRegistry.INSTANCE.get(UserService.class).register(RequestBody.create(MediaType.parse("multipart/form-data"),JsonUtils.toJson(new  HashMap<String,Object>().addEntry("username",ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).getText().toString().trim()).addEntry("password",ObjectUtils.cast(super.findViewById(R.id.password),StyleableEditView.class).getText().toString().trim()).addEntry("nickname",ObjectUtils.cast(super.findViewById(R.id.nickname),StyleableEditView.class).getText().toString().trim()))),portrait == null ? null : MultipartBody.Part.createFormData("portrait",portrait.getName(),RequestBody.create(MediaType.parse("multipart/form-data"),portrait))).enqueue( new  ResponseRetrofit2Callback<Void>(this,true).addResponseHandler(200,(call,response) -> onRegistered()).addHint(new  Hint(601,R.string.register_registered,R.color.white,com.irozon.sneaker.R.drawable.ic_error,R.color.red)) );
		}
		else
		{
			/*
			Toast.makeText(this,R.string.registration_form_error,Toast.LENGTH_LONG).show();
			*/
			super.showSneakerWindow( Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_error,R.string.register_form_error,R.color.white,R.color.red );
		}
	}

	private  void  onRegistered()
	{
		super.showSneakerWindow(Sneaker.with(this).setOnSneakerDismissListener(()-> application().getMainLooperHandler().postDelayed(() -> ContextUtils.finish(RegisterActivity.this),500)),com.irozon.sneaker.R.drawable.ic_success,R.string.register_registered,R.color.white,R.color.limegreen );
	}

	public  void  onVisibilityChanged( boolean  softinputVisible )
	{
		ObjectUtils.cast(super.findViewById(R.id.collapsing_bar_layout),AppBarLayout.class).setExpanded( !softinputVisible,true );
	}

	public  void  onClick( BasisDialog  dialog,View  item,int  i )
	{
		if( i == 0 )
		{
			ActivityCompat.startActivityForResult( this,new  Intent(this,CamcorderActivity.class).putExtra("MEDIA_TYPE",1),0,ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );
		}
		else
		if( i == 1 )
		{
			ActivityCompat.startActivityForResult( this,new  Intent(this,AlbumMediaMultichoiceActivity.class).putExtra("MEDIA_TYPE",1).putExtra("MAX_COUNT",1),   0,ActivityOptionsCompat.makeCustomAnimation(this, R.anim.right_in,R.anim.left_out).toBundle() );
		}
	}

	protected  void  onActivityResult(int  requestCode,int  resultCode,Intent  resultData )
	{
		super.onActivityResult(requestCode,resultCode,resultData);

		if( resultData  != null )
		{
			if( requestCode== 0 )
			{
				ActivityCompat.startActivityForResult(    this,new  Intent(this,ImageCropingActivity.class).putExtra("IMAGE_FILE_PATH",ObjectUtils.cast(resultData.getSerializableExtra("MEDIAS"),new  TypeReference<List<Media>>(){}).get(0).getPath()),1,null );
			}
			else
			if( requestCode== 1 )
			{
				ObjectUtils.cast(super.findViewById(R.id.portrait_input),SimpleDraweeView.class).setImageURI( Uri.fromFile(portrait = new  File(ObjectUtils.cast(resultData.getSerializableExtra("MEDIAS"),Media.class).getPath())) );
			}
		}
	}
}