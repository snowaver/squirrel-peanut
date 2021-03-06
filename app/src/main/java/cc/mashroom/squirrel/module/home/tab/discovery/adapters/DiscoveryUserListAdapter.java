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

		ObjectUtils.cast(convertView.findViewById(R.id.nickname),TextView.class).setText( user.getNickname() );

		ObjectUtils.cast(convertView.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI( Uri.parse(context.application().baseUrl().addPathSegments("user/"+user.getId()+"/portrait").build().toString()) );  return  convertView;
	}
}