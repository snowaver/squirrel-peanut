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

import  com.google.android.material.bottomsheet.BottomSheetBehavior;
import  com.google.android.material.bottomsheet.BottomSheetDialog;
import  com.irozon.sneaker.Sneaker;

import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.widget.BottomSheetEditor;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.client.storage.repository.user.ContactRepository;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.common.services.ContactService;
import  cc.mashroom.squirrel.module.common.services.UserService;
import  cc.mashroom.squirrel.module.home.adapters.ContactGroupAdapter;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cn.refactor.library.SmoothCheckBox;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;
import  retrofit2.Call;
import  retrofit2.Response;

public  class  ContactProfileEditActivity             extends  AbstractActivity  implements  SmoothCheckBox.OnCheckedChangeListener, View.OnClickListener
{
	@Accessors( chain = true )
	@Setter
	private  Contact  contact;
	@Accessors( chain = true )
	@Getter
	@Setter
	private  String  nickname;
	@Accessors( chain = true )
	@Setter
	private  BottomSheetEditor     newGroupBottomSheetEditor;
	@Accessors( chain = true )
	@Setter
	private  BottomSheetEditor       remarkBottomSheetEditor;
	@Accessors( chain = true )
	@Setter
	private  BottomSheetDialog   bottomSheet;

	private  cc.mashroom.util.collection.map.Map<Integer,Integer>  buttonTexts = new  HashMap<Integer,Integer>().addEntry(1,R.string.subscribe_add_contact).addEntry(2,R.string.subscribe_accept_request).addEntry(7,R.string.message).addEntry( 8,R.string.message );

	protected  void  onCreate(   Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		this.setContact(ObjectUtils.cast(super.getIntent().getSerializableExtra("CONTACT"))).setNickname(super.getIntent().getStringExtra("NICKNAME") );

		super.setContentView( R.layout.activity_contact_profile_edit );

		ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).setText(     contact.getUsername() );

		if( StringUtils.isBlank( nickname ) )
		{
			RetrofitRegistry.INSTANCE.get(UserService.class).get(contact.getId()).enqueue(new  AbstractRetrofit2Callback<User>(this){public  void  onResponse(Call<User>  call,Response<User>  response){ObjectUtils.cast(ContactProfileEditActivity.this.findViewById(R.id.nickname),StyleableEditView.class).setText(setNickname(response.body().getNickname()).getNickname());}} );
		}
		else
		{
			ObjectUtils.cast(super.findViewById(R.id.nickname),StyleableEditView.class).setText( nickname );
		}

		ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setText( contact != null && StringUtils.isNotBlank(contact.getGroupName()) ? contact.getGroupName() : "" );

		if( contact  != null &&  contact.getSubscribeStatus() != null )
		{
			if(      this.contact.getSubscribeStatus() == 1 )
			{
				ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setBackgroundColor( super.getResources().getColor(R.color.gainsboro) );
			}

			ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setText(   buttonTexts.get(contact.getSubscribeStatus()) );
		}

        super.findViewById(R.id.chat_or_subscribe_button).setOnClickListener(           this );

