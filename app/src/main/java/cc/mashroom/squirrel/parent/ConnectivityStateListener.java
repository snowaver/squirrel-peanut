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

        SharedPreferences  sharedPreferences  = application.getSharedPreferences( "LATEST_LOGIN_FORM",MODE_PRIVATE );

        if( sharedPreferences.getLong("ID",0) >= 1 && !application.getSquirrelClient().isAuthenticated() )
        {

        }
    }
}