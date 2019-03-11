package cc.mashroom.squirrel.module.home.tab.contact.adapters;

import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.TextView;

import  com.facebook.drawee.view.SimpleDraweeView;

import  cc.mashroom.hedgehog.parent.BaseAdapter;
import  cc.mashroom.hedgehog.util.ImageUtils;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import  cc.mashroom.squirrel.module.home.tab.contact.fragment.ChatGroupFragment;
import  cc.mashroom.util.ObjectUtils;
import  lombok.AllArgsConstructor;
import  lombok.SneakyThrows;

@AllArgsConstructor

public  class  ChatGroupAdapter  extends  BaseAdapter
{
	protected  ChatGroupFragment  context;

	@SneakyThrows
	public  ChatGroup  getItem( int  position )
	{
		return  ChatGroup.dao.getOne("SELECT  ID,CREATE_TIME,LAST_MODIFY_TIME,NAME  FROM  "+ChatGroup.dao.getDataSourceBind().table()+"  ORDER  BY  NAME  ASC  LIMIT  1  OFFSET  ?",new  Object[]{position});
	}
	@SneakyThrows
	public  int  getCount()
	{
		return  ChatGroup.dao.getOne("SELECT  COUNT(ID)  AS  COUNT  FROM  "+ChatGroup.dao.getDataSourceBind().table(),new  Object[]{}).getLong("COUNT").intValue();
	}

	public  View  getView( int  position,View  convertView,ViewGroup  parent )
	{
		convertView = convertView != null ? convertView : LayoutInflater.from(context.getContext()).inflate( R.layout.fragment_contact_chat_group_item,parent,false );

		ObjectUtils.cast(convertView.findViewById(R.id.portrait),SimpleDraweeView.class).setImageURI( ImageUtils.toUri(context.getActivity(),R.drawable.lightgray_placeholder) );

		ObjectUtils.cast(convertView.findViewById(R.id.name),TextView.class).setText( getItem(position).getString("NAME") );  return  convertView;
	}
}
