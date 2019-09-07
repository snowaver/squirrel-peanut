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

import  java.io.File;
import  java.io.IOException;
import  java.sql.Connection;
import  java.util.List;
import  java.util.concurrent.atomic.AtomicBoolean;

import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;

import  cc.mashroom.hedgehog.util.MultimediaUtils;
import  cc.mashroom.db.common.Db;
import  cc.mashroom.hedgehog.widget.ViewSwitcher;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.connect.PacketEventDispatcher;
import  cc.mashroom.squirrel.client.connect.PacketListener;
import  cc.mashroom.squirrel.client.storage.repository.chat.NewsProfileRepository;
import  cc.mashroom.squirrel.client.storage.repository.chat.group.ChatGroupRepository;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.chat.adapters.GroupChatMessageListviewAdapter;
import  cc.mashroom.squirrel.module.chat.adapters.MoreInputsAdapter;
import  cc.mashroom.squirrel.module.chat.listener.AudioTouchRecoder;
import  cc.mashroom.squirrel.module.common.services.FileService;
import  cc.mashroom.squirrel.paip.message.Packet;
import  cc.mashroom.squirrel.paip.message.PAIPPacketType;
import  cc.mashroom.squirrel.paip.message.TransportState;
import  cc.mashroom.squirrel.paip.message.chat.ChatContentType;
import  cc.mashroom.squirrel.paip.message.chat.GroupChatPacket;
import  cc.mashroom.hedgehog.system.Media;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.hedgehog.widget.HeaderBar;
import  es.dmoral.toasty.Toasty;
import  lombok.Setter;
import  lombok.SneakyThrows;
import  lombok.experimental.Accessors;
import  okhttp3.MediaType;
import  okhttp3.MultipartBody;
import  okhttp3.RequestBody;

