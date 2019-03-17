package cc.mashroom.squirrel.module.chat.activity;

import  android.opengl.GLSurfaceView;
import  android.os.Bundle;
import  android.view.ViewGroup;
import  android.widget.Toast;

import  com.facebook.drawee.view.SimpleDraweeView;

import  cc.mashroom.hedgehog.util.ImageUtils;
import  cc.mashroom.hedgehog.widget.ViewSwitcher;
import  cc.mashroom.squirrel.client.connect.call.Call;
import  cc.mashroom.squirrel.client.connect.call.CallState;
import  cc.mashroom.squirrel.client.connect.call.webrtc.PeerConnectionParameters;
import  cc.mashroom.squirrel.client.storage.model.chat.ChatMessage;
import  cc.mashroom.squirrel.client.storage.model.chat.NewsProfile;
import  cc.mashroom.squirrel.parent.Application;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.connect.call.CallError;
import  cc.mashroom.squirrel.client.connect.call.CallEventDispatcher;
import  cc.mashroom.squirrel.client.connect.call.CallListener;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.paip.message.call.Candidate;
import  cc.mashroom.squirrel.paip.message.call.SDP;
import  cc.mashroom.squirrel.paip.message.call.CallAckPacket;
import  cc.mashroom.hedgehog.widget.Stopwatch;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.Reference;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;
import  org.webrtc.VideoRendererGui;

import  java.sql.Timestamp;

import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.util.MultimediaUtils;
import  es.dmoral.toasty.Toasty;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;

public  class  CallActivity   extends  AbstractActivity  implements  CallListener
{
	protected  void  onCreate( Bundle  savedInstanceState )
	{
		CallEventDispatcher.addListener(  this );

		super.onCreate( savedInstanceState );

		this.setContactId(super.getIntent().getLongExtra("CONTACT_ID", 0)).setCallContentType( CallContentType.valueOf( super.getIntent().getIntExtra("CALL_TYPE" , 0) ) );

		ContextUtils.setupImmerseBar( this );

		super.setContentView( this.callContentType== CallContentType.AUDIO ? R.layout.activity_audio_call : R.layout.activity_video_call );

		if( this.callContentType == CallContentType.VIDEO )
		{
			VideoRendererGui.setView( ObjectUtils.cast(super.findViewById(R.id.glsurface_view),GLSurfaceView.class) );
		}

		getWindow().getDecorView().setKeepScreenOn( true );
		/*
		ObjectUtils.cast( super.findViewById(R.id.glsurface_view) , GLSurfaceView.class ).setRenderer( new  CallBlankRenderer() );
		*/
		ViewGroup.LayoutParams  statusBarlayoutParams  = super.findViewById( R.id.status_bar_hint ).getLayoutParams();

		statusBarlayoutParams.height   = ContextUtils.getStatusBarHeight( this );

		ObjectUtils.cast(findViewById(R.id.cancel_button) , SimpleDraweeView.class).setImageURI( ImageUtils.toUri(this, cc.mashroom.hedgehog.R.drawable.red_placeholder) );

		super.findViewById(R.id.status_bar_hint).setLayoutParams( statusBarlayoutParams );


	}

    @Accessors( chain= true )
    @Setter
    private  CallContentType     callContentType;

	private  Map<CallState,Integer>  closeProfiles = new  HashMap<CallState,Integer>().addEntry(CallState.REQUESTING,R.string.canceled).addEntry(CallState.REQUESTED,R.string.counterpart_canceled).addEntry(CallState.REJECT,R.string.rejected).addEntry( CallState.REJECTED,R.string.counterpart_reject );

	private  Map<CallError,Integer>  errors = new  HashMap<CallError,Integer>().addEntry(CallError.OFFLINE,R.string.contact_offline).addEntry( CallError.NO_RESPONSE,R.string.call_no_response );
	@Accessors( chain= true )
	@Setter
	@Getter
	private  Call  call;
	@Accessors( chain= true )
	@Setter
	private  long  contactId;

