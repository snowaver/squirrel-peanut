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
import  android.net.Uri;
import  android.os.Bundle;

import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.View;
import  android.widget.Button;

import  com.facebook.drawee.view.SimpleDraweeView;
import  com.irozon.sneaker.Sneaker;

import  java.io.Serializable;

import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.widget.HeaderBar;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.client.storage.repository.user.ContactRepository;
import  cc.mashroom.squirrel.http.ResponseRetrofit2Callback;
import  cc.mashroom.squirrel.http.ServiceRegistry;
import  cc.mashroom.squirrel.module.chat.activity.ChatActivity;
import  cc.mashroom.squirrel.module.common.services.UserService;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribeAckPacket;
import  cc.mashroom.squirrel.parent.AbstractPacketListenerActivity;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;

public  class  ContactProfileActivity  extends  AbstractPacketListenerActivity
{
	protected  void  onCreate( Bundle  savedInstanceState )
	{
		super.onCreate(savedInstanceState);

		super.setContentView(      R.layout.activity_contact_profile );

		this.setContact(ObjectUtils.cast(super.getIntent().getSerializableExtra("CONTACT"))).setNickname(  super.getIntent().getStringExtra("NICKNAME") );

		ObjectUtils.cast(super.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI( Uri.parse(application().baseUrl().addPathSegments("user/"+this.contact.getId()+"/portrait").build().toString()) );

		ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).setText(this.contact.getUsername());

		if( StringUtils.isNotBlank(this.nickname) )
		{
			ObjectUtils.cast(super.findViewById(R.id.nickname),StyleableEditView.class).setText(this.nickname );
		}
		else
		{
			ServiceRegistry.INSTANCE.get(UserService.class).get(contact.getId()).enqueue( new  ResponseRetrofit2Callback<User>(this,false).addResponseHandler(200,(call,response) -> ObjectUtils.cast(super.findViewById(R.id.nickname),StyleableEditView.class).setText(this.nickname = response.body().getNickname())) );
		}

		ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button), Button.class).setOnClickListener( (btn) -> onChatOrSubscribeButtonClicked() );

	    ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).findViewById(R.id.additional_switcher).setOnClickListener( (button) -> ActivityCompat.startActivity(this,new  Intent(this,ContactProfileEditActivity.class).putExtra("CONTACT",contact),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle()) );
	}

	private  Map<Integer,Integer>  buttonTexts = new  HashMap<Integer,Integer>().addEntry(0,R.string.subscribe_add_contact).addEntry(1,R.string.subscribe_accept_request).addEntry(6,R.string.message).addEntry( 7,R.string.message );
	@Accessors(  chain = true )
	@Setter
    @Getter
	private  Contact   contact;
	@Accessors(  chain = true )
	@Setter
	@Getter
	private  String   nickname;

	protected   void  onStart()
	{
		super.onStart();

		Contact  contact = ContactRepository.DAO.getContactDirect().get( this.contact.getId() );

		if( contact != null  && super.application().getSquirrelClient().getUserMetadata().getId().longValue() != contact.getId() )
		{
			if( contact.getSubscribeStatus() == 1 )
			{
				ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setBackgroundColor(super.getResources().getColor(R.color.gainsboro) );
			}

			ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setText(this.buttonTexts.get(contact.getSubscribeStatus()) );

			ContextUtils.setVisibility(View.VISIBLE,super.findViewById(R.id.remark),super.findViewById(R.id.grouping) );

			ObjectUtils.cast(super.findViewById(R.id.remark),  StyleableEditView.class).setText(  contact.getRemark() );

			ObjectUtils.cast(super.findViewById(R.id.grouping)        ,StyleableEditView.class).setText( contact.getGroupName() );
		}
		else
		{
			ContextUtils.setVisibility( View.INVISIBLE,contact != null && super.application().getSquirrelClient().getUserMetadata().getId().longValue() == contact.getId() ? new  View[]{super.findViewById(R.id.remark),super.findViewById(R.id.grouping),super.findViewById(R.id.chat_or_subscribe_button)} : new  View[]{super.findViewById(R.id.remark),super.findViewById(R.id.grouping)} );
		}
	}

	public  void  onReceived(      Packet  packet )
	{
		if( packet  instanceof SubscribeAckPacket )
		{
			application().getMainLooperHandler().post( () ->{ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setBackgroundColor(super.getResources().getColor(R.color.limegreen));  ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setText(super.getResources().getText(R.string.message));} );
		}
	}

	private  void  onChatOrSubscribeButtonClicked()
	{
		Contact  contact = ContactRepository.DAO.getContactDirect().get( this.contact.getId() );

		if( contact != null &&  contact.getSubscribeStatus() == 1 )
		{
			super.showSneakerWindow(     Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_success,R.string.subscribe_request_sent,R.color.white,R.color.limegreen );
		}
		else
		if( contact != null && (contact.getSubscribeStatus() == 7||                 contact.getSubscribeStatus() == 8) )
		{
			ActivityCompat.startActivity( this,new  Intent(this,ChatActivity.class).putExtra("CONTACT_ID",contact.getId()),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in ,R.anim.left_out).toBundle() );
		}
		else
		{
			ActivityCompat.startActivity( this,new  Intent(this,ContactProfileEditActivity.class).putExtra("NICKNAME",this.nickname).putExtra("CONTACT",ObjectUtils.cast(contact != null ? contact : this.contact,Serializable.class)),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );
		}
	}
}