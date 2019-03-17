package cc.mashroom.squirrel.module.home.adapters;

import  android.app.Activity;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.CompoundButton;
import  android.widget.TextView;

import  com.google.common.collect.Lists;

import  java.util.ArrayList;
import  java.util.List;

import  cc.mashroom.hedgehog.module.common.listener.SinglechoiceListener;
import  cc.mashroom.hedgehog.parent.BaseAdapter;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.stream.Stream;
import  cn.refactor.library.SmoothCheckBox;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;

public  class  ContactGroupAdapter  extends  BaseAdapter
{
    public  ContactGroupAdapter( Activity  context,CompoundButton.OnCheckedChangeListener  listener )
    {
        super(new  ArrayList() );

        Stream.forEach( Contact.dao.search("SELECT  DISTINCT(GROUP_NAME)  AS  GROUP_NAME  FROM  "+Contact.dao.getDataSourceBind().table()+"  WHERE  (GROUP_NAME  !=  ''  AND  GROUP_NAME  IS  NOT  NULL)"),(contact) -> super.items.add(contact.getString("GROUP_NAME")) );

        if( ! super.items.contains( context.getString(R.string.my_buddies) ) )
        {
            super.items.add( 0,context.getString(R.string.my_buddies) );
        }

        setContext(context).setListener(new  SinglechoiceListener<String>(this,listener)).getListener().getChecked().set( ObjectUtils.cast(items.get( 0 )) );
    }

    public  ContactGroupAdapter  addNewGroup( String  name, boolean  checked )
    {
        super.items.add(  name );

        if( checked )
        {
            this.getListener().getChecked().set( name );
        }

        return  this;
    }
    @Accessors( chain=true )
    @Setter
    @Getter
    protected  SinglechoiceListener<String>    listener;
    @Accessors( chain=true )
    @Setter
    protected  Activity  context;

    public  List<String> groups()
    {
        return  Lists.newArrayList( super.items );
    }

    public  View  getView(int  position,View  convertView,ViewGroup  parent )
    {
        convertView = convertView != null ? convertView : LayoutInflater.from( context ).inflate( R.layout.activity_switch_contact_group_item,parent,false );

        ObjectUtils.cast(convertView.findViewById(R.id.contact_group_name),TextView.class).setText( ObjectUtils.cast(super.getItem(position),String.class) );

        ObjectUtils.cast(convertView.findViewById(R.id.checkbox),SmoothCheckBox.class).setOnCheckedChangeListener( listener );

        ObjectUtils.cast(convertView.findViewById(R.id.checkbox),SmoothCheckBox.class).setTag( super.getItem(position) );

        ObjectUtils.cast(convertView.findViewById(R.id.checkbox),SmoothCheckBox.class).setChecked( ObjectUtils.cast(super.getItem(position),String.class).equals(listener.getChecked().get().toString()) );  return  convertView;
    }
}
