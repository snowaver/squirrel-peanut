package cc.mashroom.squirrel.module.home.tab.contact.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.module.home.tab.contact.fragment.AddressTableFragment;
import  cc.mashroom.squirrel.module.home.tab.contact.fragment.ChatGroupFragment;
import  cc.mashroom.squirrel.module.home.tab.contact.fragment.ContactGroupFragment;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.LinkedMap;
import  cc.mashroom.util.collection.map.Map;

public  class    ContactPagerAdapter  extends  FragmentPagerAdapter
{
	public  final  static  LinkedMap<String,Map<String,Object>>  tabs = new  LinkedMap<String,Map<String,Object>>().addEntry("table",new  HashMap<String,Object>().addEntry("title",R.string.contact).addEntry("fragment.instance",new  ContactGroupFragment())).addEntry("group",new  HashMap<String,Object>().addEntry("title",R.string.chat_group).addEntry("fragment.instance",new  ChatGroupFragment())).addEntry("group",new  HashMap<String,Object>().addEntry("title",R.string.chat_group).addEntry("fragment.instance",new  ChatGroupFragment())).addEntry( "addresses",new  HashMap<String,Object>().addEntry("title",R.string.addresses).addEntry("fragment.instance",new  AddressTableFragment()) );

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
