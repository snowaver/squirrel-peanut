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
package cc.mashroom.squirrel.module.chat.activity;

import  android.content.Intent;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.widget.GridView;
import  android.widget.ImageView;

import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.irozon.sneaker.Sneaker;

import  androidx.core.content.res.ResourcesCompat;

import  cc.mashroom.hedgehog.module.common.activity.EditorActivity;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.db.common.Db;
import  cc.mashroom.hedgehog.util.StyleUnifier;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroupUser;
import  cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupRepository;
import  cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupUserRepository;
import  cc.mashroom.squirrel.module.chat.adapters.GroupChatProfileMemberGridviewAdapter;
import  cc.mashroom.squirrel.module.chat.services.ChatGroupService;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.common.activity.ContactMultichoiceActivity;
import  cc.mashroom.squirrel.module.chat.services.ChatGroupUserService;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.hedgehog.widget.HeaderBar;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;
import  retrofit2.Call;
import  retrofit2.Response;

import  java.io.Serializable;
import  java.sql.Connection;
import  java.util.HashSet;
import  java.util.Set;

public  class  GroupChatProfileActivity     extends  AbstractActivity
{
	@SneakyThrows
	protected  void  onCreate( Bundle   savedInstanceState )
	{
		super.onCreate(        savedInstanceState );

		super.setContentView( R.layout.activity_group_chat_profile );

		this.setChatGroup( ChatGroupRepository.DAO.lookupOne(ChatGroup.class,"SELECT  *  FROM  "+ ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{ super.getIntent().getLongExtra("CHAT_GROUP_ID", 0) }) );

		this.setChatGroupUser( ChatGroupUserRepository.DAO.lookupOne(ChatGroupUser.class,"SELECT  *  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  CONTACT_ID = ?",new  Object[]{this.chatGroup.getId(),application().getSquirrelClient().getUserMetadata().getId()}) );

		ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).setTitle(    chatGroup.getName() );

		ObjectUtils.cast(super.findViewById(R.id.name)  ,StyleableEditView.class).setText( chatGroup.getName() );

		ObjectUtils.cast(super.findViewById(R.id.name)  ,StyleableEditView.class).getContentSwitcher().getDisplayedChild().setOnClickListener( (v) -> ActivityCompat.startActivityForResult(this,new  Intent(this,EditorActivity.class).putExtra("CONTENT",chatGroup.getName()).putExtra("TITLE",super.getString(R.string.name)).putExtra("MAX_COUNT",16),1,ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle()) );

	    ObjectUtils.cast(super.findViewById(R.id.invite_button),StyleableEditView.class).setOnClickListener(    (inviteContactButton) -> inviteMembers() );

		ObjectUtils.cast(super.findViewById(R.id.more_members_button),ImageView.class).setOnClickListener( (seeMoreGroupMemberButton) -> ActivityCompat.startActivity(this,new  Intent(this,ChatGroupContactActivity.class).putExtra("CHAT_GROUP_ID",chatGroup.getId()),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle()) );

		super.findViewById(R.id.leave_or_delete_button).setOnClickListener((leaveButton)   ->  leaveOrDelete() );

