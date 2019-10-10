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

import  android.content.DialogInterface;
import  android.content.Intent;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.EditText;
import  android.widget.LinearLayout;
import  android.widget.ListView;
import  android.widget.TextView;

import  com.aries.ui.widget.alert.UIAlertDialog;
import  com.irozon.sneaker.Sneaker;

import  java.sql.Connection;
import  java.util.Locale;

import  androidx.core.content.res.ResourcesCompat;
import  cc.mashroom.db.common.Db;
import  cc.mashroom.hedgehog.system.LocaleChangeEventDispatcher;
import  cc.mashroom.hedgehog.util.StyleUnifier;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import  cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupRepository;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.parent.AbstractFragment;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import cc.mashroom.squirrel.http.ServiceRegistry;
import  cc.mashroom.squirrel.module.chat.activity.GroupChatActivity;
import  cc.mashroom.squirrel.module.home.tab.contact.adapters.ChatGroupAdapter;
import  cc.mashroom.squirrel.module.chat.services.ChatGroupService;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  retrofit2.Call;
import  retrofit2.Response;

public  class  ChatGroupFragment  extends  AbstractFragment  implements  DialogInterface.OnClickListener,LocaleChangeEventDispatcher.LocaleChangeListener
{
	public  View  onCreateView( LayoutInflater  inflater,ViewGroup  container,Bundle  savedInstanceState )
	{
		LocaleChangeEventDispatcher.addListener(    ChatGroupFragment.this );

		if( contentView == null )
		{
			contentView = inflater.inflate( R.layout.fragment_contact_chat_group,container,false );

			ObjectUtils.cast(contentView.findViewById(R.id.chat_group_list),ListView.class).setAdapter( new  ChatGroupAdapter(this ) );

			ObjectUtils.cast(contentView.findViewById(R.id.chat_group_list),ListView.class).setOnItemClickListener( (parent,view,position,id) -> ActivityCompat.startActivity(super.getActivity(),new  Intent(this.getActivity(),GroupChatActivity.class).putExtra("CHAT_GROUP_ID",ObjectUtils.cast(parent.getAdapter().getItem(position),ChatGroup.class).getId()),ActivityOptionsCompat.makeCustomAnimation(super.getActivity(),R.anim.right_in,R.anim.left_out).toBundle()) );

			ObjectUtils.cast(contentView.findViewById(R.id.create_button),LinearLayout.class).setOnClickListener( (view) -> StyleUnifier.unify(new  UIAlertDialog.DividerIOSBuilder(this.getActivity()).setBackgroundRadius(15).setTitle(R.string.chat_create_new_group).setTitleTextSize(18).setView(R.layout.dlg_editor).setCancelable(false).setCanceledOnTouchOutside(false).setNegativeButtonTextColorResource(R.color.red).setNegativeButtonTextSize(18).setNegativeButton(R.string.cancel,(dialog, which) -> {}).setPositiveButtonTextSize(18).setPositiveButton(R.string.ok,this).create().setWidth((int)  (super.getResources().getDisplayMetrics().widthPixels*0.88)),ResourcesCompat.getFont(this.getActivity(),R.font.droid_sans_mono)).show() );
		}

		return  this.contentView;
	}

	protected  View  contentView;

	public  void  onDestroy()
	{
		super.onDestroy();

		LocaleChangeEventDispatcher.removeListener( ChatGroupFragment.this );
	}

	public  void  onChange( Locale   locale )
	{
		ObjectUtils.cast(ObjectUtils.cast(this.contentView.findViewById(R.id.create_button),LinearLayout.class).getChildAt(1),TextView.class).setText( R.string.chat_create_new_group );
	}

	public  void  onClick(  DialogInterface  dialog ,int  witch )
	{
		String  addingGroupName = ObjectUtils.cast(ObjectUtils.cast(dialog,UIAlertDialog.class).getContentView().findViewById(R.id.edit_inputor),EditText.class).getText().toString().trim();

		if( StringUtils.isNotBlank(addingGroupName) )
		{
			ServiceRegistry.INSTANCE.get(ChatGroupService.class).add(     addingGroupName).enqueue
			(
				new  AbstractRetrofit2Callback<OoIData>(     ObjectUtils.cast(super.getActivity()))
				{
					public  void  onResponse( Call<OoIData>  call   , Response<OoIData>  response )
					{
						if( response.code()  == 200 )
						{
							response.body().getChatGroupUsers().get(0).setVcard(     application().getSquirrelClient().getUserMetadata().getNickname() );

							Db.tx(String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_SERIALIZABLE,(connection) -> ChatGroupRepository.DAO.attach(application().getSquirrelClient(),response.body(),false) );

							ObjectUtils.cast(ObjectUtils.cast(ChatGroupFragment.this.getView().findViewById(R.id.chat_group_list),ListView.class).getAdapter(),ChatGroupAdapter.class).notifyDataSetChanged();
						}
						else
						{
							ObjectUtils.cast(ChatGroupFragment.this,AbstractActivity.class).showSneakerWindow( Sneaker.with(ChatGroupFragment.this.getActivity()),com.irozon.sneaker.R.drawable.ic_error,response.code() == 601 ? R.string.chat_group_exist : R.string.network_or_internal_server_error,R.color.white,R.color.red );
						}
					}
				}
			);
		}
	}
}