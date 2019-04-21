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

import  android.content.Context;
import  android.content.Intent;
import  android.content.SharedPreferences;
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
import  cc.mashroom.router.BalancerStateListener;
import  cc.mashroom.router.DefaultBalancingProxyFactory;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.LifecycleListener;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.connect.PacketEventDispatcher;
import  cc.mashroom.squirrel.client.connect.PacketListener;
import  cc.mashroom.squirrel.client.storage.Storage;
import  cc.mashroom.squirrel.client.storage.model.chat.NewsProfile;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.chat.activity.AudioCallActivity;
import  cc.mashroom.squirrel.module.chat.activity.VideoCallActivity;
import  cc.mashroom.squirrel.module.system.activity.LoadingActivity;
import  cc.mashroom.squirrel.module.system.activity.LoginActivity;
import  cc.mashroom.squirrel.module.system.activity.RegisterActivity;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.push.PushService;
import  cc.mashroom.squirrel.push.PushServiceNotifier;

import  java.net.URL;
import  java.util.List;
import  java.util.Set;
import  java.util.concurrent.ScheduledThreadPoolExecutor;
import  java.util.concurrent.TimeUnit;
import  java.util.concurrent.atomic.AtomicInteger;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.util.LocaleUtils;
import  cc.mashroom.util.NoopHostnameVerifier;
import  cc.mashroom.util.NoopX509TrustManager;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.util.stream.Stream;
import  es.dmoral.toasty.Toasty;
import  io.netty.util.concurrent.DefaultThreadFactory;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;
import  okhttp3.HttpUrl;
import  okhttp3.OkHttpClient;

public  class  Application  extends  cc.mashroom.hedgehog.parent.Application  implements  BalancerStateListener, LifecycleListener, PacketListener
{
	public  static  List<PeerConnection.IceServer>  ICE_SERVERS = Lists.newArrayList( new  PeerConnection.IceServer("stun:47.105.210.154:3478"),new  PeerConnection.IceServer("stun:stun.l.google.com:19302"),new  PeerConnection.IceServer("turn:47.105.210.154:3478","snowaver","snowaver") );

	public  static  String  BALANCING_PROXY_URL     ="https://192.168.1.114:8011/system/balancingproxy?action=1&keyword=0";

	public  static  List<String>  BALANCING_PROXY_BACKUP_ADDRESSES       = Lists.newArrayList( "118.24.16.67", "118.24.19.163","118.25.216.217" );

    public  static  final  Map<String,Integer>  PLACEHOLDER_PROFILES = new  HashMap<String,Integer>().addEntry("&0b01;",R.string.chat_image_message).addEntry("&0b02;",R.string.chat_audio_message).addEntry("&0b03;",R.string.chat_video_message).addEntry( "&0b00;",R.string.subscribe_contact_added );

    @SneakyThrows
	public  void  onCreate()
	{
		super.onCreate();

		this.squirrelClient = new  SquirrelClient(  this , super.getCacheDir() );

		PacketEventDispatcher.addListener(   this );

		LocaleUtils.change( this , null );

		Toasty.Config.getInstance().setTextSize(14 ).allowQueue( false ).apply();

		PushServiceNotifier.INSTANCE.install(this );

		Long  userId = super.getSharedPreferences("LATEST_LOGIN_FORM",MODE_PRIVATE).getLong("ID",0);

		if( userId >= 1 )
		{
			super.startService(  new  Intent(this,PushService.class) );

			SharedPreferences  networkSharedPreference = super.getSharedPreferences( "LATEST_NETWORK_ROUTE",MODE_PRIVATE );

			//  scheduler  job  will  not  be  executed  on  xiaomi  (4a)  and   vivo  (y66)  after  pushing  off  the  application,  so  the  proposal  using  scheduler  job  is  not  available  to  keep  application  alive.  give  up.
            /*
            JobInfo  applicationProcessKeeperJobInfo = new  JobInfo.Builder(PROCESS_KEEPER_SCHEDUAL_JOB_ID.incrementAndGet(),new  ComponentName(super.getPackageName(),ProcessKeeper.class.getName())).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true).setRequiresCharging(false).setRequiresDeviceIdle(false).setMinimumLatency(1000).setOverrideDeadline(15*1000).build();

            ObjectUtils.cast(super.getSystemService(Context.JOB_SCHEDULER_SERVICE),JobScheduler.class).schedule(applicationProcessKeeperJobInfo );
            */
			Storage.INSTANCE.initialize( squirrelClient, null, super.getCacheDir(), new  HashMap().addEntry("ID",userId) );

			squirrelClient.route( networkSharedPreference.getString("HOST",null),networkSharedPreference.getInt("PORT",8012),networkSharedPreference.getInt("HTTPPORT",8011) );
		}
		else
		{
			URL  balancingProxyUrl   = new  URL( BALANCING_PROXY_URL );

			squirrelClient.route( balancingProxyUrl.getHost(),  8012, balancingProxyUrl.getPort() );
		}

		RetrofitRegistry.install( Application.this);

		Fresco.initialize( this,OkHttpImagePipelineConfigFactory.newBuilder(this,new  OkHttpClient.Builder().hostnameVerifier(new  NoopHostnameVerifier()).sslSocketFactory(SquirrelClient.SSL_CONTEXT.getSocketFactory(),new  NoopX509TrustManager()).connectTimeout(2,TimeUnit.SECONDS).writeTimeout(2,TimeUnit.SECONDS).readTimeout(8,TimeUnit.SECONDS).addInterceptor((chain) -> chain.proceed(chain.request().newBuilder().header("SECRET_KEY",squirrelClient.getUserMetadata().getString("SECRET_KEY") == null ? "" : squirrelClient.getUserMetadata().getString("SECRET_KEY")).build())).build()).build() );

		squirrelClient.route( new  DefaultBalancingProxyFactory(new  URL(BALANCING_PROXY_URL),BALANCING_PROXY_BACKUP_ADDRESSES,SquirrelClient.SSL_CONTEXT.getSocketFactory(),5,TimeUnit.SECONDS),this );

		ObjectUtils.cast(super.getSystemService(Context.CONNECTIVITY_SERVICE),ConnectivityManager.class).requestNetwork( new  NetworkRequest.Builder().build(),new  ConnectivityStateListener( this ) );
	}

