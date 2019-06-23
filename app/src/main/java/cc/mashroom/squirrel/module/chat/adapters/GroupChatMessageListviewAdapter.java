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
import  lombok.AccessLevel;
import  lombok.AllArgsConstructor;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;

import  java.io.File;
import  java.io.IOException;

@AllArgsConstructor

public  class  GroupChatMessageListviewAdapter  extends  BaseAdapter
{
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors( chain=true )
	protected  GroupChatActivity  context;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors( chain=true )
	protected  long groupId;
	@SneakyThrows
	public  GroupChatMessage  getItem(   int  position )
	{
		return  GroupChatMessage.dao.getOne("SELECT  ID,CREATE_TIME,CONTACT_ID,MD5,CONTENT_TYPE,CONTENT,TRANSPORT_STATE  FROM  "+GroupChatMessage.dao.getDataSourceBind().table()+"  WHERE  GROUP_ID = ?  ORDER  BY  CREATE_TIME  ASC  LIMIT  1  OFFSET  ?",new  Object[]{groupId,position});
	}
	@SneakyThrows
	public  int   getCount()
	{
		return  GroupChatMessage.dao.getOne("SELECT  COUNT(ID)  AS  COUNT  FROM  "+GroupChatMessage.dao.getDataSourceBind().table()+"  WHERE  GROUP_ID = ?",new  Object[]{groupId}).getLong("COUNT").intValue();
	}

	public  View  getView( final  int  position,View  convertView,ViewGroup  parent )
	{
		convertView = convertView != null ? convertView : LayoutInflater.from(context).inflate( R.layout.activity_chat_message_item,parent,false );

		GroupChatMessage  message = getItem( position );

		ObjectUtils.cast(convertView.findViewById(TransportState.valueOf(message.getInteger("TRANSPORT_STATE")) == TransportState.RECEIVED ? R.id.other_portrait : R.id.owner_portrait),SimpleDraweeView.class).setImageURI( Uri.parse(context.application().baseUrl().addPathSegments("user/"+(TransportState.valueOf(message.getInteger("TRANSPORT_STATE")) == TransportState.RECEIVED ? message.getLong("CONTACT_ID") : context.application().getUserMetadata().getLong("ID"))+"/portrait").build().toString()) );

		ObjectUtils.cast(convertView.findViewById(R.id.message_vest_to_switcher),ViewSwitcher.class).setDisplayedChild( TransportState.valueOf(message.getInteger("TRANSPORT_STATE")) == TransportState.RECEIVED ? 0 : 1 );

		View  contentFrame = ObjectUtils.cast(convertView.findViewById(R.id.message_vest_to_switcher),ViewSwitcher.class).getDisplayedChild();

		ObjectUtils.cast(contentFrame.findViewById(R.id.send_failed_warning_image),ImageView.class).setVisibility( TransportState.valueOf(message.getInteger("TRANSPORT_STATE")) == TransportState.SEND_FAILED ? View.VISIBLE : View.GONE );

		ViewSwitcher  contentSwitcher = ObjectUtils.cast( contentFrame.findViewById(R.id.message_content_switcher) );

		if( ChatContentType.valueOf(message.getInteger("CONTENT_TYPE")) == ChatContentType.IMAGE || ChatContentType.valueOf(message.getInteger("CONTENT_TYPE")) == ChatContentType.VIDEO )
		{
			File  screenshotFile = new  File( context.application().getCacheDir(),"file/"+message.getString("MD5")+"$TMB" );

			ObjectUtils.cast(contentSwitcher.setDisplayedChild(1).findViewById(R.id.screenshot),FlexibleSimpleDraweeView.class).setImageURI( screenshotFile.exists() ? Uri.parse(screenshotFile.toURI().toString()) : Uri.parse(context.application().baseUrl().addPathSegments("file/"+message.getString("MD5")+"$TMB").addQueryParameter("SECRET_KEY",context.application().getSquirrelClient().getUserMetadata().getString("SECRET_KEY")).build().toString()) );

			contentSwitcher.findViewById(R.id.upload_progress_bar).setVisibility( TransportState.valueOf( message.getInteger("TRANSPORT_STATE") ) == TransportState.SENDING ? View.VISIBLE : View.GONE );

			ObjectUtils.cast(contentSwitcher.findViewById(R.id.play_button),ImageView.class).setVisibility( TransportState.valueOf(message.getInteger("TRANSPORT_STATE")) == TransportState.SENDING || ChatContentType.valueOf(message.getInteger("CONTENT_TYPE")) == ChatContentType.IMAGE ? View.GONE : View.VISIBLE );

			contentSwitcher.getDisplayedChild().setOnClickListener( (view) -> ActivityCompat.startActivity(context,new  Intent(context,ChatContentType.valueOf(message.getInteger("CONTENT_TYPE")) == ChatContentType.IMAGE ? ImagePreviewActivity.class : VideoPreviewActivity.class).putExtra("PATH",new  File(context.application().getCacheDir(),"file/"+message.getString("MD5")).getPath()).putExtra("URL",context.application().baseUrl().addPathSegments("file/"+message.getString("MD5")).addQueryParameter("SECRET_KEY",context.application().getSquirrelClient().getUserMetadata().getString("SECRET_KEY")).build().toString()),ActivityOptionsCompat.makeCustomAnimation(context,R.anim.right_in,R.anim.left_out).toBundle()) );
		}
		else
		if( ChatContentType.valueOf(message.getInteger("CONTENT_TYPE")) == ChatContentType.AUDIO )
		{
			ObjectUtils.cast(contentSwitcher.setDisplayedChild(2).getDisplayedChild().findViewById(R.id.content),TextView.class).setText( new  DateTime(Long.parseLong(message.getString("CONTENT")),DateTimeZone.UTC).toString( "ss" ) );

			contentSwitcher.getDisplayedChild().setOnClickListener( (view) -> { try{ new  MediaPlayer().play(new  File(context.application().getCacheDir(), "file/"+message.getString("MD5")).getPath(),null,null); }catch( IOException  ie ){} } );
		}
		else
		{
			ObjectUtils.cast(contentSwitcher.setDisplayedChild(0).getDisplayedChild().findViewById(R.id.message),TextView.class).setText( message.getString("CONTENT") );
		}

		ObjectUtils.cast(convertView.findViewById(TransportState.valueOf(message.getInteger("TRANSPORT_STATE")) == TransportState.RECEIVED ? R.id.owner_portrait : R.id.other_portrait),SimpleDraweeView.class).setImageResource(   R.color.white );  return  convertView;
	}
}