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
package cc.mashroom.squirrel.parent;

import  android.os.Bundle;
import  android.widget.Toast;

import  androidx.annotation.Nullable;

import  java.util.Locale;

import  cc.mashroom.hedgehog.system.LocaleChangeEventDispatcher;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.hedgehog.util.ContextUtils;
import  es.dmoral.toasty.Toasty;

public  abstract  class  AbstractActivity  extends  cc.mashroom.hedgehog.parent.AbstractActivity  implements  LocaleChangeEventDispatcher.LocaleChangeListener
{
	public  Application   application()
	{
		return  ObjectUtils.cast( super.getApplication() );
	}
	@Override
	public  void  onCreate(    @Nullable  Bundle  savedInstanceState )
	{
		LocaleChangeEventDispatcher.addListener(    this );

		super.onCreate( savedInstanceState );
	}

	public  void  onBackPressed()
	{

	}
	@Override
	public  void  onChange( Locale  locale  )
	{

	}

	public  void  error( Throwable  e )
	{
		super.error(  e );  super.application().getMainLooperHandler().post( () -> Toasty.error(AbstractActivity.this,e.getMessage(),Toast.LENGTH_LONG,false).show() );  ContextUtils.finish( this );
	}
	@Override
	public  void  onDestroy()
	{
		super.onDestroy();

		LocaleChangeEventDispatcher.removeListener( this );
	}
}