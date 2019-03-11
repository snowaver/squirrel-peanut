package cc.mashroom.squirrel.parent;

import  android.content.SharedPreferences;
import  android.net.ConnectivityManager;
import  android.net.Network;

import  lombok.AllArgsConstructor;

import  static  android.content.Context.MODE_PRIVATE;

@AllArgsConstructor

public  class  ConnectivityStateListener  extends  ConnectivityManager.NetworkCallback
{
    private  Application  application;

    public  void  onAvailable(    Network  network )
    {
        super.onAvailable(  network );

        SharedPreferences  sharedPreferences  = application.getSharedPreferences( "LOGIN_FORM",MODE_PRIVATE );

        if( sharedPreferences.getLong("ID",0) >= 1 && !application.getSquirrelClient().isAuthenticated() )
        {
            /*
            application.connect( sharedPreferences.getLong("ID",0),NetworkUtils.getLocation(application),AbstractActivity.STACK.isEmpty() ? null : new  UIProgressDialog.WeBoBuilder(AbstractActivity.STACK.get(0)).setTextSize(18).setMessage(R.string.waiting).setCancelable(false).setCanceledOnTouchOutside(false).create().setHeight(DensityUtils.px(application,140)) );
            */
        }
    }
}
