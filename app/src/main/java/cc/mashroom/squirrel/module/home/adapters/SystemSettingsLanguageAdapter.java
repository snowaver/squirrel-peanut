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

        setContext(context).setListener(new  SinglechoiceListener<String>(this,listener)).getListener().getChecked().set( ObjectUtils.cast(super.items.get(context.getSharedPreferences("CONFIGURATION",MODE_PRIVATE).getString("LOCALE",Locale.ENGLISH.toLanguageTag()).equals(Locale.CHINESE.toLanguageTag()) ? 0 : 1)) );
    }

    @Accessors( chain=true )
    @Setter
    @Getter
    protected  SinglechoiceListener<String>  listener;
    @Accessors( chain=true )
    @Setter
    protected  Activity  context;

    public  View  getView( int  position, View  convertView, ViewGroup  parent )
    {
        convertView = convertView != null ? convertView : LayoutInflater.from( context ).inflate(R.layout.activity_system_settings_language_item, parent,false );

        ObjectUtils.cast(convertView.findViewById(R.id.name),TextView.class).setText( ObjectUtils.cast(super.getItem(position),String.class) );

        ObjectUtils.cast(convertView.findViewById(R.id.checkbox),SmoothCheckBox.class).setOnCheckedChangeListener( listener );

        ObjectUtils.cast(convertView.findViewById(R.id.checkbox),SmoothCheckBox.class).setTag(    super.getItem( position ) );

        ObjectUtils.cast(convertView.findViewById(R.id.checkbox),SmoothCheckBox.class).setChecked( ObjectUtils.cast(super.getItem(position),String.class).equals(listener.getChecked().get().toString()) );

        convertView.setBackgroundResource( ObjectUtils.cast(convertView.findViewById(R.id.checkbox),SmoothCheckBox.class).isChecked() ? R.color.lightgray : R.color.white );  return  convertView;
    }
}