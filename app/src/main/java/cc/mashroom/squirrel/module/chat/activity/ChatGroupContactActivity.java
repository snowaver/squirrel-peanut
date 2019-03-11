package cc.mashroom.squirrel.module.chat.activity;

import  android.os.Bundle;
import  android.widget.ListView;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.module.chat.adapters.ChatGroupContactListviewAdapter;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.hedgehog.widget.HeaderBar;

public  class  ChatGroupContactActivity     extends  AbstractActivity
{
	protected  void  onCreate( Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		super.setContentView( R.layout.activity_chat_group_contact );

		ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).setTitle( super.getString(R.string.member_list) );

		ObjectUtils.cast(super.findViewById(R.id.contact_list),ListView.class).setAdapter( new  ChatGroupContactListviewAdapter(this,super.getIntent().getLongExtra("CHAT_GROUP_ID",0)) );
	}
}
