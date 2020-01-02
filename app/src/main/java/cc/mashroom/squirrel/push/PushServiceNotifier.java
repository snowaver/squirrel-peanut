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
package cc.mashroom.squirrel.push;

import  android.app.NotificationManager;
import  android.app.PendingIntent;
import  android.content.Context;
import  android.content.Intent;
import  androidx.core.app.NotificationCompat;
import  android.widget.RemoteViews;

import  java.text.SimpleDateFormat;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.chat.NewsProfile;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.client.storage.repository.user.ContactRepository;
import  cc.mashroom.squirrel.module.system.activity.NetworkPreinitializeActivity;
import  cc.mashroom.squirrel.parent.Application;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.hedgehog.util.ContextUtils;
import  lombok.AccessLevel;
import  lombok.NonNull;
import  lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access=AccessLevel.PUBLIC)

public  class  PushServiceNotifier
{
	@NonNull
	protected  Application  application;

	protected  SimpleDateFormat  format = new  SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

	public  NewsProfile  notify( NewsProfile  newsProfile )
	{
		if( !ContextUtils.isApplicationRunningBackground(this.application) )
		{
		return  newsProfile;
		}

		NotificationManager  notificationManager = ObjectUtils.cast( this.application.getSystemService(Context.NOTIFICATION_SERVICE) );

		RemoteViews  notification = new  RemoteViews( this.application.getPackageName(),R.layout.notification_push );

		Contact  contact   = ContactRepository.DAO.getContactDirect().get( newsProfile.getContactId() );

		notification.setTextViewText( R.id.content,contact.getRemark()+this.application.getString(R.string.colon)+newsProfile.getContent() );

		notification.setTextViewText( R.id.time,format.format(newsProfile.getCreateTime()).toString() );

		notificationManager.notify( 0,new  NotificationCompat.Builder( this.application,"default").setSmallIcon(R.drawable.app).setTicker(this.application.getString(R.string.notification_received_a_new_message)).setPriority(NotificationCompat.PRIORITY_DEFAULT).setContent(notification).setContentIntent(PendingIntent.getActivity(this.application,0,new  Intent(this.application,NetworkPreinitializeActivity.class),PendingIntent.FLAG_UPDATE_CURRENT)).build() );  return  newsProfile;
	}
}