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

import  com.fasterxml.jackson.core.type.TypeReference;
import  com.irozon.sneaker.Sneaker;

import  cc.mashroom.db.common.Db;
import  cc.mashroom.hedgehog.widget.BottomSheetEditor;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroupUser;
import  cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupRepository;
import  cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupUserRepository;
import  cc.mashroom.squirrel.http.ResponseRetrofit2Callback;
import  cc.mashroom.squirrel.module.chat.adapters.GroupChatProfileMemberGridviewAdapter;
import  cc.mashroom.squirrel.module.chat.services.ChatGroupService;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.http.ServiceRegistry;
import  cc.mashroom.squirrel.module.common.activity.ContactMultichoiceActivity;
import  cc.mashroom.squirrel.module.chat.services.ChatGroupUserService;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatEventPacket;
import  cc.mashroom.squirrel.parent.AbstractPacketListenerActivity;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.hedgehog.widget.HeaderBar;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;
import  retrofit2.Response;

import  java.io.Serializable;
import  java.sql.Connection;
import  java.util.HashSet;
import  java.util.Set;

public  class  GroupChatProfileActivity     extends       AbstractPacketListenerActivity
{
	@SneakyThrows
	protected  void  onCreate( Bundle   savedInstanceState )
	{
		super.onCreate(  savedInstanceState );

		super.setContentView(R.layout.activity_group_chat_profile );

		this.setChatGroup( ChatGroupRepository.DAO.lookupOne(ChatGroup.class,"SELECT  *  FROM  "+ ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{super.getIntent().getLongExtra("CHAT_GROUP_ID",0)}) );

		this.setChatGroupUser( ChatGroupUserRepository.DAO.lookupOne(ChatGroupUser.class,"SELECT  *  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  CONTACT_ID = ?  AND  IS_DELETED = FALSE",new  Object[]{this.chatGroup.getId(),application().getSquirrelClient().getUserMetadata().getId()}) );

		ObjectUtils.cast(super.findViewById(R.id.header_bar)  ,HeaderBar.class).setTitle( this.chatGroup.getName() );

		super.findViewById(R.id.invite_button).setOnClickListener((v)->inviteMembers());
		
		ObjectUtils.cast(super.findViewById(R.id.name),StyleableEditView.class).setText(  this.chatGroup.getName() );

		if( chatGroup.getCreateBy() == super.application().getSquirrelClient().getUserMetadata().getId() )
        {
            ObjectUtils.cast(super.findViewById(R.id.name),StyleableEditView.class).getContentSwitcher().getDisplayedChild().setOnClickListener( (v) -> new  BottomSheetEditor(this,16).setOnEditCompleteListener((groupName) -> ServiceRegistry.INSTANCE.get(ChatGroupService.class).update(this.chatGroup.getId(),groupName.toString()).enqueue(new  ResponseRetrofit2Callback(this,true).addResponseHandler(200,(call,response) -> onNameChanged(response)))).show() );
        }

		super.findViewById(R.id.more_members_button).setOnClickListener( (seeMoreGroupMemberButton) -> ActivityCompat.startActivity(this,new  Intent(this,ChatGroupContactActivity.class).putExtra("CHAT_GROUP_ID",chatGroup.getId()),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle()) );

		super.findViewById(R.id.leave_or_delete_button).setOnClickListener((leaveButton) -> ServiceRegistry.INSTANCE.get(ChatGroupUserService.class).secede(this.chatGroup.getId(),this.chatGroupUser.getId()).enqueue(new  ResponseRetrofit2Callback(this,true).addResponseHandler(200,(call,response) -> onLeftAndDeleted(response))) );

        ObjectUtils.cast(super.findViewById(R.id.members),GridView.class).setAdapter( new  GroupChatProfileMemberGridviewAdapter(this,this.chatGroup.getId()) );
    }

	@Accessors( chain= true )
	@Setter
	private  ChatGroup  chatGroup;
	@Accessors( chain= true )
	@Setter
	private  ChatGroupUser      chatGroupUser;
	@Override
	public  void  onReceived( Packet  packet )
	{
		super.onReceived(packet );

		if( packet instanceof GroupChatEventPacket && ObjectUtils.cast(packet,GroupChatEventPacket.class).getGroupId()==this.chatGroup.getId() )
        {
            super.application().getMainLooperHandler().post(  ()  ->   this.refresh() );
        }
	}
	
