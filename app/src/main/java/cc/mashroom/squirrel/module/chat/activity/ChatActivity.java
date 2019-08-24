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
package cc.mashroom.squirrel.module.chat.activity;

import  android.content.Intent;
import  android.os.Bundle;
import  android.view.KeyEvent;
import  android.view.View;
import  android.widget.AbsListView;
import  android.widget.Button;
import  android.widget.EditText;
import  android.widget.GridView;
import  android.widget.ImageView;
import  android.widget.ListView;
import  android.widget.Toast;

import  com.fasterxml.jackson.core.type.TypeReference;
import  com.google.common.collect.Lists;
import  com.irozon.sneaker.Sneaker;

import  cc.mashroom.hedgehog.util.MultimediaUtils;
import  cc.mashroom.db.common.Db;
import  cc.mashroom.hedgehog.widget.ViewSwitcher;
import  cc.mashroom.squirrel.client.storage.repository.chat.NewsProfileRepository;
import  cc.mashroom.squirrel.client.storage.repository.user.ContactRepository;
import  cc.mashroom.squirrel.module.chat.adapters.MoreInputsAdapter;
import  cc.mashroom.squirrel.module.chat.listener.AudioTouchRecoder;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.connect.PacketEventDispatcher;
import  cc.mashroom.squirrel.client.connect.PacketListener;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.chat.adapters.ChatMessageListviewAdapter;
import  cc.mashroom.squirrel.module.common.services.FileService;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.chat.ChatPacket;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.hedgehog.system.Media;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;

import  java.io.File;
import  java.sql.Connection;
import  java.util.List;

import  cc.mashroom.hedgehog.widget.HeaderBar;
import  es.dmoral.toasty.Toasty;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;
import  okhttp3.MediaType;
import  okhttp3.MultipartBody;
import  okhttp3.RequestBody;

public  class  ChatActivity  extends  AbstractActivity      implements  PacketListener,  View.OnKeyListener, AbsListView.OnScrollListener
{
	protected  void  onCreate( Bundle  savedInstanceState )
	{
		PacketEventDispatcher.addListener( this );

		super.onCreate( savedInstanceState );

		setContentView( R.layout.activity_chat  );

		setContactId( super.getIntent().getLongExtra("CONTACT_ID",0) );

		ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).setTitle( ContactRepository.DAO.getContactDirect().get(contactId).getRemark() );

		ObjectUtils.cast(super.findViewById(R.id.editor),EditText.class).setOnKeyListener(  this );

		ObjectUtils.cast(super.findViewById(R.id.switch_to_voice_recording_button),ImageView.class).setOnClickListener( (view)->ObjectUtils.cast(super.findViewById(R.id.editor_switcher),ViewSwitcher.class).setDisplayedChild(1) );

		ObjectUtils.cast(super.findViewById(R.id.voice_recording_button),Button.class).setOnTouchListener( new  AudioTouchRecoder(this,application().getCacheDir(),(audioFile) -> send(audioFile)) );

		ObjectUtils.cast(super.findViewById(R.id.messages),ListView.class).setAdapter( new  ChatMessageListviewAdapter(this,contactId) );

		ObjectUtils.cast(super.findViewById(R.id.messages),ListView.class).setSelection( ObjectUtils.cast(super.findViewById(R.id.messages) , ListView.class).getAdapter().getCount() - 1 );

        ObjectUtils.cast(super.findViewById(R.id.messages),ListView.class).setOnScrollListener(      this );

		ObjectUtils.cast(super.findViewById(R.id.more_inputs_button),ImageView.class).setOnClickListener( (view) -> ObjectUtils.cast(super.findViewById(R.id.more_inputs),GridView.class).setVisibility(ObjectUtils.cast(super.findViewById(R.id.more_inputs),GridView.class).getVisibility() == View.GONE ? View.VISIBLE : View.GONE) );

