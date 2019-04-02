package cc.mashroom.squirrel.module.system.activity;

import  android.content.Intent;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.View;
import  android.view.WindowManager;
import  android.widget.Button;
import  android.widget.TextView;

import  com.aries.ui.widget.alert.UIAlertDialog;
import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.irozon.sneaker.Sneaker;

import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.hedgehog.util.NetworkUtils;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;

public  class  LoginActivity  extends  AbstractActivity  implements  Button.OnClickListener
{
    protected  void  onActivityResult( int  requestCode,int  rstCode,Intent  data )
    {
        super.onActivityResult( requestCode,rstCode,data );

        if( data != null )
        {
            ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).setText( data.getStringExtra("USERNAME") == null ? "" : data.getStringExtra("USERNAME") );
        }
    }

    protected  void  onCreate( Bundle  savedInstanceState )
    {
        super.onCreate(  savedInstanceState );

        super.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );

        super.setContentView(    R.layout.activity_login );

        ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).setText( super.getIntent().getStringExtra("USERNAME") == null ? "" : super.getIntent().getStringExtra("USERNAME") );

        if( super.getIntent().getIntExtra("RELOGIN_REASON",0) == 1 )
        {
            new  UIAlertDialog.DividerIOSBuilder(this).setBackgroundRadius(15).setTitle(R.string.warning).setTitleTextSize(18).setMessage(R.string.offsite_landing_error).setMessageTextSize(18).setCancelable(false).setCanceledOnTouchOutside(false).setPositiveButtonTextSize(18).setPositiveButton(R.string.close,(dialog, which) -> android.os.Process.killProcess(android.os.Process.myPid())).create().setWidth((int)  (super.getResources().getDisplayMetrics().widthPixels*0.9)).show();
        }
        //  clear  the  password  input  box  after  successful  registration,  logout  or  squeezing  off  the  line  by  remote  login.
        ObjectUtils.cast(super.findViewById(R.id.password),StyleableEditView.class).getText().clear();

        ObjectUtils.cast(super.findViewById(R.id.login_button),Button.class).setOnClickListener(this);

        ObjectUtils.cast(super.findViewById(R.id.jump_to_registration_link),TextView.class).setOnClickListener( (view) -> ActivityCompat.startActivityForResult(this,new  Intent(this,RegisterActivity.class),0,ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle()) );
    }

    public  void  onClick( View  loginButton )
    {
        if( StringUtils.isAnyBlank(ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).getText().toString(),ObjectUtils.cast(super.findViewById(R.id.password),StyleableEditView.class).getText().toString()) || !NetworkUtils.isNetworkAvailable(this) )
        {
            application().getMainLooperHandler().post( () -> showSneakerWindow(Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_error,!NetworkUtils.isNetworkAvailable(this) ? R.string.network_or_internal_server_error : R.string.connect_form_error,R.color.white,R.color.red) );
        }
        else
        {
            ContextUtils.hideSoftinput(this );

            application().connect( ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).getText().toString(),ObjectUtils.cast(super.findViewById(R.id.password),StyleableEditView.class).getText().toString(),NetworkUtils.getLocation(this),new  UIProgressDialog.WeBoBuilder(this).setTextSize(18).setMessage(R.string.waiting).setCancelable(false).setCanceledOnTouchOutside(false).create().setHeight(DensityUtils.px(this,140)) );
        }
    }
}