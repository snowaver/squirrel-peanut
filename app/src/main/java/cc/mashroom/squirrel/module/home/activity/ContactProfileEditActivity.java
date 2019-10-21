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
import  android.widget.Button;
import  android.widget.ListView;
import  android.widget.TextView;

import  com.google.android.material.bottomsheet.BottomSheetBehavior;
import  com.google.android.material.bottomsheet.BottomSheetDialog;
import  com.irozon.sneaker.Sneaker;

import  java.sql.Connection;

import  cc.mashroom.db.common.Db;
import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.widget.BottomSheetEditor;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.client.storage.repository.user.ContactRepository;
import  cc.mashroom.squirrel.http.ResponseRetrofit2Callback;
import  cc.mashroom.squirrel.http.ServiceRegistry;
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
import  lombok.experimental.Accessors;
import  retrofit2.Response;

public  class  ContactProfileEditActivity   extends  AbstractActivity  implements  SmoothCheckBox.OnCheckedChangeListener
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

		this.setContact(         ObjectUtils.cast(super.getIntent().getSerializableExtra("CONTACT"))).setNickname(super.getIntent().getStringExtra("NICKNAME") );

		super.setContentView(       R.layout.activity_contact_profile_edit );

		ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).setText(this.contact.getUsername() );

		if( StringUtils.isBlank( nickname ) )
		{
			ServiceRegistry.INSTANCE.get(UserService.class).get(contact.getId()).enqueue( new  ResponseRetrofit2Callback<User>(this,true).addResponseHandler(200,(call,response) -> ObjectUtils.cast(super.findViewById(R.id.nickname),StyleableEditView.class).setText(this.nickname = response.body().getNickname())) );
		}
		else
		{
			ObjectUtils.cast(super.findViewById(R.id.nickname),StyleableEditView.class).setText(         this.nickname );
		}

		ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setText( this.contact != null && StringUtils.isNotBlank(contact.getGroupName()) ? contact.getGroupName() : "" );

		if( contact  != null &&  contact.getSubscribeStatus()       != null )
		{
			if(      this.contact.getSubscribeStatus() == 1 )
			{
				ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setBackgroundColor(   super.getResources().getColor( R.color.gainsboro ) );
			}

			ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setText(  this.buttonTexts.get(this.contact.getSubscribeStatus()) );
		}

        super.findViewById(R.id.chat_or_subscribe_button).setOnClickListener( (b) ->  onChatOrSubscribeButtonClicked() );

        this.addBottomSheet();

		ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setOnClickListener((bsdialog)  -> this.bottomSheet.show() );

		ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).setText( StringUtils.isBlank(contact.getRemark()) ? this.nickname : contact.getRemark() );

		this.remarkBottomSheetEditor = new  BottomSheetEditor(this,16).withText(ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText()).setOnEditCompleteListener( (remark) -> onActivityResult(0,0,new  Intent().putExtra("EDIT_CONTENT", remark.toString())) );

		this.newGroupBottomSheetEditor = new  BottomSheetEditor(this,16).withText("").setOnEditCompleteListener(     (groupName) -> onNewGroupAdded(groupName.toString()) );

		ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).setOnClickListener( (editorView) -> this.remarkBottomSheetEditor.withText(ObjectUtils.cast(editorView,StyleableEditView.class).getText()).show() );
	}

	private  void  onNewGroupAdded(   String  addedNewGroup )
	{
		if( StringUtils.isNotBlank(addedNewGroup) && !ObjectUtils.cast(ObjectUtils.cast(bottomSheet.findViewById(R.id.contact_groups),ListView.class).getAdapter() , ContactGroupAdapter.class).getGroups().contains(addedNewGroup) )
		{
			ObjectUtils.cast(ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).addNewGroup(addedNewGroup , true).notifyDataSetChanged();

			this.newGroupBottomSheetEditor.cancel();

			this.update( ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).getChoiceListener().getChecked().get() );
		}
		else
		{
			super.showSneakerWindow( new  Sneaker(this),com.irozon.sneaker.R.drawable.ic_warning,StringUtils.isBlank( addedNewGroup ) ? R.string.content_empty : R.string.contact_group_exist,R.color.white,R.color.red );
		}
	}
	@Override
	protected void onDestroy()
	{
		bottomSheet.dismiss();

		super.onDestroy();
	}

	private  void   onChatOrSubscribeButtonClicked()
	{
		Contact  contact = ContactRepository.DAO.getContactDirect().get( this.contact.getId() );

		if( contact  == null )
		{
			{
				if( StringUtils.isNoneBlank(ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping),  StyleableEditView.class).getText().toString().trim()) )
				{
					ServiceRegistry.INSTANCE.get(ContactService.class).subscribe(this.contact.getId(),ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText().toString().trim()).enqueue( new  ResponseRetrofit2Callback<Contact>(this,true).addResponseHandler(200,(call,response) -> onSubscribed(response)) );
				}
				else
				{
					super.showSneakerWindow( Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_error,R.string.subscribe_form_error,R.color.white,R.color.red );
				}
			}
		}
		else
		{
			if( contact.getSubscribeStatus()  == 1 )
			{
				super.showSneakerWindow(  Sneaker.with(this), com.irozon.sneaker.R.drawable.ic_success, R.string.subscribe_request_sent, R.color.white, R.color.limegreen );
			}
			else
			if( contact.getSubscribeStatus()  == 2 )
			{
				if( StringUtils.isAnyBlank(ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText().toString().trim()) )
				{
					super.showSneakerWindow( Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_error,R.string.subscribe_form_error,R.color.white,R.color.red );
				}
				else
				{
					ServiceRegistry.INSTANCE.get(ContactService.class).changeSubscribeStatus(8,contact.getId(),ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText().toString().trim()).enqueue( new  ResponseRetrofit2Callback<Contact>(this,true).addResponseHandler(200,(call,response) -> onAgreeSubscribe(response)) );
				}
			}
		}
	}

	private  void  onSubscribed(Response<Contact>  response )
	{
		Db.tx( String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_REPEATABLE_READ,(connection) -> ContactRepository.DAO.upsert(response.body(),true) );

		ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setBackgroundColor(  super.getResources().getColor(R.color.gainsboro) );

		ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setText( this.buttonTexts.get(ContactRepository.DAO.getContactDirect().get(this.contact.getId()).getSubscribeStatus()) );

		super.showSneakerWindow( Sneaker.with(this).setOnSneakerDismissListener(() -> application().getMainLooperHandler().postDelayed(() -> ContextUtils.finish(ContactProfileEditActivity.this),500)),com.irozon.sneaker.R.drawable.ic_success,R.string.subscribe_request_sent,R.color.white,  R.color.limegreen );
	}

	private  void  onGroupUpdated( Contact  old,Response<Contact>  response )
	{
		Db.tx( String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_REPEATABLE_READ,(connection) -> ContactRepository.DAO.upsert(old.clone().setLastModifyTime(response.body().getLastModifyTime()).setRemark(response.body().getRemark()).setGroupName(response.body().getGroupName()),true) );

		contact = ContactRepository.DAO.getContactDirect().get(old.getId() );

		ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setText(this.contact.getGroupName());

		ObjectUtils.cast(super.findViewById(R.id.remark  ),StyleableEditView.class).setText(this.contact.getRemark()   );

		super.showSneakerWindow( Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_success,R.string.updated,R.color.white,R.color.limegreen );
	}

	private  void  update( String  remark, String  newGroup )
	{
		if( ContactRepository.DAO.getContactDirect().containsKey(       this.contact.getId() ) )
		{
			ServiceRegistry.INSTANCE.get(ContactService.class).update(this.contact.getId(),remark,newGroup).enqueue( new  ResponseRetrofit2Callback<Contact>(this,true).addResponseHandler(200,(call,  response)->    onGroupUpdated(ContactRepository.DAO.getContactDirect().get(this.contact.getId()),response)) );
		}
		else
		{
			ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setText( newGroup );

			ObjectUtils.cast(super.findViewById(R.id.remark  ),StyleableEditView.class).setText(   remark );
		}
	}

	private  void  onAgreeSubscribe(            Response<Contact>  response )
	{
		Db.tx( String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_REPEATABLE_READ,(connection) -> ContactRepository.DAO.upsert(ContactRepository.DAO.getContactDirect().get(this.contact.getId()).clone().setLastModifyTime(response.body().getLastModifyTime()).setSubscribeStatus(response.body().getSubscribeStatus()).setRemark(response.body().getRemark()).setGroupName(response.body().getGroupName()),true) );

		super.findViewById(R.id.chat_or_subscribe_button).setBackgroundColor(    super.getResources().getColor(R.color.limegreen) );

		ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setText( this.buttonTexts.get(ContactRepository.DAO.getContactDirect().get(this.contact.getId()).getSubscribeStatus()) );

		super.showSneakerWindow( Sneaker.with(ContactProfileEditActivity.this).setOnSneakerDismissListener(() -> application().getMainLooperHandler().postDelayed(() -> ContextUtils.finish(ContactProfileEditActivity.this),500)),com.irozon.sneaker.R.drawable.ic_success ,R.string.subscribe_contact_added,R.color.white,R.color.limegreen );
	}

    public   void  onCheckedChanged( SmoothCheckBox  smoothCheckbox,boolean  isNewGroupChecked )
    {
        if( isNewGroupChecked)
        {
            this.bottomSheet.hide();

            this.update( ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).getChoiceListener().getChecked().get() );
        }
    }

	private  void   addBottomSheet()
    {
        (this.bottomSheet = new  BottomSheetDialog(this)).setContentView(     LayoutInflater.from(this).inflate(R.layout.activity_switch_contact_group, null ) );
        //  swiping  down  event  on  bottom  sheet  dialog  is  conflict  with  sliding  down  event  on  the  listview,  so  set  bottom  sheet  dialog's  behavior  as  non-hideable.
        BottomSheetBehavior.from(bottomSheet.findViewById(R.id.design_bottom_sheet )).setHideable(  false );

        ObjectUtils.cast(this.bottomSheet.findViewById(R.id.add_to_new_group_button),TextView.class).setOnClickListener((addToNewGroupButton) ->   {this.bottomSheet.hide();  this.newGroupBottomSheetEditor.withText("").show();} );

        ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).setAdapter(   new  ContactGroupAdapter(this,this) );

        ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).setOnItemClickListener( (parent, view, position ,id) -> ObjectUtils.cast(view.findViewById(R.id.checkbox),SmoothCheckBox.class).setChecked(true) );

		Contact  contact = ContactRepository.DAO.getContactDirect().get( this.contact.getId() );

		ObjectUtils.cast(ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).getChoiceListener().getChecked().set( contact != null && StringUtils.isNotBlank(contact.getGroupName()) ? contact.getGroupName() : super.getString(R.string.contact_group_default_name) );
	}
}