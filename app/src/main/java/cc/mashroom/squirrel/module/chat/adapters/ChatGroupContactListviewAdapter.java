package cc.mashroom.squirrel.module.chat.adapters;

import  android.net.Uri;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.TextView;

import  com.facebook.drawee.view.SimpleDraweeView;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroupUser;
import  cc.mashroom.squirrel.module.chat.activity.ChatGroupContactActivity;
import  cc.mashroom.util.ObjectUtils;
import  lombok.AccessLevel;
import  lombok.AllArgsConstructor;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;

@AllArgsConstructor

public  class  ChatGroupContactListviewAdapter  extends  cc.mashroom.hedgehog.parent.BaseAdapter
{
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors( chain=true )
	protected  ChatGroupContactActivity   context;
	@Setter( value=AccessLevel.PROTECTED )
	@Accessors( chain=true )
	protected  long  chatGroupId;
	@SneakyThrows
	public  int   getCount()
	{
		return  ChatGroupUser.dao.getOne("SELECT  COUNT(ID)  AS  COUNT  FROM  "+ChatGroupUser.dao.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?",new  Object[]{chatGroupId}).getLong("COUNT").intValue();
	}
	@SneakyThrows
	public  ChatGroupUser  getItem(int  position )
	{
		return  ChatGroupUser.dao.getOne("SELECT  ID,CREATE_TIME,LAST_MODIFY_TIME,CONTACT_ID,VCARD  FROM  "+ChatGroupUser.dao.getDataSourceBind().table()+"  WHERE  CHAT_GROUP_ID = ?  ORDER  BY  ID  ASC  LIMIT  1  OFFSET  ?",new  Object[]{chatGroupId,position});
	}

	public  View  getView( int  position,View  convertView,ViewGroup  parent )
	{
		convertView = convertView != null ? convertView : LayoutInflater.from(context).inflate( R.layout.activity_chat_group_contact_item,parent,false );

		ChatGroupUser  chatGroupUser = getItem( position );

		ObjectUtils.cast(convertView.findViewById(R.id.name),TextView.class).setText( chatGroupUser.getString("VCARD") );

		ObjectUtils.cast(convertView.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI(Uri.parse(context.application().baseUrl().addPathSegments("user/"+chatGroupUser.getLong("CONTACT_ID")+"/portrait").build().toString()) );   return  convertView;
	}
}