	private  Set<Class>  authenticateNeedlessActivityClasses = Sets.newHashSet(LoadingActivity.class,LoginActivity.class,RegisterActivity.class );

	public  final  static  AtomicInteger  PROCESS_KEEPER_SCHEDUAL_JOB_ID  = new  AtomicInteger( 0 );
	@Accessors(  chain = true )
	@Getter
	@Setter
	private  SquirrelClient  squirrelClient  = null;
	@Getter
	private  ScheduledThreadPoolExecutor  scheduler = new  ScheduledThreadPoolExecutor( 1 , new  DefaultThreadFactory("DEFAULT-TASK-SCHEDULER") );

	public   Map<String,Object>    getUserMetadata()
	{
		return  !squirrelClient.getUserMetadata().isEmpty() ? squirrelClient.getUserMetadata() : (super.getSharedPreferences("LATEST_LOGIN_FORM",MODE_PRIVATE).getLong("ID",0) <= 0 ? null : User.dao.getOne("SELECT  ID,USERNAME,NAME,NICKNAME  FROM  "+User.dao.getDataSourceBind().table()+"  ORDER  BY  LAST_ACCESS_TIME  DESC  LIMIT  1  OFFSET  0",new  Object[]{}));
	}

	public  void  onTerminate()
	{
		super.onTerminate();

		squirrelClient.close();

		scheduler.shutdown(  );
	}

	public      HttpUrl.Builder  baseUrl()
	{
		return  new  HttpUrl.Builder().scheme("https").host(squirrelClient.getHost()).port( squirrelClient.getHttpPort() );
	}

	public  void  onAuthenticateComplete( int  returnCode )
	{
		if( returnCode == 601||  returnCode == 602 )
		{
			super.getSharedPreferences( "LATEST_LOGIN_FORM", MODE_PRIVATE ).edit().clear().commit();

			if( !AbstractActivity.STACK.isEmpty() && !authenticateNeedlessActivityClasses.contains(AbstractActivity.STACK.getLast().getClass() ) )
			{
				clearStackActivitiesAndStart( new  Intent(this,LoginActivity.class).putExtra("USERNAME",super.getSharedPreferences("LOGIN_FORM",MODE_PRIVATE).getString("USERNAME","")).putExtra("RELOGIN_REASON",0).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );
			}
		}
		else
		if( returnCode == 200 )
		{
			super.startService(  new  Intent(this,PushService.class) );

			super.getSharedPreferences("LATEST_LOGIN_FORM", MODE_PRIVATE).edit().putLong("ID",getUserMetadata().getLong("ID")).putString("USERNAME",getUserMetadata().getString("USERNAME")).putString("NAME",getUserMetadata().getString("NAME")).putString("NICKNAME",getUserMetadata().getString("NICKNAME")).commit();
		}
	}

