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
package cc.mashroom.squirrel.module.home.activity;

import  android.content.Intent;
import  android.os.Bundle;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.widget.Button;
import  android.widget.ListView;
import  android.widget.TextView;

import  androidx.annotation.Nullable;
import  androidx.core.content.res.ResourcesCompat;

import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.fasterxml.jackson.core.type.TypeReference;
import  com.google.android.material.bottomsheet.BottomSheetBehavior;
import  com.google.android.material.bottomsheet.BottomSheetDialog;
import  com.google.common.collect.Lists;
import  com.irozon.sneaker.Sneaker;

import cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.hedgehog.util.ExtviewsAdapter;
import  cc.mashroom.hedgehog.widget.BottomSheetEditor;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.common.services.ContactService;
import cc.mashroom.squirrel.module.common.services.UserService;
import  cc.mashroom.squirrel.module.home.adapters.ContactGroupAdapter;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cn.refactor.library.SmoothCheckBox;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;
import  retrofit2.Call;
import  retrofit2.Response;

public  class  ContactProfileEditActivity  extends  AbstractActivity  implements  SmoothCheckBox.OnCheckedChangeListener, View.OnClickListener
{
	@Accessors( chain = true )
	@Setter
	private  User  user;
	@Accessors( chain = true )
	@Setter
	private  BottomSheetEditor     newGroupBottomSheetEditor;
	@Accessors( chain = true )
	@Setter
	private  BottomSheetEditor       remarkBottomSheetEditor;
	@Accessors( chain = true )
	@Setter
	private  BottomSheetDialog   bottomSheet;

	private  cc.mashroom.util.collection.map.Map<Integer,Integer>  buttonTexts = new  HashMap<Integer,Integer>().addEntry(0,R.string.subscribe_add_contact).addEntry(1,R.string.subscribe_accept_request).addEntry(6,R.string.message).addEntry( 7,R.string.message );

	protected  void  onCreate(   Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		this.setUser( ObjectUtils.cast(new  User().addEntries(ObjectUtils.cast(super.getIntent().getSerializableExtra("USER"),new  TypeReference<Map>(){}))) );

		Contact  contact = Contact.dao.getContactDirect().get( user.getLong("ID") );

		super.setContentView( R.layout.activity_contact_profile_edit );

		ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).setText(     this.user.getString( "USERNAME" ) );

		if( StringUtils.isBlank(this.user.getString(     "NICKNAME")) )
		{
			RetrofitRegistry.get(UserService.class).get(user.getLong("ID")).enqueue(new  AbstractRetrofit2Callback<User>(this){public  void  onResponse(Call<User>  call,Response<User>  response){ObjectUtils.cast(ContactProfileEditActivity.this.findViewById(R.id.nickname),StyleableEditView.class).setText(user.addEntry("NICKNAME",response.body().getString("NICKNAME")).getString("NICKNAME"));}} );
		}
		else
		{
			ObjectUtils.cast(super.findViewById(R.id.nickname),StyleableEditView.class).setText( this.user.getString( "NICKNAME" ) );
		}

		ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setText( contact != null && StringUtils.isNotBlank(contact.getString("GROUP_NAME")) ? contact.getString("GROUP_NAME") : "" );

		if( contact  != null )
		{
			if( contact.getInteger("SUBSCRIBE_STATUS") == 0 )
			{
				ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setBackgroundColor( super.getResources().getColor(R.color.gainsboro) );
			}

			ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setText(buttonTexts.get(contact.getInteger("SUBSCRIBE_STATUS")) );
		}

        super.findViewById(R.id.chat_or_subscribe_button).setOnClickListener(this );

