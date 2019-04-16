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

import  android.app.Activity;
import  android.content.Intent;
import  android.widget.Toast;

import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.hedgehog.util.ContextUtils;
import  es.dmoral.toasty.Toasty;

public  abstract  class  AbstractActivity  extends  cc.mashroom.hedgehog.parent.AbstractActivity
{
	public  void  putResultDataAndFinish( Activity  context,int  resultCode,Intent  resultData )
	{
		context.setResult( resultCode,resultData );

		context.finish();
	}

	public  void  error( Throwable  e )
	{
		e.printStackTrace();  application().getMainLooperHandler().post( () -> Toasty.error(AbstractActivity.this,e.getMessage(),Toast.LENGTH_LONG,false).show() );  ContextUtils.finish( this );
	}

	public  Application   application()
	{
		return  ObjectUtils.cast( super.getApplication() );
	}

	public  void  onBackPressed()
	{

	}
}