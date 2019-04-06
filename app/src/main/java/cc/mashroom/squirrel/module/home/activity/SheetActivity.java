package cc.mashroom.squirrel.module.home.activity;

import  android.app.Activity;
import  android.content.Intent;
import  android.net.Uri;
import  android.os.Bundle;
import  com.google.android.material.tabs.TabLayout;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  androidx.viewpager.widget.ViewPager;
import  android.view.WindowManager;
import  android.widget.Button;
import  android.widget.ImageView;
import android.widget.LinearLayout;
import  android.widget.ListView;
import  android.widget.SimpleAdapter;
import  android.widget.TextView;

import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.facebook.drawee.view.SimpleDraweeView;

import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.connect.ClientConnectEventDispatcher;
import  cc.mashroom.squirrel.client.connect.ClientConnectListener;
import  cc.mashroom.squirrel.client.connect.ConnectState;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.common.services.UserService;
import  cc.mashroom.squirrel.module.system.activity.RegisterActivity;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.module.home.adapters.SheetPagerAdapter;
import  cc.mashroom.squirrel.module.system.activity.LoginActivity;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.LinkedMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.stream.Stream;
import  retrofit2.Call;
import  retrofit2.Response;

import  java.util.LinkedList;
import  java.util.List;

public  class  SheetActivity  extends  AbstractActivity  implements  ClientConnectListener,TabLayout.OnTabSelectedListener
{
	public  void  onTabSelected(   TabLayout.Tab  tab )
	{
		tab.getCustomView().setBackgroundColor(     super.getResources().getColor( R.color.lightgray ) );

		ObjectUtils.cast(super.findViewById(R.id.tab_content),ViewPager.class).setCurrentItem( tab.getPosition(), false );

		if( application().getSquirrelClient().getConnectState() == ConnectState.CONNECTED )
		{
			ObjectUtils.cast(super.findViewById(R.id.title),TextView.class).setText( ObjectUtils.cast(ObjectUtils.cast(this.findViewById(R.id.tab_content),ViewPager.class).getAdapter(),SheetPagerAdapter.class).getTabs().getValue(tab.getPosition()).getInteger("title") );
		}
	}

	protected  void  onCreate(  Bundle  savedInstanceState )
	{
		ClientConnectEventDispatcher.addListener(this);

		super.onCreate( savedInstanceState );

		super.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );

		super.setContentView(R.layout.activity_sheet );

		ObjectUtils.cast(findViewById(R.id.tab_layout),TabLayout.class).addOnTabSelectedListener( this );

		connectStateChanged( application().getSquirrelClient().getConnectState() );

		ObjectUtils.cast(super.findViewById(R.id.tab_content),ViewPager.class).setAdapter( new  SheetPagerAdapter(this.getSupportFragmentManager()) );

		List<Map<String,Object>>  carteBar = new  LinkedList<Map<String,Object>>();

		for( String  title :  super.getResources().getStringArray( R.array.buddy_settings_menu_titles ) )
		{
			carteBar.add( new  HashMap<String,Object>().addEntry("title", title) );
		}

		for( java.util.Map.Entry<Integer,Integer>  tabResource  : tabs.entrySet() )
		{
			TabLayout.Tab  newTab = ObjectUtils.cast(super.findViewById(R.id.tab_layout),TabLayout.class).newTab().setCustomView( R.layout.activity_sheet_tab_indicator );

			ObjectUtils.cast(newTab.getCustomView().findViewById(R.id.icon),ImageView.class).setImageResource( tabResource.getValue() );

			ObjectUtils.cast(super.findViewById(R.id.tab_layout),TabLayout.class).addTab(       newTab );
		}

		ObjectUtils.cast(super.findViewById(R.id.settings_button),LinearLayout.class).setOnClickListener( (logoutButton) -> ActivityCompat.startActivity(this,new  Intent(this,SystemSettingsActivity.class),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle()) );

		ObjectUtils.cast(super.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI( Uri.parse(application().baseUrl().addPathSegments("user/"+application().getUserMetadata().get("ID")+"/portrait").build().toString()) );

		ObjectUtils.cast(super.findViewById(R.id.menu_list),ListView.class).setAdapter( new  SimpleAdapter(this,carteBar,R.layout.activity_sheet_menu_item,new String[]{"title"},new  int[]{R.id.name}) );
	}

	private  Map<ConnectState,Integer>  connectStateResIds = new  ConcurrentHashMap<ConnectState,Integer>().addEntry(ConnectState.NONE,R.string.connectionless).addEntry(ConnectState.CONNECTED,R.string.squirrel).addEntry(ConnectState.CONNECTING,R.string.connecting).addEntry( ConnectState.DISCONNECTED,R.string.disconnected );

	private  Map<Integer,Integer>  tabs = new  LinkedMap<Integer,Integer>().addEntry(0,R.drawable.message).addEntry(1,R.drawable.contact).addEntry(2,R.drawable.discovery).addEntry( 3,R.drawable.dynam );

	protected  void    onDestroy()
	{
		super.onDestroy();

		ClientConnectEventDispatcher.removeListener( this );
	}

	public  void  onTabReselected( TabLayout.Tab  tab )
	{

	}

	public  void  connectStateChanged( ConnectState  connectState )
	{
		application().getMainLooperHandler().post( () -> ObjectUtils.cast(SheetActivity.this.findViewById(R.id.title),TextView.class).setText(connectState == ConnectState.CONNECTED ? ObjectUtils.cast(ObjectUtils.cast(this.findViewById(R.id.tab_content),ViewPager.class).getAdapter(),SheetPagerAdapter.class).getTabs().getValue(ObjectUtils.cast(super.findViewById(R.id.tab_layout),TabLayout.class).getSelectedTabPosition()).getInteger("title") : connectStateResIds.get(connectState)) );
	}

	public  void  onTabUnselected( TabLayout.Tab  tab )
	{
		tab.getCustomView().setBackgroundColor(     super.getResources().getColor( R.color.white     ) );
	}
}
