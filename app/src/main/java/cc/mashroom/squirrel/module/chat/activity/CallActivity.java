package cc.mashroom.squirrel.module.chat.activity;

import  android.net.Uri;
import  android.opengl.GLSurfaceView;
import  android.os.Bundle;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.TextView;
import  android.widget.Toast;

import  com.facebook.drawee.view.SimpleDraweeView;

import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.hedgehog.util.ImageUtils;
import  cc.mashroom.hedgehog.widget.ViewSwitcher;
import  cc.mashroom.squirrel.client.connect.call.Call;
import  cc.mashroom.squirrel.client.connect.call.CallState;
import  cc.mashroom.squirrel.client.connect.call.webrtc.PeerConnectionParameters;
import  cc.mashroom.squirrel.client.storage.model.chat.ChatMessage;
import  cc.mashroom.squirrel.client.storage.model.chat.NewsProfile;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.paip.message.call.CloseCallReason;
import  cc.mashroom.squirrel.parent.Application;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.connect.call.CallError;
import  cc.mashroom.squirrel.client.connect.call.CallEventDispatcher;
import  cc.mashroom.squirrel.client.connect.call.CallListener;
import  cc.mashroom.squirrel.parent.AbstractActivity;
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

		this.setContactId(  super.getIntent().getLongExtra("CONTACT_ID", 0)).setCallContentType( CallContentType.valueOf( super.getIntent().getIntExtra("CALL_TYPE" , 0) ) );

		ContextUtils.setupImmerseBar( this );

		super.setContentView( this.callContentType  == CallContentType.AUDIO ? R.layout.activity_audio_call : R.layout.activity_video_call );

		if( this.callContentType == CallContentType.VIDEO )
		{
			VideoRendererGui.setView( ObjectUtils.cast(super.findViewById(R.id.glsurface_view),GLSurfaceView.class) );
		}

		getWindow().getDecorView().setKeepScreenOn( true );

		ViewGroup.LayoutParams  statusBarlayoutParams  = super.findViewById( R.id.status_bar_hint ).getLayoutParams();

		statusBarlayoutParams.height   = ContextUtils.getStatusBarHeight( this );

        ObjectUtils.cast(super.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI( Uri.parse(application().baseUrl().addPathSegments("user/"+contactId+"/portrait").build().toString()) );

		super.findViewById(R.id.status_bar_hint).setLayoutParams( statusBarlayoutParams );

        ObjectUtils.cast(super.findViewById(R.id.nickname),TextView.class).setText( Contact.dao.getContactDirect().get(contactId).getString("REMARK") );

        if( !getIntent().getBooleanExtra("CALLED" , true) )
        {
            ObjectUtils.cast(super.findViewById(R.id.prompt_message),TextView.class).setText(getString(R.string.call_waiting_for_response) );
        }
        else
        {
            ObjectUtils.cast(super.findViewById(R.id.prompt_message),TextView.class).setText(callContentType == CallContentType.AUDIO ? R.string.call_counterparty_demand_a_audio_call_with_you : R.string.call_counterparty_demand_a_video_call_with_you );
        }

        ObjectUtils.cast(findViewById(R.id.cancel_button) , SimpleDraweeView.class).setImageURI( ImageUtils.toUri(this , cc.mashroom.hedgehog.R.drawable.red_placeholder ) );
	}

	private  Map<String,Integer>  closeProfiles = new  HashMap<String,Integer>().addEntry(CloseCallReason.UNKNOWN.getValue()+":"+0,R.string.unknown_error).addEntry(CloseCallReason.UNKNOWN.getValue()+":"+1,R.string.unknown_error).addEntry(CloseCallReason.ROOM_NOT_FOUND.getValue()+":"+0,R.string.call_room_not_found).addEntry(CloseCallReason.ROOM_NOT_FOUND.getValue()+":"+1,R.string.call_room_not_found).addEntry(CloseCallReason.STATE_ERROR.getValue()+":"+0,R.string.call_state_error).addEntry(CloseCallReason.STATE_ERROR.getValue()+":"+1,R.string.call_state_error)
		.addEntry(CloseCallReason.CANCEL.getValue()+":"+0,R.string.call_counterparty_canceled).addEntry(CloseCallReason.CANCEL.getValue()+":"+1,R.string.call_canceled).addEntry(CloseCallReason.TIMEOUT.getValue()+":"+0,R.string.call_counterparty_canceled).addEntry(CloseCallReason.TIMEOUT.getValue()+":"+1,R.string.call_no_response)
		.addEntry(CloseCallReason.REJECT.getValue()+":"+0,R.string.call_counterparty_rejected).addEntry(CloseCallReason.REJECT.getValue()+":"+1,R.string.call_rejected).addEntry(CloseCallReason.NETWORK_ERROR.getValue()+":"+0,R.string.network_or_internal_server_error).addEntry(CloseCallReason.NETWORK_ERROR.getValue()+":"+1,R.string.network_or_internal_server_error);
	private  Map<CallError,Integer>  errors = new  HashMap<CallError,Integer>().addEntry(CallError.OFFLINE,R.string.contact_offline).addEntry(     CallError.NO_RESPONSE , R.string.call_no_response );
	@Accessors( chain= true )
	@Setter
	@Getter
	private  Call  call;
	@Accessors( chain= true )
	@Setter
	private  long  contactId;
	@Accessors( chain= true )
	@Setter
	private  CallContentType     callContentType;

	@SneakyThrows
	public  void  permissionsGranted()
	{
		//  set  audio  manager  mode  as  MODE_IN_CALL  while  the  other  modes  appears  serious  noise  and  echo.
		try
		{
			MultimediaUtils.setupCellphoneMode(     this );
		}
		catch( Throwable  e )
		{
			super.error( e );
		}

		ObjectUtils.cast(super.findViewById(R.id.cancel_button),SimpleDraweeView.class).setOnClickListener( (cancelButton) -> call.close() );

		if( !  getIntent().getBooleanExtra("CALLED",true) )
		{
            application().getSquirrelClient().newCall(-1,this.contactId,callContentType );
		}
		else
		{
			this.setCall(application().getSquirrelClient().getCall()).getCall().initialize( application(),new  PeerConnectionParameters(application(),callContentType == CallContentType.VIDEO,"VP9",1280,720,25,callContentType != CallContentType.VIDEO ? null : VideoRendererGui.create(0,0,100,100),callContentType != CallContentType.VIDEO ? null : VideoRendererGui.create(74-(int)  (((double)  DensityUtils.px(this,10)/super.getResources().getDisplayMetrics().widthPixels)*100),(int)  ((((double)  ContextUtils.getStatusBarHeight(this)+DensityUtils.px(this,5))/super.getResources().getDisplayMetrics().heightPixels)*100)+1,25,25),"opus",1,Application.ICE_SERVERS) );

			application().getMainLooperHandler().post( () -> ObjectUtils.cast(super.findViewById(R.id.control_switcher),ViewSwitcher.class).setDisplayedChild(1) );

			ObjectUtils.cast(super.findViewById(R.id.accept_button),SimpleDraweeView.class).setOnClickListener( (button) -> {  call.accept();  ObjectUtils.cast(super.findViewById(R.id.control_switcher),ViewSwitcher.class).setDisplayedChild(0);} );

			ObjectUtils.cast(super.findViewById(R.id.reject_button),SimpleDraweeView.class).setOnClickListener( (button) -> {  call.reject();  ContextUtils.finish(this);} );
		}
	}

	public  void  onRoomCreated(     Call  call )
	{
		{
			this.setCall(application().getSquirrelClient().getCall()).getCall().initialize( application(),new  PeerConnectionParameters(application(),callContentType == CallContentType.VIDEO,"VP9",1280,720,25,callContentType != CallContentType.VIDEO ? null : VideoRendererGui.create(0,0,100,100),callContentType != CallContentType.VIDEO ? null : VideoRendererGui.create(74-(int)  (((double)  DensityUtils.px(this,10)/super.getResources().getDisplayMetrics().widthPixels)*100),(int)  ((((double)  ContextUtils.getStatusBarHeight(this)+DensityUtils.px(this,5))/super.getResources().getDisplayMetrics().heightPixels)*100)+1,25,25),"opus",1,Application.ICE_SERVERS) ).demand();
		}
	}

	public  void  onStart(Call  call )
	{
		if( this.callContentType == CallContentType.VIDEO )
		{
			this.application().getMainLooperHandler().post(        () -> {super.findViewById(R.id.chronometer).setVisibility( View.VISIBLE );  super.findViewById(R.id.glsurface_view).setVisibility(View.VISIBLE);  super.findViewById(R.id.call_details).setVisibility(View.GONE);} );
		}

		this.application().getMainLooperHandler().post( () -> { ObjectUtils.cast(super.findViewById(R.id.prompt_message) , TextView.class).setText(  R.string.call_calling );  ObjectUtils.cast(super.findViewById(R.id.chronometer),Stopwatch.class).start("HH:mm:ss");} );
	}

	public  void  onError(Call  call,CallError  callError )
	{
		application().getMainLooperHandler().post( () -> Toasty.error(this,super.getString(errors.containsKey(callError) ? errors.get(callError) : R.string.network_or_internal_server_error),Toast.LENGTH_LONG,false).show() );

		ContextUtils.finish(   this );
	}

	protected  void  onDestroy()
	{
		super.onDestroy();

		CallEventDispatcher.removeListener(this);
	}

	public  void  onClose(       Call  call,boolean  proactively,CloseCallReason  reason )
	{
		DateTime  now   = DateTime.now( DateTimeZone.UTC );

		if( reason ==   CloseCallReason.BY_USER )
		{
			NewsProfile.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfile.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{contactId,new  Timestamp(now.getMillis()),PAIPPacketType.CHAT.getValue(),contactId,super.getString(call.getContentType() == CallContentType.AUDIO ? R.string.audio_call : R.string.video_call)+super.getString(R.string.colon)+ObjectUtils.cast(super.findViewById(R.id.chronometer),Stopwatch.class).getText()+super.getString(R.string.comma)+super.getString(proactively ? R.string.call_closed : R.string.call_counterparty_closed),0} );

			ChatMessage.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+ChatMessage.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE)  VALUES  (?,?,?,?,?,?,?)",new  Object[]{now.getMillis(),new  Timestamp(now.getMillis()),contactId,null,ChatContentType.WORDS.getValue(),super.getString(call.getContentType() == CallContentType.AUDIO ? R.string.audio_call : R.string.video_call)+super.getString(R.string.colon)+ObjectUtils.cast(super.findViewById(R.id.chronometer),Stopwatch.class).getText()+super.getString(R.string.comma)+super.getString(proactively ? R.string.call_closed : R.string.call_counterparty_closed),TransportState.SENT.getValue()} );
		}
		else
		if( reason ==   CloseCallReason.TIMEOUT )
		{
			NewsProfile.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfile.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{contactId,new  Timestamp(now.getMillis()),PAIPPacketType.CHAT.getValue(),contactId,super.getString(call.getContentType() == CallContentType.AUDIO ? R.string.audio_call : R.string.video_call)+super.getString(R.string.colon)+super.getString(closeProfiles.get(reason.getValue()+":"+(call.getState() == CallState.REQUESTING ? 1 : 0))),0} );

			ChatMessage.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+ChatMessage.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE)  VALUES  (?,?,?,?,?,?,?)",new  Object[]{now.getMillis(),new  Timestamp(now.getMillis()),contactId,null,ChatContentType.WORDS.getValue(),super.getString(call.getContentType() == CallContentType.AUDIO ? R.string.audio_call : R.string.video_call)+super.getString(R.string.colon)+super.getString(closeProfiles.get(reason.getValue()+":"+(call.getState() == CallState.REQUESTING ? 1 : 0))),TransportState.SENT.getValue()} );
		}
		else
		{
			NewsProfile.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfile.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{contactId,new  Timestamp(now.getMillis()),PAIPPacketType.CHAT.getValue(),contactId,super.getString(call.getContentType() == CallContentType.AUDIO ? R.string.audio_call : R.string.video_call)+super.getString(R.string.colon)+super.getString(closeProfiles.get(reason.getValue()+":"+(proactively ? 1 : 0))),0} );

			ChatMessage.dao.insert( new  Reference<Object>(),"MERGE  INTO  "+ChatMessage.dao.getDataSourceBind().table()+"  (ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE)  VALUES  (?,?,?,?,?,?,?)",new  Object[]{now.getMillis(),new  Timestamp(now.getMillis()),contactId,null,ChatContentType.WORDS.getValue(),super.getString(call.getContentType() == CallContentType.AUDIO ? R.string.audio_call : R.string.video_call)+super.getString(R.string.colon)+super.getString(closeProfiles.get(reason.getValue()+":"+(proactively ? 1 : 0))),TransportState.SENT.getValue()} );
		}

		application().getMainLooperHandler().post( () -> {Toasty.warning( this,super.getString(R.string.call_closed),Toast.LENGTH_LONG,false ).show();  ContextUtils.finish(this);  super.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);} );
	}
}
