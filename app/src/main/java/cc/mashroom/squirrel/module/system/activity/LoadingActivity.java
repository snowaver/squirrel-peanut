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
import  android.graphics.Color;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;

import  android.view.WindowManager;

import  com.aries.ui.widget.alert.UIAlertDialog;
import  com.aries.ui.widget.progress.UIProgressDialog;

import  java.util.List;
import  java.util.concurrent.TimeUnit;

import  androidx.core.content.res.ResourcesCompat;

import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.hedgehog.util.NetworkUtils;
import  cc.mashroom.hedgehog.util.StyleUnifier;
import  cc.mashroom.router.Service;
import  cc.mashroom.router.ServiceRouteListener;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.module.home.activity.SheetActivity;
import  cc.mashroom.squirrel.util.LocaleUtils;

public  class  LoadingActivity   extends  AbstractActivity implements  Runnable  ,ServiceRouteListener
{
	protected  void  onCreate(Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		LocaleUtils.change(    this , null );

		super.getWindow().addFlags(    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS );

		super.getWindow().setStatusBarColor( super.getResources().getColor(R.color.gainsboro) );

		super.setContentView( R.layout.activity_loading );

		this.progressDialog = StyleUnifier.unify(new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCanceledOnTouchOutside(false).create(),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).setWidth(DensityUtils.px(this,220)).setHeight( DensityUtils.px(this,150) );

		super.application().getScheduler().schedule( this,5,TimeUnit.SECONDS );
	}

	private UIProgressDialog  progressDialog;

	@Override
	public  void  onRequestComplete( List<Service>  list )
	{
		if( this.progressDialog.isShowing() )
		{
			super.application().getMainLooperHandler().post(()-> this.progressDialog.cancel() );

			run();
		}
	}
	@Override
	protected  void       onStart()
	{
		super.onStart(  );

		super.application().getSquirrelClient().getServiceRouteManager().addListener(    this );
	}
    @Override
    protected  void     onDestroy()
    {
        super.onDestroy();

        super.application().getSquirrelClient().getServiceRouteManager().removeListener( this );
    }

    public  void     run()
	{
		if( application().getSquirrelClient().getServiceRouteManager().getServices().isEmpty() )
		{
			application().getMainLooperHandler().post( () -> StyleUnifier.unify(new  UIAlertDialog.DividerIOSBuilder(this).setBackgroundRadius(15).setTitle(R.string.warning).setTitleTextSize(18).setMessage(R.string.network_configuration_error).setMessageTextSize(18).setCancelable(false).setCanceledOnTouchOutside(false).setPositiveButtonTextColor(Color.RED).setNegativeButtonTextSize(18).setNegativeButton(R.string.retry,(dialog,which) -> {this.progressDialog.show();  application().getSquirrelClient().reroute();}).setPositiveButtonTextSize(18).setPositiveButton(R.string.exit,(dialog,which) -> android.os.Process.killProcess(android.os.Process.myPid())).create().setWidth((int)  (super.getResources().getDisplayMetrics().widthPixels*0.88)),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).show() );

			return;
		}

		Long  userId = super.getSharedPreferences("LATEST_LOGIN_FORM",MODE_PRIVATE).getLong("ID" ,0 );

		if( userId   > 0 )
		{
			super.application().connect( userId, NetworkUtils.getLocation(this),super.application() );
		}

		ActivityCompat.startActivity( this,new  Intent(LoadingActivity.this,userId> 0 ? SheetActivity.class : LoginActivity.class),ActivityOptionsCompat.makeCustomAnimation(LoadingActivity.this,R.anim.fade_in,R.anim.fade_out).toBundle() );  super.finish();
	}
	@Override
	public  void  onBeforeRequest()
	{
		if( !super.isFinishing() && !super.isDestroyed() )
		{
			super.application().getMainLooperHandler().post( () -> this.progressDialog.show() );
		}
	}
    @Override
    public  void  onChanged( Service  oldService,Service  newService )
    {

    }
}