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

import  android.app.job.JobInfo;
import  android.app.job.JobParameters;
import  android.app.job.JobScheduler;
import  android.app.job.JobService;
import  android.content.ComponentName;
import  android.content.Context;
import  android.content.Intent;

import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.squirrel.parent.Application;
import  cc.mashroom.util.ObjectUtils;

public  class  ProcessKeeper  extends  JobService
{
    public  boolean  onStopJob(  JobParameters  jobParameters )
    {
        return   true;
    }

    public  int  onStartCommand( Intent  intent,int  flags,int  startId )
    {
        return  super.onStartCommand( intent, flags, startId );
    }

    public  boolean  onStartJob( JobParameters  jobParameters )
    {
        if( !ContextUtils.isServiceRunning(ProcessKeeper.this,PushService.class) )
        {
            super.startService( new  Intent( this, PushService.class ) );
        }

        ObjectUtils.cast(super.getSystemService(Context.JOB_SCHEDULER_SERVICE),JobScheduler.class).cancel( Application.PROCESS_KEEPER_SCHEDUAL_JOB_ID.get() );

        ObjectUtils.cast(super.getSystemService(Context.JOB_SCHEDULER_SERVICE),JobScheduler.class).schedule( new  JobInfo.Builder(Application.PROCESS_KEEPER_SCHEDUAL_JOB_ID.incrementAndGet(),new  ComponentName(this.getPackageName(),ProcessKeeper.class.getName())).setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE).setPersisted(true).setRequiresCharging(false).setRequiresDeviceIdle(false).setMinimumLatency(1000).setOverrideDeadline(15*1000).build() );

        return  false;
    }
}