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
package cc.mashroom.squirrel.module.system.activity;

import  android.content.Intent;
import  android.graphics.Color;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;

import  android.view.WindowManager;

import  com.aries.ui.widget.alert.UIAlertDialog;

import  java.util.concurrent.TimeUnit;

import  androidx.core.content.res.ResourcesCompat;

import cc.mashroom.hedgehog.util.NetworkUtils;
import  cc.mashroom.hedgehog.util.StyleUnifier;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.module.home.activity.SheetActivity;
import  cc.mashroom.squirrel.util.LocaleUtils;

public  class  LoadingActivity  extends  AbstractActivity  implements  Runnable
{
	protected  void  onCreate( Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		LocaleUtils.change(    this , null );

		super.getWindow().addFlags(    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS );

		super.getWindow().setStatusBarColor( super.getResources().getColor(R.color.gainsboro) );

		super.setContentView(  R.layout.activity_loading );

		super.application().getScheduler().schedule( this,super.getSharedPreferences("LATEST_LOGIN_FORM",MODE_PRIVATE).getLong("ID",0) > 0 ? 5 : 5, TimeUnit.SECONDS );
	}

	public  void  run()
	{
		Long  userId = super.getSharedPreferences("LATEST_LOGIN_FORM",MODE_PRIVATE).getLong( "ID",0 );

		if( userId> 0 )
		{
			super.application().connect( userId, NetworkUtils.getLocation(this),super.application() );
		}

		ActivityCompat.startActivity( this,new  Intent(LoadingActivity.this,userId> 0 ? SheetActivity.class : LoginActivity.class),ActivityOptionsCompat.makeCustomAnimation(LoadingActivity.this,R.anim.fade_in,R.anim.fade_out).toBundle() );  super.finish();
	}
}