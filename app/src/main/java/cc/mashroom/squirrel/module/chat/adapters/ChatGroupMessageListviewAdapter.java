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

import  org.joda.time.DateTime;
import  org.joda.time.DateTimeZone;

import  cc.mashroom.hedgehog.parent.BaseAdapter;
import  cc.mashroom.squirrel.client.storage.model.chat.ChatGroupMessage;
import  cc.mashroom.squirrel.client.storage.repository.chat.ChatGroupMessageRepository;
import  cc.mashroom.squirrel.module.chat.activity.ChatGroupActivity;
import  cc.mashroom.hedgehog.module.common.activity.ImagePreviewActivity;
import  cc.mashroom.hedgehog.module.common.activity.VideoPreviewActivity;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.hedgehog.widget.FlexibleSimpleDraweeView;
import  cc.mashroom.hedgehog.device.MediaPlayer;
import  cc.mashroom.hedgehog.widget.ViewSwitcher;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  lombok.AccessLevel;
import  lombok.AllArgsConstructor;
import  lombok.Setter;
import  lombok.experimental.Accessors;

import  java.io.File;
import  java.io.IOException;
import  java.util.LinkedList;

@AllArgsConstructor

public  class  ChatGroupMessageListviewAdapter  extends  BaseAdapter  <ChatGroupMessage>
{
    public  ChatGroupMessageListviewAdapter( ChatGroupActivity  context, long  groupId )
    {
    	super( new  LinkedList<ChatGroupMessage>() );

        this.setContext(context).setGroupId(groupId).setTotalCount( ChatGroupMessageRepository.DAO.lookupOne(Long.class,"SELECT  COUNT(ID)  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  GROUP_ID = ?",new  Object[]{groupId}).intValue() );
    }

    @Setter( value=AccessLevel.PROTECTED )
    @Accessors( chain=true )
    protected  long groupId;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors( chain=true )
	protected  ChatGroupActivity  context;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors( chain=true )
	protected  int   totalCount;
	@Setter( value=   AccessLevel.PUBLIC )
	@Accessors( chain=true )
	protected  boolean  isStackFromBottom;

	protected  Map<Long, ChatGroupMessage>  oqp = new  HashMap<Long,ChatGroupMessage>();

	public  void     cacheIncoming()
	{
		synchronized( this )
		{
			for( ChatGroupMessage  ChatGroupMessage : ChatGroupMessageRepository.DAO.lookup(ChatGroupMessage.class,"SELECT  ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  GROUP_ID = ?  AND  ID > ?  ORDER  BY  ID  ASC",new  Object[]{groupId,items.isEmpty() ? 0 : items.get(0).getId()}) )
			{
				if( this.oqp.put( ChatGroupMessage.getId(),ChatGroupMessage  ) == null )
				{
					this.totalCount = totalCount + 1;

					items.add( 0, ChatGroupMessage );
				}
			}

			super.notifyDataSetChanged( );
		}
	}

	public  long  getItemId(int  position)
	{
		return  totalCount - position - 1;
	}

	public  int   getCount()
	{
		return      this.totalCount;
	}

	public  ChatGroupMessage  getItem(int  position )
	{
		int  i    = (int)  this.getItemId( position);

		ChatGroupMessage  cachedMessage = i >= 0 && i <super.items.size() ? super.items.get(i) : null;

		if( cachedMessage  == null )
		{
			for( ChatGroupMessage  ChatGroupMessage : ChatGroupMessageRepository.DAO.lookup(ChatGroupMessage.class,"SELECT  ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  GROUP_ID = ?  ORDER  BY  ID  DESC  LIMIT  ?,10",new  Object[]{groupId,items.size()}) )
			{
				//  it  is  necessary  that  checking  the  existence  of  the  chat  message  while  the  method  is  triggered  after  a  chat  message  is  added  to  the  empty  list  by  <append>  method  and  the  listview  scrolled  to  the  top.
				if( this.oqp.put( ChatGroupMessage.getId(),ChatGroupMessage  ) == null )
				{
					this.items.add(ChatGroupMessage);

					if(       cachedMessage == null )  cachedMessage = ChatGroupMessage;
				}
			}
		}

		return        cachedMessage;
	}

