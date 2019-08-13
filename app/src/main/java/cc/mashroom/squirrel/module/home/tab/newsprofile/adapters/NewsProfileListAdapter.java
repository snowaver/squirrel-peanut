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
import cc.mashroom.squirrel.client.storage.repository.chat.NewsProfileRepository;
import cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupRepository;
import cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupUserRepository;
import cc.mashroom.squirrel.client.storage.repository.user.ContactRepository;
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
	public  NewsProfile  getItem(   int   position )
	{
		return  NewsProfileRepository.DAO.lookupOne( NewsProfile.class,"SELECT  ID,CREATE_TIME,PACKET_TYPE,CONTACT_ID,CONTENT,BADGE_COUNT  FROM  "+ NewsProfileRepository.DAO.getDataSourceBind().table()+"  ORDER  BY  CREATE_TIME  DESC  LIMIT  1  OFFSET  ?",new  Object[]{position} );
	}
	public  long  getItemId( int  position )
	{
		return  getCount()-position-1;
	}
	@SneakyThrows
	public  int  getCount()
	{
		return  NewsProfileRepository.DAO.lookupOne(Long.class,"SELECT  COUNT(*)  AS  COUNT  FROM  "+NewsProfileRepository.DAO.getDataSourceBind().table(),new  Object[]{}).intValue();
	}
	private  String  getProfileMessage( String  content,PAIPPacketType  type )
	{
		if( type==PAIPPacketType.SUBSCRIBE )
		{
			return  context.getString( Integer.valueOf(content) == 0 ? R.string.subscribe_request_sent : R.string.subscribe_received_a_adding_contact_request );
		}
		else
		{
			return  Application.NEWS_PROFILE_PLACEHOLDERS.containsKey(content) ? context.getString(Application.NEWS_PROFILE_PLACEHOLDERS.get(content)): content;
		}
	}
	@SneakyThrows
	public  View  getView( int  position,View  convertView,ViewGroup  parent )
	{
		convertView = convertView != null ? convertView : LayoutInflater.from(context.getContext()).inflate( R.layout.fragment_news_profile_item,parent,false );

		NewsProfile  newsProfile = this.getItem( position );

		if( PAIPPacketType.valueOf(newsProfile.getPacketType()) == PAIPPacketType.GROUP_CHAT  )
		{
			ObjectUtils.cast(convertView.findViewById(R.id.nickname),TextView.class).setText( ChatGroupRepository.DAO.lookupOne(String.class,"SELECT  NAME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{newsProfile.getId()}) );

			ObjectUtils.cast(convertView.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI( Uri.parse("res://" + context.getContext().getPackageName() + "/" + R.drawable.placeholder) );

			if(   newsProfile.getContactId()       == null )
			{
				ObjectUtils.cast(convertView.findViewById(R.id.profile_message),TextView.class).setText( R.string.start_to_chat );
			}
			else
			{
				ObjectUtils.cast(convertView.findViewById(R.id.profile_message),TextView.class).setText( ChatGroupUserRepository.DAO.lookupOne(String.class,"SELECT  VCARD  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CONTACT_ID = ?",new  Object[]{newsProfile.getContactId()})+context.getContext().getString(R.string.colon)+(Application.NEWS_PROFILE_PLACEHOLDERS.containsKey(newsProfile.getContent()) ? context.getString(Application.NEWS_PROFILE_PLACEHOLDERS.get(newsProfile.getContent())) : newsProfile.getContent()) );
			}
		}
		else
		if( PAIPPacketType.valueOf(newsProfile.getPacketType()) == PAIPPacketType.CHAT || PAIPPacketType.valueOf(newsProfile.getPacketType()) == PAIPPacketType.SUBSCRIBE || PAIPPacketType.valueOf(newsProfile.getPacketType()) == PAIPPacketType.SUBSCRIBE_ACK )
		{
			ObjectUtils.cast(convertView.findViewById(R.id.nickname),TextView.class).setText( ContactRepository.DAO.getContactDirect().get(newsProfile.getContactId()).getRemark() );

			ObjectUtils.cast(convertView.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI( Uri.parse(context.application().baseUrl().addPathSegments("user/"+newsProfile.getContactId()+"/portrait").build().toString()) );

			ObjectUtils.cast(convertView.findViewById(R.id.profile_message),TextView.class).setText( getProfileMessage(newsProfile.getContent(), PAIPPacketType.valueOf(newsProfile.getPacketType())) );
		}

		ObjectUtils.cast(convertView.findViewById(R.id.remove_button),RelativeLayout.class).setOnClickListener( (removeButton) -> {ExtviewsAdapter.adapter(new  UIAlertDialog.DividerIOSBuilder(context.getActivity()).setBackgroundRadius(15).setTitle(R.string.notice).setTitleTextSize(18).setMessage(R.string.message_whether_to_delete).setMessageTextSize(18).setCancelable(true).setCanceledOnTouchOutside(false).setNegativeButtonTextColorResource(R.color.red).setNegativeButtonTextSize(18).setNegativeButton(R.string.cancel,(button,which) ->{}).setPositiveButtonTextSize(18).setPositiveButton(R.string.ok,(dialog, which) -> {NewsProfileRepository.DAO.update("DELETE  FROM  "+NewsProfileRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?  AND  PACKET_TYPE = ?",new  Object[]{newsProfile.getId(),newsProfile.getPacketType()});  NewsProfileListAdapter.this.notifyDataSetChanged();}).create().setWidth((int)  (context.getResources().getDisplayMetrics().widthPixels*0.9)),ResourcesCompat.getFont(context.getActivity(),R.font.droid_sans_mono)).show();} );

		ObjectUtils.cast(convertView.findViewById(R.id.badge),BadgeView.class).setBadge( newsProfile.getBadgeCount(),0, 99, "." );          return  convertView;
	}
}