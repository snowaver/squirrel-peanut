package cc.mashroom.squirrel.module.home.activity;

import  android.os.Bundle;
import  android.widget.ListView;

import  com.aries.ui.widget.alert.UIAlertDialog;

import  androidx.core.content.res.ResourcesCompat;
import  cc.mashroom.hedgehog.util.ExtviewsAdapter;
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
        ExtviewsAdapter.adapter(new  UIAlertDialog.DividerIOSBuilder(this).setBackgroundRadius(15).setTitle(R.string.notice).setTitleTextSize(18).setMessageTextSize(18).setCancelable(false).setCanceledOnTouchOutside(false).setPositiveButtonTextSize(18).create().setWidth((int)  (super.getResources().getDisplayMetrics().widthPixels*0.9)),ResourcesCompat.getFont(this,R.font.droid_sans_mono)).show();
    }

    public  void  onCheckedChanged( SmoothCheckBox  smoothCheckbox,boolean  isChecked )
    {

    }
}
