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
import  android.content.Context;
import  android.content.Intent;
import  android.graphics.Typeface;
import  android.location.Location;
import  android.net.ConnectivityManager;
import  android.net.NetworkRequest;
import  android.os.Bundle;

import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;

import  com.facebook.common.internal.Sets;
import  com.facebook.drawee.backends.pipeline.Fresco;
import  com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import  com.google.common.collect.Lists;

import  org.joda.time.DateTime;
import  org.webrtc.PeerConnection;

import  cc.mashroom.hedgehog.util.NetworkUtils;
import  cc.mashroom.router.DefaultServiceListRequestStrategy;
import  cc.mashroom.router.Schema;
import  cc.mashroom.router.Service;
import  cc.mashroom.router.ServiceRouteListener;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.LifecycleListener;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.connect.ConnectState;
import  cc.mashroom.squirrel.client.PacketListener;
import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.client.storage.model.chat.NewsProfile;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.chat.activity.AudioCallActivity;
import  cc.mashroom.squirrel.module.chat.activity.VideoCallActivity;
import  cc.mashroom.squirrel.module.home.tab.newsprofile.adapters.NewsProfileListAdapter;
import  cc.mashroom.squirrel.module.system.activity.LoadingActivity;
import  cc.mashroom.squirrel.module.system.activity.LoginActivity;
import  cc.mashroom.squirrel.module.system.activity.RegisterActivity;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.push.PushServiceNotifier;

import  java.sql.Timestamp;
import java.util.LinkedList;
import  java.util.List;
import  java.util.Set;
import  java.util.concurrent.ScheduledThreadPoolExecutor;
import  java.util.concurrent.TimeUnit;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.util.LocaleUtils;
import  cc.mashroom.util.FileUtils;
import  cc.mashroom.util.ObjectUtils;
import cc.mashroom.util.StringUtils;
import  es.dmoral.toasty.Toasty;
import  io.netty.util.concurrent.DefaultThreadFactory;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;
import  okhttp3.HttpUrl;

public  class  Application  extends  cc.mashroom.hedgehog.parent.Application  implements  LifecycleListener,PacketListener,   ServiceRouteListener
{
	public  static  List<PeerConnection.IceServer>  ICE_SERVERS = Lists.newArrayList(new  PeerConnection.IceServer("stun:47.105.210.154:3478"),new  PeerConnection.IceServer("stun:stun.l.google.com:19302"),new  PeerConnection.IceServer("turn:47.105.210.154:3478","snowaver","snowaver") );

	public  static  String  SERVICE_LIST_REQUEST_URL      = "https://192.168.1.114:8011/system/service?action=1&keyword=0";

	private  Set<Class> authenticateNeedlessActivityClasses  = Sets.newHashSet(LoadingActivity.class,LoginActivity.class,RegisterActivity.class );
	@Accessors(  chain = true )
	@Getter
	@Setter
	private  SquirrelClient  squirrelClient  = null;
	@Getter
	private  ScheduledThreadPoolExecutor   scheduler  = new  ScheduledThreadPoolExecutor( 1,new  DefaultThreadFactory("DEFAULT-TASK-SCHEDULER") );

	@SneakyThrows
	public   void    onCreate()
	{
		super.onCreate();

		LocaleUtils.change(this,null);

		this.squirrelClient = new  SquirrelClient(this,super.setCacheDir(FileUtils.createDirectoryIfAbsent(super.getDir(".squirrel",Context.MODE_PRIVATE))).getCacheDir()).route(new  DefaultServiceListRequestStrategy(Lists.newArrayList(SERVICE_LIST_REQUEST_URL),SquirrelClient.SSL_CONTEXT.getSocketFactory(),5,TimeUnit.SECONDS)).addPacketListener(     this );

		this.squirrelClient.getServiceRouteManager().addListener(this );

		PushServiceNotifier.INSTANCE.initialize(    this );

		Toasty.Config.getInstance().allowQueue(true).setTextSize(14).setToastTypeface(Typeface.createFromAsset(super.getResources().getAssets(),"font/droid_sans_mono.ttf")).apply();

		Fresco.initialize( Application.this,OkHttpImagePipelineConfigFactory.newBuilder(this, this.squirrelClient.okhttpClient(5,5,10)).build() );

		ObjectUtils.cast(super.getSystemService(Context.CONNECTIVITY_SERVICE),ConnectivityManager.class).requestNetwork( new  NetworkRequest.Builder().build(),new  ConnectivityStateListener( this ) );
	}

	public  void  onAuthenticateComplete( int  returnCode )
	{
		if( returnCode == 601||  returnCode == 602 )
		{
			super.getSharedPreferences("LATEST_LOGIN_FORM",MODE_PRIVATE).edit().clear().apply();

			if( !AbstractActivity.STACK.isEmpty() && !authenticateNeedlessActivityClasses.contains(AbstractActivity.STACK.getLast().getClass() ) )
			{
				this.clearStackActivitiesAndStart( new  Intent(AbstractActivity.STACK.getLast(),LoginActivity.class).putExtra("USERNAME",super.getSharedPreferences("LATEST_LOGIN_FORM",MODE_PRIVATE).getString("USERNAME","")).putExtra("RELOGIN_REASON",0),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );
			}
		}
		else
		if( returnCode == 200 )
		{
			super.getSharedPreferences("LATEST_LOGIN_FORM",MODE_PRIVATE).edit().putLong("ID",squirrelClient.getUserMetadata().getId()).putString("USERNAME",squirrelClient.getUserMetadata().getUsername()).putString("NAME",squirrelClient.getUserMetadata().getName()).putString("NICKNAME",squirrelClient.getUserMetadata().getNickname()).putString("HTTPS_BASE_URL",baseUrl().toString()).apply();
		}
	}

