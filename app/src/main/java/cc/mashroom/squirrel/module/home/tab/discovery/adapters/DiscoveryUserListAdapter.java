package cc.mashroom.squirrel.module.home.tab.discovery.adapters;

import  android.net.Uri;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.TextView;

import  com.facebook.drawee.view.SimpleDraweeView;

import  cc.mashroom.hedgehog.parent.BaseAdapter;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.module.home.tab.discovery.fragment.DiscoveryFragment;
import  cc.mashroom.util.ObjectUtils;
import  lombok.AccessLevel;
import  lombok.Setter;
import  lombok.experimental.Accessors;

import  java.util.List;

public  class  DiscoveryUserListAdapter  extends  BaseAdapter<User>
{
	public  DiscoveryUserListAdapter(  DiscoveryFragment  context,List<User>  users )
	{
		super( users );

		this.setContext( context );
	}

	@Setter( value=AccessLevel.PROTECTED )
	@Accessors( chain=true )
	protected  DiscoveryFragment  context;

	public  View  getView( final  int  position,View  convertView,ViewGroup  parent )
	{
		convertView = convertView != null ? convertView : LayoutInflater.from(context.getContext()).inflate( R.layout.fragment_discovery_user_item,parent,false );

		User  user  = getItem( position );

		ObjectUtils.cast(convertView.findViewById(R.id.nickname),TextView.class).setText( user.getString("NICKNAME") );

		ObjectUtils.cast(convertView.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI( Uri.parse(context.application().baseUrl().addPathSegments("user/"+user.get("ID")+"/portrait").build().toString()) );  return  convertView;
	}
}
