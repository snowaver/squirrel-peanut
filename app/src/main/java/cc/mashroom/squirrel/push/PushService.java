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

import  android.app.PendingIntent;
import  android.app.Service;
import  android.content.Intent;
import  android.os.IBinder;
import  androidx.core.app.NotificationCompat;
import  android.widget.RemoteViews;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.module.system.activity.TransferActivity;

public  class  PushService   extends  Service
{
	public  int  onStartCommand( Intent  intent,int  flags,int  startId )
	{
		return  Service.START_STICKY;
	}

	public  IBinder  onBind( Intent  intent )
	{
		return  null;
	}

	public  void  onCreate()
	{
		super.onCreate();

		RemoteViews  notification = new  RemoteViews( super.getPackageName(),R.layout.notification_push );

		notification.setImageViewResource(  R.id.icon , R.drawable.app );

		notification.setTextViewText( R.id.title,super.getString(R.string.squirrel) );

		notification.setTextViewText( R.id.content,super.getString(R.string.program_running) );
		//  running  notification  clicked  receiver  can  not  receive  the  broadcast  from  broadcast  pending  intent  or  with  a  very  high  delay,  so  use  an  empty  activity  instead.
		super.startForeground( 1,new  NotificationCompat.Builder(this,"default").setPriority(NotificationCompat.PRIORITY_MAX).setAutoCancel(false).setSmallIcon(R.drawable.app).setCustomContentView(notification).setContentIntent(PendingIntent.getActivity(this,0,new  Intent(this,TransferActivity.class),PendingIntent.FLAG_UPDATE_CURRENT)).build() );
	}
}