	public  void  onTerminate()
	{
		super.onTerminate();

		this.squirrelClient.release();

		scheduler.shutdown(  );
	}

	public  void  connect( String  username,  String  password,Location  geometryLoc,LifecycleListener  lifecycleListener )
	{
		this.squirrelClient.connect( username,password,geometryLoc == null ? null : geometryLoc.getLongitude(),geometryLoc == null ? null : geometryLoc.getLatitude(),NetworkUtils.getMac(),Lists.newArrayList(this,lifecycleListener) );
	}

	public  void  connect( Long  id,Location  geometryLoc,LifecycleListener  lifecycleListener )
	{
		this.squirrelClient.connect( id,geometryLoc == null ? null : geometryLoc.getLongitude(),geometryLoc == null? null : geometryLoc.getLatitude(),NetworkUtils.getMac(),Lists.newArrayList(this,lifecycleListener) );
	}
	@Override
	public  void     onBeforeRequest()
	{

	}
	@Override
	public  void  onRequestComplete( List  <Service> list )
	{

	}
	@Override
	public  void  onChanged( Service  oldService ,  Service newService )
	{
		if( newService != null && oldService  != newService     && Schema.valueOf(StringUtils.upperCase(newService.getSchema())) == Schema.HTTPS )
		{
			RetrofitRegistry.INSTANCE.initialize(   this );
		}
	}

	public  void  clearStackActivitiesAndStart( Intent  intent ,  Bundle bundle )
	{
		LinkedList<Activity>  activities  = new  LinkedList<Activity>( AbstractActivity.STACK );

		ActivityCompat.startActivity(  activities.isEmpty()  ? Application.this :   activities.getLast(), intent, bundle );

		if( !   activities.isEmpty() )
		{
			for(  Activity  activity : activities  )  activity.finish();
		}
	}

	public  void  onError(   Throwable   throwable )
	{

	}

	public  void  onLogoutComplete( int  code,int  reason )
	{
		//  remove  credentials  if  logout  or  squeezed  off  the  line  by  remote  login  and  skip  to  loginactivity.
		if( code == 200 &&        ( reason == DisconnectAckPacket.REASON_REMOTE_SIGNIN || reason == DisconnectAckPacket.REASON_CLIENT_LOGOUT   ) )
		{
			this.clearStackActivitiesAndStart(new  Intent(this,LoginActivity.class).putExtra("USERNAME",super.getSharedPreferences("LATEST_LOGIN_FORM",MODE_PRIVATE).getString("USERNAME","")).putExtra("RELOGIN_REASON",reason).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );

			super.getSharedPreferences("LATEST_LOGIN_FORM",MODE_PRIVATE).edit().clear().apply();
		}
	}

	public  void  onConnectStateChanged(    ConnectState  connectState )
	{

	}

    public  void  onReceivedOfflineData( OoIData  offline )
    {

    }

	public  HttpUrl.Builder  baseUrl()
	{
		Service  service = this.squirrelClient.getServiceRouteManager().current( Schema.HTTPS );

		return  new  HttpUrl.Builder().scheme(      service.getSchema()).host(service.getHost()).port( service.getPort() );
	}

	public  boolean  onBeforeSend(  Packet  packet )//throws   Throwable
	{
		return  true;
	}

	public  void  onSent(     Packet  sentPacket,TransportState  transportState )
	{

	}

	public  void  onReceived(Packet receivedPacket )
	{
		if( receivedPacket instanceof   CallPacket )
		{
			ActivityCompat.startActivity( this,new  Intent(this,ObjectUtils.cast(receivedPacket,CallPacket.class).getContentType() == CallContentType.AUDIO ? AudioCallActivity.class : VideoCallActivity.class).putExtra("CONTACT_ID",ObjectUtils.cast(receivedPacket,CallPacket.class).getContactId()).putExtra("CALL_TYPE",ObjectUtils.cast(receivedPacket,CallPacket.class).getContentType().getValue()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("CALLED",true),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );
		}
		else
		if( receivedPacket instanceof   ChatPacket )
		{
			PushServiceNotifier.INSTANCE.notify( new  NewsProfile(ObjectUtils.cast(receivedPacket,ChatPacket.class).getContactId(),new  Timestamp(DateTime.now().getMillis()),PAIPPacketType.CHAT.getValue(),ObjectUtils.cast(receivedPacket,ChatPacket.class).getContactId(),ObjectUtils.cast(receivedPacket,ChatPacket.class).getContentType() == ChatContentType.WORDS ? new  String(ObjectUtils.cast(receivedPacket,ChatPacket.class).getContent()) : super.getString(NewsProfileListAdapter.NEWS_PROFILE_PLACEHOLDERS.get(ObjectUtils.cast(receivedPacket,ChatPacket.class).getContentType().getPlaceholder())),0) );
		}
	}
}