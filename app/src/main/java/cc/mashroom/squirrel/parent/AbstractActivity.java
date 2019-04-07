package cc.mashroom.squirrel.parent;

import  android.app.Activity;
import  android.content.Intent;
import  android.widget.Toast;

import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.hedgehog.util.ContextUtils;
import  es.dmoral.toasty.Toasty;

public  abstract  class  AbstractActivity  extends  cc.mashroom.hedgehog.parent.AbstractActivity
{
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
