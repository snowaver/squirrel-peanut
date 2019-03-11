package cc.mashroom.squirrel.parent;

import  cc.mashroom.squirrel.client.connect.PacketEventDispatcher;
import  cc.mashroom.squirrel.client.connect.PacketListener;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;

public  class  AbstractPacketListenerFragment  extends  AbstractFragment  implements  PacketListener
{
    public  void  sent( Packet  packet,TransportState sendState )  throws  Exception
    {

    }

    public  void  received( Packet  packet )  throws  Exception
    {

    }

    public  boolean  beforeSend( Packet  packet )  throws  Exception
    {
        return  true;
    }

    public  void  onDestroyView()
    {
        super.onDestroyView();

        PacketEventDispatcher.removeListener( this );
    }
}
