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
import  cc.mashroom.squirrel.client.storage.model.chat.GroupChatMessage;
import  cc.mashroom.squirrel.client.storage.repository.chat.GroupChatMessageRepository;
import  cc.mashroom.squirrel.module.chat.activity.GroupChatActivity;
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

public  class  GroupChatMessageListviewAdapter  extends  BaseAdapter  <GroupChatMessage>
{
    public  GroupChatMessageListviewAdapter( GroupChatActivity  context, long  groupId )
    {
    	super( new  LinkedList<GroupChatMessage>() );

        this.setContext(context).setGroupId(groupId).setTotalCount( GroupChatMessageRepository.DAO.lookupOne(Long.class,"SELECT  COUNT(ID)  FROM  "+GroupChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  GROUP_ID = ?",new  Object[]{groupId}).intValue() );
    }

    @Setter( value=AccessLevel.PROTECTED )
    @Accessors( chain=true )
    protected  long groupId;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors( chain=true )
	protected  GroupChatActivity  context;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors( chain=true )
	protected  int  totalCount;
	@Setter( value=   AccessLevel.PUBLIC )
	@Accessors( chain=true )
	protected  boolean  isStackFromBottom;

	protected  Map<Long, GroupChatMessage>  oqp = new  HashMap<Long,GroupChatMessage>();

	public  void     cacheIncoming()
	{
		synchronized( this )
		{
			for( GroupChatMessage  groupChatMessage : GroupChatMessageRepository.DAO.lookup(GroupChatMessage.class,"SELECT  ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE  FROM  "+GroupChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  GROUP_ID = ?  AND  ID > ?  ORDER  BY  ID  ASC",new  Object[]{groupId,items.isEmpty() ? 0 : items.get(0).getId()}) )
			{
				if( this.oqp.put( groupChatMessage.getId(),groupChatMessage  ) == null )
				{
					this.totalCount = totalCount + 1;

					items.add( 0, groupChatMessage );
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

	public  GroupChatMessage  getItem(int  position )
	{
		int  i    = (int)  this.getItemId( position);

		GroupChatMessage  cachedMessage = i >= 0 && i <super.items.size() ? super.items.get( i ) : null;

		if( cachedMessage  == null )
		{
			for( GroupChatMessage  groupChatMessage : GroupChatMessageRepository.DAO.lookup(GroupChatMessage.class,"SELECT  ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE  FROM  "+GroupChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  GROUP_ID = ?  ORDER  BY  ID  DESC  LIMIT  ?,10",new  Object[]{groupId,items.size()}) )
			{
				//  it  is  necessary  that  checking  the  existence  of  the  chat  message  while  the  method  is  triggered  after  a  chat  message  is  added  to  the  empty  list  by  <append>  method  and  the  listview  scrolled  to  the  top.
				if( this.oqp.put( groupChatMessage.getId(),groupChatMessage  ) == null )
				{
					this.items.add(groupChatMessage);

					if(       cachedMessage == null )  cachedMessage = groupChatMessage;
				}
			}
		}

		return        cachedMessage;
	}

	public  void  upsert( long  id )
	{
		synchronized( this )
		{
			GroupChatMessage  cachedMessage = this.oqp.get( id );

			if(    cachedMessage != null )
			{
				cachedMessage.setTransportState( GroupChatMessageRepository.DAO.lookupOne(Integer.class,"SELECT  TRANSPORT_STATE  FROM  "+GroupChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  GROUP_ID = ?",new  Object[]{id,groupId}) );
			}
			else
			{
				GroupChatMessage  groupChatMessage = GroupChatMessageRepository.DAO.lookupOne( GroupChatMessage.class,"SELECT  ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE  FROM  "+GroupChatMessageRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  GROUP_ID = ?",new  Object[]{id,groupId} );
				//  it  is  necessary  that  checking  the  existence  of  the  chat  message  while  the  method  is  triggered  after  a  chat  message  is  added  to  the  empty  list  by  <append>  method  and  the  listview  scrolled  to  the  top.
				if( this.oqp.put( groupChatMessage.getId(),groupChatMessage  ) == null )
				{
					this.totalCount = totalCount + 1;

					items.add( 0, groupChatMessage );
				}
			}

			super.notifyDataSetChanged( );
		}
	}

	public  View  getView( final  int  position , View  convertView, ViewGroup  parent )
	{
		convertView = convertView != null ? convertView : LayoutInflater.from(context).inflate( R.layout.activity_chat_message_item,parent,false );

		GroupChatMessage  groupChatMessage = getItem( position );

		ObjectUtils.cast(convertView.findViewById(TransportState.valueOf(groupChatMessage.getTransportState()) == TransportState.RECEIVED ? R.id.other_portrait : R.id.owner_portrait),SimpleDraweeView.class).setImageURI( Uri.parse(context.application().baseUrl().addPathSegments("user/"+(TransportState.valueOf(groupChatMessage.getTransportState()) == TransportState.RECEIVED ? groupChatMessage.getContactId() : context.application().getSquirrelClient().getUserMetadata().getId())+"/portrait").build().toString()) );

		ObjectUtils.cast(convertView.findViewById(R.id.message_vest_to_switcher),ViewSwitcher.class).setDisplayedChild( TransportState.valueOf(groupChatMessage.getTransportState()) == TransportState.RECEIVED ? 0 : 1 );

		View  contentFrame      = ObjectUtils.cast(convertView.findViewById(R.id.message_vest_to_switcher),ViewSwitcher.class).getDisplayedChild();

		ObjectUtils.cast(contentFrame.findViewById(R.id.send_failed_warning_image),ImageView.class).setVisibility( TransportState.valueOf(groupChatMessage.getTransportState()) == TransportState.SEND_FAILED ? View.VISIBLE : View.GONE );

		ViewSwitcher  contentSwitcher = ObjectUtils.cast( contentFrame.findViewById(R.id.message_content_switcher) );

		if( ChatContentType.valueOf(groupChatMessage.getContentType()) == ChatContentType.IMAGE || ChatContentType.valueOf(groupChatMessage.getContentType()) == ChatContentType.VIDEO )
		{
			File  screenshotFile = new  File( context.application().getCacheDir(), "file/"+groupChatMessage.getMd5()+"$TMB" );

			ObjectUtils.cast(contentSwitcher.setDisplayedChild(1).findViewById(R.id.screenshot),FlexibleSimpleDraweeView.class).setCacheFile(screenshotFile).setImageURI( screenshotFile.exists() ? Uri.parse(screenshotFile.toURI().toString()) : Uri.parse(context.application().baseUrl().addPathSegments("file/"+groupChatMessage.getMd5()+"$TMB").addQueryParameter("SECRET_KEY",context.application().getSquirrelClient().getUserMetadata().getSecretKey()).build().toString()) );

			contentSwitcher.findViewById(R.id.upload_progress_bar).setVisibility( TransportState.valueOf(groupChatMessage.getTransportState()) == TransportState.SENDING ? View.VISIBLE : View.GONE );

			ObjectUtils.cast(contentSwitcher.findViewById(R.id.play_button),ImageView.class).setVisibility( TransportState.valueOf(groupChatMessage.getTransportState()) == TransportState.SENDING || ChatContentType.valueOf(groupChatMessage.getContentType()) == ChatContentType.IMAGE ? View.GONE : View.VISIBLE );

			contentSwitcher.getDisplayedChild().setOnClickListener( (view) -> ActivityCompat.startActivity(context,new  Intent(context,ChatContentType.valueOf(groupChatMessage.getContentType()) == ChatContentType.IMAGE ? ImagePreviewActivity.class : VideoPreviewActivity.class).putExtra("CACHE_FILE_PATH",new  File(context.application().getCacheDir(),"file/"+groupChatMessage.getMd5()).getPath()).putExtra("URL",context.application().baseUrl().addPathSegments("file/"+groupChatMessage.getMd5()).addQueryParameter("SECRET_KEY",context.application().getSquirrelClient().getUserMetadata().getSecretKey()).build().toString()),ActivityOptionsCompat.makeCustomAnimation(context,R.anim.right_in,R.anim.left_out).toBundle()) );
		}
		else
		if( ChatContentType.valueOf(groupChatMessage.getContentType()) == ChatContentType.AUDIO )
		{
			ObjectUtils.cast(contentSwitcher.setDisplayedChild(2).getDisplayedChild().findViewById(R.id.content),TextView.class).setText( new  DateTime(Long.parseLong(groupChatMessage.getContent()),  DateTimeZone.UTC).toString("ss") );

			contentSwitcher.getDisplayedChild().setOnClickListener( (view) -> { try{ new  MediaPlayer().play(new  File(context.application().getCacheDir(),"file/"+groupChatMessage.getMd5()).getPath(),null,null); }catch(IOException  e){} } );
		}
		else
		{
			ObjectUtils.cast(contentSwitcher.setDisplayedChild(0).getDisplayedChild().findViewById(R.id.message),TextView.class).setText(      groupChatMessage.getContent() );
		}

		ObjectUtils.cast(convertView.findViewById(TransportState.valueOf(groupChatMessage.getTransportState()) == TransportState.RECEIVED ? R.id.owner_portrait : R.id.other_portrait), SimpleDraweeView.class).setImageResource( R.color.white );  return  convertView;
	}
}