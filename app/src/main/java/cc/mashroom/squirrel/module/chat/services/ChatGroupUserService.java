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
package cc.mashroom.squirrel.module.chat.services;

import  cc.mashroom.squirrel.client.storage.model.OoIData;
import  retrofit2.Call;
import  retrofit2.http.DELETE;
import  retrofit2.http.Field;
import  retrofit2.http.FormUrlEncoded;
import  retrofit2.http.POST;
import  retrofit2.http.Query;

public  interface  ChatGroupUserService
{
	@POST(   value="/chat/group/user" )
	@FormUrlEncoded
	public  Call<OoIData>  add(@Field(value="chatGroupId")  long  chatGroupId, @Field(value="inviteeIds")  String  inviteeIds );

	@DELETE( value="/chat/group/user" )
	public  Call<OoIData>  secede( @Query(value="chatGroupId")  long  chatGroupId,@Query(value="chatGroupUserId")  long  chatGroupUserId );
}