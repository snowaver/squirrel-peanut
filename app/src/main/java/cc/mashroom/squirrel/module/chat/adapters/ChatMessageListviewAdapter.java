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
package cc.mashroom.squirrel.module.chat.adapters;

import  android.content.Intent;
import  android.net.Uri;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.ImageView;
import  android.widget.TextView;

import  com.facebook.drawee.view.SimpleDraweeView;
import  com.fasterxml.jackson.core.type.TypeReference;

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;

import  java.io.File;
import  java.io.IOException;
import java.sql.Timestamp;
import  java.util.LinkedList;

import  cc.mashroom.hedgehog.parent.BaseAdapter;
import  cc.mashroom.hedgehog.widget.TipWindow;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.chat.ChatMessage;
import  cc.mashroom.squirrel.client.storage.repository.chat.ChatMessageRepository;
import  cc.mashroom.squirrel.module.chat.activity.ChatActivity;
import  cc.mashroom.hedgehog.module.common.activity.ImagePreviewActivity;
import  cc.mashroom.hedgehog.module.common.activity.VideoPreviewActivity;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.call.CloseCallReason;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.hedgehog.widget.FlexibleSimpleDraweeView;
import  cc.mashroom.hedgehog.device.MediaPlayer;
import  cc.mashroom.hedgehog.widget.ViewSwitcher;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.util.JsonUtils;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  lombok.AccessLevel;
import  lombok.AllArgsConstructor;
import  lombok.Setter;
import  lombok.Synchronized;
import  lombok.experimental.Accessors;

@AllArgsConstructor

public  class  ChatMessageListviewAdapter  extends  BaseAdapter  <ChatMessage>
{
	public  ChatMessageListviewAdapter(ChatActivity  context,long  contactId )
	{
		this.setContext(context).setContactId(contactId).setItems( new  LinkedList<ChatMessage>() );

		cachePreviousPage();
	}

	protected  Map<Long, ChatMessage>  oqp = new  HashMap<Long,ChatMessage>();

	public  final  static  Map<String, Integer>  CLOSE_PROFILES = new HashMap<String,Integer>().addEntry(CloseCallReason.ROOM_NOT_FOUND.getPlaceholder()+":"+0,R.string.call_room_not_found).addEntry(CloseCallReason.ROOM_NOT_FOUND.getPlaceholder()+":"+1,R.string.call_room_not_found).addEntry(CloseCallReason.STATE_ERROR.getPlaceholder()+":"+0,R.string.call_state_error).addEntry(CloseCallReason.STATE_ERROR.getPlaceholder()+":"+1,R.string.call_state_error)
		.addEntry(CloseCallReason.CANCEL.getPlaceholder()+":"+0,R.string.call_peer_canceled).addEntry(CloseCallReason.CANCEL.getPlaceholder()+":"+1,R.string.call_canceled).addEntry(CloseCallReason.TIMEOUT.getPlaceholder()+":"+0,R.string.call_peer_canceled).addEntry(CloseCallReason.TIMEOUT.getPlaceholder()+":"+1,R.string.call_no_response).addEntry(CloseCallReason.REJECT.getPlaceholder()+":"+0,R.string.call_peer_rejected).addEntry(CloseCallReason.REJECT.getPlaceholder()+":"+1,R.string.call_rejected).addEntry(CloseCallReason.NETWORK_ERROR.getPlaceholder()+":"+0,R.string.network_or_internal_server_error).addEntry(CloseCallReason.NETWORK_ERROR.getPlaceholder()+":"+1,R.string.network_or_internal_server_error);

	@Setter( value=AccessLevel.PROTECTED )
	@Accessors( chain=true )
	protected  ChatActivity context;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors( chain=true )
	protected  long  contactId;

	public  void cachePreviousPage()
	{
		synchronized( this )
		{
			boolean  dataSetChanged=false;

			for( ChatMessage  chatMessage : ChatMessageRepository.DAO.lookup(ChatMessage.class,"SELECT  ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE,IS_LOCAL,LOCAL_DESCRIPTION  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?  ORDER  BY  CREATE_TIME  DESC  LIMIT  ?,20",new  Object[]{contactId,items.size()}) )
			{
				dataSetChanged=true;
				//  it  is  necessary  that  checking  the  existence  of  the  chat  message  while  the  method  is  triggered  after  a  chat  message  is  added  to  the  empty  list  by  <append>  method  and  the  listview  scrolled  to  the  top.
				if( this.oqp.put( chatMessage.getId(), chatMessage ) == null )
				{
					this.items.add(0,chatMessage);
				}
			}

			if( !   dataSetChanged )
			{
				return;
			}

			super.notifyDataSetChanged( );
		}
	}

