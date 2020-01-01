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
package cc.mashroom.squirrel.parent;

import  android.app.Activity;
import  android.content.Intent;
import  android.content.SharedPreferences;
import  android.net.ConnectivityManager;
import  android.net.Network;

import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;

import  com.facebook.common.internal.Sets;
import  com.google.common.collect.Lists;

import  java.util.ArrayList;
import  java.util.List;
import  java.util.concurrent.TimeUnit;

import  cc.mashroom.hedgehog.util.NetworkUtils;
import  cc.mashroom.router.Service;
import  cc.mashroom.router.ServiceListRequestEventListener;
import  cc.mashroom.router.impl.DefaultServiceListRequestStrategy;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.event.LifecycleEventListener;
import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.module.home.activity.SystemSettingsActivity;
import  cc.mashroom.squirrel.module.system.activity.LoginActivity;
import  cc.mashroom.squirrel.module.system.activity.NetworkPreinitializeActivity;
import  cc.mashroom.squirrel.module.system.activity.RegisterActivity;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.transport.ConnectState;
import  cc.mashroom.util.NoopHostnameVerifier;
import  cc.mashroom.util.NoopX509TrustManager;
import  java8.util.stream.Collectors;
import  java8.util.stream.StreamSupport;
import  lombok.AllArgsConstructor;
import  okhttp3.OkHttpClient;

import  static  android.content.Context.MODE_PRIVATE;

@AllArgsConstructor

public  class  ConnectivityAndActivityScheduler     extends  ConnectivityManager.NetworkCallback  implements  ServiceListRequestEventListener,LifecycleEventListener
{
    protected  Application  application;

    public  void  onAvailable(     Network  network )
    {
        super.onAvailable(  network   );
    }
    @Override
    public  void  onBeforeRequest()
    {

    }
    public  void  route()
    {
        this.application.getSquirrelClient().route( new  DefaultServiceListRequestStrategy(new  OkHttpClient.Builder().hostnameVerifier(new  NoopHostnameVerifier()).sslSocketFactory(SquirrelClient.SSL_CONTEXT.getSocketFactory(),new NoopX509TrustManager()).connectTimeout(5, TimeUnit.SECONDS).writeTimeout(5,TimeUnit.SECONDS).readTimeout(10,TimeUnit.SECONDS).build(),Lists.newArrayList(Application.SERVICE_LIST_REQUEST_URL)),this );
    }
    @Override
    public  void  onRequestComplete( int  code,List<Service>  services )
    {
        SharedPreferences  sdpf = this.application.getSharedPreferences( "LATEST_LOGIN_FORM",MODE_PRIVATE );

        if( code == 200 && sdpf.getLong("USER_ID",0L)  > 0 )
        {
            this.application.connect( sdpf.getString("USERNAME",""),sdpf.getString("ENCRYPT_PASSWORD",""),NetworkUtils.getLocation(this.application) );
        }
        else
        if( code != 200 && sdpf.getLong("USER_ID",0L)  > 0 )
        {
            this.route();
        }
    }
    @Override
    public  void  onError(     Throwable  throwable )
    {

    }
    @Override
    public  void  onLogoutComplete(  int  code,int  reason )
    {
        SharedPreferences  sdpf = this.application.getSharedPreferences( "LATEST_LOGIN_FORM",MODE_PRIVATE );
        //  remove  credentials  if  logout  or  squeezed  off  the  line  by  remote  login  and  skip  to  loginactivity.
        if( code == 200 )
        {
            if( reason == DisconnectAckPacket.REASON_REMOTE_SIGNIN ||  reason == DisconnectAckPacket.REASON_CLIENT_LOGOUT )
            {
                List<Activity>  stackActivities = new  ArrayList<Activity>( AbstractActivity.STACK );

                ActivityCompat.startActivity( AbstractActivity.STACK.getLast(),new  Intent(AbstractActivity.STACK.getLast(),LoginActivity.class).putExtra("USERNAME",sdpf.getString("USERNAME","")).putExtra("RELOGIN_REASON",reason),ActivityOptionsCompat.makeCustomAnimation(this.application,R.anim.right_in,R.anim.left_out).toBundle() );

                StreamSupport.stream(stackActivities).forEach( (stackActivity)  -> stackActivity.finish() );
            }
        }
    }
    @Override
    public  void  onAuthenticateComplete(int   code )
    {
        SharedPreferences  sdpf = this.application.getSharedPreferences( "LATEST_LOGIN_FORM",MODE_PRIVATE );

        if( code == 200 )
        {
            sdpf.edit().putString("HOST",this.application.getSquirrelClient().service().getHost()).putLong("ID",this.application.getSquirrelClient().userMetadata().getId()).putString("USERNAME",this.application.getSquirrelClient().userMetadata().getUsername()).putString("NAME",this.application.getSquirrelClient().userMetadata().getName()).putString("NICKNAME",this.application.getSquirrelClient().userMetadata().getNickname()).apply();
        }
        else
        if( code == 601 || code == 602 )
        {
            sdpf.edit().clear().apply();

            if( !Sets.newHashSet(NetworkPreinitializeActivity.class,LoginActivity.class,RegisterActivity.class,SystemSettingsActivity.class).containsAll(StreamSupport.stream(AbstractActivity.STACK).map((activity) -> activity.getClass()).collect(Collectors.toUnmodifiableSet())) )
            {
                List<Activity>  stackActivities = new  ArrayList<Activity>( AbstractActivity.STACK );

                ActivityCompat.startActivity( AbstractActivity.STACK.getLast(),new  Intent(AbstractActivity.STACK.getLast(),LoginActivity.class).putExtra("USERNAME",sdpf.getString("USERNAME","")).putExtra("RELOGIN_REASON",0),ActivityOptionsCompat.makeCustomAnimation(this.application,R.anim.right_in,R.anim.left_out).toBundle() );

                StreamSupport.stream(stackActivities).forEach( (stackActivity)  -> stackActivity.finish() );
            }
        }
    }
    @Override
    public  void  onReceivedOfflineData( OoIData   ooIData )
    {

    }
    @Override
    public  void  onConnectStateChanged( ConnectState  newConnectState )
    {

    }
}