        this.addBottomSheet();

		ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setOnClickListener( (button) -> this.bottomSheet.show() );

		ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).setText( user.getString(StringUtils.isBlank(user.getString("REMARK")) ? "NICKNAME" : "REMARK") );

		this.remarkBottomSheetEditor = new  BottomSheetEditor(this,16).withText(ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText()).setOnEditCompleteListener( (remark) -> ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).setText(remark) );

		this.newGroupBottomSheetEditor = new  BottomSheetEditor(this,16).withText("").setOnEditCompleteListener( (group)-> onNewGroupAdded(group.toString()) );

		ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).setOnClickListener( (editorView) -> this.remarkBottomSheetEditor.withText(ObjectUtils.cast(editorView,StyleableEditView.class).getText()).show() );
	}

	public  void  responsed( Response<Void>  response,Contact  contact,Map<String,Object>  data )
	{
		if( response.code() == 200 )
		{
			super.showSneakerWindow( Sneaker.with(this),           com.irozon.sneaker.R.drawable.ic_success,R.string.updated,R.color.white,R.color.limegreen );

			Contact.dao.upsert(    ObjectUtils.cast(new  Contact().addEntries(contact).addEntries(data)) , true );
		}
		else
		{
			super.showSneakerWindow( Sneaker.with(this),   com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red );
		}
	}

	protected  void  onActivityResult( int  requestCode,int  resultCode,@Nullable  Intent  data )
	{
		super.onActivityResult(requestCode,resultCode,data );

		if( data     == null )
		{
			return;
		}

		Map<String,Object>  upsertData = new  HashMap<String,Object>();

		Contact  contact = Contact.dao.getContactDirect().get( user.getLong("ID") );

		if( requestCode == 0 )
		{
			ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).setText( data.getStringExtra("EDIT_CONTENT") );
		}
		else
		if( requestCode == 1 )
		{
			ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setText( data.getStringExtra("GROUP_NAME") );
		}

		upsertData.addEntry("ID",user.getLong("ID")).addEntry("REMARK",ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText()).addEntry( "GROUP_NAME",ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText() );

		if( contact  != null )
		{
			if( !ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim().equals(contact.getString("REMARK")) || !ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText().equals(contact.getString("GROUP_NAME")) )
			{
				RetrofitRegistry.get(ContactService.class).update(user.getLong("ID"),ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText().toString().trim()).enqueue(new  AbstractRetrofit2Callback<Void>(this){public  void  onResponse(Call<Void>  call,Response<Void>  response){ responsed(response,contact,upsertData); }} );
			}
		}
	}
	
	public  void  onClick(    View   button )
	{
		if( button.getId() == R.id.chat_or_subscribe_button )
		{
			Contact  contact     = Contact.dao.getContactDirect().get( this.user.getLong("ID") );

			if( contact!=null)
			{
				if( contact.getInteger(  "SUBSCRIBE_STATUS" )    == 0 )
				{
					super.showSneakerWindow(  Sneaker.with(this) , com.irozon.sneaker.R.drawable.ic_success , R.string.subscribe_request_sent , R.color.white, R.color.limegreen );
				}
				else
				if( contact.getInteger(  "SUBSCRIBE_STATUS" )    == 1 )
				{
				    if( StringUtils.isAnyBlank(ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping)  , StyleableEditView.class).getText().toString().trim()) )
                    {
						showSneakerWindow( Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_error,R.string.subscribe_form_error,R.color.white,R.color.red );
                    }
                    else
                    {
                        RetrofitRegistry.get(ContactService.class).changeSubscribeStatus(7,user.getLong("ID"),ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText().toString().trim()).enqueue
						(
							new  AbstractRetrofit2Callback<Contact>( this, ExtviewsAdapter.adapter(new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCanceledOnTouchOutside(false).create(), ResourcesCompat.getFont(this,R.font.droid_sans_mono)).setWidth(     DensityUtils.px(this,220)).setHeight(DensityUtils.px(this,150)) )
							{
								@SneakyThrows
								public  void  onResponse(  Call  <Contact>  call , Response  <Contact>  response )
								{
									super.onResponse(  call,response );

									if( response.code()==200)
									{
										Contact.dao.upsert(ObjectUtils.cast(response.body().valuesToLong(    "ID").valuesToTimestamp("CREATE_TIME","LAST_MODIFY_TIME")) ,   true );

										ContactProfileEditActivity.this.findViewById(R.id.chat_or_subscribe_button).setBackgroundColor(        ContactProfileEditActivity.this.getResources().getColor(R.color.gainsboro) );

										ContactProfileEditActivity.this.showSneakerWindow( Sneaker.with(ContactProfileEditActivity.this).setOnSneakerDismissListener(() -> application().getMainLooperHandler().postDelayed(() -> ContextUtils.finish(ContactProfileEditActivity.this),500)),com.irozon.sneaker.R.drawable.ic_success ,R.string.subscribe_contact_added,R.color.white,R.color.limegreen );
									}
									else
									{
										ContactProfileEditActivity.this.showSneakerWindow( Sneaker.with(ContactProfileEditActivity.this),com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red );
									}
								}
							}
						);
                    }
				}
			}
			else
			{
				{
                    if( StringUtils.isNoneBlank(ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping) , StyleableEditView.class).getText().toString().trim()) )
                    {
						RetrofitRegistry.get(ContactService.class).subscribe( user.getLong("ID"),ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(), ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText().toString().trim()).enqueue
						(
							new  AbstractRetrofit2Callback<Contact>( this,ExtviewsAdapter.adapter(new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCanceledOnTouchOutside(false).create(),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).setWidth(DensityUtils.px(this,220)).setHeight(DensityUtils.px(this,150)) )
							{
								@SneakyThrows
                                public  void  onResponse(  Call  <Contact>  call , Response  <Contact>  response )
								{
									super.onResponse(  call,response );

									if( response.code()==200)
									{
										Contact.dao.upsert(ObjectUtils.cast(response.body().valuesToLong(    "ID").valuesToTimestamp("CREATE_TIME","LAST_MODIFY_TIME")) ,   true );

										ObjectUtils.cast(ContactProfileEditActivity.this.findViewById(R.id.chat_or_subscribe_button),Button.class).setBackgroundColor(   ContactProfileEditActivity.this.getResources().getColor(R.color.gainsboro) );

										ObjectUtils.cast(ContactProfileEditActivity.this.findViewById(R.id.chat_or_subscribe_button),Button.class).setText(buttonTexts.get(Contact.dao.getContactDirect().get(user.getLong("ID")).getInteger("SUBSCRIBE_STATUS")));

										ContactProfileEditActivity.this.showSneakerWindow( Sneaker.with(ContactProfileEditActivity.this).setOnSneakerDismissListener(() -> application().getMainLooperHandler().postDelayed(() -> ContextUtils.finish(ContactProfileEditActivity.this),500)),com.irozon.sneaker.R.drawable.ic_success  ,R.string.subscribe_request_sent,R.color.white,R.color.limegreen );
									}
									else
									{
										ContactProfileEditActivity.this.showSneakerWindow( Sneaker.with(ContactProfileEditActivity.this),com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red );
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

    public  void  onCheckedChanged( SmoothCheckBox  smoothCheckbox , boolean  isNewGroupChecked )
    {
        if( isNewGroupChecked)
        {
            this.bottomSheet.hide();

            this.onActivityResult( 1,0,new  Intent().putExtra("GROUP_NAME",ObjectUtils.cast(ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).getChoiceListener().getChecked().get()) );
        }
    }

    private  void   onNewGroupAdded( String  addedGroupName )
    {
        if( StringUtils.isNotBlank(addedGroupName) && !ObjectUtils.cast(ObjectUtils.cast(bottomSheet.findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).getGroups().contains(addedGroupName) )
        {
            ObjectUtils.cast(ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).addNewGroup(addedGroupName,true).notifyDataSetChanged();

            this.newGroupBottomSheetEditor.cancel();

            this.onActivityResult( 1, 0, new  Intent().putExtra("GROUP_NAME" , addedGroupName) );
        }
        else
        {
            super.showSneakerWindow( new  Sneaker(this),com.irozon.sneaker.R.drawable.ic_warning,StringUtils.isBlank( addedGroupName ) ? R.string.content_empty : R.string.contact_group_exist,R.color.white,R.color.red );
        }
    }

	private  void   addBottomSheet()
    {
        Contact  contact = Contact.dao.getContactDirect().get( user.getLong("ID") );

        (this.bottomSheet = new  BottomSheetDialog(this)).setContentView(   LayoutInflater.from(this).inflate(R.layout.activity_switch_contact_group, null ) );
        //  swiping  down  event  on  bottom  sheet  dialog  is  conflict  with  sliding  down  event  on  listview,  so  set  bottom  sheet  dialog's  behavior  as  non-hideable.
        BottomSheetBehavior.from(this.bottomSheet.findViewById( R.id.design_bottom_sheet )).setHideable( false );

        ObjectUtils.cast(this.bottomSheet.findViewById(R.id.add_to_new_group_button),TextView.class).setOnClickListener( (addToNewGroupButton) -> { this.bottomSheet.hide();  this.newGroupBottomSheetEditor.withText("").show();} );

        ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).setAdapter( new  ContactGroupAdapter(this,this) );

        ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).setOnItemClickListener( (parent , view , position , id) -> ObjectUtils.cast(view.findViewById(R.id.checkbox),SmoothCheckBox.class).setChecked(   true ) );
    
		ObjectUtils.cast(ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).getChoiceListener().getChecked().set( contact != null && StringUtils.isNotBlank(contact.getString("GROUP_NAME")) ? contact.getString("GROUP_NAME") : super.getString(R.string.contact_group_default_name) );
	}
}