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

import  android.os.Bundle;
import  android.widget.ListView;

import  com.aries.ui.widget.alert.UIAlertDialog;

import  androidx.core.content.res.ResourcesCompat;
import  cc.mashroom.hedgehog.util.StyleUnifier;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.module.home.adapters.ContactGroupAdapter;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.util.ObjectUtils;
import  cn.refactor.library.SmoothCheckBox;

public  class  SwitchContactGroupActivity  extends  AbstractActivity  implements  SmoothCheckBox.OnCheckedChangeListener
{
    protected  void  onCreate( Bundle  savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        super.setContentView( R.layout.activity_switch_contact_group );

        ObjectUtils.cast(super.findViewById(R.id.contact_groups),ListView.class).setAdapter( new  ContactGroupAdapter(this,this) );

        super.findViewById(R.id.add_to_new_group_button).setOnClickListener( (button) -> add() );
    }

    private  void  add()
    {
        StyleUnifier.unify(new  UIAlertDialog.DividerIOSBuilder(this).setBackgroundRadius(15).setTitle(R.string.notice).setTitleTextSize(18).setMessageTextSize(18).setCancelable(false).setCanceledOnTouchOutside(false).setPositiveButtonTextSize(18).create().setWidth((int)  (super.getResources().getDisplayMetrics().widthPixels*0.9)),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).show();
    }

    public  void  onCheckedChanged( SmoothCheckBox  smoothCheckbox,boolean  isChecked )
    {

    }
}