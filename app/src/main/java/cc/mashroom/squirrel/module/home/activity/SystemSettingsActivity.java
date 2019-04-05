package cc.mashroom.squirrel.module.home.activity;

import  android.app.Activity;
import  android.content.Context;
import  android.content.Intent;
import  android.content.res.Configuration;
import  android.os.Build;
import  android.os.Bundle;
import  android.view.View;
import  android.widget.ListView;
import  android.widget.TextView;

import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.google.android.material.bottomsheet.BottomSheetDialog;

import  java.util.Locale;

import  androidx.core.app.ActivityCompat;
import  androidx.core.content.res.ResourcesCompat;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.hedgehog.util.ExtviewsAdapter;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.common.services.UserService;
import  cc.mashroom.squirrel.module.home.adapters.SystemSettingsLanguageAdapter;
import  cc.mashroom.squirrel.module.system.activity.LoginActivity;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.stream.Stream;
import  cn.refactor.library.SmoothCheckBox;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;
import  retrofit2.Call;
import  retrofit2.Response;

public  class  SystemSettingsActivity  extends  AbstractActivity  implements  SmoothCheckBox.OnCheckedChangeListener
{
	private   void  logout()
	{
		RetrofitRegistry.get(UserService.class).logout(application().getSquirrelClient().getId()).enqueue
		(
			new  AbstractRetrofit2Callback<Void>( this,ExtviewsAdapter.adapter(new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCancelable(false).setCanceledOnTouchOutside(false).create(),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).setHeight(DensityUtils.px(this , 140)) )
			{
				public  void  onResponse( Call<Void>  call,  Response<Void>  response )
				{
					super.onResponse(    call , response );

					application().getSquirrelClient().disconnect();
					//  remove  credentials  if  logout  or  squeezed  off  the  line  by  remote  login  and  skip  to  login  activity.
					Stream.forEach( AbstractActivity.STACK,  (Activity  activity) -> activity.finish() );

					ActivityCompat.startActivity( SystemSettingsActivity.this,new Intent(SystemSettingsActivity.this,LoginActivity.class).putExtra("USERNAME",SystemSettingsActivity.this.getSharedPreferences("LOGIN_FORM",MODE_PRIVATE).getString("USERNAME","")).putExtra("RELOGIN_REASON",0).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),null );

					SystemSettingsActivity.this.getSharedPreferences("LOGIN_FORM",MODE_PRIVATE).edit().remove("ID").remove("USERNAME").remove("NAME").remove("NICKNAME").commit();
				}
			}
		);
	}

	@Accessors( chain=true )
	@Setter
	@Getter
	private   BottomSheetDialog  languageBottomSheetDialog;

	public  void  onCheckedChanged( SmoothCheckBox  smoothCheckbox,boolean  isChecked )
	{
		Locale  locale = ObjectUtils.cast(ObjectUtils.cast(smoothCheckbox.getParent(),View.class).findViewById(R.id.name),TextView.class).getText().toString().equals("ENGLISH") ? Locale.ENGLISH : Locale.CHINESE;

		Configuration  previousConfiguration = super.getResources().getConfiguration();

		if( Build.VERSION.SDK_INT < Build.VERSION_CODES.N )
		{
			previousConfiguration.locale          = locale;
		}
		else
		{
			previousConfiguration.setLocale(      locale );
		}

		super.getResources().updateConfiguration( previousConfiguration, super.getResources().getDisplayMetrics() );

		super.getSharedPreferences("CONFIGURATION",      Context.MODE_PRIVATE).edit().putString("LOCAL",locale.toLanguageTag()).commit();

		ObjectUtils.cast(super.findViewById(R.id.language_selector),StyleableEditView.class).setText( ObjectUtils.cast(ObjectUtils.cast(smoothCheckbox.getParent(),View.class).findViewById(R.id.name),TextView.class).getText().toString() );

//		for( Activity  activity  : AbstractActivity.STACK )activity.recreate();
	}

	protected  void  onCreate( Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		super.setContentView(  R.layout.activity_system_settings );

		super.findViewById(R.id.logout_button).setOnClickListener( (view)-> logout() );

		this.setLanguageBottomSheetDialog(new  BottomSheetDialog(this)).getLanguageBottomSheetDialog().setContentView( R.layout.activity_system_settings_language_bottomsheet );

		this.getLanguageBottomSheetDialog().setCanceledOnTouchOutside(  true );

		ObjectUtils.cast(super.findViewById(R.id.language_selector),StyleableEditView.class).findViewById(R.id.edit_inputor).setOnClickListener( (selector) -> languageBottomSheetDialog.show() );

		ObjectUtils.cast(this.languageBottomSheetDialog.findViewById(R.id.languages),ListView.class).setAdapter( new  SystemSettingsLanguageAdapter(this,this ) );

		ObjectUtils.cast(super.findViewById(R.id.language_selector),StyleableEditView.class).setText( ObjectUtils.cast(ObjectUtils.cast(this.languageBottomSheetDialog.findViewById(R.id.languages),ListView.class).getAdapter(),SystemSettingsLanguageAdapter.class).getListener().getChecked().get() );
	}
}
