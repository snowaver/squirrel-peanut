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
package cc.mashroom.squirrel.module.chat.adapters;

import  android.net.Uri;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;

import  com.facebook.drawee.view.SimpleDraweeView;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroupUser;
import  cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupUserRepository;
import cc.mashroom.squirrel.module.chat.activity.ChatGroupProfileActivity;
import  cc.mashroom.util.ObjectUtils;
import  lombok.AccessLevel;
import  lombok.AllArgsConstructor;
import  lombok.Setter;
import  lombok.experimental.Accessors;

@AllArgsConstructor

public  class  GroupChatProfileMemberGridviewAdapter  extends  cc.mashroom.hedgehog.parent.BaseAdapter
{
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors( chain=true )
	protected ChatGroupProfileActivity context;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors( chain=true )
	protected  long  chatGroupId;

	public  int   getCount()
	{
		return  Math.min( 6,ChatGroupUserRepository.DAO.lookupOne(Long.class,"SELECT  COUNT(ID)  AS  COUNT  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  IS_DELETED = FALSE",new  Object[]{chatGroupId}).intValue() );
	}

	public  ChatGroupUser  getItem(int  position )
	{
		return  ChatGroupUserRepository.DAO.lookupOne(ChatGroupUser.class,"SELECT  ID,CREATE_TIME,CREATE_BY,LAST_MODIFY_TIME,LAST_MODIFY_BY,CONTACT_ID,VCARD  FROM  "+ChatGroupUserRepository.DAO.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  AND  IS_DELETED = FALSE  ORDER  BY  ID  ASC  LIMIT  1  OFFSET  ?",new  Object[]{chatGroupId,position});
	}

	public  View  getView( int  position,View  convertView,ViewGroup  parent )
	{
		convertView = convertView != null ? convertView : LayoutInflater.from(context).inflate( R.layout.activity_group_chat_profile_member_item,parent,false );

		ChatGroupUser  chatGroupUser = getItem( position );

		ObjectUtils.cast(convertView.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI(Uri.parse(context.application().baseUrl().addPathSegments("user/"+chatGroupUser.getContactId()+"/portrait").build().toString()) );  return  convertView;
	}
}