	public  void  append( long  id )
	{
		synchronized( this )
		{
			ChatMessage   cached =  oqp.get( id );

			if( cached     == null )
			{
				ChatMessage  chatMessage = ChatMessageRepository.DAO.lookupOne( ChatMessage.class,"SELECT  ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE,IS_LOCAL,LOCAL_DESCRIPTION  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  CONTACT_ID = ?",new  Object[]{id,contactId} );

				if( this.oqp.put( chatMessage.getId(), chatMessage ) == null )
				{
					this.items.add( chatMessage );
				}
			}
			else
			{
				cached.setTransportState( ChatMessageRepository.DAO.lookupOne(Integer.class,"SELECT  TRANSPORT_STATE  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  CONTACT_ID = ?",new  Object[]{id,contactId}) );
			}

			notifyDataSetChanged( );
		}
	}

	public  void    append()
    {
        synchronized( this )
        {
            for( ChatMessage  chatMessage : ChatMessageRepository.DAO.lookup(ChatMessage.class,"SELECT  ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE,IS_LOCAL,LOCAL_DESCRIPTION  FROM  "+ChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?  AND  CREATE_TIME > ?  ORDER  BY  CREATE_TIME  ASC",new  Object[]{contactId,items.isEmpty()?new  Timestamp(new  DateTime(2000,1,1,0,0,0).getMillis()) : items.get(items.size()-1).getCreateTime()}) )
            {
                if( this.oqp.put( chatMessage.getId(), chatMessage ) == null )
                {
                    this.items.add( chatMessage );
                }
            }

            notifyDataSetChanged( );
        }
    }

	public  View  getView( int  position,View  convertView,ViewGroup  parent )
	{
		convertView = convertView != null ? convertView : LayoutInflater.from(context).inflate( R.layout.activity_chat_message_item,parent,false );

		ChatMessage   chatMessage =  super.getItem(   position );

		ObjectUtils.cast(convertView.findViewById(TransportState.valueOf(chatMessage.getTransportState()) == TransportState.RECEIVED ? R.id.other_portrait : R.id.owner_portrait),SimpleDraweeView.class).setImageURI( Uri.parse(context.application().baseUrl().addPathSegments("user/"+(TransportState.valueOf(chatMessage.getTransportState()) == TransportState.RECEIVED ? contactId : context.application().getSquirrelClient().getUserMetadata().getId())+"/portrait").build().toString()) );

		ObjectUtils.cast(convertView.findViewById(R.id.message_vest_to_switcher),ViewSwitcher.class).setDisplayedChild( TransportState.valueOf(chatMessage.getTransportState()) == TransportState.RECEIVED ? 0 : 1 );

		View  contentFrame      = ObjectUtils.cast(convertView.findViewById(R.id.message_vest_to_switcher),ViewSwitcher.class).getDisplayedChild();

		ObjectUtils.cast(contentFrame.findViewById(R.id.send_failed_warning_image),ImageView.class).setVisibility( TransportState.valueOf(     chatMessage.getTransportState()) == TransportState.SEND_FAILED ?  View.VISIBLE : View.GONE );

		ObjectUtils.cast(contentFrame.findViewById(R.id.send_failed_warning_image),ImageView.class).setOnLongClickListener( (view) -> {TipWindow  tip = new  TipWindow(context,R.layout.activity_chat_message_item_tip,true);  tip.showAsDropDown(view);  tip.getContentView().findViewById(R.id.resend_button).setOnClickListener((resendButton) -> {context.application().getSquirrelClient().asynchronousSend(new  ChatPacket(contactId,chatMessage.getMd5(),ChatContentType.valueOf(chatMessage.getContentType()),chatMessage.getContent().getBytes()));  tip.dismiss();});  return  false;} );

		ViewSwitcher  contentSwitcher =     ObjectUtils.cast( contentFrame.findViewById(R.id.message_content_switcher) );

		if( ChatContentType.valueOf(chatMessage.getContentType()) == ChatContentType.IMAGE || ChatContentType.valueOf(chatMessage.getContentType())  == ChatContentType.VIDEO )
		{
			File  screenshotFile = new  File( context.application().getCacheDir(), "file/"+chatMessage.getMd5()+"$TMB" );

			ObjectUtils.cast(contentSwitcher.setDisplayedChild(1).findViewById(R.id.screenshot),FlexibleSimpleDraweeView.class).setImageURI( screenshotFile.exists() ? Uri.parse(screenshotFile.toURI().toString()) : Uri.parse(context.application().baseUrl().addPathSegments("file/"+chatMessage.getMd5()+"$TMB").addQueryParameter("SECRET_KEY",context.application().getSquirrelClient().getUserMetadata().getSecretKey()).build().toString()) );

			contentSwitcher.findViewById(R.id.upload_progress_bar).setVisibility(     TransportState.valueOf(chatMessage.getTransportState()) ==TransportState.SENDING ? View.VISIBLE : View.GONE );

			ObjectUtils.cast(contentSwitcher.findViewById(R.id.play_button),ImageView.class).setVisibility( TransportState.valueOf(chatMessage.getTransportState()) == TransportState.SENDING||ChatContentType.valueOf(chatMessage.getContentType()) == ChatContentType.IMAGE ? View.GONE : View.VISIBLE );

			contentSwitcher.getDisplayedChild().setOnClickListener( (view) -> ActivityCompat.startActivity(context,new  Intent(context,ChatContentType.valueOf(chatMessage.getContentType()) == ChatContentType.IMAGE ? ImagePreviewActivity.class : VideoPreviewActivity.class).putExtra("CACHE_FILE_PATH",new  File(context.application().getCacheDir(),"file/"+chatMessage.getMd5()).getPath()).putExtra("URL",context.application().baseUrl().addPathSegments("file/"+chatMessage.getMd5()).addQueryParameter("SECRET_KEY",context.application().getSquirrelClient().getUserMetadata().getSecretKey()).build().toString()),ActivityOptionsCompat.makeCustomAnimation(context,R.anim.right_in,R.anim.left_out).toBundle()) );
		}
		else
		if( ChatContentType.valueOf(chatMessage.getContentType())         == ChatContentType.AUDIO )
		{
			ObjectUtils.cast(contentSwitcher.setDisplayedChild(2).getDisplayedChild().findViewById(R.id.icon),ImageView.class).setImageResource(   R.drawable.voice_recorder );

			ObjectUtils.cast(contentSwitcher.setDisplayedChild(2).getDisplayedChild().findViewById(R.id.content),TextView.class).setText(  new  DateTime(Long.parseLong(chatMessage.getContent()),DateTimeZone.UTC).toString("ss") );

			contentSwitcher.getDisplayedChild().setOnClickListener( (view) -> { try{new  MediaPlayer().play(new  File(context.application().getCacheDir(),"file/"+chatMessage.getMd5()).getPath(),null,null); }catch(IOException  ie){} } );
		}
		else
		if( chatMessage.getIsLocal() && StringUtils.isNotBlank(chatMessage.getLocalDescription() ) )
		{
			Map<String,Object>  descripton    = JsonUtils.fromJson( chatMessage.getLocalDescription(),new  TypeReference<Map<String,Object>>(){} );

			ObjectUtils.cast(contentSwitcher.setDisplayedChild(2).getDisplayedChild().findViewById(R.id.icon),ImageView.class).setImageResource(  descripton.getInteger("TYPE") == 0 ? R.drawable.telephone : R.drawable.camcorder );

			ObjectUtils.cast(contentSwitcher.setDisplayedChild(2).getDisplayedChild().findViewById(R.id.content),TextView.class).setText( descripton.getString("REASON_PLACEHOLDER").equals(CloseCallReason.BY_USER.getPlaceholder()) ? descripton.getString("CONTENT") : context.getString(CLOSE_PROFILES.getInteger(descripton.getString("REASON_PLACEHOLDER")+":"+(descripton.getBoolean("IS_PROACTIVELY") ? 1 : 0))) );
		}
		else
		{
			ObjectUtils.cast(contentSwitcher.setDisplayedChild(0).getDisplayedChild().findViewById(R.id.message),TextView.class).setText( chatMessage.getContent() );
		}

		ObjectUtils.cast(convertView.findViewById(TransportState.valueOf(chatMessage.getTransportState()) == TransportState.RECEIVED ? R.id.owner_portrait : R.id.other_portrait),SimpleDraweeView.class).setImageResource( R.color.white );  return  convertView;
	}
}