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
package cc.mashroom.squirrel.module.home.tab.contact.adapters;

import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.TextView;

import  com.facebook.drawee.view.SimpleDraweeView;

import  cc.mashroom.hedgehog.parent.BaseAdapter;
import  cc.mashroom.hedgehog.util.ImageUtils;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import  cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupRepository;
import  cc.mashroom.squirrel.module.home.tab.contact.fragment.ChatGroupFragment;
import  cc.mashroom.util.ObjectUtils;
import  lombok.AllArgsConstructor;
import  lombok.SneakyThrows;

@AllArgsConstructor

public  class  ChatGroupAdapter  extends  BaseAdapter
{
	protected  ChatGroupFragment  context;

	@SneakyThrows
	public  ChatGroup  getItem( int  position )
	{
		return  ChatGroupRepository.DAO.lookupOne(ChatGroup.class,"SELECT  ID,CREATE_TIME,LAST_MODIFY_TIME,NAME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  ORDER  BY  NAME  ASC  LIMIT  1  OFFSET  ?",new  Object[]{position});
	}
	@SneakyThrows
	public  int  getCount()
	{
		return  ChatGroupRepository.DAO.lookupOne(Long.class,"SELECT  COUNT(ID)  AS  COUNT  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table(),new  Object[]{}).intValue();
	}

	public  View  getView( int  position,View  convertView,ViewGroup  parent )
	{
		convertView = convertView != null ? convertView : LayoutInflater.from(context.getContext()).inflate( R.layout.fragment_contact_chat_group_item,parent,false );

		ObjectUtils.cast(convertView.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI(    ImageUtils.toUri(context.getActivity(),R.drawable.lightgray_placeholder) );

		ObjectUtils.cast(convertView.findViewById(R.id.name),TextView.class).setText( getItem(position).getName() );  return  convertView;
	}
}