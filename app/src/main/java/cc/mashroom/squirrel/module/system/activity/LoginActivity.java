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
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.View;
import  android.view.WindowManager;
import  android.widget.AdapterView;
import  android.widget.Button;
import  android.widget.EditText;
import  android.widget.ListView;
import  android.widget.TextView;

import  com.aries.ui.widget.alert.UIAlertDialog;
import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.google.android.material.bottomsheet.BottomSheetDialog;
import  com.irozon.sneaker.Sneaker;

import  androidx.core.content.res.ResourcesCompat;

import  java.util.Locale;

import  cc.mashroom.hedgehog.system.LocaleChangeEventDispatcher;
import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.hedgehog.util.ExtviewsAdapter;
import  cc.mashroom.hedgehog.util.NetworkUtils;
import  cc.mashroom.hedgehog.widget.HeaderBar;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.LifecycleListener;
import  cc.mashroom.squirrel.module.home.activity.SheetActivity;
import  cc.mashroom.squirrel.module.home.adapters.SystemSettingsLanguageAdapter;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.util.LocaleUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cn.refactor.library.SmoothCheckBox;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;

public  class  LoginActivity  extends  AbstractActivity  implements  Button.OnClickListener,LifecycleListener,HeaderBar.OnItemClickListener,SmoothCheckBox.OnCheckedChangeListener,AdapterView.OnItemClickListener,LocaleChangeEventDispatcher.LocaleChangeListener
{
    protected  void  onActivityResult( int  requestCode , int  rstCode , Intent  data )
    {
        super.onActivityResult( requestCode,rstCode,data );

        if( data   != null )
        {
            ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).setText( data.getStringExtra("USERNAME") == null ? "" : data.getStringExtra("USERNAME") );
        }
    }

    protected  void  onCreate( Bundle  savedInstanceState )
    {
        LocaleChangeEventDispatcher.addListener(    this );

        super.onCreate(   savedInstanceState );

        super.getWindow().clearFlags(     WindowManager.LayoutParams.FLAG_FULLSCREEN );

        super.setContentView(    R.layout.activity_login );

        ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).setText( super.getIntent().getStringExtra("USERNAME") == null ? "" : super.getIntent().getStringExtra("USERNAME") );

        if( super.getIntent().getIntExtra("RELOGIN_REASON", 0) == 1 )
        {
            ExtviewsAdapter.adapter(new  UIAlertDialog.DividerIOSBuilder(this).setBackgroundRadius(15).setTitle(R.string.warning).setTitleTextSize(18).setMessage(R.string.login_remote_login_error).setMessageTextSize(18).setCancelable(false).setCanceledOnTouchOutside(false).setPositiveButtonTextColorResource(R.color.red).setPositiveButtonTextSize(18).setPositiveButton(R.string.close,(dialog, which) -> android.os.Process.killProcess(android.os.Process.myPid())).create().setWidth((int)  (super.getResources().getDisplayMetrics().widthPixels*0.9)),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).show();
        }
        //  clear  the  password  styleable  editview  after  successful  registration,  logout  or  squeezing  off  line  by  remote  login.
        ObjectUtils.cast(super.findViewById(R.id.password), StyleableEditView.class ).getText().clear();

        ObjectUtils.cast(super.findViewById(R.id.login_button),Button.class).setOnClickListener( this );

        ObjectUtils.cast(super.findViewById(R.id.jump_to_registration_link),TextView.class).setOnClickListener( (view) -> ActivityCompat.startActivityForResult(this,new  Intent(this,RegisterActivity.class),0,ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle()) );

        this.setLanguagesBottomSheetDialog(new  BottomSheetDialog(this)).getLanguagesBottomSheetDialog().setContentView( R.layout.activity_system_settings_language_bottomsheet );

        ObjectUtils.cast(this.getLanguagesBottomSheetDialog().findViewById(R.id.languages), ListView.class).setAdapter( new  SystemSettingsLanguageAdapter(this,this) );

        ObjectUtils.cast(this.getLanguagesBottomSheetDialog().findViewById(R.id.languages), ListView.class).setOnItemClickListener(   this );

        ObjectUtils.cast(super.findViewById(R.id.header_bar).findViewById(cc.mashroom.hedgehog.R.id.back_text),TextView.class).setOnClickListener( (view) -> languagesBottomSheetDialog.show()  );

        this.progressDialog = ExtviewsAdapter.adapter(new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCancelable(false).setCanceledOnTouchOutside(false).create(),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).setHeight( DensityUtils.px(this,140) );
    }

    public  void  onChange(    Locale  locale )
    {
        ObjectUtils.cast(super.findViewById(R.id.header_bar).findViewById(cc.mashroom.hedgehog.R.id.back_text),TextView.class).setText(  R.string.language );

        ObjectUtils.cast(super.findViewById(R.id.header_bar).findViewById(cc.mashroom.hedgehog.R.id.title),TextView.class).setText( R.string.login );

        ObjectUtils.cast(super.findViewById(R.id.username).findViewById(cc.mashroom.hedgehog.R.id.edit_inputor),EditText.class).setHint( R.string.username );

        ObjectUtils.cast(super.findViewById(R.id.login_button),Button.class).setText(  R.string.login );

        ObjectUtils.cast(super.findViewById(R.id.password).findViewById(cc.mashroom.hedgehog.R.id.edit_inputor),EditText.class).setHint( R.string.password );

        ObjectUtils.cast(super.findViewById(R.id.jump_to_registration_link),TextView.class).setText(      R.string.register_register_if_no_account );
        /*
        ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).getDropdownItems().get(0) , TextView.class).setText( R.string.language );
        */
    }

    @Accessors( chain=true )
    @Setter
    @Getter
    private  BottomSheetDialog  languagesBottomSheetDialog;
    @Accessors( chain=true )
    @Setter
    @Getter
    private  UIProgressDialog   progressDialog;

    public  void  onItemClick( AdapterView  parent, View  view,int  position,long  id )
    {
        view.setBackgroundResource(    R.color.lightgray );

        ObjectUtils.cast(view.findViewById(R.id.checkbox),SmoothCheckBox.class).setChecked( true,true );
    }

    public  void  onItemClick( View  itview,int  position )
    {
        if( position  == 0 )  this.languagesBottomSheetDialog.show();
    }

    protected  void    onDestroy()
    {
        super.onDestroy(  );

        application().getSquirrelClient().removeLifecycleListener(  this );

        LocaleChangeEventDispatcher.removeListener( this );
    }

    public  void  onAuthenticateComplete( int  returnCode )
    {
        progressDialog.cancel(  );

        if( returnCode    == 200 )
        {
            ActivityCompat.startActivity( this,new  Intent(this,SheetActivity.class),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );
        }
        else
        {
            application().getMainLooperHandler().post( () -> showSneakerWindow(Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_error,R.string.login_failed,R.color.white,R.color.red) );
        }
    }

    public  void  onCheckedChanged( SmoothCheckBox  smoothCheckbox,boolean  isChecked )
    {
        this.languagesBottomSheetDialog.hide();

        Locale locale = ObjectUtils.cast(ObjectUtils.cast(smoothCheckbox.getParent(),View.class).findViewById(R.id.name),TextView.class).getText().toString().equals("ENGLISH") ? Locale.ENGLISH : Locale.CHINESE;

        LocaleUtils.change( this ,locale.toLanguageTag() );
    }

    public  void  onClick(  View  loginButton )
    {
        if( StringUtils.isAnyBlank(ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).getText().toString(),ObjectUtils.cast(super.findViewById(R.id.password),StyleableEditView.class).getText().toString()) || !NetworkUtils.isNetworkAvailable(this) )
        {
            application().getMainLooperHandler().post( () -> showSneakerWindow(Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_error,!NetworkUtils.isNetworkAvailable(this) ? R.string.network_or_internal_server_error : R.string.login_form_error,R.color.white,R.color.red) );
        }
        else
        {
            ContextUtils.hideSoftinput( this );

            progressDialog.show();

            application().connect( ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).getText().toString(),ObjectUtils.cast(super.findViewById(R.id.password),StyleableEditView.class).getText().toString(),NetworkUtils.getLocation(this),this );
        }
    }

    public  void  onReceiveOfflineData( boolean  finished )
    {

    }

    public  void  onDisconnected( int  reason )
    {

    }
}