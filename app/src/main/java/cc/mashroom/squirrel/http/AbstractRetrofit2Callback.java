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
package cc.mashroom.squirrel.http;

import  android.app.Activity;

import  androidx.core.content.res.ResourcesCompat;

import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.irozon.sneaker.Sneaker;

import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.hedgehog.util.StyleUnifier;
import  cc.mashroom.squirrel.R;

import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.util.ObjectUtils;
import  lombok.Getter;
import  lombok.Setter;
import  lombok.experimental.Accessors;
import  retrofit2.Call;
import  retrofit2.Callback;
import  retrofit2.Response;

public  abstract    class  AbstractRetrofit2Callback<T>  implements  Callback<T>
{
	public  AbstractRetrofit2Callback( Activity  context,boolean  isShowWaitingProgressDialog )
	{
		this.setContext(context).setWaitingProgressDailog( !isShowWaitingProgressDialog? null : StyleUnifier.unify(new  UIProgressDialog.WeBoBuilder(context).setTextSize(18).setMessage(R.string.waiting).setCanceledOnTouchOutside(false).create(),ResourcesCompat.getFont(context,R.font.droid_sans_mono)).setWidth(DensityUtils.px(context,220)).setHeight(DensityUtils.px(context,150)) );

		if( isShowWaitingProgressDialog   )   this.waitingProgressDailog.show();
	}

	public  AbstractRetrofit2Callback( Activity  context )
	{
		this(context,false );
	}

	@Accessors( chain= true )
	@Setter
	@Getter
	protected  Activity    context;
	@Accessors( chain= true )
	@Setter
	@Getter
	protected  UIProgressDialog     waitingProgressDailog;

	public  void  onResponse( Call<T>  call,Response<T>  response )
	{
		if( waitingProgressDailog != null )
		{
			waitingProgressDailog.cancel();
		}
	}

	public  void  onFailure( Call<T>  call,Throwable  ie )
	{
		ie.printStackTrace();

		if( waitingProgressDailog != null )
		{
			waitingProgressDailog.cancel();
		}

		ObjectUtils.cast(context,AbstractActivity.class).showSneakerWindow( Sneaker.with(context),com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red );
	}
}