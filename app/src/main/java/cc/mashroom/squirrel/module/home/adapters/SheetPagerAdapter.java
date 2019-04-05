package cc.mashroom.squirrel.module.home.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.module.home.tab.contact.fragment.ContactFragment;
import  cc.mashroom.squirrel.module.home.tab.discovery.fragment.DiscoveryFragment;
import  cc.mashroom.squirrel.module.home.tab.dynamic.fragment.DynamicFragment;
import  cc.mashroom.squirrel.module.home.tab.newsprofile.fragment.NewsProfileFragment;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.LinkedMap;
import  cc.mashroom.util.collection.map.Map;

public  class  SheetPagerAdapter  extends  FragmentPagerAdapter
{
	public  SheetPagerAdapter( FragmentManager  fragmentManager )
	{
		super( fragmentManager );
	}

	public  LinkedMap<String,Map<String,Object>>  getTabs()
	{
		return  tabs;
	}

	private  LinkedMap<String,Map<String,Object>>  tabs = ObjectUtils.cast( new  LinkedMap<String,Map<String,Object>>().addEntry("news_profile",new  HashMap<String,Object>().addEntry("title",R.string.message).addEntry("fragment.instance",new  NewsProfileFragment())).addEntry("contact",new  HashMap<String,Object>().addEntry("title",R.string.contact).addEntry("fragment.instance",new  ContactFragment())).addEntry("discovery",new  HashMap<String,Object>().addEntry("title",R.string.discovery).addEntry("fragment.instance",new  DiscoveryFragment())).addEntry("dynamic",new  HashMap<String,Object>().addEntry("title",R.string.moments).addEntry("fragment.instance",new  DynamicFragment())) );

	public  int  getCount()
	{
		return tabs.size();
	}

	public  Fragment  getItem( int  position )
	{
		return  ObjectUtils.cast( tabs.getValue(position).get("fragment.instance"),Fragment.class );
	}
}
