package cc.mashroom.squirrel.http;

import  android.app.Activity;

import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.irozon.sneaker.Sneaker;

import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.squirrel.R;

import cc.mashroom.squirrel.parent.AbstractActivity;
import cc.mashroom.util.ObjectUtils;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;
import  retrofit2.Call;
import  retrofit2.Callback;
import  retrofit2.Response;

public  abstract  class  AbstractRetrofit2Callback<T>  implements  Callback<T>
{
	public  AbstractRetrofit2Callback( Activity  context,UIProgressDialog  waitingDailog )
	{
		this.setContext(context).setWaitingDailog( waitingDailog );

		waitingDailog.show();
	}

	public  AbstractRetrofit2Callback( Activity  context )
	{
		this.setContext( context );
	}

	@Accessors( chain= true )
	@Setter
	@Getter
	protected  Activity    context;
	@Accessors( chain= true )
	@Setter
	@Getter
	protected  UIProgressDialog  waitingDailog;

	public  void  onResponse( Call<T>  call,Response<T>  response )
	{
		if( waitingDailog != null )
		{
			waitingDailog.cancel();
		}
	}

	public  void  onFailure( Call<T>  call,Throwable  ie )
	{
		ie.printStackTrace( );

		if( waitingDailog != null )
		{
			waitingDailog.cancel();
		}

		ObjectUtils.cast(context,AbstractActivity.class).setSneakerView(Sneaker.with(context),com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white).autoHide(true).setDuration(3000).setHeight(DensityUtils.dp(context,ContextUtils.getStatusBarHeight(context))+50).sneakError();
	}
}
