package cc.mashroom.squirrel.module.home.activity;

import  android.content.DialogInterface;
import  android.content.Intent;
import  android.net.Uri;
import  android.os.Bundle;
import  com.google.android.material.appbar.AppBarLayout;
import  com.google.android.material.bottomsheet.BottomSheetBehavior;
import  com.google.android.material.bottomsheet.BottomSheetDialog;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.inputmethod.EditorInfo;
import  android.widget.Button;
import  android.widget.EditText;
import  android.widget.ListView;
import  android.widget.TextView;
import  android.widget.Toast;

import  com.aries.ui.widget.alert.UIAlertDialog;
import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.facebook.drawee.view.SimpleDraweeView;
import  com.fasterxml.jackson.core.type.TypeReference;
import  com.irozon.sneaker.Sneaker;

import  net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import  net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.hedgehog.widget.PromptInputbox;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.connect.PacketEventDispatcher;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.chat.activity.ChatActivity;
import  cc.mashroom.squirrel.module.common.services.ContactService;
import  cc.mashroom.squirrel.module.common.services.UserService;
import  cc.mashroom.squirrel.module.home.adapters.ContactGroupAdapter;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribeAckPacket;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribePacket;
import  cc.mashroom.squirrel.parent.AbstractPacketListenerActivity;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cn.refactor.library.SmoothCheckBox;
import  es.dmoral.toasty.Toasty;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;
import  retrofit2.Call;
import  retrofit2.Response;

