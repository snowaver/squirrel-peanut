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
package cc.mashroom.squirrel.module.home.activity;

import  android.os.Bundle;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  lombok.Setter;
import  lombok.experimental.Accessors;

public  class  ContactProfileEditActivity     extends  AbstractActivity
{
	@Accessors( chain = true )
	@Setter
	private  User  user;

	protected  void  onCreate( Bundle  savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		super.setContentView( R.layout.activity_contact_profile_edit );
	}
}