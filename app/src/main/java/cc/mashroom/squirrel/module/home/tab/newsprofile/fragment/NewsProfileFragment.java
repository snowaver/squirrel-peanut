package cc.mashroom.squirrel.module.home.tab.newsprofile.fragment;

import  android.content.Intent;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.AdapterView;
import  android.widget.ListView;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.connect.PacketEventDispatcher;
import  cc.mashroom.squirrel.client.storage.model.chat.NewsProfile;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.module.home.activity.SubscribeActivity;
import  cc.mashroom.squirrel.parent.AbstractPacketListenerFragment;
import  cc.mashroom.squirrel.module.chat.activity.ChatActivity;
import  cc.mashroom.squirrel.module.chat.activity.GroupChatActivity;
import  cc.mashroom.squirrel.module.home.tab.newsprofile.adapters.NewsProfileListAdapter;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.util.ObjectUtils;

public  class  NewsProfileFragment  extends  AbstractPacketListenerFragment  implements  AdapterView.OnItemClickListener
{
	public  View  onCreateView( LayoutInflater inflater,ViewGroup  container,Bundle  savedInstanceState )
	{
		PacketEventDispatcher.addListener( this );

		if( contentView == null )
		{
			contentView = inflater.inflate( R.layout.fragment_news_profile,container,false );

			ObjectUtils.cast(contentView.findViewById(R.id.profile_list),ListView.class).setOnItemClickListener( this );

			ObjectUtils.cast(contentView.findViewById(R.id.profile_list),ListView.class).setAdapter( new  NewsProfileListAdapter(this) );
		}

		return  contentView;
	}

	protected  View  contentView;

	public  void  onItemClick( AdapterView<?>  parent,View  itemView,int  position,long  id )
	{
		NewsProfile  newsProfile=ObjectUtils.cast(parent.getAdapter(),NewsProfileListAdapter.class).getItem( position );

		if( PAIPPacketType.valueOf(newsProfile.getShort("PACKET_TYPE")) == PAIPPacketType.CHAT || PAIPPacketType.valueOf( newsProfile.getShort( "PACKET_TYPE") ) == PAIPPacketType.CALL )
		{
			ActivityCompat.startActivity( super.getActivity(),new  Intent(this.getActivity(),ChatActivity.class).putExtra("CONTACT_ID", newsProfile.getLong("CONTACT_ID")),ActivityOptionsCompat.makeCustomAnimation(this.getContext(),R.anim.right_in,R.anim.left_out).toBundle() );
		}
		else
		if( PAIPPacketType.valueOf(newsProfile.getShort("PACKET_TYPE")) == PAIPPacketType.GROUP_CHAT    )
		{
			ActivityCompat.startActivity( super.getActivity(),new  Intent(this.getActivity(),GroupChatActivity.class).putExtra("CHAT_GROUP_ID", newsProfile.getLong("ID")),ActivityOptionsCompat.makeCustomAnimation(this.getContext(),R.anim.right_in,R.anim.left_out).toBundle() );
		}
		else
		if( PAIPPacketType.valueOf(newsProfile.getShort("PACKET_TYPE")) == PAIPPacketType.SUBSCRIBE && Integer.parseInt(newsProfile.getString("CONTENT")) == 1 )
		{
			Contact  contact = Contact.dao.getContactDirect().get( newsProfile.getLong( "CONTACT_ID" ) );

            ActivityCompat.startActivity( this.getActivity(),new  Intent(this.getActivity(),SubscribeActivity.class).putExtra("USER",new  User().addEntry("ID",newsProfile.getLong("CONTACT_ID")).addEntry("USERNAME",contact.getString("USERNAME")).addEntry("NICKNAME",contact.getString("REMARK"))),ActivityOptionsCompat.makeCustomAnimation(this.getActivity(),R.anim.right_in,R.anim.left_out).toBundle() );
		}
		else
		if( PAIPPacketType.valueOf(newsProfile.getShort("PACKET_TYPE")) == PAIPPacketType.SUBSCRIBE && Integer.parseInt(newsProfile.getString("CONTENT")) == 0 )
		{
			Contact  contact = Contact.dao.getContactDirect().get( newsProfile.getLong( "CONTACT_ID" ) );

			ActivityCompat.startActivity( this.getActivity(),new  Intent(this.getActivity(),SubscribeActivity.class).putExtra("USER",new  User().addEntry("ID",newsProfile.getLong("CONTACT_ID")).addEntry("USERNAME",contact.getString("USERNAME")).addEntry("REMARK"  ,contact.getString("REMARK"))),ActivityOptionsCompat.makeCustomAnimation(this.getActivity(),R.anim.right_in,R.anim.left_out).toBundle() );
		}
	}

	public  void  onResume()
	{
		super.onResume();

		ObjectUtils.cast( ObjectUtils.cast(contentView.findViewById(R.id.profile_list),ListView.class).getAdapter(),NewsProfileListAdapter.class).notifyDataSetChanged();
	}

	public  void  received( Packet  packet )  throws  Exception
	{
		application().getMainLooperHandler().post( () -> ObjectUtils.cast(ObjectUtils.cast(contentView.findViewById(R.id.profile_list),ListView.class).getAdapter(),NewsProfileListAdapter.class).notifyDataSetChanged() );
	}
}
