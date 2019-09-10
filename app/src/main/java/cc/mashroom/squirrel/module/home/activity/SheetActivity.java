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

import  android.content.DialogInterface;
import  android.content.Intent;
import  android.net.Uri;
import  android.os.Bundle;

import  com.aries.ui.widget.alert.UIAlertDialog;
import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.google.android.material.tabs.TabLayout;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  androidx.core.content.res.ResourcesCompat;
import  androidx.viewpager.widget.ViewPager;

import  android.view.View;
import  android.view.WindowManager;
import  android.widget.EditText;
import  android.widget.ImageView;
import  android.widget.LinearLayout;
import  android.widget.ListView;
import  android.widget.SimpleAdapter;
import  android.widget.TextView;

import  com.facebook.drawee.view.SimpleDraweeView;
import  com.irozon.sneaker.Sneaker;

import  cc.mashroom.db.common.Db;
import  cc.mashroom.hedgehog.system.LocaleChangeEventDispatcher;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.hedgehog.util.StyleUnifier;
import  cc.mashroom.hedgehog.widget.HeaderBar;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.connect.ClientConnectEventDispatcher;
import  cc.mashroom.squirrel.client.connect.ClientConnectListener;
import  cc.mashroom.squirrel.client.connect.ConnectState;
import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupRepository;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.chat.services.ChatGroupService;
import  cc.mashroom.squirrel.module.home.tab.newsprofile.adapters.NewsProfileListAdapter;
import  cc.mashroom.squirrel.module.home.tab.newsprofile.fragment.NewsProfileFragment;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.module.home.adapters.SheetPagerAdapter;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.util.ObjectUtils;
import  retrofit2.Call;
import  retrofit2.Response;

import  java.sql.Connection;
import  java.util.ArrayList;
import  java.util.List;
import  java.util.Locale;

public  class  SheetActivity  extends  AbstractActivity  implements  ClientConnectListener,TabLayout.OnTabSelectedListener,LocaleChangeEventDispatcher.LocaleChangeListener,HeaderBar.OnItemClickListener,DialogInterface.OnClickListener
{
	protected  void  onCreate(  Bundle  savedInstanceState )
	{
		ClientConnectEventDispatcher.addListener(this);

		LocaleChangeEventDispatcher.addListener(this );

		super.onCreate( savedInstanceState );

		super.getWindow().clearFlags(    WindowManager.LayoutParams.FLAG_FULLSCREEN );

		super.setContentView(R.layout.activity_sheet );

		ObjectUtils.cast(findViewById(R.id.tab_layout),TabLayout.class).addOnTabSelectedListener( this );

		onConnectStateChanged(  application().getSquirrelClient().getConnectState() );

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

		ObjectUtils.cast(super.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI( Uri.parse(application().baseUrl().addPathSegments("user/"+application().getSquirrelClient().getUserMetadata().getId()+"/portrait").build().toString()) );

		ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).addDropdownItem(R.string.chat_create_new_group,R.color.white,18,super.getResources().getDisplayMetrics().widthPixels/2,DensityUtils.px(this,50)).setOnItemClickListener( this );
	}

	private  Map<ConnectState,Integer>  connectStateResIds = new  ConcurrentHashMap<ConnectState,Integer>().addEntry(ConnectState.NONE,R.string.connectionless).addEntry(ConnectState.CONNECTED,R.string.squirrel).addEntry(ConnectState.CONNECTING,R.string.connecting).addEntry( ConnectState.DISCONNECTED,R.string.disconnected );

	protected  void  onDestroy()
	{
		super.onDestroy();

		LocaleChangeEventDispatcher.removeListener(  this );

		ClientConnectEventDispatcher.removeListener( this );
	}

	public  void  onTabReselected( TabLayout.Tab  tab )
	{

	}

	public  void  onItemClick(View  itemView,int  position )
	{
		if( position  == 0 )
		{
			StyleUnifier.unify(new  UIAlertDialog.DividerIOSBuilder(this).setBackgroundRadius(15).setTitle(R.string.chat_create_new_group).setTitleTextSize(18).setView(R.layout.dlg_editor).setCancelable(false).setCanceledOnTouchOutside(false).setNegativeButtonTextColorResource(R.color.red).setNegativeButtonTextSize(18).setNegativeButton(R.string.cancel,(dialog, which) -> {}).setPositiveButtonTextSize(18).setPositiveButton(R.string.ok,this).create().setWidth((int)  (super.getResources().getDisplayMetrics().widthPixels*0.9)),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).show();
		}
	}
	
