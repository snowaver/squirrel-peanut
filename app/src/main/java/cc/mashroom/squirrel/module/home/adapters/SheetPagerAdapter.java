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
package cc.mashroom.squirrel.module.home.adapters;

import  androidx.fragment.app.Fragment;
import  androidx.fragment.app.FragmentManager;
import  androidx.fragment.app.FragmentPagerAdapter;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.module.home.tab.contact.fragment.ContactGroupFragment;
import  cc.mashroom.squirrel.module.home.tab.discovery.fragment.DiscoveryFragment;
import  cc.mashroom.squirrel.module.home.tab.moments.fragment.MomentsFragment;
import  cc.mashroom.squirrel.module.home.tab.newsprofile.fragment.NewsProfileFragment;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.LinkedMap;
import  cc.mashroom.util.collection.map.Map;

public  class  SheetPagerAdapter  extends  FragmentPagerAdapter
{
	public  SheetPagerAdapter( FragmentManager  fragmentManager )
	{
		super(  fragmentManager );
	}

	public  LinkedMap<String,Map<String,Object>>  getTabs()
	{
		return  tabs;
	}

	private  LinkedMap<String,Map<String,Object>>  tabs = ObjectUtils.cast( new  LinkedMap<String,Map<String,Object>>().addEntry("news_profile",new  HashMap<String,Object>().addEntry("title",R.string.message).addEntry("icon",R.drawable.message).addEntry("fragment.instance",new  NewsProfileFragment())).addEntry("contact",new  HashMap<String,Object>().addEntry("title",R.string.contact).addEntry("icon",R.drawable.contact).addEntry("fragment.instance",new  ContactGroupFragment())).addEntry("discovery",new  HashMap<String,Object>().addEntry("title",R.string.discovery).addEntry("icon",R.drawable.discovery).addEntry("fragment.instance",new  DiscoveryFragment())).addEntry("moments",new  HashMap<String,Object>().addEntry("title",R.string.moments).addEntry("icon",R.drawable.moments).addEntry("fragment.instance",new  MomentsFragment())) );

	public  int  getCount()
	{
		return   this.tabs.size();
	}

	public  Fragment  getItem( int  position )
	{
		return  ObjectUtils.cast( tabs.getValue(position).get("fragment.instance"),Fragment.class );
	}
}