	@SneakyThrows
	public  void  permissionsGranted()
	{
		/*
		if( this.callContentType == CallContentType.AUDIO )
		{
			ObjectUtils.cast(super.findViewById(R.id.glsurface_view),GLSurfaceView.class).setVisibility(  View.GONE );
		}
		else
		{
			VideoRendererGui.setView( ObjectUtils.cast(super.findViewById(R.id.glsurface_view),GLSurfaceView.class) );
		}
		*/
		MultimediaUtils.setupCellphoneMode(this);

		ObjectUtils.cast(super.findViewById(R.id.cancel_button),SimpleDraweeView.class).setOnClickListener( (v) -> call.close() );
		/*
		Size  videoSize = CameraCaptureUtils.getOptimalSize( new  Size(super.getResources().getDisplayMetrics().widthPixels,super.getResources().getDisplayMetrics().heightPixels),ObjectUtils.cast(super.getSystemService(Context.CAMERA_SERVICE),CameraManager.class).getCameraCharacteristics(String.valueOf(CameraCharacteristics.LENS_FACING_BACK)).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(android.media.MediaRecorder.class) );
		*/
		if( ! getIntent().getBooleanExtra("CALLED", true) )
		{
			if( application().getSquirrelClient().newCall( DateTime.now(DateTimeZone.UTC).getMillis(),contactId,callContentType ) == null )
			{
				throw  new  IllegalStateException( "SQUIRREL-CLIENT:  ** CALL  ACTIVITY **  calling  state  error." );
			}

			this.setCall(application().getSquirrelClient().getCall()).getCall().initialize( application(),new  PeerConnectionParameters(application(),callContentType == CallContentType.VIDEO,"VP9",1280,720,25,callContentType != CallContentType.VIDEO ? null : VideoRendererGui.create(0,0,100,100),callContentType != CallContentType.VIDEO ? null : VideoRendererGui.create(75,(int)  (((double)  ContextUtils.getStatusBarHeight(this)/super.getResources().getDisplayMetrics().heightPixels)*100)+1,25,25),"opus",1,Application.ICE_SERVERS) ).demand();
		}
		else
		{
			this.setCall(application().getSquirrelClient().getCall()).getCall().initialize( application(),new  PeerConnectionParameters(application(),callContentType == CallContentType.VIDEO,"VP9",1280,720,25,callContentType != CallContentType.VIDEO ? null : VideoRendererGui.create(0,0,100,100),callContentType != CallContentType.VIDEO ? null : VideoRendererGui.create(75,(int)  (((double)  ContextUtils.getStatusBarHeight(this)/super.getResources().getDisplayMetrics().heightPixels)*100)+1,25,25),"opus",1,Application.ICE_SERVERS) );

			ObjectUtils.cast(super.findViewById(R.id.control_switcher),    ViewSwitcher.class).setDisplayedChild( 1 );

			ObjectUtils.cast(super.findViewById(R.id.accept_button),SimpleDraweeView.class).setOnClickListener( (button) -> {call.accept();  ObjectUtils.cast(super.findViewById(R.id.control_switcher),ViewSwitcher.class).setDisplayedChild(0);} );

			ObjectUtils.cast(super.findViewById(R.id.reject_button),SimpleDraweeView.class).setOnClickListener( (button) -> {call.reject();  ContextUtils.finish(this);} );
		}
	}

	public  void  onStart(  long  callId, long  contactId )
	{
		this.application().getMainLooperHandler().post( () -> ObjectUtils.cast(super.findViewById(R.id.chronometer),Stopwatch.class).start("HH:mm:ss") );
	}

	public  void  onReceivedSdp( long  callId,long  contactId,SDP  sdp )
	{

	}

	public  void  onWaitingResponse( long  callId,long  contactId )
	{

	}

	public  void  onError( long  callId,long  contactId,final  CallError  error )
	{
		application().getMainLooperHandler().post( () -> Toasty.error(this,super.getString(errors.containsKey(error) ? errors.get(error) : R.string.network_or_internal_server_error),Toast.LENGTH_LONG,false).show() );

		ContextUtils.finish(   this );
	}

	protected  void  onDestroy()
	{
		super.onDestroy();

		CallEventDispatcher.removeListener(this);
	}

	public  void  onReceivedCandidate( long  callID,long  contactID,Candidate  candidate )
	{

	}

	public  void  onResponded(   long  callId,long  contactId,int  responseCode )
	{
		//  deliver  the  reject  prompt  to  close  event.
		if( responseCode!= CallAckPacket.ACCEPT )    ContextUtils.finish( this );
	}

	public  void  onClose( long  callID,final  long  contactID,final  boolean  proactive,final  CallState  callState )
	{
		DateTime  now   = DateTime.now( DateTimeZone.UTC );

		if( !( "00:00:00".equals(ObjectUtils.cast(super.findViewById(R.id.chronometer),Stopwatch.class).getText()) ) )
		{
			NewsProfile.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfile.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{contactId,new  Timestamp(now.getMillis()),PAIPPacketType.CHAT.getValue(),contactId,super.getString(call.getContentType() == CallContentType.AUDIO ? R.string.audio_call : R.string.video_call)+super.getString(R.string.colon)+ObjectUtils.cast(super.findViewById(R.id.chronometer),Stopwatch.class).getText()+super.getString(R.string.comma)+super.getString(R.string.closed),0} );

			ChatMessage.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+ChatMessage.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE)  VALUES  (?,?,?,?,?,?,?)",new  Object[]{now.getMillis(),new  Timestamp(now.getMillis()),contactId,null,ChatContentType.WORDS.getValue(),super.getString(call.getContentType() == CallContentType.AUDIO ? R.string.audio_call : R.string.video_call)+super.getString(R.string.colon)+ObjectUtils.cast(super.findViewById(R.id.chronometer),Stopwatch.class).getText()+super.getString(R.string.comma)+super.getString(proactive ? R.string.closed : R.string.counterpart_closed),TransportState.SENT.getValue()} );
		}
		else
		if( this.closeProfiles.containsKey(   callState ) )
		{
			NewsProfile.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfile.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{contactId,new  Timestamp(now.getMillis()),PAIPPacketType.CHAT.getValue(),contactId,super.getString(call.getContentType() == CallContentType.AUDIO ? R.string.audio_call : R.string.video_call)+super.getString(R.string.colon)+super.getString(closeProfiles.get(callState)),0} );

			ChatMessage.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+ChatMessage.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE)  VALUES  (?,?,?,?,?,?,?)",new  Object[]{now.getMillis(),new  Timestamp(now.getMillis()),contactId,null,ChatContentType.WORDS.getValue(),super.getString(call.getContentType() == CallContentType.AUDIO ? R.string.audio_call : R.string.video_call)+super.getString(R.string.colon)+super.getString(closeProfiles.get(callState)),TransportState.SENT.getValue()} );
		}

		application().getMainLooperHandler().post( ()->{Toasty.warning( this,super.getString(R.string.call_closed),Toast.LENGTH_LONG,false ).show();  ContextUtils.finish(this);  super.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);} );
	}
}
