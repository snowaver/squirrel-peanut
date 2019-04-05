package cc.mashroom.squirrel.module.chat.activity;

import  android.app.Activity;
import  android.content.Intent;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.View;
import  android.widget.AdapterView;
import  android.widget.ListView;
import  android.widget.SimpleAdapter;

import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.irozon.sneaker.Sneaker;

import  androidx.core.content.res.ResourcesCompat;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.db.common.Db;
import  cc.mashroom.hedgehog.util.ExtviewsAdapter;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroupUser;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.common.activity.ContactMultichoiceActivity;
import  cc.mashroom.squirrel.module.chat.services.ChatGroupUserService;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatInvitedPacket;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cc.mashroom.hedgehog.widget.HeaderBar;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;
import  retrofit2.Call;
import  retrofit2.Response;

import  java.io.Serializable;
import  java.sql.Connection;
import  java.util.HashSet;
import  java.util.LinkedList;
import  java.util.List;
import  java.util.Set;

public  class  GroupChatDetailsActivity  extends  AbstractActivity  implements  AdapterView.OnItemClickListener
{
	@SneakyThrows
	protected  void  onCreate( Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		super.setContentView( R.layout.activity_group_chat_details );

		this.setChatGroup( ChatGroup.dao.getOne("SELECT  ID,CREATE_TIME,LAST_MODIFY_TIME,NAME  FROM  "+ChatGroup.dao.getDataSourceBind().table()+"  WHERE  ID = ?",new Object[]{super.getIntent().getLongExtra("CHAT_GROUP_ID",0)}) );

		ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).setTitle( chatGroup.getString("NAME") );

		List<Map<String,Object>>  functionTitles = new LinkedList<Map<String,Object>>();

		for( String  title : super.getResources().getStringArray(R.array.group_chat_details_list) )
		{
			functionTitles.add( new  HashMap<String,Object>().addEntry("title",title) );
		}

		ObjectUtils.cast(super.findViewById(R.id.function_list),ListView.class).setOnItemClickListener( this );

		ObjectUtils.cast(super.findViewById(R.id.function_list),ListView.class).setAdapter( new  SimpleAdapter(this,functionTitles,R.layout.activity_group_chat_details_item,new  String[]{"title"},new  int[]{R.id.name}) );
	}

	@Accessors( chain= true )
	@Setter
	private  ChatGroup  chatGroup;

	protected  void  onActivityResult( int  requestCode, int  resultCode, Intent  data )
	{
		if( data    != null )
		{
			RetrofitRegistry.get(ChatGroupUserService.class).invite(chatGroup.getLong("ID"),StringUtils.join((Set<Long>)  data.getSerializableExtra("SELECTED_CONTACT_IDS"),",")).enqueue
			(
				new  AbstractRetrofit2Callback<List<Map<String,Object>>>( this,ExtviewsAdapter.adapter(new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCanceledOnTouchOutside(false).create(),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).setHeight(DensityUtils.px(this,140)) )
				{
					@SneakyThrows
					public  void  onResponse( Call<List<Map<String,Object>>>  call,Response<List<Map<String,Object>>>  response )
					{
						super.onResponse( call, response );

						if( response.code() == 200 )
						{
							for(   Map<String,Object>  chatGroupUser : response.body() )
							{
								application().getSquirrelClient().asynchronousSend( new  GroupChatInvitedPacket(Long.parseLong(chatGroupUser.get("CONTACT_ID").toString()),chatGroup.getLong("ID")/*DateTime.now(DateTimeZone.UTC).getMillis()*/) );
							}

							Db.tx( String.valueOf(application().getUserMetadata().getLong("ID")),Connection.TRANSACTION_SERIALIZABLE,(connection) -> ChatGroup.dao.attach(new  HashMap<String,List<Map<String,Object>>>().addEntry("CHAT_GROUPS",new  LinkedList<Map<String,Object>>()).addEntry("CHAT_GROUP_USERS",response.body())) );

							showSneakerWindow( Sneaker.with(GroupChatDetailsActivity.this),com.irozon.sneaker.R.drawable.ic_success,R.string.added,R.color.white,R.color.limegreen );
						}
						else
						{
							showSneakerWindow( Sneaker.with(GroupChatDetailsActivity.this),com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red );
						}
					}
				}
			);
		}
	}

	@SneakyThrows
	public  void  onItemClick(AdapterView<?>  parent,View  view,int  position,long  id )
	{
		if( position == 1 )
		{
			Set<Long>  excludeContactIds = new  HashSet<Long>();

			for( ChatGroupUser  chatGroupUser : ChatGroupUser.dao.search("SELECT  CONTACT_ID  FROM  "+ChatGroupUser.dao.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?",new  Object[]{chatGroup.getLong("ID")}) )
			{
				excludeContactIds.add( chatGroupUser.getLong("CONTACT_ID") );
			}

			super.startActivityForResult( new  Intent(this,ContactMultichoiceActivity.class).putExtra("EXCLUDE_CONTACT_IDS", ObjectUtils.cast(excludeContactIds,Serializable.class)),Activity.RESULT_FIRST_USER );
		}
		else
		if( position == 0 )
		{
			ActivityCompat.startActivity( this,new  Intent(this,ChatGroupContactActivity.class).putExtra("CHAT_GROUP_ID",chatGroup.getLong("ID")) , ActivityOptionsCompat.makeCustomAnimation(this, R.anim.right_in, R.anim.left_out).toBundle() );
		}
	}
}
