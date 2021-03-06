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
package cc.mashroom.squirrel.module.chat.activity;

import  android.opengl.GLSurfaceView;
import  android.os.Bundle;
import  android.os.SystemClock;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.TextView;
import  android.widget.Toast;

import  com.facebook.drawee.view.SimpleDraweeView;

import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.hedgehog.util.ImageUtils;
import  cc.mashroom.hedgehog.widget.HeaderBar;
import  cc.mashroom.hedgehog.widget.ViewSwitcher;
import  cc.mashroom.squirrel.client.connect.call.Call;
import  cc.mashroom.squirrel.client.connect.call.CallState;
import  cc.mashroom.squirrel.client.connect.call.webrtc.ClientObserver;
import  cc.mashroom.squirrel.client.connect.call.webrtc.PeerConnectionParameters;
import  cc.mashroom.squirrel.client.storage.repository.chat.ChatMessageRepository;
import  cc.mashroom.squirrel.client.storage.repository.chat.NewsProfileRepository;
import  cc.mashroom.squirrel.client.storage.repository.user.ContactRepository;
import  cc.mashroom.squirrel.paip.message.call.CloseCallReason;
import  cc.mashroom.squirrel.parent.Application;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.connect.call.CallError;
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
		super.application().getSquirrelClient().getCallEventDispatcher().addListener(  this );

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

		super.findViewById(R.id.status_bar_hint).setLayoutParams(     statusBarlayoutParams );

		ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).setTitle( ContactRepository.DAO.getContactDirect().get(this.contactId).getRemark() );

		ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).setAdditionalText(         "00:00:00" );

		ObjectUtils.cast(super.findViewById(R.id.chronometer),Stopwatch.class).setOnChronometerTickListener( (chronometer) -> ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).setAdditionalText(new  DateTime(SystemClock.elapsedRealtime()-chronometer.getBase(),DateTimeZone.UTC).toString(chronometer.getFormat())) );

        if( !getIntent().getBooleanExtra("CALLED" , true) )
        {
            ObjectUtils.cast(super.findViewById(R.id.prompt_message),TextView.class).setText(getString(R.string.call_waiting_for_response) );
        }
        else
        {
            ObjectUtils.cast(super.findViewById(R.id.prompt_message),TextView.class).setText(callContentType == CallContentType.AUDIO ? R.string.call_peer_demand_a_audio_call_with_you : R.string.call_peer_demand_a_video_call_with_you );
        }

        ObjectUtils.cast(findViewById(R.id.cancel_button) , SimpleDraweeView.class).setImageURI( ImageUtils.toUri(this , cc.mashroom.hedgehog.R.drawable.red_placeholder ) );
	}

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

		super.findViewById(R.id.header_bar).findViewById(R.id.back_switcher).setOnClickListener( (v)-> call.close() );

		if( !  getIntent().getBooleanExtra("CALLED",true) )
		{
            application().getSquirrelClient().newCall( this.contactId, this.callContentType );
		}
		else
		{
			this.setCall(application().getSquirrelClient().getCall()).getCall().initialize( application(),new  PeerConnectionParameters(application(),callContentType == CallContentType.VIDEO,"VP9",1280,720,25,callContentType != CallContentType.VIDEO ? null : VideoRendererGui.create(0,0,100,100),callContentType != CallContentType.VIDEO ? null : VideoRendererGui.create(75-(int)  (((double)  DensityUtils.px(this,10)/super.getResources().getDisplayMetrics().widthPixels)*100),(int)  ((((double)  ContextUtils.getStatusBarHeight(this)+DensityUtils.px(this,50))/super.getResources().getDisplayMetrics().heightPixels)*100)+1,24,24),"opus",1,Application.ICE_SERVERS) );

			application().getMainLooperHandler().post( () -> ObjectUtils.cast(super.findViewById(R.id.control_switcher),ViewSwitcher.class).setDisplayedChild(1) );

			ObjectUtils.cast(super.findViewById(R.id.accept_button),SimpleDraweeView.class).setOnClickListener( (button) -> {   call.agree();  ObjectUtils.cast(super.findViewById(R.id.control_switcher),ViewSwitcher.class).setDisplayedChild(0);} );

			ObjectUtils.cast(super.findViewById(R.id.reject_button),SimpleDraweeView.class).setOnClickListener( (button) -> { call.decline();  ContextUtils.finish(this);} );
		}
	}

	protected  void        onDestroy()
	{
		super.onDestroy();

		super.application().getSquirrelClient().getCallEventDispatcher().removeListener(this);
	}

	public  void  onStart( ClientObserver  call )
	{
		if( this.callContentType == CallContentType.VIDEO )
		{
			this.application().getMainLooperHandler().post( () -> {super.findViewById(R.id.glsurface_view).setVisibility(View.VISIBLE);  super.findViewById(R.id.prompt_message).setVisibility(View.INVISIBLE);} );
		}

		this.application().getMainLooperHandler().post( () -> { ObjectUtils.cast(super.findViewById(R.id.prompt_message) , TextView.class).setText(  R.string.call_calling );  ObjectUtils.cast(super.findViewById(R.id.chronometer),Stopwatch.class).start("HH:mm:ss");} );
	}

	public  void  onRoomCreated(   ClientObserver    call )
	{
		{
			this.setCall(application().getSquirrelClient().getCall()).getCall().initialize( super.application(),new  PeerConnectionParameters(application(),callContentType == CallContentType.VIDEO,"VP9",1280,720,25,callContentType != CallContentType.VIDEO ? null : VideoRendererGui.create(0,0,100,100),callContentType != CallContentType.VIDEO ? null : VideoRendererGui.create(75-(int)  (((double)  DensityUtils.px(this,10)/super.getResources().getDisplayMetrics().widthPixels)*100),(int)  ((((double)  ContextUtils.getStatusBarHeight(this)+DensityUtils.px(this,50))/super.getResources().getDisplayMetrics().heightPixels)*100)+1,24,24),"opus",1,Application.ICE_SERVERS) );  this.call.request();
		}
	}

	public  void  onError( ClientObserver  call,CallError  callError,Throwable     throwable )
	{
		application().getMainLooperHandler().post( () -> Toasty.error(this,super.getString(errors.containsKey(callError) ? errors.get(callError) : R.string.network_or_internal_server_error),Toast.LENGTH_LONG,false).show() );

		ContextUtils.finish(   this );
	}

	public  void  onClose( ClientObserver  call,boolean  proactively,CloseCallReason  reason )
	{
		DateTime  now   = DateTime.now( DateTimeZone.UTC );

		if( reason ==   CloseCallReason.BY_USER )
		{
			NewsProfileRepository.DAO.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{contactId,new  Timestamp(now.getMillis()),PAIPPacketType.CHAT.getValue(),contactId,call.getContentType().getPlaceholder(),0} );

			ChatMessageRepository.DAO.insert( new  Reference<Object>(),"MERGE  INTO  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE,IS_LOCAL,LOCAL_DESCRIPTION)  VALUES  (?,?,?,?,?,?,?,?,?)",new  Object[]{now.getMillis(),new  Timestamp(now.getMillis()),contactId,null,ChatContentType.WORDS.getValue(),reason.getPlaceholder(),TransportState.SENT.getValue(),true,"{\"TYPE\":"+(call.getContentType() == CallContentType.AUDIO ? 0 : 1)+",\"REASON_PLACEHOLDER\":\""+reason.getPlaceholder()+"\",\"IS_PROACTIVELY\":"+proactively+",\"CONTENT\":\""+ObjectUtils.cast(super.findViewById(R.id.header_bar).findViewById(R.id.additional_text),TextView.class).getText()+"\"}"} );
		}
		else
		if( reason ==   CloseCallReason.TIMEOUT )
		{
			NewsProfileRepository.DAO.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{contactId,new  Timestamp(now.getMillis()),PAIPPacketType.CHAT.getValue(),contactId,call.getContentType().getPlaceholder(),0} );

			ChatMessageRepository.DAO.insert( new  Reference<Object>(),"MERGE  INTO  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE,IS_LOCAL,LOCAL_DESCRIPTION)  VALUES  (?,?,?,?,?,?,?,?,?)",new  Object[]{now.getMillis(),new  Timestamp(now.getMillis()),contactId,null,ChatContentType.WORDS.getValue(),reason.getPlaceholder(),TransportState.SENT.getValue(),true,"{\"TYPE\":"+(call.getContentType().getValue())+",\"REASON_PLACEHOLDER\":\""+reason.getPlaceholder()+"\",\"IS_PROACTIVELY\":"+(call.getState() == CallState.REQUESTING)+"}"} );
		}
		else
		{
			NewsProfileRepository.DAO.insert( new  Reference<Object>(),"MERGE  INTO  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT)  VALUES  (?,?,?,?,?,?)",new  Object[]{contactId,new  Timestamp(now.getMillis()),PAIPPacketType.CHAT.getValue(),contactId,call.getContentType().getPlaceholder(),0} );

			ChatMessageRepository.DAO.insert( new  Reference<Object>(),"MERGE  INTO  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  (ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE,IS_LOCAL,LOCAL_DESCRIPTION)  VALUES  (?,?,?,?,?,?,?,?,?)",new  Object[]{now.getMillis(),new  Timestamp(now.getMillis()),contactId,null,ChatContentType.WORDS.getValue(),reason.getPlaceholder(),TransportState.SENT.getValue(),true,"{\"TYPE\":"+(call.getContentType().getValue())+",\"REASON_PLACEHOLDER\":\""+reason.getPlaceholder()+"\",\"IS_PROACTIVELY\":"+proactively+"}"} );
		}

		application().getMainLooperHandler().post( () -> {Toasty.warning( this,super.getString(R.string.call_closed),Toast.LENGTH_LONG,false ).show();  ContextUtils.finish(this);  super.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);} );
	}
}