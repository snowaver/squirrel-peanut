package cc.mashroom.squirrel.module.home.adapters;

import  android.app.Activity;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.TextView;

import  com.google.common.collect.Lists;

import  java.util.Locale;

import  cc.mashroom.hedgehog.module.common.listener.SinglechoiceListener;
import  cc.mashroom.hedgehog.parent.BaseAdapter;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.util.ObjectUtils;
import  cn.refactor.library.SmoothCheckBox;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;

import  static android.content.Context.MODE_PRIVATE;

public  class  SystemSettingsLanguageAdapter  extends  BaseAdapter
{
    public  SystemSettingsLanguageAdapter( Activity  context,SmoothCheckBox.OnCheckedChangeListener  listener )
    {
        super(Lists.newArrayList(context.getString(R.string.language_chinese),context.getString(R.string.language_english)) );

        setContext(context).setListener(new  SinglechoiceListener<String>(this,listener)).getListener().getChecked().set( ObjectUtils.cast(super.items.get(context.getSharedPreferences("CONFIGURATION",MODE_PRIVATE).getString("LOCAL",Locale.CHINESE.toLanguageTag()).equals(Locale.CHINESE.toLanguageTag()) ? 0 : 1)) );
    }

    @Accessors( chain=true )
    @Setter
    @Getter
    protected  SinglechoiceListener<String>  listener;
    @Accessors( chain=true )
    @Setter
    protected  Activity  context;

    public  View  getView(int  position,View  convertView,ViewGroup  parent )
    {
        convertView = convertView != null ? convertView : LayoutInflater.from( context ).inflate(R.layout.activity_system_settings_language_item, parent,false );

        ObjectUtils.cast(convertView.findViewById(R.id.name),TextView.class).setText( ObjectUtils.cast(super.getItem(position),String.class) );

        ObjectUtils.cast(convertView.findViewById(R.id.checkbox),SmoothCheckBox.class).setOnCheckedChangeListener( listener );

        ObjectUtils.cast(convertView.findViewById(R.id.checkbox),SmoothCheckBox.class).setTag(    super.getItem( position ) );

        ObjectUtils.cast(convertView.findViewById(R.id.checkbox),SmoothCheckBox.class).setChecked( ObjectUtils.cast(super.getItem(position),String.class).equals(listener.getChecked().get().toString()) );  return  convertView;
    }
}