		ObjectUtils.cast(super.findViewById(R.id.switch_to_words_inputting_button),ImageView.class).setOnClickListener( (view)->ObjectUtils.cast(super.findViewById(R.id.editor_switcher),ViewSwitcher.class).setDisplayedChild(0) );

		ObjectUtils.cast(super.findViewById(R.id.more_inputs),GridView.class).setAdapter( new  MoreInputsAdapter(this,contactId,Lists.newArrayList(new  HashMap<String,Integer>().addEntry("image",R.drawable.camera),new  HashMap<String,Integer>().addEntry("image",R.drawable.album),new  HashMap<String,Integer>().addEntry("image",R.drawable.telephone),new  HashMap<String,Integer>().addEntry("image",R.drawable.camcorder))) );
	}

	public  void  send(  File  audioFile )
	{
		if( audioFile==null )
		{
			return;
		}

		long  voiceDuration = MultimediaUtils.getDuration( audioFile );

		if( voiceDuration  >= 200 )
		{
			application().getSquirrelClient().asynchronousSend( new  ChatPacket(this.contactId,audioFile.getName() , ChatContentType.AUDIO , String.valueOf(voiceDuration < 1000 ? 1000 : voiceDuration).getBytes()) );
		}
		else
		{
			super.showSneakerWindow(   Sneaker.with( this ) , com.irozon.sneaker.R.drawable.ic_warning , R.string.chat_recorded_voice_duration_too_short , R.color.black , R.color.orange );
		}
	}

	@Accessors( chain= true )
	@Setter
	private  long  contactId;

    @Override
    public  void  onScrollStateChanged( AbsListView  view, int  state )
    {

    }

    @Override
    public  void  onScroll(  AbsListView  view,int  firstVisibleItem,int  visibleItemCount,int  totalCount )
    {
        if( firstVisibleItem == 0 && view.getChildAt(0).getTop() == 0 )
        {
            ObjectUtils.cast(view.getAdapter(),       ChatMessageListviewAdapter.class).cachePreviousPage();
        }
    }

	public  void  onSent( Packet packet,TransportState transportState )
	{
		if( packet instanceof ChatPacket )
		{
			application().getMainLooperHandler().post( () -> ObjectUtils.cast(ObjectUtils.cast(ChatActivity.this.findViewById(R.id.messages),ListView.class).getAdapter(),ChatMessageListviewAdapter.class).append(packet.getId()) );
		}
	}

	public  void  onReceived( final  Packet  packet )
	{
		if( packet instanceof ChatPacket )
		{
			application().getMainLooperHandler().post( () -> ObjectUtils.cast(ObjectUtils.cast(ChatActivity.this.findViewById(R.id.messages),ListView.class).getAdapter(),ChatMessageListviewAdapter.class).append(packet.getId()) );
		}
	}

	@SneakyThrows
	public  boolean  onBeforeSend(   Packet  packet ) throws  Throwable
	{
		if( packet instanceof ChatPacket )
		{
			application().getMainLooperHandler().post( () -> ObjectUtils.cast(ObjectUtils.cast(ChatActivity.this.findViewById(R.id.messages),ListView.class).getAdapter(),ChatMessageListviewAdapter.class).append(packet.getId()) );

			if( ObjectUtils.cast(packet,ChatPacket.class).getContentType() == ChatContentType.IMAGE || ObjectUtils.cast(packet,ChatPacket.class).getContentType() == ChatContentType.VIDEO )
			{
				if( RetrofitRegistry.INSTANCE.get(FileService.class).add(Lists.newArrayList(MultipartBody.Part.createFormData("file",ObjectUtils.cast(packet,ChatPacket.class).getMd5(),RequestBody.create(MediaType.parse("application/otcet-stream"),new  File(application().getCacheDir(),"file/"+ObjectUtils.cast(packet,ChatPacket.class).getMd5()))),MultipartBody.Part.createFormData("thumbnailFile",ObjectUtils.cast(packet,ChatPacket.class).getMd5()+"$TMB",RequestBody.create(MediaType.parse("application/otcet-stream"),new  File(application().getCacheDir(),"file/"+ObjectUtils.cast(packet,ChatPacket.class).getMd5()+"$TMB"))))).execute().code() != 200 )
				{
					return  false;
				}
			}
			else
			if( ObjectUtils.cast(packet,ChatPacket.class).getContentType()==ChatContentType.AUDIO )
			{
				if( RetrofitRegistry.INSTANCE.get(FileService.class).add(Lists.newArrayList(MultipartBody.Part.createFormData("file",ObjectUtils.cast(packet,ChatPacket.class).getMd5(),RequestBody.create(MediaType.parse("application/otcet-stream"),new  File(application().getCacheDir(),"file/"+ObjectUtils.cast(packet,ChatPacket.class).getMd5()))))).execute().code() != 200 )
				{
					return  false;
				}
			}
		}

		return  true;
	}

	public  boolean  onKey( View  view, int  keyCode, KeyEvent  event )
	{
		if( keyCode == KeyEvent.KEYCODE_ENTER         &&  event.getAction() == KeyEvent.ACTION_UP )
		{
			if( StringUtils.isNotBlank(ObjectUtils.cast(super.findViewById(R.id.editor),EditText.class).getText().toString().trim()) )
			{
				application().getSquirrelClient().send( new  ChatPacket(contactId,null,ChatContentType.WORDS,ObjectUtils.cast(super.findViewById(R.id.editor),EditText.class).getText().toString().trim().getBytes()) );

				ObjectUtils.cast(super.findViewById(R.id.editor),EditText.class).getText().clear();
			}
			else
			{
				Toasty.warning(this,super.getString(R.string.content_empty),Toast.LENGTH_LONG,false).show();
			}
		}

		return  false;
	}

	protected  void   onResume()
	{
		super.onResume( );

		application().getMainLooperHandler().post( () -> ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.messages),ListView.class).getAdapter() , ChatMessageListviewAdapter.class ).notifyDataSetChanged() );

		application().getMainLooperHandler().post( () -> ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.more_inputs),GridView.class).getAdapter(),MoreInputsAdapter.class).notifyDataSetChanged() );
	}

	protected  void    onPause()
	{
		super.onPause(  );

		Db.tx( String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_SERIALIZABLE,(connection) -> NewsProfileRepository.DAO.clearBadgeCount(contactId,PAIPPacketType.CHAT.getValue()) );
	}

	protected  void  onDestroy()
	{
		super.onDestroy();

		PacketEventDispatcher.removeListener( this );

		Db.tx( String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_SERIALIZABLE,(connection) -> NewsProfileRepository.DAO.clearBadgeCount(contactId,PAIPPacketType.CHAT.getValue()) );
	}

	protected  void  onActivityResult(    int  requestCode , int  resultCode , Intent  resultData )
	{
		super.onActivityResult( requestCode , resultCode, resultData );

		if( resultData != null )
		{
			for( Media  choosedMedia : ObjectUtils.cast(resultData.getSerializableExtra("CAPTURED_MEDIAS"),new  TypeReference<List<Media>>(){}) )
			{
				File  cachedFile = application().cache(choosedMedia.getId(),new  File(choosedMedia.getPath()),choosedMedia.getType() == cc.mashroom.hedgehog.system.MediaType.IMAGE ? ChatContentType.IMAGE.getValue() : ChatContentType.VIDEO.getValue() );

				application().getSquirrelClient().asynchronousSend( new  ChatPacket(contactId,cachedFile.getName(),choosedMedia.getType() == cc.mashroom.hedgehog.system.MediaType.IMAGE ? ChatContentType.IMAGE : ChatContentType.VIDEO,cachedFile.getName().getBytes()) );
			}
		}
	}
}