        ObjectUtils.cast(super.findViewById(R.id.members),GridView.class).setAdapter( new  GroupChatProfileMemberGridviewAdapter(this,chatGroup.getId()) );
    }

	@Accessors( chain= true )
	@Setter
	private  ChatGroup  chatGroup;
	@Accessors( chain= true )
	@Setter
	private  ChatGroupUser  chatGroupUser;

	protected  void  onActivityResult(        int  requestCode, int  resultCode, Intent  data )
	{
		super.onActivityResult(requestCode,resultCode,data);

		if( data    == null )
		{
			return;
		}

		if( requestCode ==0 )
		{
			RetrofitRegistry.INSTANCE.get(ChatGroupUserService.class).add(chatGroup.getId(),StringUtils.join((Set<Long>)  data.getSerializableExtra("SELECTED_CONTACT_IDS"),",")).enqueue
			(
				new  AbstractRetrofit2Callback<OoIData>(  this,true )
				{
					@SneakyThrows
					public  void  onResponse( Call<OoIData>  call,Response<OoIData>  response )
					{
						super.onResponse( call,  response );

						if( response.code() == 200 )
						{
							Db.tx( String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_SERIALIZABLE,(connection) -> ChatGroupRepository.DAO.attach(application().getSquirrelClient(),response.body()) );

                            ObjectUtils.cast(ObjectUtils.cast(GroupChatProfileActivity.this.findViewById(R.id.members),GridView.class).getAdapter(),   GroupChatProfileMemberGridviewAdapter.class).notifyDataSetChanged();

							showSneakerWindow( Sneaker.with(GroupChatProfileActivity.this),com.irozon.sneaker.R.drawable.ic_success,    R.string.added,R.color.white,R.color.limegreen );
						}
						else
						{
							showSneakerWindow( Sneaker.with(GroupChatProfileActivity.this),com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red );
						}
					}
				}
			);
		}
		else
		if( requestCode ==1 )
        {
			RetrofitRegistry.INSTANCE.get(ChatGroupService.class).update(chatGroup.getId(),data.getStringExtra("EDIT_CONTENT")).enqueue
			(
				new  AbstractRetrofit2Callback<OoIData>(  this,true )
				{
					@SneakyThrows
					public  void  onResponse( Call<OoIData>  call,Response<OoIData>  response )
					{
						super.onResponse( call,  response );

						if( response.code() == 200 )
						{
							Db.tx( String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_SERIALIZABLE,(connection) -> ChatGroupRepository.DAO.attach(application().getSquirrelClient(),response.body()) );

                            setChatGroup( ChatGroupRepository.DAO.lookupOne(ChatGroup.class,"SELECT  *  FROM  "+ ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{chatGroup.getId()}) );

                            ObjectUtils.cast(GroupChatProfileActivity.this.findViewById(R.id.name),StyleableEditView.class).setText( chatGroup.getName() );

							ObjectUtils.cast(GroupChatProfileActivity.this.findViewById(R.id.header_bar),HeaderBar.class).setTitle(  chatGroup.getName() );

							showSneakerWindow( Sneaker.with(GroupChatProfileActivity.this),com.irozon.sneaker.R.drawable.ic_success,R.string.updated,R.color.white,R.color.limegreen );
						}
						else
						{
							showSneakerWindow( Sneaker.with(GroupChatProfileActivity.this),com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red );
						}
					}
				}
			);
        }
	}

	private  void  inviteMembers()
	{
		Set<Long>  invitedContactIds = new  HashSet<Long>();

		for( ChatGroupUser  chatGroupUser : ChatGroupUserRepository.DAO.lookup(ChatGroupUser.class,"SELECT  CONTACT_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  IS_DELETED = FALSE",new  Object[]{chatGroup.getId()}) )
		{
			invitedContactIds.add(    chatGroupUser.getContactId() );
		}

		ActivityCompat.startActivityForResult( this, new  Intent(this,ContactMultichoiceActivity.class).putExtra("EXCLUDE_CONTACT_IDS",ObjectUtils.cast(invitedContactIds,Serializable.class)),0,ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );
	}

	private  void  leaveOrDelete()
    {
		{
			RetrofitRegistry.INSTANCE.get(ChatGroupUserService.class).secede(this.chatGroup.getId(),this.chatGroupUser.getId()).enqueue
			(
				new  AbstractRetrofit2Callback<OoIData>(  this,true )
				{
					@SneakyThrows
					public  void  onResponse( Call<OoIData>  call,Response<OoIData>  response )
					{
						super.onResponse( call,  response );

						if( response.code() == 200 )
						{
							Db.tx( String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_SERIALIZABLE,(connection) -> ChatGroupRepository.DAO.attach(application().getSquirrelClient(),response.body()) );

							showSneakerWindow( Sneaker.with(GroupChatProfileActivity.this).setOnSneakerDismissListener(() -> application().getMainLooperHandler().postDelayed(() -> {STACK.get(STACK.size()-2).finish();  GroupChatProfileActivity.this.finish();},500)),com.irozon.sneaker.R.drawable.ic_success,R.string.chat_group_left_or_deleted,R.color.white,R.color.limegreen );
						}
						else
						{
							showSneakerWindow( Sneaker.with(GroupChatProfileActivity.this),com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red );
						}
					}
				}
			);
		}
    }
}