public  class  SubscribeActivity  extends  AbstractPacketListenerActivity  implements  View.OnClickListener , SmoothCheckBox.OnCheckedChangeListener , DialogInterface.OnClickListener , KeyboardVisibilityEventListener
{
	protected  void  onCreate(   Bundle  savedInstanceState )
	{
		super.onCreate(   savedInstanceState );

		PacketEventDispatcher.addListener(    SubscribeActivity.this );

		super.setContentView(  R.layout.activity_subscribe );

		KeyboardVisibilityEvent.setEventListener(this,this );

		ObjectUtils.cast(findViewById(R.id.subscribe_button),Button.class).setOnClickListener( this );

		this.setUser(   ObjectUtils.cast( new  User().addEntries( ObjectUtils.cast( super.getIntent().getSerializableExtra("USER"),new  TypeReference<java.util.Map>(){}) ) ) );

		super.findViewById(R.id.group).findViewById(R.id.edit_inputor ).setOnClickListener( (groupInputor) -> bottomSheet.show() );

		ObjectUtils.cast(super.findViewById(R.id.details_portrait),SimpleDraweeView.class).setImageURI( Uri.parse(application().baseUrl().addPathSegments("user/"+user.getLong("ID")+"/portrait").build().toString()) );

		ObjectUtils.cast(super.findViewById(R.id.username),PromptInputbox.class).setText( user.getString("USERNAME") );

		if( StringUtils.isBlank(this.getUser().getString("NICKNAME")) )
		{
		    RetrofitRegistry.get(UserService.class).get(user.getLong("ID")).enqueue( new  AbstractRetrofit2Callback<User>(this){public  void  onResponse(Call<User>  call,Response<User>  response){ObjectUtils.cast(SubscribeActivity.this.findViewById(R.id.nickname),PromptInputbox.class).setText(user.addEntry("NICKNAME",response.body().getString("NICKNAME")).getString("NICKNAME"));}} );
		}
		else
		{
			ObjectUtils.cast(super.findViewById(R.id.nickname),PromptInputbox.class).setText(  this.user.getString( "NICKNAME" ) );
		}

		this.setBottomSheet(new  BottomSheetDialog(this)).getBottomSheet().setContentView( LayoutInflater.from( this ).inflate(R.layout.activity_switch_contact_group, null ) );
		//  swiping  down  event  on  bottom  sheet  dialog  is  conflict  with  sliding  down  event  on  listview,   so  set  bottom  sheet  dialog's  behavior  non-hideable.
		BottomSheetBehavior.from( this.getBottomSheet().findViewById(R.id.design_bottom_sheet) ).setHideable(  false );

		ObjectUtils.cast(this.getBottomSheet().findViewById(R.id.contact_groups),ListView.class).setAdapter( new  ContactGroupAdapter(this, this) );

		ObjectUtils.cast(this.getBottomSheet().findViewById(R.id.contact_groups),ListView.class).setOnItemClickListener( ( parent, view, position, id ) -> ObjectUtils.cast(view.findViewById(R.id.checkbox),SmoothCheckBox.class).setChecked( true) );

		ObjectUtils.cast(this.getBottomSheet().findViewById(R.id.add_to_new_group_button),TextView.class).setOnClickListener( (addGroupButton) -> addGroup() );

        Contact  contact       = Contact.dao.getContactDirect().get( this.getUser().getLong( "ID" ) );
        //  press  enter  key  to  update  remark,  but  actually  group  is  updated  also.
		ObjectUtils.cast(super.findViewById(R.id.remark).findViewById(R.id.edit_inputor),EditText.class).setOnEditorActionListener( (edittext,actionId,event) -> {if(actionId == EditorInfo.IME_ACTION_DONE){ onCheckedChanged(null,true); }  return  false;} );

		ObjectUtils.cast(super.findViewById(R.id.group),PromptInputbox.class).setText( contact != null && StringUtils.isNotBlank(contact.getString("GROUP_NAME")) ? contact.getString("GROUP_NAME") : super.getString(R.string.my_buddies) );

		ObjectUtils.cast(super.findViewById(R.id.title),TextView.class).setText(  contact != null && (contact.getInteger("SUBSCRIBE_STATUS") == 6 || contact.getInteger("SUBSCRIBE_STATUS") == 7 ) ? R.string.contact_profile : R.string.add_contact );

		if( contact   != null )
		{
			ObjectUtils.cast(super.findViewById(R.id.subscribe_button),Button.class).setText( prompts.get(contact.getInteger("SUBSCRIBE_STATUS")) );

			if( contact.getInteger("SUBSCRIBE_STATUS") == 0 )
			{
				ObjectUtils.cast(super.findViewById(R.id.subscribe_button),Button.class).setBackgroundColor(super.getResources().getColor(R.color.gainsboro) );
			}
		}

		ObjectUtils.cast(super.findViewById(R.id.remark),PromptInputbox.class).setText( user.getString(StringUtils.isBlank(user.getString("REMARK")) ? "NICKNAME" : "REMARK") );

		ObjectUtils.cast(ObjectUtils.cast(this.getBottomSheet().findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).getListener().getChecked().set( contact != null && StringUtils.isNotBlank(contact.getString("GROUP_NAME")) ? contact.getString("GROUP_NAME") : super.getString(R.string.my_buddies) );
	}

	private  Map<Integer,Integer>  prompts = new  HashMap<Integer,Integer>().addEntry(0,R.string.send).addEntry(1,R.string.accept).addEntry(6,R.string.chat).addEntry( 7,R.string.chat );
	@Accessors(  chain = true )
	@Setter
	@Getter
	private  BottomSheetDialog     bottomSheet;
	@Accessors(  chain = true )
	@Setter
    @Getter
	private  User  user;

    public  void  received( Packet  packet )throws  Exception
    {
        if(            packet instanceof SubscribeAckPacket )
        {
        	application().getMainLooperHandler().post( () ->{ObjectUtils.cast(super.findViewById(R.id.subscribe_button),Button.class).setBackgroundColor( super.getResources().getColor( R.color.limegreen ) );  ObjectUtils.cast(super.findViewById(R.id.subscribe_button),Button.class).setText(super.getResources().getText(R.string.chat));} );
        }
    }

	private  void    addGroup()
	{
		new  UIAlertDialog.DividerIOSBuilder(SubscribeActivity.this).setBackgroundRadius(15).setTitle(R.string.add_to_new_group).setTitleTextSize(18).setView(R.layout.dlg_editor).setCancelable(false).setCanceledOnTouchOutside(false).setNegativeButton(R.string.cancel,(dialog,which) -> {}).setPositiveButtonTextSize(18).setPositiveButton(R.string.ok,this).create().setWidth((int)  (SubscribeActivity.this.getResources().getDisplayMetrics().widthPixels*0.9)).show();
	}

	public  void  onClick( DialogInterface  dialog , int  i )
	{
		String  name=ObjectUtils.cast(ObjectUtils.cast(dialog,UIAlertDialog.class).getContentView().findViewById(R.id.edit_inputor),EditText.class).getText().toString().trim();

		if( StringUtils.isNotBlank(name) && !ObjectUtils.cast(ObjectUtils.cast(this.getBottomSheet().findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).groups().contains(name) )
		{
			ObjectUtils.cast(ObjectUtils.cast(this.getBottomSheet().findViewById(R.id.contact_groups), ListView.class).getAdapter(), ContactGroupAdapter.class).addNewGroup(name, true).notifyDataSetChanged();

			this.onCheckedChanged( null,true );
		}
		else
		{
			Toasty.warning(SubscribeActivity.this,super.getString(StringUtils.isBlank(name) ? R.string.content_blank_error : R.string.group_exist_error),Toast.LENGTH_LONG,false).show();
		}
	}

	public  void  onClick(  View  clickedView )
	{
		if( clickedView.getId() == R.id.subscribe_button    )
		{
			Contact  contact     = Contact.dao.getContactDirect().get( this.getUser().getLong("ID") );

			if( contact!=null )
			{
				ObjectUtils.cast( super.findViewById(R.id.remark),PromptInputbox.class ).clearFocus();

				ContextUtils.hideSoftinput(   SubscribeActivity.this );

				if( contact.getInteger(  "SUBSCRIBE_STATUS" )    == 0 )
				{
					showSneakerWindow( Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_success, R.string.subscribe_packet_sent, R.color.white, R.color.limegreen );
				}
				else
				if( contact.getInteger(  "SUBSCRIBE_STATUS" )    == 1 )
				{
				    if( StringUtils.isAnyBlank(ObjectUtils.cast(super.findViewById(R.id.remark),PromptInputbox.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.group), PromptInputbox.class).getText().toString().trim()) )
                    {
						showSneakerWindow( Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_error,R.string.subscribe_form_error,R.color.white,R.color.red );
                    }
                    else
                    {
                        RetrofitRegistry.get(ContactService.class).changeSubscribeStatus(7,user.getLong("ID"),application().getUserMetadata().getLong("ID"),ObjectUtils.cast(super.findViewById(R.id.remark),PromptInputbox.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.group),PromptInputbox.class).getText().toString().trim()).enqueue
						(
							new AbstractRetrofit2Callback<Void>( this,new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCanceledOnTouchOutside(false).create().setHeight(DensityUtils.px(this,140)) )
							{
								@SneakyThrows
								public  void  onResponse( Call<Void>  call, Response<Void>  response )
								{
									super.onResponse( call, response );

									if( response.code()==200 )
									{
										PacketEventDispatcher.sent( new  SubscribeAckPacket(user.getLong("ID"),SubscribeAckPacket.ACK_ACCEPT,new  HashMap<String,Object>().addEntry("GROUP",ObjectUtils.cast(SubscribeActivity.this.findViewById(R.id.group),PromptInputbox.class).getText().toString().trim()).addEntry("USERNAME",user.get("USERNAME")).addEntry("NICKNAME",user.getString("NICKNAME"))),TransportState.SENT );

										ObjectUtils.cast(findViewById(R.id.subscribe_button),Button.class).setText( R.string.chat );

										showSneakerWindow( Sneaker.with(SubscribeActivity.this),com.irozon.sneaker.R.drawable.ic_success,R.string.contact_added,R.color.white,R.color.limegreen );
									}
									else
									{
										showSneakerWindow( Sneaker.with(SubscribeActivity.this),com.irozon.sneaker.R.drawable.ic_error, R.string.network_or_internal_server_error, R.color.white, R.color.red );
									}
								}
							}
						);
                    }
				}
				else
				if( contact.getInteger(  "SUBSCRIBE_STATUS" )    == 6 || contact.getInteger("SUBSCRIBE_STATUS" ) == 7 )
				{
					ActivityCompat.startActivity( this , new  Intent(this,ChatActivity.class).putExtra("CONTACT_ID",contact.getLong("ID")) , ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in ,R.anim.left_out).toBundle() );
				}
			}
			else
			{
				ObjectUtils.cast( super.findViewById(R.id.remark),PromptInputbox.class ).clearFocus();

				ContextUtils.hideSoftinput(   SubscribeActivity.this );
				{
                    if( StringUtils.isNoneBlank(ObjectUtils.cast(super.findViewById(R.id.remark),PromptInputbox.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.group),PromptInputbox.class).getText().toString().trim()) )
                    {
						RetrofitRegistry.get(ContactService.class).subscribe( application().getUserMetadata().getLong("ID"), user.getLong("ID"),ObjectUtils.cast(super.findViewById(R.id.remark),PromptInputbox.class).getText().toString().trim(), ObjectUtils.cast(super.findViewById(R.id.group), PromptInputbox.class).getText().toString().trim()).enqueue
						(
							new AbstractRetrofit2Callback<Void>( this,new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCanceledOnTouchOutside(false).create().setHeight(DensityUtils.px(this,140)) )
							{
								@SneakyThrows
								public  void  onResponse( Call<Void>  call, Response<Void>  response )
								{
									super.onResponse( call, response );

									if( response.code()==200 )
									{
										PacketEventDispatcher.sent(new  SubscribePacket(user.getLong("ID"),new  HashMap<String,Object>().addEntry("REMARK",ObjectUtils.cast(SubscribeActivity.this.findViewById(R.id.remark),PromptInputbox.class).getText().toString().trim()).addEntry("USERNAME",user.getString("USERNAME")).addEntry("NICKNAME",user.getString("NICKNAME")).addEntry("GROUP",ObjectUtils.cast(SubscribeActivity.this.findViewById(R.id.group),PromptInputbox.class).getText().toString().trim())),TransportState.SENT );

										ObjectUtils.cast(SubscribeActivity.this.findViewById(R.id.subscribe_button),Button.class).setBackgroundColor(SubscribeActivity.super.getResources().getColor(R.color.gainsboro));

										showSneakerWindow( Sneaker.with(SubscribeActivity.this),com.irozon.sneaker.R.drawable.ic_success,R.string.subscribe_packet_sent,R.color.white,R.color.limegreen );
									}
									else
									{
										showSneakerWindow( Sneaker.with(SubscribeActivity.this),com.irozon.sneaker.R.drawable.ic_error, R.string.network_or_internal_server_error, R.color.white, R.color.red );
									}
								}
							}
						);
                    }
                    else
					{
						showSneakerWindow( Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_error,R.string.subscribe_form_error,R.color.white,R.color.red );
					}
				}
			}
		}
	}

	public  void  onCheckedChanged(  SmoothCheckBox  smoothCheckboxButton,boolean  otherGroupChecked )
	{
		if( otherGroupChecked )
		{
			bottomSheet.hide();

			String  group = ObjectUtils.cast(ObjectUtils.cast(this.getBottomSheet().findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).getListener().getChecked().get();

			ObjectUtils.cast(   super.findViewById(R.id.remark) , PromptInputbox.class ).clearFocus();

			ObjectUtils.cast( super.findViewById(R.id.group) , PromptInputbox.class ).setText(group );

			ContextUtils.hideSoftinput( this );

			Contact  contact     = Contact.dao.getContactDirect().get( this.getUser().getLong("ID") );

			if( contact!=null )
			{
				if( !contact.getString("REMARK").equals(ObjectUtils.cast(super.findViewById(R.id.remark),PromptInputbox.class).getText().toString().trim()) || !group.equals(contact.getString("GROUP_NAME")) )
				{
					RetrofitRegistry.get(ContactService.class).update(Long.parseLong(application().getSquirrelClient().getId()),user.getLong("ID"),ObjectUtils.cast(super.findViewById(R.id.remark),PromptInputbox.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.group),PromptInputbox.class).getText().toString().trim()).enqueue( new  AbstractRetrofit2Callback<Void>(this){public  void  onResponse(Call<Void>  call,Response<Void>  response){ if(response.code() == 200){showSneakerWindow(Sneaker.with(SubscribeActivity.this),com.irozon.sneaker.R.drawable.ic_success,R.string.updated,R.color.white,R.color.limegreen);  Contact.dao.update("UPDATE  "+Contact.dao.getDataSourceBind().table()+"  SET  REMARK = ?,GROUP_NAME = ?  WHERE  ID = ?",new  Object[]{ObjectUtils.cast(SubscribeActivity.this.findViewById(R.id.remark),PromptInputbox.class).getText().toString().trim(),group,user.getLong("ID")});  Contact.dao.getContactDirect().get(user.getLong("ID")).addEntry("REMARK",ObjectUtils.cast(SubscribeActivity.this.findViewById(R.id.remark),PromptInputbox.class).getText().toString().trim()).addEntry("GROUP_NAME",group);}}} );
				}
			}
		}
	}

	public  void  onVisibilityChanged(boolean  softinputVisible )
	{
		ObjectUtils.cast(super.findViewById( R.id.collapsing_bar_layout),AppBarLayout.class).setExpanded( !softinputVisible,true );
	}
}
