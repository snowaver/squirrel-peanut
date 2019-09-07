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
package cc.mashroom.squirrel.module.chat.adapters;

import  android.content.Intent;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.widget.ImageView;

import  java.util.List;

import  cc.mashroom.hedgehog.parent.BaseAdapter;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.module.chat.activity.AudioCallActivity;
import  cc.mashroom.squirrel.module.chat.activity.VideoCallActivity;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.hedgehog.module.common.activity.AlbumMediaMultichoiceActivity;
import  cc.mashroom.hedgehog.module.common.activity.CamcorderActivity;
import  cc.mashroom.squirrel.paip.message.call.CallContentType;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.collection.map.Map;
import  lombok.AccessLevel;
import  lombok.Setter;
import  lombok.experimental.Accessors;

public  class  MoreInputsAdapter  extends  BaseAdapter  implements  View.OnClickListener
{
    public  MoreInputsAdapter( AbstractActivity  context,long  contactId,List<Map<String,Integer>>  inputs )
    {
        super( inputs );

        this.setContext(context).setContactId(contactId).setInputs(  inputs );
    }

    @Setter( value=AccessLevel.PROTECTED )
    @Accessors( chain=true )
    protected  AbstractActivity   context;
    @Setter( value=AccessLevel.PROTECTED )
    @Accessors( chain=true )
    protected  long  contactId;
    @Setter( value=AccessLevel.PROTECTED )
    @Accessors( chain=true )
    protected  List<Map<String,Integer>>  inputs;

    public  View  getView( int  position,View  convertView,ViewGroup  parent )
    {
        convertView = convertView != null ? convertView : LayoutInflater.from(context).inflate( R.layout.activity_chat_more_item,parent,false );

        Map<String,Integer>  input  = ObjectUtils.cast( getItem( position ) );

        ObjectUtils.cast(convertView.findViewById(R.id.image),ImageView.class).setImageResource( input.getInteger("image") );

        ObjectUtils.cast(convertView.findViewById(R.id.image),ImageView.class).setOnClickListener(   this );

        ObjectUtils.cast(convertView.findViewById(R.id.image),ImageView.class).setTag( position );  return  convertView;
    }

    public  void  onClick( View  iconBtn )
    {
        switch( ObjectUtils.cast(iconBtn.getTag(),Integer.class) )
        {
            case  0:
            {
                ActivityCompat.startActivityForResult( context,new  Intent(context,CamcorderActivity.class),0,ActivityOptionsCompat.makeCustomAnimation(context,R.anim.right_in,R.anim.left_out).toBundle() );  break;
            }
            case  1:
            {
                ActivityCompat.startActivityForResult( context,new  Intent(context,AlbumMediaMultichoiceActivity.class).putExtra("MAX_COUNT",3).putExtra("MAX_FILE_SIZE",10*1024*1024L),1,ActivityOptionsCompat.makeCustomAnimation(context,R.anim.right_in,R.anim.left_out).toBundle() );  break;
            }
            case  2:
            {
                ActivityCompat.startActivity( context,new  Intent(context,AudioCallActivity.class).putExtra("CONTACT_ID",contactId).putExtra("CALLED",false).putExtra("CALL_TYPE",CallContentType.AUDIO.getValue()),ActivityOptionsCompat.makeCustomAnimation(context,R.anim.right_in,R.anim.left_out).toBundle() );  break;
            }
            case  3:
            {
                ActivityCompat.startActivity( context,new  Intent(context,VideoCallActivity.class).putExtra("CONTACT_ID",contactId).putExtra("CALLED",false).putExtra("CALL_TYPE",CallContentType.VIDEO.getValue()),ActivityOptionsCompat.makeCustomAnimation(context,R.anim.right_in,R.anim.left_out).toBundle() );  break;
            }
        }
    }
}