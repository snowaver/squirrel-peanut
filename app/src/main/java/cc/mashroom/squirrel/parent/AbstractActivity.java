package cc.mashroom.squirrel.parent;

import  android.app.Activity;
import  android.content.Context;
import  android.content.Intent;
import  android.content.res.Configuration;
import  android.os.Build;
import  android.widget.Toast;

import  java.util.Locale;

import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.hedgehog.util.ContextUtils;
import  es.dmoral.toasty.Toasty;

public  abstract  class  AbstractActivity  extends  cc.mashroom.hedgehog.parent.AbstractActivity
{
	protected  void  attachBaseContext(  Context  newBase )
	{
		Configuration  configuration = newBase.getResources().getConfiguration();

		configuration.setLocale( Locale.forLanguageTag(newBase.getSharedPreferences("CONFIGURATION",MODE_PRIVATE).getString("LOCAL",Locale.ENGLISH.toLanguageTag())) );

		super.attachBaseContext( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? super.createConfigurationContext(configuration) : newBase );
	}

	public  void  putResultDataAndFinish( Activity  context,int  resultCode,Intent  resultData )
	{
		context.setResult( resultCode,resultData );

		context.finish();
	}

	public  void  error( Throwable  e )
	{
		e.printStackTrace();  application().getMainLooperHandler().post( () -> Toasty.error(AbstractActivity.this,e.getMessage(),Toast.LENGTH_LONG,false).show() );  ContextUtils.finish( this );
	}

	public  Application   application()
	{
		return  ObjectUtils.cast( super.getApplication() );
	}

	public  void  onBackPressed()
	{

	}
}
