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
package cc.mashroom.squirrel.module.home.activity;

import  android.app.Activity;
import  android.content.Intent;
import  android.os.Bundle;
import  android.view.View;
import  android.widget.AdapterView;
import  android.widget.Button;
import  android.widget.ListView;
import  android.widget.TextView;

import  com.google.android.material.bottomsheet.BottomSheetDialog;

import  java.util.ArrayList;
import  java.util.List;
import  java.util.Locale;

import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  cc.mashroom.hedgehog.system.LocaleChangeEventDispatcher;
import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.common.services.UserService;
import  cc.mashroom.squirrel.module.home.adapters.SystemSettingsLanguageAdapter;
import  cc.mashroom.squirrel.module.system.activity.LoginActivity;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.util.LocaleUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.stream.Stream;
import  cn.refactor.library.SmoothCheckBox;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;
import  retrofit2.Call;
import  retrofit2.Response;

public  class  SystemSettingsActivity  extends  AbstractActivity  implements  SmoothCheckBox.OnCheckedChangeListener,AdapterView.OnItemClickListener,LocaleChangeEventDispatcher.LocaleChangeListener
{
	@Accessors( chain=true )
	@Setter
	@Getter
	private  BottomSheetDialog  languagesBottomSheetDialog;

	public  void  onCheckedChanged( SmoothCheckBox  smoothCheckbox,boolean  isChecked )
	{
		Locale  locale = ObjectUtils.cast(ObjectUtils.cast(smoothCheckbox.getParent(),View.class).findViewById(R.id.name),TextView.class).getText().toString().trim().equals("ENGLISH") ? Locale.ENGLISH : Locale.CHINESE;

		LocaleUtils.change( this, locale.toLanguageTag() );

		this.languagesBottomSheetDialog.hide();

		ObjectUtils.cast(super.findViewById(R.id.language_selector),StyleableEditView.class).setText( ObjectUtils.cast(ObjectUtils.cast(smoothCheckbox.getParent(),View.class).findViewById(R.id.name),TextView.class).getText().toString() );
	}

	public  void  onItemClick( AdapterView  parent, View  view,int  position,long  id )
	{
		view.setBackgroundResource(    R.color.lightgray );

		ObjectUtils.cast(view.findViewById(R.id.checkbox),SmoothCheckBox.class).setChecked( true, true );
	}

	public  void  onChange(    Locale  locale )
	{
		ObjectUtils.cast(super.findViewById(R.id.language_selector).findViewById(cc.mashroom.hedgehog.R.id.title),TextView.class).setText(    R.string.language );

		ObjectUtils.cast(super.findViewById(R.id.header_bar).findViewById(cc.mashroom.hedgehog.R.id.title),TextView.class).setText( R.string.settings );

		ObjectUtils.cast(super.findViewById(R.id.change_password_button).findViewById(R.id.title),TextView.class).setText( super.getString( R.string.password ) );

		ObjectUtils.cast(super.findViewById(R.id.logout_button),Button.class).setText( R.string.logout );

		ObjectUtils.cast(this.languagesBottomSheetDialog.findViewById(R.id.title),TextView.class).setText( R.string.language );
	}

	protected  void  onDestroy()
	{
		super.onDestroy();

		LocaleChangeEventDispatcher.removeListener( this );
	}

	protected  void  onCreate( Bundle  savedInstanceState )
	{
		LocaleChangeEventDispatcher.addListener(    this );

		super.onCreate(  savedInstanceState );

		super.setContentView(  R.layout.activity_system_settings );

		super.findViewById(R.id.logout_button).setOnClickListener(  (view) -> application().getSquirrelClient().disconnect() );

		(this.languagesBottomSheetDialog = new  BottomSheetDialog(this)).setContentView(       R.layout.activity_system_settings_language_bottomsheet );

		this.languagesBottomSheetDialog.setCanceledOnTouchOutside( true );

		ObjectUtils.cast(super.findViewById(R.id.language_selector),StyleableEditView.class).setOnClickListener( (selector) -> this.languagesBottomSheetDialog.show() );

		ObjectUtils.cast(languagesBottomSheetDialog.findViewById(R.id.languages),ListView.class).setOnItemClickListener(this );

		ObjectUtils.cast(this.languagesBottomSheetDialog.findViewById(R.id.languages),ListView.class).setAdapter( new  SystemSettingsLanguageAdapter(this,this) );

		ObjectUtils.cast(super.findViewById(R.id.language_selector),StyleableEditView.class).setText( ObjectUtils.cast(ObjectUtils.cast(this.languagesBottomSheetDialog.findViewById(R.id.languages),ListView.class).getAdapter(),SystemSettingsLanguageAdapter.class).getListener().getChecked().get() );
	}
}