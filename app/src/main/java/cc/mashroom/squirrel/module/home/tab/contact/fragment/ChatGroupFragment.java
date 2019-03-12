package cc.mashroom.squirrel.module.home.tab.contact.fragment;

import  android.content.DialogInterface;
import  android.content.Intent;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.Button;
import  android.widget.EditText;
import  android.widget.LinearLayout;
import  android.widget.ListView;

import  com.aries.ui.widget.alert.UIAlertDialog;
import  com.irozon.sneaker.Sneaker;

import  java.sql.Connection;
import  java.util.List;

import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.db.common.Db;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.parent.AbstractFragment;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.chat.activity.GroupChatActivity;
import  cc.mashroom.squirrel.module.home.tab.contact.adapters.ChatGroupAdapter;
import  cc.mashroom.squirrel.module.chat.services.ChatGroupService;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.Map;
import  lombok.SneakyThrows;
import  retrofit2.Call;
import  retrofit2.Response;

public  class  ChatGroupFragment  extends  AbstractFragment    implements  DialogInterface.OnClickListener
{
	public  View  onCreateView( LayoutInflater  inflater,ViewGroup  container,Bundle  savedInstanceState )
	{
		if( contentView == null )
		{
			contentView = inflater.inflate( R.layout.fragment_contact_chat_group,container,false );

			ObjectUtils.cast(contentView.findViewById(R.id.chat_group_list),ListView.class).setAdapter( new  ChatGroupAdapter(this) );

			ObjectUtils.cast(contentView.findViewById(R.id.chat_group_list),ListView.class).setOnItemClickListener( (parent,view,position,id) -> ActivityCompat.startActivity(super.getActivity(),new  Intent(this.getActivity(),GroupChatActivity.class).putExtra("CHAT_GROUP_ID",ObjectUtils.cast(parent.getAdapter().getItem(position),ChatGroup.class).getLong("ID")),ActivityOptionsCompat.makeCustomAnimation(super.getActivity(),R.anim.right_in,R.anim.left_out).toBundle()) );

			ObjectUtils.cast(contentView.findViewById(R.id.create_button),LinearLayout.class).setOnClickListener( (view) -> new  UIAlertDialog.DividerIOSBuilder(this.getActivity()).setBackgroundRadius(15).setTitle(R.string.create_chat_group).setTitleTextSize(18).setView(R.layout.dlg_editor).setCancelable(false).setCanceledOnTouchOutside(false).setNegativeButton(R.string.cancel,(dialog, which) -> {}).setPositiveButtonTextSize(18).setPositiveButton(R.string.ok,this).create().setWidth((int)  (super.getResources().getDisplayMetrics().widthPixels*0.9)).show() );
		}

		return  contentView;
	}

	protected  View  contentView;

	public  void  onClick( DialogInterface  dialog,int  which )
	{
		String  addingGroupName = ObjectUtils.cast(ObjectUtils.cast(dialog,UIAlertDialog.class).getContentView().findViewById(R.id.edit_inputor),EditText.class).getText().toString().trim();

		if( StringUtils.isNotBlank(addingGroupName) )
		{
			RetrofitRegistry.get(ChatGroupService.class).add( application().getUserMetadata().getLong("ID") , addingGroupName).enqueue
			(
				new  AbstractRetrofit2Callback<Map<String,List<Map<String,Object>>>>(super.getActivity() )
				{
					@SneakyThrows
					public  void  onResponse( Call<Map<String,List<Map<String,Object>>>>  call,Response<Map<String,List<Map<String,Object>>>>  response )
					{
						if( response.code()  == 200 )
						{
							response.body().get("CHAT_GROUP_USERS").get(0).addEntry( "VCARD" , application().getUserMetadata().getString( "NICKNAME" ) );

							Db.tx(String.valueOf(application().getUserMetadata().getLong("ID")),Connection.TRANSACTION_SERIALIZABLE,(connection) -> ChatGroup.dao.attach(response.body()) );

							ObjectUtils.cast(ObjectUtils.cast(ChatGroupFragment.this.getView().findViewById(R.id.chat_group_list),ListView.class).getAdapter(),ChatGroupAdapter.class).notifyDataSetChanged();
						}
						else
						{
							ObjectUtils.cast(ChatGroupFragment.this,AbstractActivity.class).showSneakerWindow( Sneaker.with(ChatGroupFragment.this.getActivity()),com.irozon.sneaker.R.drawable.ic_error,response.code() == 601 ? R.string.group_name_exist : R.string.network_or_internal_server_error,R.color.white,R.color.red );
						}
					}
				}
			);
		}
	}
}
