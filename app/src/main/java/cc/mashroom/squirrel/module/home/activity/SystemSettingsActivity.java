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

import  android.os.Bundle;
import  android.view.View;
import  android.widget.AdapterView;
import  android.widget.Button;
import  android.widget.ListView;
import  android.widget.TextView;

import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.google.android.material.bottomsheet.BottomSheetDialog;

import  java.util.Locale;

import  androidx.core.content.res.ResourcesCompat;

import  cc.mashroom.hedgehog.system.LocaleChangeEventDispatcher;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.hedgehog.util.StyleUnifier;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.module.home.adapters.SystemSettingsLanguageAdapter;
import cc.mashroom.squirrel.parent.AbstractLifecycleEventListenerActivity;
import  cc.mashroom.squirrel.util.LocaleUtils;
import  cc.mashroom.util.ObjectUtils;
import  cn.refactor.library.SmoothCheckBox;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;

public  class  SystemSettingsActivity  extends AbstractLifecycleEventListenerActivity implements  SmoothCheckBox.OnCheckedChangeListener,AdapterView.OnItemClickListener,LocaleChangeEventDispatcher.LocaleChangeListener
{
	@Accessors(   chain = true )
	@Setter
	@Getter
	private  BottomSheetDialog  languagesBottomSheetDialog;
	@Accessors(   chain = true )
	@Setter
	private  UIProgressDialog   progressDialog;

	public  void  onCheckedChanged( SmoothCheckBox  smoothCheckbox,boolean  isChecked )
	{
		Locale  locale = ObjectUtils.cast(ObjectUtils.cast(smoothCheckbox.getParent(),View.class).findViewById(R.id.name),TextView.class).getText().toString().trim().equals("ENGLISH") ? Locale.ENGLISH : Locale.CHINESE;

		LocaleUtils.change(this , locale.toLanguageTag() );

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

		ObjectUtils.cast(this.languagesBottomSheetDialog.findViewById(R.id.title),TextView.class).setText(     R.string.language );
	}

	@Override
	public  void  onLogoutComplete( int  code,int  reason )
	{
		super.onLogoutComplete( code, reason );

		super.application().getMainLooperHandler().post( () -> progressDialog.hide() );
	}

	protected  void  onDestroy()
	{
		this.languagesBottomSheetDialog.cancel(/*CANCEL*/);

		progressDialog.cancel();

		super.onDestroy();

		LocaleChangeEventDispatcher.removeListener( this );
	}

	protected  void  onCreate( Bundle  savedInstanceState )
	{
		LocaleChangeEventDispatcher.addListener(    this );

		super.onCreate(   savedInstanceState );

		super.setContentView(  R.layout.activity_system_settings );

		super.findViewById(R.id.logout_button).setOnClickListener( (view) -> {this.progressDialog.show();super.application().getSquirrelClient().disconnect();} );

		this.setLanguagesBottomSheetDialog(new  BottomSheetDialog(this)).getLanguagesBottomSheetDialog().setContentView( R.layout.activity_system_settings_language_bottomsheet );

		this.languagesBottomSheetDialog.setCanceledOnTouchOutside( true );

		ObjectUtils.cast(super.findViewById(R.id.language_selector),StyleableEditView.class).setOnClickListener( (selector) -> this.languagesBottomSheetDialog.show() );

		this.setProgressDialog( StyleUnifier.unify(new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCanceledOnTouchOutside(false).create(),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).setWidth(DensityUtils.px(this,220)).setHeight(DensityUtils.px(this,150)) );

		ObjectUtils.cast(this.languagesBottomSheetDialog.findViewById(R.id.languages),ListView.class).setOnItemClickListener(this);

		ObjectUtils.cast(this.languagesBottomSheetDialog.findViewById(R.id.languages),ListView.class).setAdapter( new  SystemSettingsLanguageAdapter(this,this) );

		ObjectUtils.cast(super.findViewById(R.id.language_selector),StyleableEditView.class).setText( ObjectUtils.cast(ObjectUtils.cast(this.languagesBottomSheetDialog.findViewById(R.id.languages),ListView.class).getAdapter(),SystemSettingsLanguageAdapter.class).getListener().getChecked().get() );
	}
}