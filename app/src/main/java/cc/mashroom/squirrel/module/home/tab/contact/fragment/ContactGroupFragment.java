package cc.mashroom.squirrel.module.home.tab.contact.fragment;

import  android.content.Intent;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.ExpandableListView;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.connect.PacketEventDispatcher;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.module.home.activity.SubscribeActivity;
import  cc.mashroom.squirrel.parent.AbstractPacketListenerFragment;
import  cc.mashroom.squirrel.module.home.tab.contact.adapters.ContactGroupAdapter;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribeAckPacket;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.hedgehog.widget.PinnedHeaderExpandableListViewLayout;

public  class  ContactGroupFragment  extends  AbstractPacketListenerFragment
{
	public  void  sent( Packet  packet,TransportState  sendState )  throws  Exception
	{
		if( packet instanceof SubscribeAckPacket )
		{
			application().getMainLooperHandler().post( () -> ObjectUtils.cast(contentView.findViewById(R.id.contact_group_layout),PinnedHeaderExpandableListViewLayout.class).notifyExpandableListAdapterDatasetChanged().expandAllGroups() );
		}
	}

	public  View  onCreateView( LayoutInflater  inflater,ViewGroup  container,Bundle  savedInstanceState )
	{
		PacketEventDispatcher.addListener( this );

		if( contentView == null )
		{
			contentView = inflater.inflate( R.layout.fragment_contact_group,container,false );

			ObjectUtils.cast(contentView.findViewById(R.id.contact_group_layout),PinnedHeaderExpandableListViewLayout.class).setAdapter(new  ContactGroupAdapter(this)).expandAllGroups();

			ObjectUtils.cast(contentView.findViewById(R.id.contact_group),ExpandableListView.class).setOnGroupClickListener( (parent,v,groupPosition,id) -> true );

			ObjectUtils.cast(contentView.findViewById(R.id.contact_group),ExpandableListView.class).setOnChildClickListener( (parent,v,groupPosition,childPosition,id) -> {Contact  contact = ObjectUtils.cast(parent.getExpandableListAdapter().getChild(groupPosition,childPosition));  ActivityCompat.startActivity(super.getActivity(),new  Intent(this.getActivity(),SubscribeActivity.class).putExtra("USER",new  User().addEntry("ID",contact.getLong("ID")).addEntry("USERNAME",contact.getString("USERNAME")).addEntry("REMARK",contact.getString("REMARK"))),ActivityOptionsCompat.makeCustomAnimation(super.getActivity(),R.anim.right_in,R.anim.left_out).toBundle());  return  false;} );
		}

		return  contentView;
	}

	protected  View  contentView;

	public  void  onResume()
	{
		super.onResume();

		ObjectUtils.cast(   contentView.findViewById(R.id.contact_group_layout),PinnedHeaderExpandableListViewLayout.class).notifyExpandableListAdapterDatasetChanged().expandAllGroups();
	}

	public  void  received( Packet  packet )  throws  Exception
	{
		if( packet instanceof SubscribeAckPacket )
		{
			application().getMainLooperHandler().post( () -> ObjectUtils.cast(contentView.findViewById(R.id.contact_group_layout),PinnedHeaderExpandableListViewLayout.class).notifyExpandableListAdapterDatasetChanged().expandAllGroups() );
		}
	}
}