	protected  void  onActivityResult( int  requestCode, int  resultCode, Intent  data )
	{
		super.onActivityResult( requestCode  ,   resultCode, data );

		if( data != null && requestCode == 0 )
		{
			ServiceRegistry.INSTANCE.get(ChatGroupUserService.class).add(this.chatGroup.getId(),StringUtils.join(ObjectUtils.cast(data.getSerializableExtra("SELECTED_CONTACT_IDS"),new  TypeReference<Set<Long>>(){}),",")).enqueue( new  ResponseRetrofit2Callback(this,true).addResponseHandler(200,(call, response) -> onMembersInvited(response)) );
		}
	}

	private  void  refresh( )
    {
        this.setChatGroup( ChatGroupRepository.DAO.lookupOne(ChatGroup.class,"SELECT  *  FROM  "+ ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{super.getIntent().getLongExtra("CHAT_GROUP_ID",0)}) );

        ObjectUtils.cast(super.findViewById(R.id.header_bar)  ,HeaderBar.class).setTitle( this.chatGroup.getName() );

        ObjectUtils.cast(super.findViewById(R.id.name),StyleableEditView.class).setText(  this.chatGroup.getName() );

        ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.members),GridView.class).getAdapter(),GroupChatProfileMemberGridviewAdapter.class).notifyDataSetChanged();
    }
	
	private  void  inviteMembers()
	{
		Set<Long>  inviteeContactIds = new  HashSet<Long>();

		for( ChatGroupUser  chatGroupUser : ChatGroupUserRepository.DAO.lookup(ChatGroupUser.class,"SELECT  CONTACT_ID  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  IS_DELETED = FALSE",new  Object[]{this.chatGroup.getId()}) )
		{
			inviteeContactIds.add(   chatGroupUser.getContactId() );
		}

		ActivityCompat.startActivityForResult(this,new  Intent(this,ContactMultichoiceActivity.class).putExtra("EXCLUDE_CONTACT_IDS",ObjectUtils.cast(inviteeContactIds,Serializable.class)),0,ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );
	}

	private  void  onMembersInvited( Response  <OoIData>  response )
	{
		Db.tx( String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_REPEATABLE_READ,(connection) -> ChatGroupRepository.DAO.attach(application().getSquirrelClient(),response.body(),false) );

		ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.members),GridView.class).getAdapter(),GroupChatProfileMemberGridviewAdapter.class).notifyDataSetChanged();

		super.showSneakerWindow( Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_success,R.string.added  ,R.color.white,R.color.limegreen );
	}

	private  void  onNameChanged(    Response  <OoIData>  response )
	{
		Db.tx( String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_REPEATABLE_READ,(connection) -> ChatGroupRepository.DAO.attach(application().getSquirrelClient(),response.body(),false) );

		this.setChatGroup( ChatGroupRepository.DAO.lookupOne(ChatGroup.class,"SELECT  *  FROM  "+ ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{super.getIntent().getLongExtra("CHAT_GROUP_ID",0)}) );
		
        ObjectUtils.cast(super.findViewById(R.id.name),StyleableEditView.class).setText(  this.chatGroup.getName() );

		ObjectUtils.cast(super.findViewById(R.id.header_bar)  ,HeaderBar.class).setTitle( this.chatGroup.getName() );

		super.showSneakerWindow( Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_success,R.string.updated,R.color.white,R.color.limegreen );
	}

	private  void  onLeftAndDeleted( Response  <OoIData>  response )
    {
		Db.tx( String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_REPEATABLE_READ,(connection) -> ChatGroupRepository.DAO.attach(application().getSquirrelClient(),response.body(),false) );

		super.showSneakerWindow( Sneaker.with(this).setOnSneakerDismissListener(() -> application().getMainLooperHandler().postDelayed(() -> {STACK.get(STACK.size()-2).finish();  super.finish();},500)),com.irozon.sneaker.R.drawable.ic_success,R.string.chat_group_left_or_deleted,R.color.white,R.color.limegreen );
    }
}