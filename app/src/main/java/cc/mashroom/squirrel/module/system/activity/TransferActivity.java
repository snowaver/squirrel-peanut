package cc.mashroom.squirrel.module.system.activity;

import  android.os.Bundle;

import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.squirrel.parent.AbstractActivity;

public  class  TransferActivity   extends  AbstractActivity
{
    protected  void  onCreate( Bundle  savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        ContextUtils.finish( this );
    }
}
