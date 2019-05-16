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

import  android.content.DialogInterface;
import  android.content.Intent;
import  android.os.Bundle;
import  android.view.LayoutInflater;
import  android.widget.Button;
import  android.widget.EditText;
import  android.widget.ListView;
import  android.widget.TextView;

import  androidx.annotation.Nullable;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  androidx.core.content.res.ResourcesCompat;

import  com.aries.ui.widget.alert.UIAlertDialog;
import  com.fasterxml.jackson.core.type.TypeReference;
import  com.google.android.material.bottomsheet.BottomSheetBehavior;
import  com.google.android.material.bottomsheet.BottomSheetDialog;
import  com.google.common.collect.Lists;
import  com.irozon.sneaker.Sneaker;

import  cc.mashroom.hedgehog.module.common.activity.EditorActivity;
import  cc.mashroom.hedgehog.util.ExtviewsAdapter;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.common.services.ContactService;
import  cc.mashroom.squirrel.module.home.adapters.ContactGroupAdapter;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  cn.refactor.library.SmoothCheckBox;
import  lombok.Setter;
import  lombok.experimental.Accessors;
import  retrofit2.Call;
import  retrofit2.Response;

public  class  ContactProfileEditActivity     extends  AbstractActivity   implements     DialogInterface.OnClickListener,SmoothCheckBox.OnCheckedChangeListener
{
    @Accessors( chain = true )
    @Setter
	private  BottomSheetDialog   bottomSheet;
    @Accessors( chain = true )
    @Setter
    private  User  user;

	private cc.mashroom.util.collection.map.Map<Integer,Integer> buttonTexts = new HashMap<Integer,Integer>().addEntry(0,R.string.subscribe_add_contact).addEntry(1,R.string.subscribe_accept_request).addEntry(6,R.string.message).addEntry( 7,R.string.message );

	protected  void  onCreate(   Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		this.setUser( ObjectUtils.cast(new  User().addEntries(ObjectUtils.cast(super.getIntent().getSerializableExtra("USER"),new  TypeReference<Map>(){}))) );

		Contact  contact = Contact.dao.getContactDirect().get( user.getLong("ID") );

		super.setContentView( R.layout.activity_contact_profile_edit );

		ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setText( contact != null && StringUtils.isNotBlank(contact.getString("GROUP_NAME")) ? contact.getString("GROUP_NAME") : "" );

        ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).setOnClickListener( (button) -> this.bottomSheet.show() );

		if( contact  != null )
		{
			if( contact.getInteger("SUBSCRIBE_STATUS") == 0 )
			{
				ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setBackgroundColor( super.getResources().getColor(R.color.gainsboro) );
			}

			ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setText(buttonTexts.get(contact.getInteger("SUBSCRIBE_STATUS")) );
		}

        this.addBottomSheet();

		ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).setText( user.getString(StringUtils.isBlank(user.getString("REMARK")) ? "NICKNAME" : "REMARK") );

		ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).setOnClickListener( (v) -> ActivityCompat.startActivityForResult(this,new  Intent(this,EditorActivity.class).putExtra("EDIT_CONTENT",ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText()).putExtra("TITLE",super.getString(R.string.remark)).putExtra("LIMITATION",16),0,ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle()) );
	}

	public  void  responsed( Response<Void>  response,Contact  contact,Map<String,Object>  data )
	{
		if( response.code() == 200 )
		{
			super.showSneakerWindow( Sneaker.with(this),           com.irozon.sneaker.R.drawable.ic_success,R.string.updated,R.color.white,R.color.limegreen );

			Contact.dao.attach(Lists.newArrayList(data));contact.addEntries( data );
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
			if( !contact.getString("REMARK").equals(ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim()) || !contact.getString("GROUP_NAME").equals(ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText()) )
			{
				RetrofitRegistry.get(ContactService.class).update(user.getLong("ID"),ObjectUtils.cast(super.findViewById(R.id.remark),StyleableEditView.class).getText().toString().trim(),ObjectUtils.cast(super.findViewById(R.id.grouping),StyleableEditView.class).getText().toString().trim()).enqueue(new  AbstractRetrofit2Callback<Void>(this){public  void  onResponse(Call<Void>  call,Response<Void>  response){ responsed(response,contact,upsertData); }} );
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

    public  void  onClick( DialogInterface  dialog , int  i )
    {
        String  name = ObjectUtils.cast(ObjectUtils.cast(dialog,UIAlertDialog.class).getContentView().findViewById(R.id.edit_inputor), EditText.class).getText().toString().trim();

        if( StringUtils.isNotBlank(name) && !ObjectUtils.cast(ObjectUtils.cast(bottomSheet.findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).getGroups().contains(name) )
        {
            ObjectUtils.cast(ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).addNewGroup(name,true).notifyDataSetChanged();

            this.onActivityResult( 1,0,new  Intent().putExtra("GROUP_NAME", name) );
        }
        else
        {
            super.showSneakerWindow( new  Sneaker(this),com.irozon.sneaker.R.drawable.ic_warning , StringUtils.isBlank(name) ? R.string.content_empty : R.string.contact_group_exist,R.color.white,R.color.red );
        }
    }

    private  void   addGroup()
    {
        ExtviewsAdapter.adapter(new  UIAlertDialog.DividerIOSBuilder(this).setBackgroundRadius(15).setTitle(R.string.contact_add_to_new_group).setTitleTextSize(18).setView(R.layout.dlg_editor).setCancelable(false).setCanceledOnTouchOutside(false).setNegativeButtonTextColorResource(R.color.red).setNegativeButtonTextSize(18).setNegativeButton(R.string.cancel,(dialog,which) -> {}).setPositiveButtonTextSize(18).setPositiveButton(R.string.finish,this).create().setWidth((int)  (ContactProfileEditActivity.this.getResources().getDisplayMetrics().widthPixels*0.9)), ResourcesCompat.getFont(this,R.font.droid_sans_mono)).show();
    }

	private  void   addBottomSheet()
    {
        Contact  contact = Contact.dao.getContactDirect().get( user.getLong("ID") );

        (this.bottomSheet = new  BottomSheetDialog(this)).setContentView(   LayoutInflater.from(this).inflate(R.layout.activity_switch_contact_group, null ) );
        //  swiping  down  event  on  bottom  sheet  dialog  is  conflict  with  sliding  down  event  on  listview,  so  set  bottom  sheet  dialog's  behavior  as  non-hideable.
        BottomSheetBehavior.from(this.bottomSheet.findViewById( R.id.design_bottom_sheet )).setHideable( false );

        ObjectUtils.cast(this.bottomSheet.findViewById(R.id.add_to_new_group_button),TextView.class).setOnClickListener(   (addNewGroupButton) -> addGroup() );

        ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).setAdapter( new  ContactGroupAdapter(this,this) );

        ObjectUtils.cast(ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).getAdapter(),ContactGroupAdapter.class).getChoiceListener().getChecked().set( contact != null && StringUtils.isNotBlank(contact.getString("GROUP_NAME")) ? contact.getString("GROUP_NAME") : super.getString(R.string.contact_group_default_name) );

        ObjectUtils.cast(this.bottomSheet.findViewById(R.id.contact_groups),ListView.class).setOnItemClickListener( (parent,view,position,id ) -> ObjectUtils.cast(view.findViewById(R.id.checkbox),SmoothCheckBox.class).setChecked( true ) );
    }
}