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

import  android.net.Uri;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.TextView;

import  com.facebook.drawee.view.SimpleDraweeView;
import  com.google.common.collect.Ordering;

import  cc.mashroom.hedgehog.parent.BaseExpandableListAdapter;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.module.home.tab.contact.fragment.ContactGroupFragment;
import  cc.mashroom.util.ObjectUtils;
import  lombok.AllArgsConstructor;

import  java.util.LinkedList;

@AllArgsConstructor

public  class  ContactGroupAdapter  extends  BaseExpandableListAdapter
{
	protected  ContactGroupFragment  context;

	public  View  getChildView(      int  groupPosition,int  childPosition,boolean  isLastChild,View  convertView,ViewGroup  parent )
	{
		convertView = convertView != null ? convertView : LayoutInflater.from(context.getContext()).inflate( R.layout.fragment_contact_child_item,parent,false );

		Contact  contact    = getChild( groupPosition,childPosition );

		ObjectUtils.cast(convertView.findViewById(R.id.nickname),TextView.class).setText( contact.getString("REMARK") );

		ObjectUtils.cast(convertView.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI(Uri.parse(context.application().baseUrl().addPathSegments("user/"+contact.getLong("ID")+"/portrait").build().toString()) );

		return  convertView;
	}

	public  View  getGroupView( int  groupPosition,boolean  isExpanded,View  convertView,ViewGroup  parent )
	{
		convertView = convertView != null ? convertView : LayoutInflater.from(context.getContext()).inflate( R.layout.fragment_contact_group_item,parent,false );

		ObjectUtils.cast(convertView.findViewById(R.id.name),TextView.class).setText( getGroup(groupPosition) );

		return  convertView;
	}

	public  Contact  getChild( int  groupPosition,int  childPosition )
	{
		return  Contact.dao.getContactGroups().get(Ordering.natural().sortedCopy(new  LinkedList<String>(Contact.dao.getContactGroups().keySet())).get(groupPosition)).get( childPosition );
	}

	public  int  getGroupCount()
	{
		return  Contact.dao.getContactGroups().keySet().size();
	}

	public  String  getGroup( int  groupPosition )
	{
		return  Ordering.natural().sortedCopy(new  LinkedList<String>(Contact.dao.getContactGroups().keySet())).get( groupPosition );
	}

	public  int  getChildrenCount( int  groupPosition )
	{
		return  Contact.dao.getContactGroups().get(Ordering.natural().sortedCopy(new  LinkedList<String>(Contact.dao.getContactGroups().keySet())).get(groupPosition)).size();
	}
}