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
package cc.mashroom.squirrel.module.chat.listener;

import  android.Manifest;
import  android.app.Activity;
import  android.view.MotionEvent;
import  android.view.View;
import  android.widget.Button;

import  com.irozon.sneaker.Sneaker;

import  java.io.File;

import  androidx.core.app.ActivityCompat;
import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.common.Tracer;
import cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.parent.Application;
import  cc.mashroom.hedgehog.device.MediaRecorder;
import  cc.mashroom.util.ObjectUtils;
import  lombok.Setter;
import  lombok.experimental.Accessors;
import  permissions.dispatcher.PermissionUtils;

public  class  AudioTouchRecoder   implements  View.OnTouchListener
{
    public   interface  OnRecordListener
    {
        public  void  onRecord(File audio);
    }

    @Accessors( chain= true )
    @Setter
    private  Activity  context;
    @Accessors( chain= true )
    @Setter
    private  File   cacheDir;
    @Accessors( chain= true )
    @Setter
    private  OnRecordListener  listener;

    private  MediaRecorder  recorder;

    private  boolean  recording = false;

    public  AudioTouchRecoder( Activity  context,File  cacheDir,OnRecordListener  listener )
    {
        this.setContext(context).setCacheDir(cacheDir).setListener( listener );
    }

    public  boolean  onTouch( View  view, MotionEvent  touchEvent )
    {
        try
        {
            if( touchEvent.getAction() == MotionEvent.ACTION_DOWN )
            {
                ObjectUtils.cast(context.findViewById(R.id.voice_recording_button),Button.class).setBackgroundResource(     R.drawable.voice_recording_backround_pressed );

                ObjectUtils.cast(context.findViewById(R.id.voice_recording_button),Button.class).setText(           R.string.chat_talking );

                try
                {
                    recording = true;

                    (recorder = new  MediaRecorder()).prepare(android.media.MediaRecorder.AudioSource.MIC,android.media.MediaRecorder.AudioEncoder.AMR_WB,android.media.MediaRecorder.OutputFormat.AMR_WB,new  File(ObjectUtils.cast(context.getApplication(),Application.class).getCacheDir(),"audio.amr.tmp")).wrapped().start();
                }
                catch( Throwable  e )
                {
                    recording =false;

                    if( !PermissionUtils.hasSelfPermissions(context,Manifest.permission.RECORD_AUDIO) )
                    {
                        ActivityCompat.requestPermissions(context,new  String[]{Manifest.permission.RECORD_AUDIO},0);
                    }
                    else
                    {
                        ObjectUtils.cast(context,AbstractActivity.class).showSneakerWindow( Sneaker.with(context),com.irozon.sneaker.R.drawable.ic_error,R.string.unknown_error,R.color.white,R.color.red );
                    }

                    Tracer.trace(e );
                }
                finally
                {
                    return  true;
                }
            }
            else
            //  pressed  state  is  forcely  removed  by  the  grant  permission  dialog  on  vivo  (y66)  if  not  granted  yet,  so  restore  the  press  state  and  close  the  media  recorder  even  permission  is  granted  now.
            if( touchEvent.getAction()==MotionEvent.ACTION_CANCEL )
            {
                try
                {
                    ObjectUtils.cast(context.findViewById(R.id.voice_recording_button),Button.class).setBackgroundResource( R.drawable.voice_recording_backround_general );

                    ObjectUtils.cast(context.findViewById(R.id.voice_recording_button),Button.class).setText( R.string.chat_press_to_talk );

                    File  tmpAudioFile = new  File( cacheDir,"audio.amr.tmp" );

                    listener.onRecord( tmpAudioFile.length() == 0 || !recording ? null : ObjectUtils.cast(context.getApplication(),Application.class).cache(-1,tmpAudioFile,2) );

                    recorder.close();
                }
                catch( Throwable  e )
                {
                    Tracer.trace(e );
                }
                finally
                {
                    recording =false;

                    return  true;
                }
            }
            else
            if( touchEvent.getAction() == MotionEvent.ACTION_UP   )
            {
                try
                {
                    ObjectUtils.cast(context.findViewById(R.id.voice_recording_button),Button.class).setBackgroundResource( R.drawable.voice_recording_backround_general );

                    ObjectUtils.cast(context.findViewById(R.id.voice_recording_button),Button.class).setText( R.string.chat_press_to_talk );

                    File  tmpAudioFile = new  File( cacheDir,"audio.amr.tmp" );

                    listener.onRecord( tmpAudioFile.length() == 0 ? null : ObjectUtils.cast(context.getApplication(),Application.class).cache(-1,tmpAudioFile,2) );

                    recorder.close();
                }
                catch( Throwable  e )
                {
                    Tracer.trace(e );
                }
                finally
                {
                    recording =false;

                    return  true;
                }
            }
        }
        catch( Throwable  ie )
        {
            ie.printStackTrace();
        }

        return  false;
    }
}