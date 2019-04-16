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

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.chat.NewsProfile;
import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  cc.mashroom.squirrel.module.system.activity.LoadingActivity;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.hedgehog.util.ContextUtils;
import  lombok.AccessLevel;
import  lombok.NoArgsConstructor;
import  lombok.Setter;
import  lombok.experimental.Accessors;

@NoArgsConstructor( access = AccessLevel.PRIVATE )

public  class  PushServiceNotifier
{
	@Accessors( chain = true )
	@Setter
	private  Context  context;

	public  void  install( Context  context )
	{
		this.context= context;
	}

	public  final  static  PushServiceNotifier  INSTANCE = new  PushServiceNotifier();

	public  NewsProfile  notify( NewsProfile  newsProfile )
	{
		if( ContextUtils.isApplicationRunningBackground(context) )
		{
			NotificationManager  notificationManager = ObjectUtils.cast( context.getSystemService(Context.NOTIFICATION_SERVICE) );

			RemoteViews  notification = new  RemoteViews( context.getPackageName(),R.layout.notification_push );

			Contact  contact = Contact.dao.getContactDirect().get( newsProfile.getLong("CONTACT_ID") );

			notification.setTextViewText( R.id.content,contact.getString("REMARK")+context.getString(R.string.colon)+newsProfile.getString("CONTENT") );

			notification.setTextViewText( R.id.time,newsProfile.get("CREATE_TIME").toString() );

			notificationManager.notify( 0,new  NotificationCompat.Builder(context,"default").setSmallIcon(R.drawable.app).setTicker(context.getString(R.string.notification_received_a_new_message)).setPriority(NotificationCompat.PRIORITY_DEFAULT).setContent(notification).setContentIntent(PendingIntent.getActivity(context,0,new  Intent(context,LoadingActivity.class),PendingIntent.FLAG_UPDATE_CURRENT)).build() );
		}

		return    newsProfile;
	}
}