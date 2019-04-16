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
package cc.mashroom.squirrel.module.home.tab.newsprofile.adapters;

import  android.graphics.Color;
import  android.net.Uri;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.RelativeLayout;
import  android.widget.TextView;

import  com.aries.ui.widget.alert.UIAlertDialog;
import  com.facebook.drawee.view.SimpleDraweeView;

import  androidx.core.content.res.ResourcesCompat;
import  cc.mashroom.hedgehog.parent.BaseAdapter;
import  cc.mashroom.hedgehog.util.ExtviewsAdapter;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.chat.NewsProfile;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroupUser;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.parent.Application;
import  cc.mashroom.squirrel.module.home.tab.newsprofile.fragment.NewsProfileFragment;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.hedgehog.widget.BadgeView;
import  cc.mashroom.util.ObjectUtils;
import  lombok.AllArgsConstructor;
import  lombok.SneakyThrows;

@AllArgsConstructor

public  class  NewsProfileListAdapter   extends  BaseAdapter
{
	protected  NewsProfileFragment  context;

	@SneakyThrows
	public  NewsProfile  getItem(  int  position )
	{
		return  NewsProfile.dao.getOne("SELECT  ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT  FROM  "+NewsProfile.dao.getDataSourceBind().table()+"  ORDER  BY  CREATE_TIME  DESC  LIMIT  1  OFFSET  ?",new  Object[]{position});
	}
	public  long  getItemId( int  position )
	{
		return  getCount()-position-1;
	}
	@SneakyThrows
	public  int  getCount()
	{
		return  NewsProfile.dao.getOne("SELECT  COUNT(*)  AS  COUNT  FROM  "+NewsProfile.dao.getDataSourceBind().table(),new  Object[]{}).getLong("COUNT").intValue();
	}
	private  String  getProfileMessage( String  content,PAIPPacketType  type,NewsProfile  newsProfile )
	{
		if( type==PAIPPacketType.SUBSCRIBE )
		{
			return  context.getString(Integer.valueOf(content) == 0 ? R.string.subscribe_request_sent : R.string.subscribe_received_a_adding_contact_request );
		}
		else
		{
			return  Application.PLACEHOLDER_PROFILES.containsKey(content) ? context.getString(Application.PLACEHOLDER_PROFILES.get(content)) : content;
		}
	}
	@SneakyThrows
	public  View  getView( int  position,View  convertView,ViewGroup  parent )
	{
		convertView       = convertView != null ? convertView : LayoutInflater.from(context.getContext()).inflate( R.layout.fragment_news_profile_item,parent,false );

		NewsProfile  newsProfile = this.getItem( position );

		if( PAIPPacketType.valueOf(newsProfile.getShort("PACKET_TYPE")) == PAIPPacketType.GROUP_CHAT  )
		{
			ObjectUtils.cast(convertView.findViewById(R.id.nickname),TextView.class).setText( ChatGroup.dao.getOne("SELECT  NAME  FROM  "+ChatGroup.dao.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{newsProfile.getLong("ID")}).getString("NAME") );

			ObjectUtils.cast(convertView.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI( Uri.parse("res://"+context.getContext().getPackageName()+"/"+R.drawable.placeholder) );

			ObjectUtils.cast(convertView.findViewById(R.id.profile_message),TextView.class).setText( ChatGroupUser.dao.getOne("SELECT  VCARD  FROM  "+ChatGroupUser.dao.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?",new  Object[]{newsProfile.getLong("CONTACT_ID")}).getString("VCARD")+context.getContext().getString(R.string.colon)+(Application.PLACEHOLDER_PROFILES.containsKey(newsProfile.getString("CONTENT")) ? context.getString(Application.PLACEHOLDER_PROFILES.get(newsProfile.getString("CONTENT"))) : newsProfile.getString("CONTENT")) );
		}
		else
		if( PAIPPacketType.valueOf(newsProfile.getShort("PACKET_TYPE")) == PAIPPacketType.CHAT || PAIPPacketType.valueOf(newsProfile.getShort("PACKET_TYPE")) == PAIPPacketType.SUBSCRIBE || PAIPPacketType.valueOf(newsProfile.getShort("PACKET_TYPE"))  == PAIPPacketType.SUBSCRIBE_ACK )
		{
			ObjectUtils.cast(convertView.findViewById(R.id.nickname),TextView.class).setText( Contact.dao.getContactDirect().get(newsProfile.getLong("CONTACT_ID")).getString("REMARK") );

			ObjectUtils.cast(convertView.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI( Uri.parse(context.application().baseUrl().addPathSegments("user/"+newsProfile.getLong("CONTACT_ID")+"/portrait").build().toString()) );

			ObjectUtils.cast(convertView.findViewById(R.id.profile_message),TextView.class).setText( getProfileMessage(newsProfile.getString("CONTENT"),PAIPPacketType.valueOf(newsProfile.getShort("PACKET_TYPE")),newsProfile) );
		}

		ObjectUtils.cast( convertView.findViewById(R.id.remove_button),RelativeLayout.class).setOnClickListener( (removeButton) -> {ExtviewsAdapter.adapter(new  UIAlertDialog.DividerIOSBuilder(context.getActivity()).setBackgroundRadius(15).setTitle(R.string.notice).setTitleTextSize(18).setMessage(R.string.message_whether_to_delete).setMessageTextSize(18).setCancelable(true).setCanceledOnTouchOutside(false).setNegativeButtonTextColorResource(R.color.red).setNegativeButtonTextSize(18).setNegativeButton(R.string.cancel,(button,which) ->{}).setPositiveButtonTextSize(18).setPositiveButton(R.string.ok,(dialog, which) -> {NewsProfile.dao.update("DELETE  FROM  "+NewsProfile.dao.getDataSourceBind().table()+"  WHERE  ID = ?  AND  PACKET_TYPE = ?",new  Object[]{newsProfile.getLong("ID"),newsProfile.getShort("PACKET_TYPE")});  NewsProfileListAdapter.this.notifyDataSetChanged();}).create().setWidth((int)  (context.getResources().getDisplayMetrics().widthPixels*0.9)),ResourcesCompat.getFont(context.getActivity(),R.font.droid_sans_mono)).show();} );

		ObjectUtils.cast(convertView.findViewById(R.id.badge),BadgeView.class).setBadge( newsProfile.getInteger( "BADGE_COUNT" ), 0, 99, "." );   return  convertView;
	}
}