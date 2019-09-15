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
import  com.google.android.material.appbar.AppBarLayout;
import  com.google.android.material.bottomsheet.BottomSheetDialog;

import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.View;
import  android.widget.Button;

import  com.facebook.drawee.view.SimpleDraweeView;
import  com.irozon.sneaker.Sneaker;

import  java.io.Serializable;

import  cc.mashroom.hedgehog.widget.HeaderBar;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.connect.PacketEventDispatcher;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.client.storage.repository.user.ContactRepository;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
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
import  retrofit2.Call;
import  retrofit2.Response;

public  class  ContactProfileActivity  extends          AbstractPacketListenerActivity  implements  View.OnClickListener
{
	protected  void  onCreate( Bundle  savedInstanceState   )
	{
		super.onCreate( savedInstanceState  );

		super.setContentView(      R.layout.activity_contact_profile );

		this.contact  = ObjectUtils.cast(   super.getIntent().getSerializableExtra("CONTACT") );

		this.nickname = super.getIntent().getStringExtra( "NICKNAME" );

		ObjectUtils.cast(super.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI( Uri.parse(application().baseUrl().addPathSegments("user/"+contact.getId()+"/portrait").build().toString()) );

		ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).setText(    contact.getUsername() );

		if( StringUtils.isBlank(   nickname) )
		{
		    RetrofitRegistry.INSTANCE.get(UserService.class).get(contact.getId()).enqueue( new  AbstractRetrofit2Callback<User>(this){public  void  onResponse(Call<User>  call,Response<User>  response){ObjectUtils.cast(ContactProfileActivity.this.findViewById(R.id.nickname),StyleableEditView.class).setText(setNickname(response.body().getNickname()).getNickname());}} );
		}
		else
		{
			ObjectUtils.cast(super.findViewById(R.id.nickname),StyleableEditView.class).setText(nickname );
		}

		ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button), Button.class).setOnClickListener(    this );

	    ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).findViewById(R.id.additional_switcher).setOnClickListener( (button) -> ActivityCompat.startActivity(this,new  Intent(this,ContactProfileEditActivity.class).putExtra("CONTACT",contact),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle()) );
	}

	private  Map<Integer,Integer>  buttonTexts = new  HashMap<Integer,Integer>().addEntry(0,R.string.subscribe_add_contact).addEntry(1,R.string.subscribe_accept_request).addEntry(6,R.string.message).addEntry( 7,R.string.message );
	@Accessors(  chain = true )
	@Setter
	@Getter
	private  BottomSheetDialog         bottomSheet;
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

		if( contact == null  ||    super.application().getSquirrelClient().getUserMetadata().getId().longValue()==contact.getId() )
		{
			ObjectUtils.cast(super.findViewById(R.id.remark),  StyleableEditView.class).setVisibility( View.INVISIBLE );  ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setVisibility(View.INVISIBLE );

			if( contact != null && super.application().getSquirrelClient().getUserMetadata().getId().longValue()==contact.getId() )
			{
				ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setVisibility(   View.INVISIBLE );

				return ;
			}
		}
		else
		{
			ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setText(buttonTexts.get(contact.getSubscribeStatus()) );

			if( contact.getSubscribeStatus() == 1 )
			{
				ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setBackgroundColor(super.getResources().getColor(R.color.gainsboro) );
			}

			ObjectUtils.cast(super.findViewById(R.id.remark),  StyleableEditView.class).setVisibility(   View.VISIBLE );

			ObjectUtils.cast(super.findViewById(R.id.remark),  StyleableEditView.class).setText(  contact.getRemark() );

			ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setVisibility(   View.VISIBLE );

			ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setText(  contact.getGroupName()         );
		}
	}

	public  void  onReceived(      Packet  packet )
    {
        if( packet  instanceof SubscribeAckPacket )
        {
        	application().getMainLooperHandler().post( () ->{ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setBackgroundColor(super.getResources().getColor(R.color.limegreen));  ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setText(super.getResources().getText(R.string.message));} );
        }
    }

	public  void  onVisibilityChanged(    boolean  isSoftinputVisible )
	{
		ObjectUtils.cast(super.findViewById(R.id.collapsing_bar_layout),AppBarLayout.class).setExpanded(!isSoftinputVisible,true );
	}

	public  void  onClick(         View    button )
	{
		if( button.getId() == R.id.chat_or_subscribe_button )
		{
			Contact contact=ContactRepository.DAO.getContactDirect().get(this.contact.getId() );

			if( contact != null &&  contact.getSubscribeStatus() == 1 )
			{
				super.showSneakerWindow( Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_success,R.string.subscribe_request_sent,R.color.white,R.color.limegreen );
			}
			else
			if( contact != null && (contact.getSubscribeStatus() == 7||            contact.getSubscribeStatus() == 8) )
			{
				ActivityCompat.startActivity( this,new  Intent(this,ChatActivity.class).putExtra("CONTACT_ID",contact.getId()),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in ,R.anim.left_out).toBundle() );
			}
			else
			{
				ActivityCompat.startActivity( this,new  Intent(this,ContactProfileEditActivity.class).putExtra("NICKNAME",nickname).putExtra("CONTACT",ObjectUtils.cast(contact != null ? contact : this.contact,Serializable.class)),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );
			}
		}
	}
}