	public  void  onConnectStateChanged(ConnectState state )
	{
		application().getMainLooperHandler().post( () -> ObjectUtils.cast(SheetActivity.this.findViewById(R.id.title),TextView.class).setText(state == ConnectState.CONNECTED ? ObjectUtils.cast(ObjectUtils.cast(this.findViewById(R.id.tab_content),ViewPager.class).getAdapter(),SheetPagerAdapter.class).getTabs().getValue(ObjectUtils.cast(super.findViewById(R.id.tab_layout),TabLayout.class).getSelectedTabPosition()).getInteger("title") : connectStateResIds.get(state)) );
	}
	
	public  void  onClick( DialogInterface  dialog, int  i )
	{
		String  createdGroupName = ObjectUtils.cast(ObjectUtils.cast(dialog, UIAlertDialog.class).getContentView().findViewById(R.id.edit_inputor), EditText.class).getText().toString().trim();

		if( StringUtils.isNotBlank( createdGroupName) )
		{
			RetrofitRegistry.INSTANCE.get(         ChatGroupService.class ).add(createdGroupName).enqueue
			(
				new  AbstractRetrofit2Callback<OoIData>( this,true )
				{
					public  void  onResponse(      Call<OoIData>  call    , Response<OoIData>  response )
					{
						super.onResponse( call , response );

						if( response.code()    == 200 )
						{
							response.body().getChatGroupUsers().get(0).setVcard(   application().getSquirrelClient().getUserMetadata().getNickname() );

							Db.tx(String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_SERIALIZABLE,(connection) -> ChatGroupRepository.DAO.attach(application().getSquirrelClient(),response.body()) );

							ObjectUtils.cast(ObjectUtils.cast(ObjectUtils.cast(ObjectUtils.cast(ObjectUtils.cast(SheetActivity.this.findViewById(R.id.tab_content),ViewPager.class).getAdapter(),SheetPagerAdapter.class).getTabs().get("news_profile").get("fragment.instance"),NewsProfileFragment.class).getContentView().findViewById(R.id.profile_list),ListView.class).getAdapter(),NewsProfileListAdapter.class).notifyDataSetChanged();
						}
						else
						{
							showSneakerWindow( Sneaker.with(SheetActivity.this),com.irozon.sneaker.R.drawable.ic_error,response.code() == 601 ? R.string.chat_group_exist :     R.string.network_or_internal_server_error,R.color.white,R.color.red );
						}
					}
				}
			);
		}
	}

	public  void  onTabSelected(   TabLayout.Tab  tab )
	{
		tab.getCustomView().setBackgroundColor(       super.getResources().getColor(R.color.lightgray) );

		ObjectUtils.cast(super.findViewById(R.id.tab_content),ViewPager.class).setCurrentItem( tab.getPosition(), false );

		if(         super.application().getSquirrelClient().getConnectState() == ConnectState.CONNECTED )
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
		onConnectStateChanged(  application().getSquirrelClient().getConnectState() );

		ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.settings_button),LinearLayout.class).getChildAt(1),TextView.class).setText(   R.string.system_settings );

		List<Map<String,Object>>  sidebarDatas = new  ArrayList<Map<String,Object>>();

		for( String  title :  super.getResources().getStringArray( R.array.buddy_settings_menu_titles ) )
		{
			sidebarDatas.add( new  HashMap<String,Object>().addEntry("title",title) );
		}

		ObjectUtils.cast(super.findViewById(R.id.menu_list),ListView.class).setAdapter( new  SimpleAdapter( this,sidebarDatas,R.layout.activity_sheet_menu_item,new  String[]{"title"},new  int[]{R.id.name}) );

		for( int  i = 0;i <= ObjectUtils.cast(super.findViewById(R.id.tab_layout),TabLayout.class).getTabCount()-1;i=i+1 )
		{
			ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.tab_layout),TabLayout.class).getTabAt(i).getCustomView().findViewById(R.id.title),TextView.class).setText( ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.tab_content),ViewPager.class).getAdapter(),SheetPagerAdapter.class).getTabs().getValue(i).getInteger("title") );
		}

		ObjectUtils.cast(super.findViewById(R.id.header_bar).findViewById(cc.mashroom.hedgehog.R.id.additional_text),           TextView.class).setText( R.string.option );

		ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).getAddtionalDropdownContent().getChildAt(0),TextView.class).setText( R.string.chat_create_new_group );
	}
}