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

		ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).setTitle( super.getString(R.string.select) );

		ObjectUtils.cast(super.findViewById(R.id.ok_button),TextView.class).setOnClickListener( (view) -> super.putResultDataAndFinish(this,0,new  Intent().putExtra("SELECTED_CONTACT_IDS",ObjectUtils.cast(ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.contact_list),ListView.class).getAdapter(),ContactMultichoiceListviewAdapter.class).getContactIds(),Serializable.class))) );

		ObjectUtils.cast(super.findViewById(R.id.contact_list),ListView.class).setAdapter( new  ContactMultichoiceListviewAdapter(this,(Set<Long>)  super.getIntent().getSerializableExtra("EXCLUDE_CONTACT_IDS")) );
	}
}