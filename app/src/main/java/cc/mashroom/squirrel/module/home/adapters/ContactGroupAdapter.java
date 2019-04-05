package cc.mashroom.squirrel.module.home.adapters;

import  android.app.Activity;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
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

public  class        ContactGroupAdapter  extends  BaseAdapter
{
    public  ContactGroupAdapter( Activity  context,SmoothCheckBox.OnCheckedChangeListener  listener )
    {
        super(    new  ArrayList() );

        Stream.forEach( Contact.dao.search("SELECT  DISTINCT(GROUP_NAME)  AS  GROUP_NAME  FROM  "+Contact.dao.getDataSourceBind().table()+"  WHERE  (GROUP_NAME  !=  ''  AND  GROUP_NAME  IS  NOT  NULL)"),(contact) -> super.items.add(contact.getString("GROUP_NAME")) );

        if( !   items.contains(context.getString(R.string.contact_group_default_name)) )
        {
            super.items.add( 0,context.getString(R.string.contact_group_default_name) );
        }

        this.setContext(context).setChoiceListener(new  SinglechoiceListener<String>(this,listener)).getChoiceListener().getChecked().set( ObjectUtils.cast(super.items.get(0)) );
    }

    public  ContactGroupAdapter  addNewGroup(     String  groupName , boolean  checked )
    {
        super.items.add( groupName );

        if( checked )
        {
            this.choiceListener.getChecked().set( groupName );
        }

        return  this;
    }
    @Accessors( chain=true )
    @Setter
    @Getter
    protected  SinglechoiceListener<String>    choiceListener;
    @Accessors( chain=true )
    @Setter
    protected  Activity  context;

    public  List<String>  getGroups()
    {
        return  Lists.newArrayList( super.items );
    }

    public  View  getView(        int  position, View  convertView , ViewGroup  parent )
    {
        convertView = convertView != null ? convertView : LayoutInflater.from( context ).inflate( R.layout.activity_switch_contact_group_item,parent,false );

        ObjectUtils.cast(convertView.findViewById(R.id.contact_group_name),TextView.class).setText( ObjectUtils.cast(super.getItem(position),String.class) );

        ObjectUtils.cast(convertView.findViewById(R.id.checkbox),SmoothCheckBox.class).setOnCheckedChangeListener( choiceListener );

        ObjectUtils.cast(convertView.findViewById(R.id.checkbox),SmoothCheckBox.class).setTag( super.getItem(position) );

        ObjectUtils.cast(convertView.findViewById(R.id.checkbox),SmoothCheckBox.class).setChecked( ObjectUtils.cast(super.getItem(position),String.class).equals(choiceListener.getChecked().get().toString()) );  return  convertView;
    }
}
