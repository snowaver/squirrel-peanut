package cc.mashroom.squirrel.parent;

import  android.content.Context;
import  android.content.Intent;
import  android.content.SharedPreferences;
import  android.location.Location;
import  android.net.ConnectivityManager;
import  android.net.NetworkRequest;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;

import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.facebook.drawee.backends.pipeline.Fresco;
import  com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import  com.facebook.imagepipeline.core.ImagePipelineConfig;
import  com.google.common.collect.Lists;
import  com.irozon.sneaker.Sneaker;

import  org.joda.time.DateTime;
import  org.webrtc.PeerConnection;

import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.util.DensityUtils;
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
import  cc.mashroom.squirrel.module.home.activity.SheetActivity;
import  cc.mashroom.squirrel.module.system.activity.LoadingActivity;
import  cc.mashroom.squirrel.module.system.activity.LoginActivity;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.squirrel.push.PushService;
import  cc.mashroom.squirrel.push.PushServiceNotifier;

import  java.net.URL;
import  java.util.List;
import  java.util.concurrent.ScheduledThreadPoolExecutor;
import  java.util.concurrent.TimeUnit;
import  java.util.concurrent.atomic.AtomicInteger;

import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.call.CallPacket;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
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
	public  static  List<PeerConnection.IceServer>  ICE_SERVERS = Lists.newArrayList( new  PeerConnection.IceServer("stun:118.24.16.67:3478"),new  PeerConnection.IceServer("stun:118.24.19.163:3478"),new  PeerConnection.IceServer("stun:118.25.216.217:3478"),new  PeerConnection.IceServer("stun:23.21.150.121"),new  PeerConnection.IceServer("stun:stun.l.google.com:19302") );

	public  static  String  BALANCING_PROXY_URL     = "http://10.208.60.191:8011/system/balancingproxy?action=1&keyword=0";

	public  static  List<String>  BALANCING_PROXY_BACKUP_ADDRESSES       = Lists.newArrayList( "118.24.16.67", "118.24.19.163","118.25.216.217" );

    public  final  static  Map<String,Integer>  PLACEHOLDER_PROFILES = new  HashMap<String,Integer>().addEntry("&0b01;",R.string.image_message).addEntry("&0b02;",R.string.audio_message).addEntry("&0b03;",R.string.video_message).addEntry( "&0b00;",R.string.contact_added );

    @SneakyThrows
	public  void  onCreate()
	{
		super.onCreate();

		Toasty.Config.getInstance().setTextSize(14 ).allowQueue( false ).apply();
		/*
		Toasty.Config.getInstance().setErrorColor(super.getResources().getColor( R.color.red ) ).setTextSize( 14 ).apply();
		*/
		setSquirrelClient( new  SquirrelClient(Application.this,getCacheDir()) );

		PacketEventDispatcher.addListener(   this );

		PushServiceNotifier.INSTANCE.install(this );

		SharedPreferences  sharedPreferences  = getSharedPreferences( "LOGIN_FORM",MODE_PRIVATE );

		if( sharedPreferences.getLong("ID",0) >= 1 )
		{
			super.startService(new  Intent(this,PushService.class) );

			//  scheduler  job  will  not  be  executed  on  xiaomi  (4a)  and   vivo  (y66)  after  pushing  off  the  application,  so  the  proposal  using  scheduler  job  is  not  available  to  keep  application  alive.  give  up.
            /*
            JobInfo  applicationProcessKeeperJobInfo = new  JobInfo.Builder(PROCESS_KEEPER_SCHEDUAL_JOB_ID.incrementAndGet(),new  ComponentName(super.getPackageName(),ProcessKeeper.class.getName())).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true).setRequiresCharging(false).setRequiresDeviceIdle(false).setMinimumLatency(1000).setOverrideDeadline(15*1000).build();

            ObjectUtils.cast(super.getSystemService(Context.JOB_SCHEDULER_SERVICE),JobScheduler.class).schedule(applicationProcessKeeperJobInfo );
            */
			Storage.INSTANCE.initialize( getSquirrelClient(),this,super.getCacheDir(),(new  HashMap()).addEntry("ID"  , getSharedPreferences("LOGIN_FORM",MODE_PRIVATE).getLong("ID",0)) );

			this.getSquirrelClient().route( sharedPreferences.getString("HOST",null) , sharedPreferences.getInt("PORT",8012) ,  sharedPreferences.getInt("HTTPPORT",8011) );
		}
		else
		{
			URL  balancingProxyUrl = new  URL( BALANCING_PROXY_URL );

			squirrelClient.route( balancingProxyUrl.getHost(),8012, balancingProxyUrl.getPort() );
		}

		Fresco.initialize( this,OkHttpImagePipelineConfigFactory.newBuilder(this,new  OkHttpClient.Builder().hostnameVerifier(new  NoopHostnameVerifier()).sslSocketFactory(SquirrelClient.SSL_CONTEXT.getSocketFactory(),new  NoopX509TrustManager()).connectTimeout(2,TimeUnit.SECONDS).writeTimeout(2,TimeUnit.SECONDS).readTimeout(8,TimeUnit.SECONDS).addInterceptor((chain) -> chain.proceed(chain.request().newBuilder().header("SECRET_KEY",getSquirrelClient().getUserMetadata().getString("SECRET_KEY") == null ? "" : getSquirrelClient().getUserMetadata().getString("SECRET_KEY")).build())).build()).build() );

		RetrofitRegistry.install(Application.this );

		getSquirrelClient().route( new  DefaultBalancingProxyFactory(new  URL(BALANCING_PROXY_URL),BALANCING_PROXY_BACKUP_ADDRESSES,SquirrelClient.SSL_CONTEXT.getSocketFactory(),5,TimeUnit.SECONDS), Application.this );

		ObjectUtils.cast(super.getSystemService(Context.CONNECTIVITY_SERVICE),ConnectivityManager.class).requestNetwork( new  NetworkRequest.Builder().build(),new  ConnectivityStateListener(Application.this) );
	}

	public  final  static  AtomicInteger  PROCESS_KEEPER_SCHEDUAL_JOB_ID= new  AtomicInteger( 0 );
	@Accessors( chain=true )
	@Getter
	@Setter
	private  SquirrelClient  squirrelClient  = null;
	@Getter
	private  ScheduledThreadPoolExecutor  executor    = new  ScheduledThreadPoolExecutor( 1 , new  DefaultThreadFactory( "SQUIRREL-SCHEDULER" ) );
	@Accessors( chain=true )
	@Setter
	@Getter
	private  UIProgressDialog  connectWaitingDialog;

	public   Map<String,Object>    getUserMetadata()
	{
		return  !squirrelClient.getUserMetadata().isEmpty() ? squirrelClient.getUserMetadata() : (super.getSharedPreferences("LOGIN_FORM",MODE_PRIVATE).getLong("ID",0) <= 0 ? null : User.dao.getOne("SELECT  ID,USERNAME,NAME,NICKNAME  FROM  "+User.dao.getDataSourceBind().table()+"  ORDER  BY  LAST_ACCESS_TIME  DESC  LIMIT  1  OFFSET  0",new  Object[]{}));
	}

	public      HttpUrl.Builder  baseUrl()
	{
		return  new  HttpUrl.Builder().scheme("https").host(squirrelClient.getHost()).port( squirrelClient.getHttpPort() );
	}

	public  void  onTerminate()
	{
		super.onTerminate();

		squirrelClient.close();

		executor.shutdown();
	}

	public  void  onAuthenticateComplete(int  code )
	{
		if( connectWaitingDialog != null )
		{
			getMainLooperHandler().post(   () -> connectWaitingDialog.cancel() );
		}

		if( code    == 200 )
		{
			super.startService(new  Intent(this,PushService.class) );

			super.getSharedPreferences("LOGIN_FORM",MODE_PRIVATE).edit().putLong("ID",getUserMetadata().getLong("ID")).putString("USERNAME",getUserMetadata().getString("USERNAME")).putString("NAME",getUserMetadata().getString("NAME")).putString("NICKNAME",getUserMetadata().getString("NICKNAME")).putString("HOST",squirrelClient.getHost()).putInt("PORT",squirrelClient.getPort()).putInt("HTTPPORT",squirrelClient.getHttpPort()).commit();
			//  skipping  to  sheet  activity  action  is  yield  to  loading  activity  when  connecting  by  native  stored  credentials,  which  is  deprecated  on  2018.10.27.  new  rule  is  skipping  to  sheet  activity  directly  if  actiivty  stack  top  is  loading  or  login  activity.
			if( ! AbstractActivity.STACK.isEmpty() && ( /* AbstractActivity.STACK.getLast() instanceof LoadingActivity || */  AbstractActivity.STACK.getLast() instanceof LoginActivity ) )
			{
				Stream.forEach(    AbstractActivity.STACK,(trmActivity) -> trmActivity.finish() );

				ActivityCompat.startActivity( this,new  Intent(this,SheetActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.fade_in,R.anim.fade_out).toBundle() );
			}
		}
		else
		if( code    == 601 )
		{
			if( ! AbstractActivity.STACK.isEmpty() )
			{
				//  remove  credentials  and  skip  to  login  activity  if  connecting  by  native  stored  credentials  is  failed,  possible  reason  is  that  credentials  stored  is  expired  or  not  available  any  more.
				if( !(AbstractActivity.STACK.getLast() instanceof LoadingActivity  || AbstractActivity.STACK.getLast() instanceof LoginActivity) )
				{
					Stream.forEach(AbstractActivity.STACK,(trmActivity) -> trmActivity.finish() );

					ActivityCompat.startActivity( this,new  Intent(this,LoginActivity.class).putExtra("USERNAME",super.getSharedPreferences("LOGIN_FORM",MODE_PRIVATE).getString("USERNAME","")).putExtra("RELOGIN_REASON" , 0).addFlags( Intent.FLAG_ACTIVITY_NEW_TASK ),null );
				}
				else
				{
					getMainLooperHandler().post( () -> ObjectUtils.cast(AbstractActivity.STACK.getLast(),AbstractActivity.class).showSneakerWindow(Sneaker.with(AbstractActivity.STACK.getLast()),com.irozon.sneaker.R.drawable.ic_error,R.string.authenticate_failed,R.color.white,R.color.red) );
				}

				getSharedPreferences("LOGIN_FORM",MODE_PRIVATE).edit().remove("ID").remove("USERNAME").remove("NAME").remove("NICKNAME").commit();
			}
		}
		else
		{
			if( ! AbstractActivity.STACK.isEmpty() )
			{
				if(     AbstractActivity.STACK.getLast() instanceof LoadingActivity || AbstractActivity.STACK.getLast() instanceof LoginActivity )
				{
					getMainLooperHandler().post( () -> ObjectUtils.cast(AbstractActivity.STACK.getLast(),AbstractActivity.class).showSneakerWindow(Sneaker.with(AbstractActivity.STACK.getLast()),com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red) );
				}
			}
		}
	}

	@SneakyThrows
	public  void  connect( String  username,String  password,Location  geometryLoc,UIProgressDialog  connectWaitingDialog )
	{
		if( !NetworkUtils.isNetworkAvailable(this) )
		{
			return;
		}

		if( connectWaitingDialog != null )
		{
			this.setConnectWaitingDialog(  connectWaitingDialog).getConnectWaitingDialog().show();
		}

		squirrelClient.asynchronousConnect( username,password,geometryLoc == null ? null : geometryLoc.getLongitude(),geometryLoc == null ? null : geometryLoc.getLatitude(),NetworkUtils.getMac(),this );
	}

	@SneakyThrows
	public  void  connect( Long  id,Location  geometryLoc,UIProgressDialog  connectWaitingDialog )
	{
		if( !NetworkUtils.isNetworkAvailable(this) )
		{
			return;
		}

		if( connectWaitingDialog != null )
		{
			this.setConnectWaitingDialog( connectWaitingDialog ).getConnectWaitingDialog().show();
		}

		squirrelClient.asynchronousConnect( id,geometryLoc == null ? null : geometryLoc.getLongitude(),geometryLoc == null ? null : geometryLoc.getLatitude(),NetworkUtils.getMac(),this );
	}

	public  void  onReceiveOfflineData( boolean  finished )
	{

	}

	public  void  onDisconnected(  boolean  active )
	{
		//  active  logout  is  delivered  to  http  request  to  avoid  endless  waiting  if  active  disconnect  ack  packet  lost  on  network.
		if( active )
		{
			return ;
		}
		//  remove  credentials  if  logout  or  squeezed  off  the  line  by  remote  login  and  skip  to  loginactivity.
		Stream.forEach( AbstractActivity.STACK,(activity) -> activity.finish() );

		ActivityCompat.startActivity( this,new  Intent(this,LoginActivity.class).putExtra("USERNAME",super.getSharedPreferences("LOGIN_FORM",MODE_PRIVATE).getString("USERNAME","")).putExtra("RELOGIN_REASON",!active ? 1 : 0).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),null );

		super.getSharedPreferences("LOGIN_FORM",MODE_PRIVATE).edit().remove( "ID" ).remove("USERNAME").remove("NAME").remove("NICKNAME").commit();
	}

	public  boolean  beforeSend(    Packet  packet )throws  Exception
	{
		return  true;
	}

	public  void  sent(       Packet  sentPacket,TransportState  transportState )throws  Exception
	{

	}

	public  void  received( Packet  receivedPacket )throws  Exception
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

	public  void  onBalanceComplete( int  code,Throwable  throwable )
	{
		RetrofitRegistry.install(Application.this );

		SharedPreferences  sharedPreferences  = getSharedPreferences( "LOGIN_FORM",MODE_PRIVATE );

		if( code != 200 )
		{
			//  use   default  balancing  proxy  or  last  fetched  proxy  instead  of  returning.
		}
		else
		{
			sharedPreferences.edit().putString("HOST",getSquirrelClient().getHost()).putInt("PORT",getSquirrelClient().getPort()).putInt( "HTTPPORT" , getSquirrelClient().getHttpPort() );
		}

		if( sharedPreferences.getLong("ID",0) >= 1 )
		{
			connect( sharedPreferences.getLong("ID",0),NetworkUtils.getLocation(this),connectWaitingDialog = AbstractActivity.STACK.isEmpty() ? null : new  UIProgressDialog.WeBoBuilder(AbstractActivity.STACK.get(0)).setTextSize(18).setMessage(R.string.waiting).setCancelable(false).setCanceledOnTouchOutside(false).create().setHeight(DensityUtils.px(this,140)) );
		}
	}
}