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
import  cc.mashroom.router.Service;
import  cc.mashroom.router.ServiceChangeEventListener;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.client.event.LifecycleEventListener;
import  cc.mashroom.squirrel.client.event.PacketEventListener;
import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.client.storage.model.chat.NewsProfile;
import  cc.mashroom.squirrel.http.ServiceRegistry;
import  cc.mashroom.squirrel.module.chat.activity.AudioCallActivity;
import  cc.mashroom.squirrel.module.chat.activity.VideoCallActivity;
import  cc.mashroom.squirrel.module.home.tab.newsprofile.adapters.NewsProfileListAdapter;
import  cc.mashroom.squirrel.module.system.activity.NetworkPreinitializeActivity;
import  cc.mashroom.squirrel.module.system.activity.LoginActivity;
import  cc.mashroom.squirrel.module.system.activity.RegisterActivity;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.squirrel.paip.message.connect.DisconnectAckPacket;
import  cc.mashroom.squirrel.push.PushServiceNotifier;

import  java.sql.Timestamp;
import  java.util.LinkedList;
import  java.util.List;
import  java.util.Set;
import  java.util.concurrent.ScheduledThreadPoolExecutor;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.transport.ConnectState;
import  cc.mashroom.squirrel.util.LocaleUtils;
import  cc.mashroom.util.FileUtils;
import  cc.mashroom.util.ObjectUtils;
import  es.dmoral.toasty.Toasty;
import  io.netty.util.concurrent.DefaultThreadFactory;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;
import  okhttp3.HttpUrl;

public  class  Application  extends  cc.mashroom.hedgehog.parent.Application  implements  ServiceChangeEventListener
{
	public  static  List<PeerConnection.IceServer>  ICE_SERVERS = Lists.newArrayList(new  PeerConnection.IceServer("stun:47.105.210.154:3478"),new  PeerConnection.IceServer("stun:stun.l.google.com:19302"),new  PeerConnection.IceServer("turn:47.105.210.154:3478","snowaver","snowaver") );

	public  static  String  SERVICE_LIST_REQUEST_URL      = "https://192.168.1.114:8011/system/service?action=1&keyword=0";
	@Getter
	private  ScheduledThreadPoolExecutor   scheduler = new  ScheduledThreadPoolExecutor( 1,new  DefaultThreadFactory("DEFAULT-TASK-SCHEDULER") );

	@SneakyThrows
	public  void     onCreate()
	{
		super.onCreate();

		LocaleUtils.change(this,null);

		setSquirrelClient(new  SquirrelClient( this,super.setCacheDir(FileUtils.createDirectoryIfAbsent(super.getDir(".squirrel",Context.MODE_PRIVATE))).getCacheDir())).getSquirrelClient().getLifecycleEventDispatcher().addListener( this );

		PushServiceNotifier.INSTANCE.initialize(    this );

		Toasty.Config.getInstance().allowQueue(true).setTextSize(14).setToastTypeface(Typeface.createFromAsset(super.getResources().getAssets(),"font/droid_sans_mono.ttf")).apply();

		ObjectUtils.cast(super.getSystemService(Context.CONNECTIVITY_SERVICE),ConnectivityManager.class).requestNetwork( new  NetworkRequest.Builder().build(),new ConnectivityAndActivityScheduler( this ) );
	}
	@Accessors(  chain = true )
	@Getter
	@Setter
	private  SquirrelClient  squirrelClient = null;

	@Override
	public  void  onTerminate()
	{
		super.onTerminate();

		this.squirrelClient.release();

		this.scheduler.shutdown(/**/);
	}
	@Override
	public  void  onChange( Service  oldService,Service  newService )
	{
		if( oldService != newService )
		{
		Fresco.initialize( Application.this,OkHttpImagePipelineConfigFactory.newBuilder(this,this.squirrelClient.okhttpClient(5,5,10)).build() );

		ServiceRegistry.INSTANCE.initialize(this );
		}
	}

	public  HttpUrl.Builder  baseUrl()
	{
		return  new  HttpUrl.Builder().scheme(System.getProperty("squirrel.dt.server.schema","https")).host(this.squirrelClient.service().getHost()).port( Integer.parseInt(System.getProperty("squirrel.dt.server.port","8011")) );
	}
}