        this.addBottomSheet();

		ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setOnClickListener( (btn)-> bottomSheet.show() );

		ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).setText( StringUtils.isBlank(contact.getRemark()) ?  nickname : contact.getRemark() );

		this.remarkBottomSheetEditor = new  BottomSheetEditor(this,16).withText(ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText()).setOnEditCompleteListener( (remark) -> onActivityResult(0,0,new  Intent().putExtra("EDIT_CONTENT", remark.toString())) );

		this.newGroupBottomSheetEditor = new  BottomSheetEditor(this,16).withText("").setOnEditCompleteListener( (groupName) -> onNewGroupAdded(groupName.toString()) );

		ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).setOnClickListener( (editorView) -> this.remarkBottomSheetEditor.withText(ObjectUtils.cast(editorView,StyleableEditView.class).getText()).show() );
	}

	protected  void  onActivityResult(int requestCode,int  resultCode,@Nullable  Intent  data )
	{
		super.onActivityResult(requestCode,resultCode,data );

		if( data     == null )
		{
			return;
		}

		Contact  contact = ContactRepository.DAO.getContactDirect().get(this.contact.getId() );

		if( requestCode == 0 )
		{
			ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).setText( data.getStringExtra("EDIT_CONTENT") );
		}
		else
		if( requestCode == 1 )
		{
			ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setText( data.getStringExtra("GROUP_NAME") );
		}

		if( contact  != null )
		{
			if( !ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim().equals(contact.getRemark()) || !ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText().toString().trim().equals(contact.getGroupName()) )
			{
				RetrofitRegistry.INSTANCE.get(ContactService.class).update(contact.getId(),ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText().toString().trim()).enqueue(new  AbstractRetrofit2Callback<Contact>(this){public  void  onResponse(Call<Contact>  call,Response<Contact>  response){ responsed(response,contact); }} );
			}
		}
	}

	@SneakyThrows
	public  void  responsed( Response<Contact>  response,Contact  old )
	{
		if( response.code() == 200 )
		{
			ContactRepository.DAO.upsert( old.clone().setLastModifyTime(response.body().getLastModifyTime()).setRemark(response.body().getRemark()).setGroupName(response.body().getGroupName()),true );

			super.showSneakerWindow( Sneaker.with(this),   com.irozon.sneaker.R.drawable.ic_success, R.string.updated,R.color.white,R.color.limegreen );
		}
		else
		{
			super.showSneakerWindow( Sneaker.with(this),   com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red );
		}
	}
	
	public  void  onClick(    View   button )
	{
		if( button.getId() == R.id.chat_or_subscribe_button )
		{
			Contact  contact         = ContactRepository.DAO.getContactDirect().get( this.contact.getId() );

			if( contact!=null)
			{
				if(       contact.getSubscribeStatus() == 1 )
				{
					super.showSneakerWindow(  Sneaker.with(this) , com.irozon.sneaker.R.drawable.ic_success,  R.string.subscribe_request_sent , R.color.white, R.color.limegreen );
				}
				else
				if(       contact.getSubscribeStatus() == 2 )
				{
				    if( StringUtils.isAnyBlank(ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping),   StyleableEditView.class).getText().toString().trim()) )
                    {
						super.showSneakerWindow( Sneaker.with(this),   com.irozon.sneaker.R.drawable.ic_error,R.string.subscribe_form_error,R.color.white,R.color.red );
                    }
                    else
                    {
                        RetrofitRegistry.INSTANCE.get(ContactService.class).changeSubscribeStatus(8,contact.getId(),ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText().toString().trim()).enqueue
						(
							new  AbstractRetrofit2Callback<Contact>( this,true )
							{
								@SneakyThrows
								public  void  onResponse( Call<Contact>  call, Response<Contact>  response )
								{
									super.onResponse(  call,response );

									if( response.code()==200)
									{
										//  only  last  modify  time,  subscribe  status,  remark  and  group  name  are  updated,  so  update  them  on  local  storage
										ContactRepository.DAO.upsert( contact.clone().setLastModifyTime(response.body().getLastModifyTime()).setSubscribeStatus(response.body().getSubscribeStatus()).setRemark(response.body().getRemark()).setGroupName(response.body().getGroupName()), true );

										ContactProfileEditActivity.this.findViewById(R.id.chat_or_subscribe_button).setBackgroundColor(       ContactProfileEditActivity.this.getResources().getColor(R.color.limegreen) );

										ObjectUtils.cast(ContactProfileEditActivity.this.findViewById(R.id.chat_or_subscribe_button),Button.class).setText( buttonTexts.get(ContactRepository.DAO.getContactDirect().get(contact.getId()).getSubscribeStatus()) );

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
                    if( StringUtils.isNoneBlank(ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping),  StyleableEditView.class).getText().toString().trim()) )
                    {
						RetrofitRegistry.INSTANCE.get(ContactService.class).subscribe(this.contact.getId(),ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText().toString().trim()).enqueue
						(
							new  AbstractRetrofit2Callback<Contact>( this,true )
							{
								@SneakyThrows
                                public  void  onResponse( Call<Contact>  call, Response<Contact>  response )
								{
									super.onResponse(  call,response );

									if( response.code()==200)
									{
										ContactRepository.DAO.upsert( response.body() , true );

										ObjectUtils.cast(ContactProfileEditActivity.this.findViewById(R.id.chat_or_subscribe_button),Button.class).setBackgroundColor(   ContactProfileEditActivity.this.getResources().getColor(R.color.gainsboro) );

										ObjectUtils.cast(ContactProfileEditActivity.this.findViewById(R.id.chat_or_subscribe_button),Button.class).setText( buttonTexts.get(ContactRepository.DAO.getContactDirect().get(ContactProfileEditActivity.this.contact.getId()).getSubscribeStatus()) );

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
						super.showSneakerWindow( Sneaker.with(this),   com.irozon.sneaker.R.drawable.ic_error,R.string.subscribe_form_error,R.color.white,R.color.red );
					}
				}
			}
		}
	}

    public  void  onCheckedChanged( SmoothCheckBox  smoothCheckbox,boolean  isNewGroupChecked )
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

            this.onActivityResult( 1, 0, new  Intent().putExtra("GROUP_NAME",addedGroupName) );
        }
        else
        {
            super.showSneakerWindow( new  Sneaker(this),com.irozon.sneaker.R.drawable.ic_warning,StringUtils.isBlank( addedGroupName ) ? R.string.content_empty : R.string.contact_group_exist,R.color.white,R.color.red );
        }
    }

	private  void   addBottomSheet()
    {
        Contact  contact = ContactRepository.DAO.getContactDirect().get(this.contact.getId() );

        (this.bottomSheet = new  BottomSheetDialog(this)).setContentView(            LayoutInflater.from(this).inflate(R.layout.activity_switch_contact_group, null ) );
        //  swiping  down  event  on  bottom  sheet  dialog  is  conflict  with  sliding  down  event  on  listview,  so  set  bottom  sheet  dialog's  behavior  as  non-hideable.
        BottomSheetBehavior.from(bottomSheet.findViewById(R.id.design_bottom_sheet )).setHideable(  false );

        ObjectUtils.cast(this.bottomSheet.findViewById(R.id.add_to_new_group_button),TextView.class).setOnClickListener( (addToNewGroupButton) -> {  bottomSheet.hide();      this.newGroupBottomSheetEditor.withText("").show();} );

        ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).setAdapter( new  ContactGroupAdapter(this,this) );

        ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).setOnItemClickListener( (parent , view , position , id) -> ObjectUtils.cast(view.findViewById(R.id.checkbox),SmoothCheckBox.class).setChecked(   true ) );
    
		ObjectUtils.cast(ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).getChoiceListener().getChecked().set( contact != null && StringUtils.isNotBlank(contact.getGroupName()) ? contact.getGroupName() : super.getString(R.string.contact_group_default_name) );
	}
}