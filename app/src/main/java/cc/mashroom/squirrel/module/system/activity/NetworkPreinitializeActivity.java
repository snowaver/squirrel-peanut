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
import  com.google.common.collect.Lists;

import  java.util.List;
import  java.util.concurrent.TimeUnit;

import  androidx.core.content.res.ResourcesCompat;

import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.hedgehog.util.NetworkUtils;
import  cc.mashroom.hedgehog.util.StyleUnifier;
import  cc.mashroom.router.Service;
import  cc.mashroom.router.ServiceListRequestEventListener;
import  cc.mashroom.router.impl.DefaultServiceListRequestStrategy;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.module.home.activity.SheetActivity;
import  cc.mashroom.squirrel.parent.Application;
import  cc.mashroom.squirrel.util.LocaleUtils;
import  cc.mashroom.util.NoopHostnameVerifier;
import  cc.mashroom.util.NoopX509TrustManager;
import  cc.mashroom.util.StringUtils;
import  java8.util.stream.Collectors;
import  java8.util.stream.StreamSupport;
import  okhttp3.OkHttpClient;

public  class    NetworkPreinitializeActivity      extends     AbstractActivity  implements  Runnable,ServiceListRequestEventListener
{
	protected  void  onCreate(Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		LocaleUtils.change(    this , null );

		super.getWindow().addFlags(    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS );

		super.getWindow().setStatusBarColor( super.getResources().getColor(R.color.gainsboro) );

		super.setContentView( R.layout.activity_network_preinitialize );

		super.application().getSquirrelClient().route(new  DefaultServiceListRequestStrategy(new  OkHttpClient.Builder().hostnameVerifier(new NoopHostnameVerifier()).sslSocketFactory(SquirrelClient.SSL_CONTEXT.getSocketFactory(),new NoopX509TrustManager()).connectTimeout(5,TimeUnit.SECONDS).writeTimeout(5,TimeUnit.SECONDS).readTimeout(10,TimeUnit.SECONDS).build(),Lists.newArrayList(Application.SERVICE_LIST_REQUEST_URL),StreamSupport.stream(Lists.newArrayList(super.getSharedPreferences("LATEST_LOGIN_FORM",MODE_PRIVATE).getString("HOSTS","").split(","))).filter((host) -> StringUtils.isNotBlank(host)).map((host) -> new  Service().setHost(host)).collect(Collectors.toList())),this );

		this.progressDialog = StyleUnifier.unify(new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCanceledOnTouchOutside(false).create(),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).setWidth(DensityUtils.px(this,220)).setHeight( DensityUtils.px(this,150) );

		super.application().getScheduler().schedule( this, 5 , TimeUnit.SECONDS );
	}

	private UIProgressDialog  progressDialog;
	@Override
	public  void  onRequestComplete( List<Service>  list )
	{
		if( this.progressDialog.isShowing() )
		{
			super.application().getMainLooperHandler().post(()-> this.progressDialog.cancel() );run();
		}
	}
	@Override
	public  void  onBeforeRequest()
	{
		if( !super.isFinishing() && !super.isDestroyed() )  super.application().getMainLooperHandler().post( () -> this.progressDialog.show() );
	}
    @Override
    protected  void     onDestroy()
    {
        super.onDestroy();

        super.application().getSquirrelClient().getServiceRouteManager().getServiceListRequestEventDispatcher().removeListener(this);
    }

    public  void     run()
	{
		if( super.application().getSquirrelClient().service()  != null )
		{
			super.application().getMainLooperHandler().post( () -> StyleUnifier.unify(new  UIAlertDialog.DividerIOSBuilder(this).setBackgroundRadius(15).setTitle(R.string.warning).setTitleTextSize(18).setMessage(R.string.network_configuration_error).setMessageTextSize(18).setCancelable(false).setCanceledOnTouchOutside(false).setPositiveButtonTextColor(Color.RED).setNegativeButtonTextSize(18).setNegativeButton(R.string.retry,(dialog,which) -> {this.progressDialog.show();  application().getSquirrelClient().reroute();}).setPositiveButtonTextSize(18).setPositiveButton(R.string.exit,(dialog,which) -> android.os.Process.killProcess(android.os.Process.myPid())).create().setWidth((int)  (super.getResources().getDisplayMetrics().widthPixels*0.88)),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).show() );  return;
		}

		SharedPreferences  sharedPrf = super.getSharedPreferences( "LATEST_LOGIN_FORM",MODE_PRIVATE );

		if( sharedPrf.getLong(      "USER_ID" , 0L ) > 0 )
		{
			super.application().connect( sharedPrf.getString("USERNAME",""),sharedPrf.getString("ENCRYPT_PASSWORD",""),NetworkUtils.getLocation(NetworkPreinitializeActivity.this) );
		}

		ActivityCompat.startActivity( this,new  Intent(NetworkPreinitializeActivity.this,sharedPrf.getLong("USER_ID",0L) > 0 ? SheetActivity.class : LoginActivity.class),ActivityOptionsCompat.makeCustomAnimation(NetworkPreinitializeActivity.this,R.anim.fade_in,R.anim.fade_out).toBundle() );  super.finish();
	}
}