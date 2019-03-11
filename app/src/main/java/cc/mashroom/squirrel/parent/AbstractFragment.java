package cc.mashroom.squirrel.parent;

import androidx.fragment.app.Fragment;

import  cc.mashroom.util.ObjectUtils;

public  abstract  class  AbstractFragment  extends  Fragment
{
	public  Application  application()
	{
		return  ObjectUtils.cast( getActivity().getApplication(),Application.class );
	}
}
