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
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;

import  androidx.annotation.NonNull;
import  androidx.annotation.Nullable;

import  cc.mashroom.squirrel.client.event.PacketEventListener;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;

public  class  AbstractPacketEventListenerFragment  extends   AbstractFragment  implements  PacketEventListener
{
    @Nullable
    @Override
    public  View  onCreateView( @NonNull  LayoutInflater  inflater,@Nullable  ViewGroup  container,@Nullable  Bundle  savedInstanceState )
    {
        super.application().getSquirrelClient().getPacketEventDispatcher().addListener(    this );

        return  super.onCreateView( inflater,container,savedInstanceState );
    }
    @Override
    public  void  onBeforeSend(Packet  packet )
    {

    }
    @Override
    public  void  onSent(   Packet  packet, TransportState  transportState )
    {

    }
    @Override
    public  void  onReceived(  Packet  packet )
    {

    }
    @Override
    public  void  onDestroyView()
    {
        super.onDestroyView();

        super.application().getSquirrelClient().getPacketEventDispatcher().removeListener( this );
    }
}