public  class  GroupChatActivity  extends  AbstractActivity       implements  PacketListener,View.OnKeyListener,AbsListView.OnScrollListener
{
	@SneakyThrows
	protected  void  onCreate( Bundle  savedInstanceState )
	{
		PacketEventDispatcher.addListener( this );

		super.onCreate( savedInstanceState   );

		setContentView(     R.layout.activity_group_chat );

		setGroupId(super.getIntent().getLongExtra("CHAT_GROUP_ID",0) );

		ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).setTitle( ChatGroupRepository.DAO.lookupOne(String.class,"SELECT  NAME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{groupId}) );

		ObjectUtils.cast(super.findViewById(R.id.editor),EditText.class).setOnKeyListener(  this );

		ObjectUtils.cast(super.findViewById(R.id.switch_to_voice_recording_button),ImageView.class).setOnClickListener( (imageView) -> ObjectUtils.cast(super.findViewById(R.id.editor_switcher), ViewSwitcher.class).setDisplayedChild( 1 ) );

		ObjectUtils.cast(super.findViewById(R.id.voice_recording_button),Button.class).setOnTouchListener( new  AudioTouchRecoder(this,application().getCacheDir(),(audioFile) -> send(audioFile)) );

		ObjectUtils.cast(super.findViewById(R.id.messages),ListView.class).setAdapter( new  GroupChatMessageListviewAdapter(this,groupId) );

		ObjectUtils.cast(super.findViewById(R.id.more_inputs_button),ImageView.class).setOnClickListener( (view) -> ObjectUtils.cast(super.findViewById(R.id.more_inputs),GridView.class).setVisibility(ObjectUtils.cast(super.findViewById(R.id.more_inputs),GridView.class).getVisibility() == View.GONE ? View.VISIBLE : View.GONE) );

		ObjectUtils.cast(super.findViewById(R.id.switch_to_words_inputting_button),ImageView.class).setOnClickListener( (imageView) -> ObjectUtils.cast(super.findViewById(R.id.editor_switcher), ViewSwitcher.class).setDisplayedChild( 0 ) );

		ObjectUtils.cast(super.findViewById(R.id.more_inputs),GridView.class).setAdapter( new  MoreInputsAdapter(this,groupId,Lists.newArrayList(new  HashMap<String,Integer>().addEntry("image",R.drawable.camera),new  HashMap<String,Integer>().addEntry("image",R.drawable.album))) );

		ObjectUtils.cast(super.findViewById(R.id.additional_switcher),ViewSwitcher.class).setOnClickListener( (view) -> ActivityCompat.startActivity( this , new  Intent(this, GroupChatProfileActivity.class).putExtra("CHAT_GROUP_ID",groupId) , ActivityOptionsCompat.makeCustomAnimation(this,R.anim.right_in,R.anim.left_out).toBundle() ) );
	}

	public  void  send(       File  audioFile )
	{
		if( audioFile==null )
		{
			return;
		}

		long  voiceDuration = MultimediaUtils.getDuration( audioFile );

		if( voiceDuration >= 200 )
		{
			application().getSquirrelClient().asynchronousSend( new  GroupChatPacket(application().getSquirrelClient().getUserMetadata().getId(), this.groupId, audioFile.getName(), ChatContentType.AUDIO,     String.valueOf(voiceDuration < 1000 ? 1000 : voiceDuration).getBytes()) );
		}
		else
		{
			super.showSneakerWindow( Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_warning,R.string.chat_recorded_voice_duration_too_short,R.color.black,R.color.orange );
		}
	}

	@Accessors( chain= true )
	@Setter
	private  long    groupId;

	private  AtomicBoolean  initialized =  new  AtomicBoolean( false );

    @Override
    public  void  onScrollStateChanged( AbsListView  view, int  state )
    {

    }

    @Override
    public  void  onScroll( AbsListView  view,int  firstVisibleItem,int  visibleItemCount, int  totalCount )
    {
        //  deperacated:  should  stack  from  bottom  on  conditions,  in  which  it  is  necessary  that  visible  item  count  is  greater  than  zero:  1.  visible  item  count  is  less  than  total  count.  2.  last  item  is  partially  visible.
        if( view.getLastVisiblePosition()-firstVisibleItem > 0 && view.getChildAt(0).getTop()== 0 )
        {

        }
    }

	public  void  onSent( Packet packet,TransportState transportState )
	{
		if( packet instanceof GroupChatPacket )
		{
			application().getMainLooperHandler().post( () -> ObjectUtils.cast(ObjectUtils.cast(GroupChatActivity.this.findViewById(R.id.messages),ListView.class).getAdapter(),GroupChatMessageListviewAdapter.class).upsert(packet.getId()) );
		}
	}

	public  void  onReceived(  Packet  packet )
	{
		if( packet instanceof GroupChatPacket )
		{
			application().getMainLooperHandler().post( () -> ObjectUtils.cast(ObjectUtils.cast(GroupChatActivity.this.findViewById(R.id.messages),ListView.class).getAdapter(),GroupChatMessageListviewAdapter.class).upsert(packet.getId()) );
		}
	}

	public  boolean  onBeforeSend(  Packet  packet )  throws  Throwable
	{
		if( packet instanceof GroupChatPacket )
		{
			application().getMainLooperHandler().post( () -> ObjectUtils.cast(ObjectUtils.cast(GroupChatActivity.this.findViewById(R.id.messages),ListView.class).getAdapter(),GroupChatMessageListviewAdapter.class).upsert(packet.getId()) );

			if( ObjectUtils.cast(packet,GroupChatPacket.class).getContentType() == ChatContentType.IMAGE || ObjectUtils.cast(packet,GroupChatPacket.class).getContentType() == ChatContentType.VIDEO )
			{
				if( RetrofitRegistry.INSTANCE.get(FileService.class).add(Lists.newArrayList(MultipartBody.Part.createFormData("file",ObjectUtils.cast(packet,GroupChatPacket.class).getMd5(),RequestBody.create(MediaType.parse("application/otcet-stream"),new  File(application().getCacheDir(),"file/"+ObjectUtils.cast(packet,GroupChatPacket.class).getMd5()))),MultipartBody.Part.createFormData("thumbnailFile",ObjectUtils.cast(packet,GroupChatPacket.class).getMd5()+"$TMB",RequestBody.create(MediaType.parse("application/otcet-stream"),new  File(application().getCacheDir(),"file/"+ObjectUtils.cast(packet,GroupChatPacket.class).getMd5()+"$TMB"))))).execute().code() != 200 )
				{
					return  false;
				}
			}
			else
			if( ObjectUtils.cast(packet,GroupChatPacket.class).getContentType() == ChatContentType.AUDIO && RetrofitRegistry.INSTANCE.get(FileService.class).add(Lists.newArrayList(MultipartBody.Part.createFormData("file",ObjectUtils.cast(packet,GroupChatPacket.class).getMd5(),RequestBody.create(MediaType.parse("application/otcet-stream"),new  File(application().getCacheDir(),"file/"+ObjectUtils.cast(packet,GroupChatPacket.class).getMd5()))))).execute().code() != 200 )
			{
				{
					return  false;
				}
			}
		}

		return  true;
	}

	public  boolean  onKey( View  view, int  keyCode, KeyEvent  event )
	{
		if( keyCode == KeyEvent.KEYCODE_ENTER         && event.getAction()  == KeyEvent.ACTION_UP )
		{
			if( StringUtils.isNotBlank(ObjectUtils.cast(      super.findViewById(R.id.editor),EditText.class).getText().toString().trim()) )
			{
				application().getSquirrelClient().send( new  GroupChatPacket(application().getSquirrelClient().getUserMetadata().getId(),this.groupId,"",ChatContentType.WORDS,ObjectUtils.cast(super.findViewById(R.id.editor),EditText.class).getText().toString().trim().getBytes()) );

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

        ListView  listview  = ObjectUtils.cast( super.findViewById(R.id.messages),ListView.class );



//		ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.messages),ListView.class).getAdapter(),GroupChatMessageListviewAdapter.class).notifyDataSetChanged();

		ObjectUtils.cast(ObjectUtils.cast(super.findViewById(R.id.more_inputs),GridView.class).getAdapter(),MoreInputsAdapter.class).notifyDataSetChanged();

		ObjectUtils.cast(super.findViewById(R.id.header_bar),HeaderBar.class).setTitle( ChatGroupRepository.DAO.lookupOne(String.class,"SELECT  NAME  FROM  "+ChatGroupRepository.DAO.getDataSourceBind().table()+"  WHERE  ID = ?",new  Object[]{groupId}) );
	}

	protected  void    onPause()
	{
		super.onPause(  );

		Db.tx( String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_SERIALIZABLE,(connection) -> NewsProfileRepository.DAO.clearBadgeCount(this.groupId, PAIPPacketType.GROUP_CHAT.getValue()) );
	}

	protected  void  onActivityResult(    int  requestCode , int  resultCode , Intent  resultData )
	{
		super.onActivityResult( requestCode , resultCode, resultData );

		if( resultData != null )
		{
			for( Media  media : ObjectUtils.cast(         resultData.getSerializableExtra("MEDIAS"), new  TypeReference<List<Media>>(){} ) )
			{
				try
				{
					File  cachedFile= application().cache( media.getId(),new  File(media.getPath()), media.getType()  == cc.mashroom.hedgehog.system.MediaType.IMAGE ? ChatContentType.IMAGE.getValue() : ChatContentType.VIDEO.getValue() );

					application().getSquirrelClient().asynchronousSend( new  GroupChatPacket(application().getSquirrelClient().getUserMetadata().getId(),groupId,cachedFile.getName(),media.getType() == cc.mashroom.hedgehog.system.MediaType.IMAGE ? ChatContentType.IMAGE : ChatContentType.VIDEO,cachedFile.getName().getBytes()) );
				}
				catch(       IOException  ioe )
				{
					error(ioe );

					super.showSneakerWindow( Sneaker.with(this),com.irozon.sneaker.R.drawable.ic_error,  R.string.io_exception, R.color.white,R.color.red );
				}
			}
		}
	}

	protected  void  onDestroy()
	{
		super.onDestroy();

		PacketEventDispatcher.removeListener( GroupChatActivity.this );

		Db.tx( String.valueOf(application().getSquirrelClient().getUserMetadata().getId()),Connection.TRANSACTION_SERIALIZABLE,(connection) -> NewsProfileRepository.DAO.clearBadgeCount(this.groupId, PAIPPacketType.GROUP_CHAT.getValue()) );
	}
}