	public  void  upsert( long  id )
	{
		synchronized( this )
		{
			ChatGroupMessage  cachedMessage = this.oqp.get( id );

			if(    cachedMessage != null )
			{
				cachedMessage.setTransportState( ChatGroupMessageRepository.DAO.lookupOne(Integer.class,"SELECT  TRANSPORT_STATE  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  GROUP_ID = ?",new  Object[]{id,groupId}) );
			}
			else
			{
				ChatGroupMessage  ChatGroupMessage = ChatGroupMessageRepository.DAO.lookupOne( ChatGroupMessage.class,"SELECT  ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE  FROM  "+ChatGroupMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  GROUP_ID = ?",new  Object[]{id,groupId} );
				//  it  is  necessary  that  checking  the  existence  of  the  chat  message  while  the  method  is  triggered  after  a  chat  message  is  added  to  the  empty  list  by  <append>  method  and  the  listview  scrolled  to  the  top.
				if( this.oqp.put( ChatGroupMessage.getId(),ChatGroupMessage  ) == null )
				{
					this.totalCount = totalCount + 1;

					items.add( 0, ChatGroupMessage );
				}
			}

			super.notifyDataSetChanged( );
		}
	}

	public  View  getView( final  int  position , View  convertView, ViewGroup  parent )
	{
		convertView = convertView != null ? convertView : LayoutInflater.from(context).inflate( R.layout.activity_chat_message_item,parent,false );

		ChatGroupMessage  ChatGroupMessage = getItem( position );

		ObjectUtils.cast(convertView.findViewById(TransportState.valueOf(ChatGroupMessage.getTransportState()) == TransportState.RECEIVED ? R.id.other_portrait : R.id.owner_portrait),SimpleDraweeView.class).setImageURI( Uri.parse(context.application().baseUrl().addPathSegments("user/"+(TransportState.valueOf(ChatGroupMessage.getTransportState()) == TransportState.RECEIVED ? ChatGroupMessage.getContactId() : context.application().getSquirrelClient().userMetadata().getId())+"/portrait").build().toString()) );

		ObjectUtils.cast(convertView.findViewById(R.id.message_vest_to_switcher),ViewSwitcher.class).setDisplayedChild( TransportState.valueOf(ChatGroupMessage.getTransportState()) == TransportState.RECEIVED ? 0 : 1 );

		View  contentFrame      = ObjectUtils.cast(convertView.findViewById(R.id.message_vest_to_switcher),ViewSwitcher.class).getDisplayedChild();

		ObjectUtils.cast(contentFrame.findViewById(R.id.send_failed_warning_image),ImageView.class).setVisibility( TransportState.valueOf(ChatGroupMessage.getTransportState()) == TransportState.SEND_FAILED ? View.VISIBLE : View.GONE );

		ViewSwitcher  contentSwitcher = ObjectUtils.cast( contentFrame.findViewById(R.id.message_content_switcher) );

		if( ChatContentType.valueOf(ChatGroupMessage.getContentType()) == ChatContentType.IMAGE || ChatContentType.valueOf(ChatGroupMessage.getContentType()) == ChatContentType.VIDEO )
		{
			File  screenshotFile = new  File( context.application().getCacheDir(), "file/"+ChatGroupMessage.getMd5()+"$TMB" );

			ObjectUtils.cast(contentSwitcher.setDisplayedChild(1).findViewById(R.id.screenshot),FlexibleSimpleDraweeView.class).setCacheFile(screenshotFile).setImageURI( screenshotFile.exists() ? Uri.parse(screenshotFile.toURI().toString()) : Uri.parse(context.application().baseUrl().addPathSegments("file/"+ChatGroupMessage.getMd5()+"$TMB").addQueryParameter("SECRET_KEY",context.application().getSquirrelClient().userMetadata().getSecretKey()).build().toString()) );

			contentSwitcher.findViewById(R.id.upload_progress_bar).setVisibility(TransportState.valueOf(ChatGroupMessage.getTransportState()) == TransportState.SENDING ? View.VISIBLE : View.GONE );

			ObjectUtils.cast(contentSwitcher.findViewById(R.id.play_button),ImageView.class).setVisibility( TransportState.valueOf(ChatGroupMessage.getTransportState()) == TransportState.SENDING || ChatContentType.valueOf(ChatGroupMessage.getContentType()) == ChatContentType.IMAGE ? View.GONE : View.VISIBLE );

			contentSwitcher.getDisplayedChild().setOnClickListener( (view) -> ActivityCompat.startActivity(context,new  Intent(context,ChatContentType.valueOf(ChatGroupMessage.getContentType()) == ChatContentType.IMAGE ? ImagePreviewActivity.class : VideoPreviewActivity.class).putExtra("CACHE_FILE_PATH",new  File(context.application().getCacheDir(),"file/"+ChatGroupMessage.getMd5()).getPath()).putExtra("URL",context.application().baseUrl().addPathSegments("file/"+ChatGroupMessage.getMd5()).addQueryParameter("SECRET_KEY",context.application().getSquirrelClient().userMetadata().getSecretKey()).build().toString()),ActivityOptionsCompat.makeCustomAnimation(context,R.anim.right_in,R.anim.left_out).toBundle()) );
		}
		else
		if( ChatContentType.valueOf(ChatGroupMessage.getContentType()) == ChatContentType.AUDIO      )
		{
			ObjectUtils.cast(contentSwitcher.setDisplayedChild(2).getDisplayedChild().findViewById(R.id.content),TextView.class).setText( new  DateTime(Long.parseLong(ChatGroupMessage.getContent()),  DateTimeZone.UTC).toString("ss") );

			contentSwitcher.getDisplayedChild().setOnClickListener( (view) -> { try{ new  MediaPlayer().play(new  File(context.application().getCacheDir(), "file/"+ChatGroupMessage.getMd5()).getPath(),null,null); }catch(IOException  e){} } );
		}
		else
		{
			ObjectUtils.cast(contentSwitcher.setDisplayedChild(0).getDisplayedChild().findViewById(R.id.message),TextView.class).setText( ChatGroupMessage.getContent() );
		}

		ObjectUtils.cast(convertView.findViewById(TransportState.valueOf(ChatGroupMessage.getTransportState()) == TransportState.RECEIVED ? R.id.owner_portrait : R.id.other_portrait), SimpleDraweeView.class).setImageResource( R.color.white );  return  convertView;
	}
}