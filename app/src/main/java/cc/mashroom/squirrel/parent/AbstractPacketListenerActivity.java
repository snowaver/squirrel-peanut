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

import  cc.mashroom.squirrel.client.PacketListener;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;

public  class  AbstractPacketListenerActivity  extends  AbstractActivity  implements  PacketListener
{
    @Override
    protected  void  onCreate( Bundle  savedInstanceState )
    {
        super.application().getSquirrelClient().addPacketListener(this);

        super.onCreate(savedInstanceState);
    }
    @Override
    public  boolean  onBeforeSend(   Packet  packet )  throws  Throwable
    {
        return  true;
    }
    @Override
    public  void  onSent(Packet packet,TransportState  transportState  )
    {

    }
    @Override
    public  void  onReceived(        Packet  packet )
    {

    }
    @Override
    protected  void  onDestroy()
    {
        super.onDestroy();

        application().getSquirrelClient().removePacketListener(  this );
    }
}