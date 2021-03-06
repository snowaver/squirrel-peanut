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
import  android.content.SharedPreferences;
import  android.graphics.Color;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;

import  android.view.WindowManager;

import  com.aries.ui.widget.alert.UIAlertDialog;
import  com.aries.ui.widget.progress.UIProgressDialog;

import  java.util.List;

import  androidx.core.content.res.ResourcesCompat;

import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.hedgehog.util.StyleUnifier;
import  cc.mashroom.router.Service;
import  cc.mashroom.router.ServiceListRequestEventListener;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.module.home.activity.SheetActivity;
import  cc.mashroom.squirrel.util.LocaleUtils;
import  lombok.Setter;

public  class    NetworkPreinitializeActivity  extends  AbstractActivity  implements  ServiceListRequestEventListener
{
	@Override
	protected  void  onCreate(Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		LocaleUtils.change(    this , null );

		super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS );

		super.getWindow().setStatusBarColor( super.getResources().getColor( R.color.gainsboro ) );

		super.setContentView( R.layout.activity_network_preinitialize );


		setProgressDialog( StyleUnifier.unify(new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCanceledOnTouchOutside(false).create(),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).setWidth(DensityUtils.px(this,220)).setHeight(DensityUtils.px(this,150)) );
	}
	@Setter
	private UIProgressDialog  progressDialog;
	@Override
	public  void  onRequestComplete( int  code,List<Service>  services )
	{
        SharedPreferences  sdprf = super.getSharedPreferences( "LATEST_LOGIN_FORM",MODE_PRIVATE );

        if( this.progressDialog.isShowing() )  super.application().getMainLooperHandler().post( () ->   this.progressDialog.cancel() );

        if( code == 200  )
        {
            ActivityCompat.startActivity( this,new  Intent(this,sdprf.getLong("USER_ID",0L) > 0 ? SheetActivity.class : LoginActivity.class),ActivityOptionsCompat.makeCustomAnimation(NetworkPreinitializeActivity.this,R.anim.fade_in,R.anim.fade_out).toBundle() );  super.finish();
        }
        else
        {
        if( sdprf.getLong("USER_ID",0L) > 0 )
            {
            ActivityCompat.startActivity( this,new  Intent(this,SheetActivity.class),ActivityOptionsCompat.makeCustomAnimation(NetworkPreinitializeActivity.this,R.anim.fade_in,R.anim.fade_out).toBundle() );  super.finish();
            }
            else
            {
            super.application().getMainLooperHandler().post( () -> StyleUnifier.unify(new  UIAlertDialog.DividerIOSBuilder(this).setBackgroundRadius(15).setTitle(R.string.warning).setTitleTextSize(18).setMessage(R.string.network_configuration_error).setMessageTextSize(18).setCancelable(false).setCanceledOnTouchOutside(false).setPositiveButtonTextColor(Color.RED).setNegativeButtonTextSize(18).setNegativeButton(R.string.retry,(dialog,which) -> {this.progressDialog.show();}).setPositiveButtonTextSize(18).setPositiveButton(R.string.exit,(dialog,which) -> android.os.Process.killProcess(android.os.Process.myPid())).create().setWidth((int)  (super.getResources().getDisplayMetrics().widthPixels*0.88)),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).show() );
            }
        }
	}
    @Override
    protected  void     onDestroy()
    {
        super.onDestroy();

        super.application().getSquirrelClient().getServiceRouteManager().getServiceListRequestEventDispatcher().removeListener( this );
    }
	@Override
	public  void  onBeforeRequest()
	{
		if( !super.isFinishing() && !super.isDestroyed() )  super.application().getMainLooperHandler().post( () -> this.progressDialog.show() );
	}
}