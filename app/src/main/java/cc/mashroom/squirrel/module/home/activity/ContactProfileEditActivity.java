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
package cc.mashroom.squirrel.module.home.activity;

import  android.content.Intent;
import  android.os.Bundle;
import  android.widget.Button;

import  androidx.annotation.Nullable;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;

import  com.fasterxml.jackson.core.type.TypeReference;
import  com.google.common.collect.Lists;
import  com.irozon.sneaker.Sneaker;

import  cc.mashroom.hedgehog.module.common.activity.EditorActivity;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.common.services.ContactService;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  lombok.Setter;
import  lombok.experimental.Accessors;
import  retrofit2.Call;
import  retrofit2.Response;

public  class  ContactProfileEditActivity     extends  AbstractActivity
{
	@Accessors( chain = true )
	@Setter
	private  User  user;

	private cc.mashroom.util.collection.map.Map<Integer,Integer> buttonTexts = new HashMap<Integer,Integer>().addEntry(0,R.string.subscribe_add_contact).addEntry(1,R.string.subscribe_accept_request).addEntry(6,R.string.message).addEntry( 7,R.string.message );

	protected  void  onCreate(   Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		this.setUser( ObjectUtils.cast(new  User().addEntries(ObjectUtils.cast(super.getIntent().getSerializableExtra("USER"),new  TypeReference<Map>(){}))) );

		Contact  contact = Contact.dao.getContactDirect().get( user.getLong("ID") );

		super.setContentView( R.layout.activity_contact_profile_edit );

		ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setText( contact != null && StringUtils.isNotBlank(contact.getString("GROUP_NAME")) ? contact.getString("GROUP_NAME") : "" );

		if( contact  != null )
		{
			if( contact.getInteger("SUBSCRIBE_STATUS") == 0 )
			{
				ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setBackgroundColor( super.getResources().getColor(R.color.gainsboro) );
			}

			ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setText(buttonTexts.get(contact.getInteger("SUBSCRIBE_STATUS")) );
		}

		ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).setText( user.getString(StringUtils.isBlank(user.getString("REMARK")) ? "NICKNAME" : "REMARK") );

		ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).setOnClickListener( (v) -> ActivityCompat.startActivityForResult(this,new  Intent(this,EditorActivity.class).putExtra("EDIT_CONTENT",ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText()).putExtra("TITLE",super.getString(R.string.remark)).putExtra("LIMITATION",16),0,ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle()) );
	}

	public  void  responsed( Response<Void>  response,Contact  contact,Map<String,Object>  data )
	{
		if( response.code() == 200 )
		{
			super.showSneakerWindow( Sneaker.with(this),           com.irozon.sneaker.R.drawable.ic_success,R.string.updated,R.color.white,R.color.limegreen );

			Contact.dao.attach(Lists.newArrayList(data));contact.addEntries( data );
		}
		else
		{
			super.showSneakerWindow( Sneaker.with(this),   com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red );
		}
	}

	protected  void  onActivityResult( int  requestCode,int  resultCode,@Nullable  Intent  data )
	{
		super.onActivityResult(requestCode,resultCode,data );

		if( data     == null )
		{
			return;
		}

		Map<String,Object>  upsertData = new  HashMap<String,Object>();

		Contact  contact = Contact.dao.getContactDirect().get( user.getLong("ID") );

		if( requestCode == 0 )
		{
			ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).setText( data.getStringExtra("EDIT_CONTENT") );
		}
		else
		if( requestCode == 1 )
		{
			ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setText( data.getStringExtra("GROUP_NAME") );
		}

		upsertData.addEntry("ID",user.getLong("ID")).addEntry("REMARK",ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText()).addEntry( "GROUP_NAME",ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText() );

		if( contact  != null )
		{
			if( !contact.getString("REMARK").equals(ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim()) || !contact.getString("GROUP_NAME").equals(ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText()) )
			{
				RetrofitRegistry.get(ContactService.class).update(user.getLong("ID"),ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText().toString().trim()).enqueue(new  AbstractRetrofit2Callback<Void>(this){public  void  onResponse(Call<Void>  call,Response<Void>  response){ responsed(response,contact,upsertData); }} );
			}
		}
	}
}