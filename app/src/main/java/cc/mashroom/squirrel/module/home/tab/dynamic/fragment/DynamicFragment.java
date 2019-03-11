package cc.mashroom.squirrel.module.home.tab.dynamic.fragment;

import  android.os.Bundle;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.parent.AbstractFragment;

public  class  DynamicFragment  extends  AbstractFragment
{
	public  View  onCreateView( LayoutInflater  inflater,ViewGroup  container,Bundle  savedInstanceState )
	{
		if( contentView == null )
		{
			contentView = inflater.inflate( R.layout.fragment_dynamic,container,false );
		}

		return  contentView;
	}

	protected  View  contentView;
}
