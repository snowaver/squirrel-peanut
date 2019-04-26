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
package cc.mashroom.squirrel.module.home.activity;

import  android.content.Intent;
import  android.net.Uri;
import  android.os.Bundle;
import  com.google.android.material.tabs.TabLayout;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  androidx.viewpager.widget.ViewPager;
import  android.view.WindowManager;
import  android.widget.ImageView;
import  android.widget.LinearLayout;
import  android.widget.ListView;
import  android.widget.SimpleAdapter;
import  android.widget.TextView;

import  com.facebook.drawee.view.SimpleDraweeView;

import  cc.mashroom.hedgehog.system.LocaleChangeEventDispatcher;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.connect.ClientConnectEventDispatcher;
import  cc.mashroom.squirrel.client.connect.ClientConnectListener;
import  cc.mashroom.squirrel.client.connect.ConnectState;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.module.home.adapters.SheetPagerAdapter;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.LinkedMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.util.ObjectUtils;

import  java.util.ArrayList;
import  java.util.List;
import  java.util.Locale;

public  class  SheetActivity  extends  AbstractActivity  implements  ClientConnectListener,TabLayout.OnTabSelectedListener,LocaleChangeEventDispatcher.LocaleChangeListener
{
	protected  void  onCreate(  Bundle  savedInstanceState )
	{
		ClientConnectEventDispatcher.addListener(this);

		LocaleChangeEventDispatcher.addListener(this );

		super.onCreate( savedInstanceState );

		super.getWindow().clearFlags(    WindowManager.LayoutParams.FLAG_FULLSCREEN );

		super.setContentView(R.layout.activity_sheet );

		ObjectUtils.cast(findViewById(R.id.tab_layout),TabLayout.class).addOnTabSelectedListener( this );

		connectStateChanged(    application().getSquirrelClient().getConnectState() );

		ObjectUtils.cast(super.findViewById(R.id.tab_content),ViewPager.class).setAdapter( new  SheetPagerAdapter(super.getSupportFragmentManager()) );

		List<Map<String,Object>>  sidebarDatas = new  ArrayList<Map<String,Object>>();

		for( String  title :  super.getResources().getStringArray( R.array.buddy_settings_menu_titles ) )
		{
			sidebarDatas.add( new  HashMap<String,Object>().addEntry("title",title) );
		}

		ObjectUtils.cast(super.findViewById(R.id.menu_list),ListView.class).setAdapter( new  SimpleAdapter( this,sidebarDatas,R.layout.activity_sheet_menu_item,new  String[]{"title"},new  int[]{R.id.name}) );

		for( Map<String,Object>  bottomTabResources : ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.tab_content),ViewPager.class).getAdapter(),SheetPagerAdapter.class).getTabs().values() )
		{
			TabLayout.Tab  newTab = ObjectUtils.cast(super.findViewById(R.id.tab_layout),TabLayout.class).newTab().setCustomView(  R.layout.activity_sheet_tab_indicator );

			ObjectUtils.cast(newTab.getCustomView().findViewById(R.id.title),TextView.class).setText( bottomTabResources.getInteger("title") );

			ObjectUtils.cast(newTab.getCustomView().findViewById(R.id.icon),ImageView.class).setImageResource( bottomTabResources.getInteger("icon") );

			ObjectUtils.cast(super.findViewById(R.id.tab_layout),      TabLayout.class).addTab( newTab );
		}

		ObjectUtils.cast(super.findViewById(R.id.settings_button),LinearLayout.class).setOnClickListener( (logoutButton) -> ActivityCompat.startActivity(this,new  Intent(this,SystemSettingsActivity.class),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle()) );

		ObjectUtils.cast(super.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI( Uri.parse(application().baseUrl().addPathSegments("user/"+application().getUserMetadata().get("ID")+"/portrait").build().toString()) );
	}

	private  Map<ConnectState,Integer>  connectStateResIds = new  ConcurrentHashMap<ConnectState,Integer>().addEntry(ConnectState.NONE,R.string.connectionless).addEntry(ConnectState.CONNECTED,R.string.squirrel).addEntry(ConnectState.CONNECTING,R.string.connecting).addEntry( ConnectState.DISCONNECTED,R.string.disconnected );

	private  Map<Integer,Integer>  tabIcons = new  LinkedMap<Integer,Integer>().addEntry(0,R.drawable.message).addEntry(1,R.drawable.contact).addEntry(2,R.drawable.discovery).addEntry( 3,R.drawable.moments );

	protected  void  onDestroy()
	{
		super.onDestroy();

		LocaleChangeEventDispatcher.removeListener(  this );

		ClientConnectEventDispatcher.removeListener( this );
	}

	public  void  onTabReselected( TabLayout.Tab  tab )
	{

	}

	public  void  connectStateChanged(   ConnectState  connectState )
	{
		application().getMainLooperHandler().post( () -> ObjectUtils.cast(SheetActivity.this.findViewById(R.id.title),TextView.class).setText(connectState == ConnectState.CONNECTED ? ObjectUtils.cast(ObjectUtils.cast(this.findViewById(R.id.tab_content),ViewPager.class).getAdapter(),SheetPagerAdapter.class).getTabs().getValue(ObjectUtils.cast(super.findViewById(R.id.tab_layout),TabLayout.class).getSelectedTabPosition()).getInteger("title") : connectStateResIds.get(connectState)) );
	}

	public  void  onTabSelected(   TabLayout.Tab  tab )
	{
		tab.getCustomView().setBackgroundColor(       super.getResources().getColor(R.color.lightgray) );

		ObjectUtils.cast(super.findViewById(R.id.tab_content),ViewPager.class).setCurrentItem( tab.getPosition(), false );

		if( super.application().getSquirrelClient().getConnectState()         == ConnectState.CONNECTED )
		{
			ObjectUtils.cast(super.findViewById(R.id.title),TextView.class).setText( ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.tab_content),ViewPager.class).getAdapter(),SheetPagerAdapter.class).getTabs().getValue(tab.getPosition()).getInteger("title") );
		}
	}

	public  void  onTabUnselected( TabLayout.Tab  tab )
	{
		tab.getCustomView().setBackgroundColor(super.getResources().getColor(R.color.white)/* COLOR */ );
	}

	public  void  onChange( Locale  locale )
	{
		connectStateChanged(    application().getSquirrelClient().getConnectState() );

		ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.settings_button),LinearLayout.class).getChildAt(1),TextView.class).setText(   R.string.system_settings );

		List<Map<String,Object>>  sidebarDatas = new  ArrayList<Map<String,Object>>();

		for( String  title :  super.getResources().getStringArray( R.array.buddy_settings_menu_titles ) )
		{
			sidebarDatas.add( new  HashMap<String,Object>().addEntry("title",title) );
		}

		ObjectUtils.cast(super.findViewById(R.id.menu_list),ListView.class).setAdapter( new  SimpleAdapter( this,sidebarDatas,R.layout.activity_sheet_menu_item,new  String[]{"title"},new  int[]{R.id.name}) );
	}
}