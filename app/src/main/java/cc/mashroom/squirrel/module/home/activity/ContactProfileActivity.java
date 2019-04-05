package cc.mashroom.squirrel.module.home.activity;

import  android.content.Intent;
import  android.net.Uri;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.View;
import  android.widget.Button;
import  android.widget.TextView;

import  com.facebook.drawee.view.SimpleDraweeView;
import  com.fasterxml.jackson.core.type.TypeReference;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.connect.PacketEventDispatcher;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribePacket;
import  cc.mashroom.squirrel.parent.AbstractPacketListenerActivity;
import  cc.mashroom.squirrel.module.chat.activity.ChatActivity;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.subscribes.SubscribeAckPacket;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;

import  lombok.Setter;
import  lombok.experimental.Accessors;

public  class  ContactProfileActivity     extends  AbstractPacketListenerActivity  implements  View.OnClickListener
{
	protected  void  onCreate(   Bundle  savedInstanceState )
	{
		PacketEventDispatcher.addListener( this );

		super.onCreate( savedInstanceState );

		setContentView( R.layout.activity_contact_profile  );

		super.findViewById(R.id.chat_or_subscribe_button).setOnClickListener( this );

		setUser( ObjectUtils.cast(new  User().addEntries(ObjectUtils.cast(getIntent().getSerializableExtra("USER"),  new  TypeReference<java.util.Map<String,Object>>(){}))) );

		ObjectUtils.cast(super.findViewById(R.id.details_portrait),SimpleDraweeView.class).setImageURI( Uri.parse(application().baseUrl().addPathSegments("user/"+user.getLong("ID")+"/portrait").build().toString()) );

		ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setVisibility( user.getLong("ID") == application().getUserMetadata().getLong("ID") ? View.GONE : View.VISIBLE );

		ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setText( !Contact.dao.getContactDirect().containsKey(user.getLong("ID")) ? R.string.subscribe_add_contact : prompts.get(Contact.dao.getContactDirect().get(user.getLong("ID")).getInteger("SUBSCRIBE_STATUS")) );

		ObjectUtils.cast(super.findViewById(R.id.username),StyleableEditView.class).setText(     this.user.getString("USERNAME") );

		if( StringUtils.isBlank(user.getString("NICKNAME")) )
		{
			ObjectUtils.cast(super.findViewById(R.id.nickname),StyleableEditView.class).setVisibility( View.GONE );
		}
		else
		{
			ObjectUtils.cast(super.findViewById(R.id.nickname),StyleableEditView.class).setText( this.user.getString("NICKNAME") );
		}

		if( StringUtils.isBlank(user.getString("REMARK"  )) )
		{
			ObjectUtils.cast(super.findViewById(R.id.remark ), StyleableEditView.class).setVisibility( View.GONE );
		}
		else
		{
			ObjectUtils.cast(super.findViewById(R.id.remark ), StyleableEditView.class).setText( this.user.getString(  "REMARK") );
		}

		Contact  contact = Contact.dao.getContactDirect().get( user.getLong( "ID") );

		if( contact!= null )
		{
			ObjectUtils.cast(super.findViewById( R.id.group ), StyleableEditView.class).setText( contact.getString("GROUP_NAME") );
		}
		else
		{
			ObjectUtils.cast(super.findViewById( R.id.group ), StyleableEditView.class).setVisibility( View.GONE );
		}

		ObjectUtils.cast(super.findViewById(R.id.additional_text),TextView.class).setVisibility( user.getLong("ID") != Long.parseLong(application().getSquirrelClient().getId()) && contact != null ? View.VISIBLE : View.GONE );
	}

	private  Map<Integer,Integer>  prompts = new  HashMap<Integer,Integer>().addEntry(0,R.string.subscribe_request_sent).addEntry(1,R.string.accept).addEntry(6,R.string.chat).addEntry(    7 , R.string.chat );
	@Accessors( chain=true )
	@Setter
	private  User  user;

	public  void  sent( Packet  packet,TransportState  sendState )  throws  Exception
	{
		if( packet instanceof SubscribePacket && sendState == TransportState.SENT && ObjectUtils.cast(packet,SubscribePacket.class).getContactId() == this.user.getLong("ID") )
		{
			ObjectUtils.cast(findViewById(R.id.chat_or_subscribe_button), Button.class).setText( R.string.subscribe_request_sent );
		}
	}

	public  void  received( Packet  packet )
	{
		if( !(packet instanceof SubscribeAckPacket) || ObjectUtils.cast(packet, SubscribeAckPacket.class).getContactId() != user.getLong("ID") )
		{
			return;
		}

		application().getMainLooperHandler().post( ()->ObjectUtils.cast(super.findViewById(R.id.chat_or_subscribe_button),Button.class).setText(R.string.chat) );
	}

	public  void  onClick(  View    button )
	{
		Contact  contact = Contact.dao.getContactDirect().get(user.getLong( "ID" ) );

		if( button.getId() == R.id.chat_or_subscribe_button )
		{
			if( contact != null && ( contact.getInteger("SUBSCRIBE_STATUS") == 6 || contact.getInteger("SUBSCRIBE_STATUS") == 7 ) )
			{
				ActivityCompat.startActivity( this,new  Intent(this,ChatActivity.class).putExtra("CONTACT_ID",user.getLong("ID")),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );
			}
			else
			{
				ActivityCompat.startActivity( this,new  Intent(this,SubscribeActivity.class).putExtra("USER",user),ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() );
			}
		}
	}
}
