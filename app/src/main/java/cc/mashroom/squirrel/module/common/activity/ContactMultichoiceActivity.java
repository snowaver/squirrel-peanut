package cc.mashroom.squirrel.module.common.activity;

import  android.content.Intent;
import  android.os.Bundle;
import  android.widget.Button;
import  android.widget.ListView;
import android.widget.TextView;

import  java.io.Serializable;
import  java.util.Set;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.module.common.adapters.ContactMultichoiceListviewAdapter;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.hedgehog.widget.HeaderBar;
import  cc.mashroom.util.ObjectUtils;

public  class  ContactMultichoiceActivity    extends  AbstractActivity
{
	protected  void  onCreate( Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		super.setContentView( R.layout.activity_contact_multichoice );

		ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).setTitle( super.getString(R.string.selecting_contact) );

		ObjectUtils.cast(super.findViewById(R.id.ok_button),TextView.class).setOnClickListener( (view) -> super.putResultDataAndFinish(this,0,new  Intent().putExtra("SELECTED_CONTACT_IDS",ObjectUtils.cast(ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.contact_list),ListView.class).getAdapter(),ContactMultichoiceListviewAdapter.class).getContactIds(),Serializable.class))) );

		ObjectUtils.cast(super.findViewById(R.id.contact_list),ListView.class).setAdapter( new  ContactMultichoiceListviewAdapter(this,(Set<Long>)  super.getIntent().getSerializableExtra("EXCLUDE_CONTACT_IDS")) );
	}
}
