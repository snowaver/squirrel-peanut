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
package cc.mashroom.squirrel.module.home.tab.contact.fragment;

import  android.os.Bundle;
import  com.google.android.material.tabs.TabLayout;
import  com.google.common.collect.Lists;

import  androidx.viewpager.widget.ViewPager;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

			for(  Integer  tabIconDrawableRes : this.tabIconDrawableReses )
			{
				TabLayout.Tab  tab = ObjectUtils.cast(this.contentView.findViewById(R.id.tab_layout),TabLayout.class).newTab().setCustomView( R.layout.fragment_contact_tab_indicator );

				ObjectUtils.cast(tab.getCustomView().findViewById(R.id.icon),ImageView.class).setImageResource( tabIconDrawableRes );

				if( tabIconDrawableReses.indexOf(tabIconDrawableRes) == 0 )  tab.getCustomView().setBackgroundResource( R.color.lightgray );

				ObjectUtils.cast(contentView.findViewById(R.id.tab_layout),TabLayout.class).addTab( tab );
			}

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
			ObjectUtils.cast(layout.getTabAt(position).getCustomView().findViewById(R.id.icon),ImageView.class).setImageResource( this.tabIconDrawableReses.get(position) );
		}
	}

	protected  List<Integer>  tabIconDrawableReses    = Lists.newArrayList( R.drawable.contact , R.drawable.group_chat );

	public  void  onTabSelected(   TabLayout.Tab  tab )
	{
		tab.getCustomView().setBackgroundResource(     R.color.lightgray );

		ObjectUtils.cast(contentView.findViewById(R.id.tab_content),ViewPager.class).setCurrentItem( tab.getPosition() );
	}

	public  void  onTabUnselected( TabLayout.Tab  tab )
	{
        tab.getCustomView().setBackgroundResource(         R.color.white );
	}

    public  void  onTabReselected( TabLayout.Tab  tab )
    {

    }
}