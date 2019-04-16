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

import  androidx.fragment.app.Fragment;
import  androidx.fragment.app.FragmentManager;
import  androidx.fragment.app.FragmentPagerAdapter;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.module.home.tab.contact.fragment.ChatGroupFragment;
import  cc.mashroom.squirrel.module.home.tab.contact.fragment.ContactGroupFragment;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.LinkedMap;
import  cc.mashroom.util.collection.map.Map;

public  class    ContactPagerAdapter  extends  FragmentPagerAdapter
{
	public  final  static  LinkedMap<String,Map<String,Object>>  tabs = new  LinkedMap<String,Map<String,Object>>().addEntry("table",new  HashMap<String,Object>().addEntry("title",R.string.contact).addEntry("fragment.instance",new  ContactGroupFragment())).addEntry("group",new  HashMap<String,Object>().addEntry("title",R.string.chat_group).addEntry("fragment.instance",new  ChatGroupFragment())).addEntry( "group",new  HashMap<String,Object>().addEntry("title",R.string.chat_group).addEntry("fragment.instance",new  ChatGroupFragment()) );

	public  ContactPagerAdapter( FragmentManager  fragmentManager )
	{
		super( fragmentManager );
	}

	public  int   getCount()
	{
		return  tabs.size();
	}

	public  Fragment  getItem( int  position )
	{
		return  ObjectUtils.cast( tabs.getValue(position).get("fragment.instance"),Fragment.class );
	}
}