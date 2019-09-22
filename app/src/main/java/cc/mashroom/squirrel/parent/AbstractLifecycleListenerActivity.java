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
package cc.mashroom.squirrel.parent;

import  android.os.Bundle;

import  cc.mashroom.squirrel.client.LifecycleListener;
import  cc.mashroom.squirrel.client.connect.ConnectState;
import  cc.mashroom.squirrel.client.storage.model.OoIData;

public  class  AbstractLifecycleListenerActivity    extends  AbstractActivity  implements  LifecycleListener
{
    @Override
    protected  void  onCreate( Bundle  savedInstanceState )
    {
        application().getSquirrelClient().addLifecycleListener(  this );

        super.onCreate(savedInstanceState);
    }
    @Override
    protected  void  onDestroy()
    {
        super.onDestroy();

        application().getSquirrelClient().removeLifecycleListener(this);
    }
    @Override
    public  void  onError(    Throwable   throwable )
    {

    }
    @Override
    public  void  onLogoutComplete( int  code,int  reason )
    {

    }
    @Override
    public  void  onConnectStateChanged( ConnectState     connectState )
    {

    }
    @Override
    public  void  onAuthenticateComplete( int  code )
    {

    }
    @Override
    public  void  onReceivedOfflineData( OoIData  ooIData )
    {

    }
}