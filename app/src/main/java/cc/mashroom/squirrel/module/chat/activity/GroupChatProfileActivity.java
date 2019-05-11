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
import  android.widget.TextView;

import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.irozon.sneaker.Sneaker;

import  androidx.core.content.res.ResourcesCompat;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.db.common.Db;
import  cc.mashroom.hedgehog.util.ExtviewsAdapter;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroupUser;
import  cc.mashroom.squirrel.module.chat.adapters.GroupChatProfileMemberGridviewAdapter;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.common.activity.ContactMultichoiceActivity;
import  cc.mashroom.squirrel.module.chat.services.ChatGroupUserService;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.hedgehog.widget.HeaderBar;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;
import  retrofit2.Call;
import  retrofit2.Response;

import  java.io.Serializable;
import  java.sql.Connection;
import  java.util.HashSet;
import  java.util.List;
import  java.util.Set;

public  class  GroupChatProfileActivity     extends  AbstractActivity
{
	@SneakyThrows
	protected  void  onCreate( Bundle  savedInstanceState )
	{
		super.onCreate(        savedInstanceState );

		super.setContentView( R.layout.activity_group_chat_profile );

		this.setChatGroup( ChatGroup.dao.getOne("SELECT  *  FROM  "+ ChatGroup.dao.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{ super.getIntent().getLongExtra("CHAT_GROUP_ID", 0) }) );

		this.setChatGroupUser( ChatGroupUser.dao.getOne("SELECT  *  FROM  "+ChatGroupUser.dao.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  CONTACT_ID = ?",new  Object[]{this.chatGroup.getLong("ID") , application().getUserMetadata().get("ID")}) );

		ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).setTitle(    this.chatGroup.getString( "NAME" ) );

		ObjectUtils.cast(super.findViewById(R.id.name)  ,StyleableEditView.class).setText( this.chatGroup.getString( "NAME" ) );

	    ObjectUtils.cast(super.findViewById(R.id.invite_button),StyleableEditView.class).setOnContentAndRightArrowClickListener( (inviteButton)  -> inviteMembers() );

		ObjectUtils.cast(super.findViewById(R.id.more_members_button),TextView.class).setOnClickListener( (seeMoreGroupMemberButton) -> ActivityCompat.startActivity(this,new  Intent(this,ChatGroupContactActivity.class).putExtra("CHAT_GROUP_ID",chatGroup.getLong("ID")),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle()) );

		super.findViewById(R.id.leave_or_delete_button).setOnClickListener((leaveButton) ->  leaveOrDelete() );

        ObjectUtils.cast(super.findViewById(R.id.members),GridView.class).setAdapter( new  GroupChatProfileMemberGridviewAdapter(this,this.chatGroup.getLong("ID")) );
    }

	@Accessors( chain= true )
	@Setter
	private  ChatGroup  chatGroup;
	@Accessors( chain= true )
	@Setter
	private  ChatGroupUser  chatGroupUser;

	protected  void  onActivityResult( int  requestCode, int  resultCode, Intent  data )
	{
		if( data    != null )
		{
			RetrofitRegistry.get(ChatGroupUserService.class).add(chatGroup.getLong("ID"),StringUtils.join((Set<Long>)data.getSerializableExtra("SELECTED_CONTACT_IDS"), ",")).enqueue
			(
				new  AbstractRetrofit2Callback<Map<String,List<Map<String,Object>>>>( this,ExtviewsAdapter.adapter(new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCanceledOnTouchOutside(false).create(),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).setWidth(DensityUtils.px(this,220)).setHeight(DensityUtils.px(this,150)) )
				{
					@SneakyThrows
					public  void  onResponse( Call<Map<String,List<Map<String,Object>>>>  call,Response<Map<String,List<Map<String,Object>>>>  response )
					{
						super.onResponse( call, response );

						if( response.code() == 200 )
						{
							Db.tx( String.valueOf(application().getUserMetadata().getLong("ID")),Connection.TRANSACTION_SERIALIZABLE,(connection) -> ChatGroup.dao.attach(application().getSquirrelClient(),response.body()) );

							showSneakerWindow( Sneaker.with(GroupChatProfileActivity.this),com.irozon.sneaker.R.drawable.ic_success,R.string.added,R.color.white,R.color.limegreen );
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
		Set<Long>  invitedContactIds =new  HashSet<Long>();

		for( ChatGroupUser  chatGroupUser : ChatGroupUser.dao.search("SELECT  CONTACT_ID  AS  INVITED_CONTACT_ID  FROM  "+ChatGroupUser.dao.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  IS_DELETED = FALSE",new  Object[]{chatGroup.getLong("ID")}) )
		{
			invitedContactIds.add(  chatGroupUser.getLong( "INVITED_CONTACT_ID" ) );
		}

		ActivityCompat.startActivityForResult( this,new  Intent(this,ContactMultichoiceActivity.class).putExtra("EXCLUDE_CONTACT_IDS",ObjectUtils.cast(invitedContactIds,Serializable.class)),0,ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );
	}

	private  void  leaveOrDelete()
    {
		{
			RetrofitRegistry.get(ChatGroupUserService.class).secede(chatGroup.getLong("ID"),chatGroupUser.getLong("ID")).enqueue
			(
				new  AbstractRetrofit2Callback<Map<String,List<Map<String,Object>>>>( this,ExtviewsAdapter.adapter(new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCanceledOnTouchOutside(false).create(),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).setWidth(DensityUtils.px(this,220)).setHeight(DensityUtils.px(this,150)) )
				{
					@SneakyThrows
					public  void  onResponse( Call<Map<String,List<Map<String,Object>>>>  call,Response<Map<String,List<Map<String,Object>>>>  response )
					{
						super.onResponse( call, response );

						if( response.code() == 200 )
						{
							Db.tx( String.valueOf(application().getUserMetadata().getLong("ID")),Connection.TRANSACTION_SERIALIZABLE,(connection) -> ChatGroup.dao.attach(application().getSquirrelClient(),response.body()) );

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