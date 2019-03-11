package cc.mashroom.squirrel.module.home.tab.contact.fragment;

import  android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.parent.AbstractFragment;
import  cc.mashroom.squirrel.module.home.tab.contact.adapters.ContactPagerAdapter;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.hedgehog.widget.NoTouchFlipViewPager;

public  class  ContactFragment  extends  AbstractFragment  implements  TabLayout.OnTabSelectedListener
{
	public  View  onCreateView( LayoutInflater  inflater,ViewGroup  container,Bundle  savedInstanceState )
	{
		if( contentView == null )
		{
			contentView = inflater.inflate( R.layout.fragment_contact,container,false );

			ObjectUtils.cast(contentView.findViewById(R.id.tab_layout),TabLayout.class).addOnTabSelectedListener( this );

			ObjectUtils.cast(contentView.findViewById(R.id.tab_content),NoTouchFlipViewPager.class).setAdapter( new  ContactPagerAdapter(super.getChildFragmentManager()) );
		}

		return  contentView;
	}

	protected  View  contentView;

	public  void  onTabSelected(   TabLayout.Tab  tab )
	{
		ObjectUtils.cast(contentView.findViewById(R.id.tab_content),ViewPager.class).setCurrentItem( tab.getPosition(),true );
	}

	public  void  onTabReselected( TabLayout.Tab  tab )
	{

	}

	public  void  onTabUnselected( TabLayout.Tab  tab )
	{

	}
}
