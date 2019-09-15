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
package cc.mashroom.squirrel.module.home.tab.newsprofile.fragment;

import  android.content.Intent;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.AdapterView;
import  android.widget.ListView;

import  java.util.Locale;

import  cc.mashroom.hedgehog.system.LocaleChangeEventDispatcher;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.connect.PacketEventDispatcher;
import  cc.mashroom.squirrel.client.storage.model.chat.NewsProfile;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.repository.user.ContactRepository;
import  cc.mashroom.squirrel.module.home.activity.ContactProfileEditActivity;
import  cc.mashroom.squirrel.parent.AbstractPacketListenerFragment;
import  cc.mashroom.squirrel.module.chat.activity.ChatActivity;
import  cc.mashroom.squirrel.module.chat.activity.GroupChatActivity;
import  cc.mashroom.squirrel.module.home.tab.newsprofile.adapters.NewsProfileListAdapter;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.util.ObjectUtils;
import  lombok.Getter;

public  class  NewsProfileFragment  extends  AbstractPacketListenerFragment  implements  AdapterView.OnItemClickListener,LocaleChangeEventDispatcher.LocaleChangeListener
{
	public  View  onCreateView( LayoutInflater  inflater,ViewGroup  container,Bundle  savedInstanceState )
	{
		super.onCreateView( inflater, container, savedInstanceState );

		LocaleChangeEventDispatcher.addListener(    NewsProfileFragment.this );

		if( contentView == null )
		{
			contentView = inflater.inflate( R.layout.fragment_news_profile, container,false );

			ObjectUtils.cast(contentView.findViewById(R.id.profile_list),ListView.class).setOnItemClickListener( this );

			ObjectUtils.cast(this.contentView.findViewById(R.id.profile_list),ListView.class).setAdapter( new  NewsProfileListAdapter( this ) );
		}

		return  this.contentView;
	}

	@Getter
	protected  View  contentView;

	public  void  onItemClick( AdapterView<?>  parent, View  itemView,int  position,long  id )
	{
		NewsProfile  newsProfile=ObjectUtils.cast(parent.getAdapter(),NewsProfileListAdapter.class).getItem( position );

		if( newsProfile.getPacketType() == PAIPPacketType.CHAT ||  newsProfile.getPacketType()  == PAIPPacketType.CALL )
		{
			ActivityCompat.startActivity( super.getActivity(),new  Intent(this.getActivity(),ChatActivity.class).putExtra("CONTACT_ID", newsProfile.getContactId()),ActivityOptionsCompat.makeCustomAnimation(this.getContext(),R.anim.right_in,R.anim.left_out).toBundle() );
		}
		else
		if( newsProfile.getPacketType() == PAIPPacketType.GROUP_CHAT )
		{
			ActivityCompat.startActivity( super.getActivity(),new  Intent(this.getActivity(),GroupChatActivity.class).putExtra("CHAT_GROUP_ID",newsProfile.getId()),ActivityOptionsCompat.makeCustomAnimation(this.getContext(),R.anim.right_in,R.anim.left_out).toBundle() );
		}
		else
		if( newsProfile.getPacketType() == PAIPPacketType.SUBSCRIBE && Integer.parseInt(newsProfile.getContent()) == 2 )
		{
			Contact  contact = ContactRepository.DAO.getContactDirect().get( newsProfile.getContactId() );

            ActivityCompat.startActivity( this.getActivity(),new  Intent(this.getActivity(),ContactProfileEditActivity.class).putExtra("CONTACT",contact),ActivityOptionsCompat.makeCustomAnimation(this.getActivity(),R.anim.right_in,R.anim.left_out).toBundle() );
		}
		else
		if( newsProfile.getPacketType() == PAIPPacketType.SUBSCRIBE && Integer.parseInt(newsProfile.getContent()) == 1 )
		{
			Contact  contact = ContactRepository.DAO.getContactDirect().get( newsProfile.getContactId() );

			ActivityCompat.startActivity( this.getActivity(),new  Intent(this.getActivity(),ContactProfileEditActivity.class).putExtra("CONTACT",contact),ActivityOptionsCompat.makeCustomAnimation(this.getActivity(),R.anim.right_in,R.anim.left_out).toBundle() );
		}
	}

	public  void onDestroy()
	{
		super.onDestroy(  );

		LocaleChangeEventDispatcher.removeListener( NewsProfileFragment.this );
	}

	public  void  onResume()
	{
		super.onResume();

		ObjectUtils.cast( ObjectUtils.cast(contentView.findViewById(R.id.profile_list),ListView.class).getAdapter(),NewsProfileListAdapter.class).notifyDataSetChanged();
	}

	public  void  onChange( Locale  updateLocale )
	{
		ObjectUtils.cast( ObjectUtils.cast(contentView.findViewById(R.id.profile_list),ListView.class).getAdapter(),NewsProfileListAdapter.class).notifyDataSetChanged();
	}

	public  void  onReceived(     Packet  packet )
	{
		application().getMainLooperHandler().post( () -> ObjectUtils.cast(ObjectUtils.cast(contentView.findViewById(R.id.profile_list),ListView.class).getAdapter(),NewsProfileListAdapter.class).notifyDataSetChanged() );
	}
}