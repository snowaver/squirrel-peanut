package cc.mashroom.squirrel.module.system.activity;

import  android.content.Intent;
import  android.graphics.Color;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;

import  android.view.WindowManager;

import  com.aries.ui.widget.alert.UIAlertDialog;

import  java.util.concurrent.TimeUnit;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.module.home.activity.SheetActivity;

public  class  LoadingActivity  extends  AbstractActivity  implements  Runnable
{
	protected  void  onCreate( Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		super.getWindow().addFlags( WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS );

		super.getWindow().setStatusBarColor( super.getResources().getColor(R.color.gainsboro) );

		super.setContentView(  R.layout.activity_loading );

		super.application().getExecutor().schedule( this,super.getSharedPreferences("LOGIN_FORM",MODE_PRIVATE).getLong("ID",0) >= 1 ? 1 : 3, TimeUnit.SECONDS );
	}

	public  void  run()
	{
        if( !application().getSquirrelClient().isRouted() )
        {
            application().getMainLooperHandler().post( () -> new  UIAlertDialog.DividerIOSBuilder(this).setBackgroundRadius(15).setTitle(R.string.warning).setTitleTextSize(18).setMessage(R.string.network_configuration_error).setMessageTextSize(18).setCancelable(false).setCanceledOnTouchOutside(false).setPositiveButtonTextColor(Color.RED).setPositiveButtonTextSize(18).setPositiveButton(R.string.exit,(dialog, which) -> android.os.Process.killProcess(android.os.Process.myPid())).create().setWidth((int)  (super.getResources().getDisplayMetrics().widthPixels*0.9)).show() );
        }
        else
		if( !super.isFinishing() )
		{
			ActivityCompat.startActivity( this,new  Intent(LoadingActivity.this,application().getUserMetadata() == null ? LoginActivity.class : SheetActivity.class),ActivityOptionsCompat.makeCustomAnimation(LoadingActivity.this,R.anim.fade_in,R.anim.fade_out).toBundle() );  super.finish();
		}
	}
}
