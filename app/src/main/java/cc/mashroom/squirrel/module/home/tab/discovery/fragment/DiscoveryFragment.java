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
package cc.mashroom.squirrel.module.home.tab.discovery.fragment;

import  android.content.Intent;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.KeyEvent;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.view.inputmethod.EditorInfo;
import  android.widget.EditText;
import  android.widget.ListView;
import  android.widget.TextView;
import  android.widget.Toast;

import  com.irozon.sneaker.Sneaker;

import  java.util.List;
import  java.util.Locale;

import  cc.mashroom.hedgehog.system.LocaleChangeEventDispatcher;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.client.storage.repository.user.ContactRepository;
import  cc.mashroom.squirrel.module.home.activity.ContactProfileActivity;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.parent.AbstractFragment;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import cc.mashroom.squirrel.http.ServiceRegistry;
import  cc.mashroom.squirrel.module.home.tab.discovery.adapters.DiscoveryUserListAdapter;
import  cc.mashroom.squirrel.module.common.services.UserService;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  es.dmoral.toasty.Toasty;
import  retrofit2.Call;
import  retrofit2.Response;

public  class   DiscoveryFragment  extends  AbstractFragment  implements  TextView.OnEditorActionListener,   LocaleChangeEventDispatcher.LocaleChangeListener
{
	public  View  onCreateView( LayoutInflater  inflater,ViewGroup  container,Bundle  savedInstanceState )
	{
		LocaleChangeEventDispatcher.addListener(    DiscoveryFragment.this );

		if( contentView == null )
		{
			contentView = inflater.inflate( R.layout.fragment_discovery,container,false );

			ObjectUtils.cast(contentView.findViewById(R.id.discovery_list),ListView.class).setOnItemClickListener( (parent,view,position,id) -> {User  user = ObjectUtils.cast( parent.getAdapter().getItem(position),User.class );  Contact  contact = ContactRepository.DAO.getContactDirect().get( user.getId() );  ActivityCompat.startActivity(super.getActivity(),new  Intent(super.getActivity(),ContactProfileActivity.class).putExtra("NICKNAME",user.getNickname()).putExtra("CONTACT",contact != null ? contact : new  Contact().setId(user.getId()).setUsername(user.getUsername())),ActivityOptionsCompat.makeCustomAnimation(super.getActivity(),R.anim.right_in,R.anim.left_out).toBundle());} );

			ObjectUtils.cast(contentView.findViewById(R.id.keyword_editor).findViewById(R.id.edit_inputor),EditText.class).setOnEditorActionListener( this );
		}

		return  this.contentView;
	}

	protected  View  contentView;

	public  void  onDestroy()
	{
		super.onDestroy();

		LocaleChangeEventDispatcher.removeListener( DiscoveryFragment.this );
	}

	public  void  onChange( Locale  locale )
	{
		ObjectUtils.cast(contentView.findViewById(R.id.keyword_editor).findViewById(cc.mashroom.hedgehog.R.id.edit_inputor),EditText.class).setHint( R.string.discovery_input_keyword );
	}

	public  boolean  onEditorAction( TextView  view,int  editorActionId, KeyEvent  event )
	{
		if( editorActionId == EditorInfo.IME_ACTION_DONE )
		{
			if( StringUtils.isNotBlank(ObjectUtils.cast(contentView.findViewById(R.id.keyword_editor),StyleableEditView.class).getText().toString().trim()) )
			{
				ServiceRegistry.INSTANCE.get(UserService.class).lookup(0,ObjectUtils.cast(contentView.findViewById(R.id.keyword_editor),StyleableEditView.class).getText().toString().trim(), "{}").enqueue
				(
					new  AbstractRetrofit2Callback<List<User>>(ObjectUtils.cast(this.getActivity()),true )
					{
						public  void  onResponse( Call<List<User>>  call, Response<List<User>>  response )
						{
							super.onResponse( call, response );

							if( response.code()   != 200 )
							{
								ObjectUtils.cast(DiscoveryFragment.this.getActivity(),AbstractActivity.class).showSneakerWindow( Sneaker.with(DiscoveryFragment.this.getActivity()),com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red );
							}
							else
							{
                                if( response.body().isEmpty() )
                                {
                                    Toasty.warning(DiscoveryFragment.this.getActivity(),DiscoveryFragment.this.getString(R.string.discovery_searched_nothing),Toast.LENGTH_LONG,false).show();
                                }

                                ObjectUtils.cast(contentView.findViewById(R.id.discovery_list),ListView.class).setAdapter( new  DiscoveryUserListAdapter(DiscoveryFragment.this,response.body()) );
							}
						}
					}
				);
			}
			else
			{
				ObjectUtils.cast(this.getActivity(),AbstractActivity.class).showSneakerWindow( Sneaker.with(this.getActivity()),com.irozon.sneaker.R.drawable.ic_error,R.string.content_empty,R.color.white,R.color.red );
			}
		}

		return  false;
	}
}