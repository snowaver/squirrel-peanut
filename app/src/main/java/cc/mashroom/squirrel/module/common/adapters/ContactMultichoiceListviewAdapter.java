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
package cc.mashroom.squirrel.module.common.adapters;

import  android.net.Uri;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.TextView;

import  com.facebook.drawee.view.SimpleDraweeView;

import  cc.mashroom.hedgehog.parent.BaseAdapter;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.module.common.activity.ContactMultichoiceActivity;
import  cc.mashroom.util.ObjectUtils;
import  cn.refactor.library.SmoothCheckBox;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;

import  java.util.HashSet;
import  java.util.LinkedList;
import  java.util.List;
import  java.util.Set;

public  class  ContactMultichoiceListviewAdapter  extends  BaseAdapter<Contact>
{
	public  ContactMultichoiceListviewAdapter(   ContactMultichoiceActivity  context, Set<Long>  excludeContactIds )
	{
		List<Contact>  contacts = new  LinkedList<Contact>();

		setContext(context).setItems( contacts );

		for( java.util.Map.Entry<Long,Contact>  entry : Contact.dao.getContactDirect().entrySet() )
		{
			if( !excludeContactIds.contains(entry.getKey()) )
			{
				contacts.add( entry.getValue() );
			}
		}
	}

	@Setter( value=AccessLevel.PROTECTED )
	@Getter
	@Accessors( chain=true )
	private  Set<Long>  contactIds    = new  HashSet<Long>();
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors( chain=true )
	private  ContactMultichoiceActivity  context;

	public  View  getView(  int  position,View  convertView,ViewGroup  parent )
	{
		convertView = convertView != null ? convertView : LayoutInflater.from(context).inflate( R.layout.activity_contact_multichoice_item,parent,false );

		Contact  contact   = getItem( position );

		ObjectUtils.cast(convertView.findViewById(R.id.choice_checkbox),SmoothCheckBox.class).setOnCheckedChangeListener( (button, isChecked) -> {boolean  removed = isChecked ? contactIds.add(contact.getLong("ID")) : contactIds.remove(contact.getLong("CONTACT_ID"));} );

		ObjectUtils.cast(convertView.findViewById(R.id.choice_checkbox),SmoothCheckBox.class).setChecked( contactIds.contains(contact.getLong("CONTACT_ID")) );

		ObjectUtils.cast(convertView.findViewById(R.id.name),TextView.class).setText( contact.getString("REMARK") );

		ObjectUtils.cast(convertView.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI( Uri.parse(context.application().baseUrl().addPathSegments("user/"+contact.getLong("ID")+"/portrait").build().toString()) );

		return  convertView;
	}
}