	@SneakyThrows
	public  void  connect( String  username, String  password, Location  geometryLoc,LifecycleListener  lifecycleListener )
	{
		if( !NetworkUtils.isNetworkAvailable(this) )
		{
			return;
		}

		squirrelClient.asynchronousConnect( username,password,geometryLoc == null ? null : geometryLoc.getLongitude(),geometryLoc == null ? null : geometryLoc.getLatitude(),NetworkUtils.getMac(),Lists.newArrayList(this,lifecycleListener) );
	}

	@SneakyThrows
	public  void  connect( Long  id , Location  geometryLoc , LifecycleListener  lifecycleListener )
	{
		if( !NetworkUtils.isNetworkAvailable(this) )
		{
			return;
		}

		squirrelClient.asynchronousConnect( id,geometryLoc == null ? null : geometryLoc.getLongitude(),geometryLoc == null ? null : geometryLoc.getLatitude(),NetworkUtils.getMac(),Lists.newArrayList(this,lifecycleListener) );
	}

	public  void  onReceiveOfflineData( boolean  finished )
	{

	}

	public  void  clearStackActivitiesAndStart( Intent  intent , Bundle  bundle )
	{
		Stream.forEach( AbstractActivity.STACK,(activity) -> activity.finish() );

		ActivityCompat.startActivity( this,intent,bundle );
	}

	public  void  onDisconnected( int  closeReason )
	{
		//  remove  credentials  if  logout  or  squeezed  off  the  line  by  remote  login  and  skip  to  loginactivity.
		if( closeReason    == DisconnectAckPacket.REASON_REMOTE_LOGIN )
		{
			clearStackActivitiesAndStart(     new  Intent(this,LoginActivity.class).putExtra("USERNAME",super.getSharedPreferences("LOGIN_FORM",MODE_PRIVATE).getString("USERNAME","")).putExtra("RELOGIN_REASON",1).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );

			super.getSharedPreferences( "LATEST_LOGIN_FORM", MODE_PRIVATE ).edit().clear().commit();
		}
	}

	public  boolean  beforeSend(    Packet  packet )  throws  Exception
	{
		return  true;
	}

	public  void  sent(       Packet  sentPacket,TransportState  transportState )  throws  Exception
	{

	}

	public  void  onBalanceComplete( int  code , Throwable  throwable )
	{
		RetrofitRegistry.install(Application.this );

		if( code == 200 )
		{
			super.getSharedPreferences("LATEST_NETWORK_ROUTE",MODE_PRIVATE).edit().putString("HOST",squirrelClient.getHost()).putInt("PORT",squirrelClient.getPort()).putInt("HTTPPORT",squirrelClient.getHttpPort()).commit();
		}

		Long  userId = super.getSharedPreferences("LATEST_LOGIN_FORM",MODE_PRIVATE).getLong("ID",0);

		if( userId >= 1 )
		{
			this.connect( userId,NetworkUtils.getLocation(this),this );
		}
	}

	public  void  received( Packet  receivedPacket )  throws  Exception
	{
		if( receivedPacket instanceof   CallPacket )
		{
			ActivityCompat.startActivity( this,new  Intent(this,ObjectUtils.cast(receivedPacket,CallPacket.class).getContentType() == CallContentType.AUDIO ? AudioCallActivity.class : VideoCallActivity.class).putExtra("CONTACT_ID",ObjectUtils.cast(receivedPacket,CallPacket.class).getContactId()).putExtra("CALL_TYPE",ObjectUtils.cast(receivedPacket,CallPacket.class).getContentType().getValue()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("CALLED",true),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );
		}
		else
		if( receivedPacket instanceof   ChatPacket )
		{
			PushServiceNotifier.INSTANCE.notify( ObjectUtils.cast(new  NewsProfile().addEntry("CONTACT_ID",ObjectUtils.cast(receivedPacket,ChatPacket.class).getContactId()).addEntry("CREATE_TIME",DateTime.now().toString("yyyy-MM-dd HH:mm:ss")).addEntry("CONTENT",((ChatPacket) receivedPacket).getContentType() == ChatContentType.WORDS ? new  String(ObjectUtils.cast(receivedPacket,ChatPacket.class).getContent()) : super.getString(PLACEHOLDER_PROFILES.get("&"+StringUtils.leftPad(Integer.toHexString(PAIPPacketType.CHAT.getValue()),2,"0")+StringUtils.leftPad(Integer.toHexString(ObjectUtils.cast(receivedPacket,ChatPacket.class).getContentType().getValue()),2,"0")+";")))) );
		}
	}
}