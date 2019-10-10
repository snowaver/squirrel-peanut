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
package cc.mashroom.squirrel.module.home.tab.contact.fragment;

import  android.content.Intent;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.LinearLayout;
import  android.widget.ListView;
import  android.widget.TextView;

import  java.util.Locale;

import  cc.mashroom.hedgehog.system.LocaleChangeEventDispatcher;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import  cc.mashroom.squirrel.parent.AbstractFragment;
import  cc.mashroom.squirrel.module.chat.activity.GroupChatActivity;
import  cc.mashroom.squirrel.module.home.tab.contact.adapters.ChatGroupAdapter;
import  cc.mashroom.util.ObjectUtils;

public  class  ChatGroupFragment  extends  AbstractFragment  implements  LocaleChangeEventDispatcher.LocaleChangeListener
{
	public  View  onCreateView( LayoutInflater  inflater,ViewGroup  container,Bundle  savedInstanceState )
	{
		LocaleChangeEventDispatcher.addListener(    ChatGroupFragment.this );

		if( contentView == null )
		{
			contentView = inflater.inflate( R.layout.fragment_contact_chat_group,container,false );

			ObjectUtils.cast(contentView.findViewById(R.id.chat_group_list),ListView.class).setAdapter( new  ChatGroupAdapter(this ) );

			ObjectUtils.cast(contentView.findViewById(R.id.chat_group_list),ListView.class).setOnItemClickListener( (parent,view,position,id) -> ActivityCompat.startActivity(super.getActivity(),new  Intent(this.getActivity(),GroupChatActivity.class).putExtra("CHAT_GROUP_ID",ObjectUtils.cast(parent.getAdapter().getItem(position),ChatGroup.class).getId()),ActivityOptionsCompat.makeCustomAnimation(super.getActivity(),R.anim.right_in,R.anim.left_out).toBundle()) );
		}

		return  this.contentView;
	}

	protected  View  contentView;

	public  void  onDestroy()
	{
		super.onDestroy();

		LocaleChangeEventDispatcher.removeListener( ChatGroupFragment.this );
	}

	public  void  onChange( Locale   locale )
	{
		ObjectUtils.cast(ObjectUtils.cast(this.contentView.findViewById(R.id.create_button),LinearLayout.class).getChildAt(1),TextView.class).setText( R.string.chat_create_new_group );
	}
}