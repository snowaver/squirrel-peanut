package cc.mashroom.squirrel.push;

import  android.content.BroadcastReceiver;
import  android.content.Context;
import  android.content.Intent;

public  class  RunningNotificationClickedReceiver  extends  BroadcastReceiver
{
    public  void  onReceive( Context  context,Intent  intent )
    {
        System.err.print( "//.RECEIVED:  "+intent+" , "+context );
    }
}
