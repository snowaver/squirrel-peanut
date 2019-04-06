package cc.mashroom.squirrel.module.home.tab.contact.fragment;

import  android.os.Bundle;
import  com.google.android.material.tabs.TabLayout;
import  com.google.common.collect.Lists;

import  androidx.viewpager.widget.ViewPager;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;

import  java.util.List;
import  java.util.Locale;

import  cc.mashroom.hedgehog.system.LocaleChangeEventDispatcher;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.parent.AbstractFragment;
import  cc.mashroom.squirrel.module.home.tab.contact.adapters.ContactPagerAdapter;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.hedgehog.widget.NoTouchFlipViewPager;

public  class  ContactFragment  extends  AbstractFragment  implements  TabLayout.OnTabSelectedListener,LocaleChangeEventDispatcher.LocaleChangeListener
{
	public  View  onCreateView( LayoutInflater  inflater,ViewGroup  container,Bundle  savedInstanceState )
	{
		LocaleChangeEventDispatcher.addListener(    ContactFragment.this );

		if( contentView == null )
		{
			contentView = inflater.inflate( R.layout.fragment_contact,container,false );

			ObjectUtils.cast(contentView.findViewById(R.id.tab_layout),TabLayout.class).addOnTabSelectedListener( this );

			ObjectUtils.cast(contentView.findViewById(R.id.tab_content),NoTouchFlipViewPager.class).setAdapter( new  ContactPagerAdapter(super.getChildFragmentManager()) );
		}

		return  this.contentView;
	}

	protected  View  contentView;

	public  void  onDestroy()
	{
		super.onDestroy();

		LocaleChangeEventDispatcher.removeListener( ContactFragment.this );
	}

	public  void  onChange( Locale  locale )
	{
		TabLayout  layout = ObjectUtils.cast( contentView.findViewById(R.id.tab_layout),TabLayout.class );

		for( int  position = 0;position <= layout.getTabCount()-1;position= position+1 )
		{
			layout.getTabAt(position).setText(tabTitleReses.get(position));
		}
	}

	protected  List<Integer>  tabTitleReses = Lists.newArrayList( R.string.contact, R.string.chat_group );

	public  void  onTabSelected(   TabLayout.Tab  tab )
	{
		ObjectUtils.cast(this.contentView.findViewById(R.id.tab_content),ViewPager.class).setCurrentItem( tab.getPosition(),true );
	}

	public  void  onTabReselected( TabLayout.Tab  tab )
	{

	}

	public  void  onTabUnselected( TabLayout.Tab  tab )
	{

	}
}
