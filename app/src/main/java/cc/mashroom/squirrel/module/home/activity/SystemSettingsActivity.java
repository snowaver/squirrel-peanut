package cc.mashroom.squirrel.module.home.activity;

import  android.app.Activity;
import  android.content.Intent;
import  android.os.Bundle;
import  android.widget.CompoundButton;
import  android.widget.ListView;

import  com.aries.ui.widget.progress.UIProgressDialog;

import  androidx.core.app.ActivityCompat;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.common.services.UserService;
import  cc.mashroom.squirrel.module.home.adapters.SystemSettingsLanguageAdapter;
import  cc.mashroom.squirrel.module.system.activity.LoginActivity;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.stream.Stream;
import  retrofit2.Call;
import  retrofit2.Response;

public  class  SystemSettingsActivity  extends  AbstractActivity  implements  CompoundButton.OnCheckedChangeListener
{
	public  void  logout()
	{
		RetrofitRegistry.get(UserService.class).logout(application().getSquirrelClient().getId()).enqueue
		(
			new AbstractRetrofit2Callback<Void>( this,new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCancelable(false).setCanceledOnTouchOutside(false).create().setHeight(DensityUtils.px(this,140)) )
			{
				public  void  onResponse( Call<Void>  call,Response<Void>  response )
				{
					super.onResponse(    call , response );

					application().getSquirrelClient().disconnect();
					//  remove  credentials  if  logout  or  squeezed  off  the  line  by  remote  login  and  skip  to  loginactivity.
					Stream.forEach( AbstractActivity.STACK  , (Activity  activity) ->activity.finish() );

					ActivityCompat.startActivity( SystemSettingsActivity.this,new Intent(SystemSettingsActivity.this,LoginActivity.class).putExtra("USERNAME",SystemSettingsActivity.this.getSharedPreferences("LOGIN_FORM",MODE_PRIVATE).getString("USERNAME","")).putExtra("RELOGIN_REASON",0).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),null );

					SystemSettingsActivity.this.getSharedPreferences("LOGIN_FORM",MODE_PRIVATE).edit().remove("ID").remove("USERNAME").remove("NAME").remove("NICKNAME").commit();
				}
			}
		);
	}
	
	protected  void  onCreate( Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		super.setContentView(  R.layout.activity_system_settings );

		ObjectUtils.cast(super.findViewById(R.id.languages),ListView.class).setAdapter(new  SystemSettingsLanguageAdapter(this,this) );

		super.findViewById(R.id.logout_button).setOnClickListener( (v) -> logout() );
	}

	public  void  onCheckedChanged( CompoundButton  buttonView , boolean  isChecked )
	{

	}
}
