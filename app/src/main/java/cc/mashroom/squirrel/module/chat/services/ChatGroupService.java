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

import  java.util.List;

import  cc.mashroom.squirrel.client.storage.model.Offline;
import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import  cc.mashroom.util.collection.map.Map;
import  retrofit2.Call;
import  retrofit2.http.Field;
import  retrofit2.http.FormUrlEncoded;
import  retrofit2.http.GET;
import  retrofit2.http.POST;
import  retrofit2.http.PUT;
import  retrofit2.http.Query;

public  interface  ChatGroupService
{
	@FormUrlEncoded
	@POST( value="/chat/group" )
	public  Call<Offline>  add( @Field(value="name")  String  name );

	@FormUrlEncoded
	@PUT(  value="/chat/group" )
	public  Call<Offline>  update( @Field(value="id")  long  chatGroupId,@Field(value="name")  String  name );

	@GET(  value="/chat/group/search" )
	public  Call<List<ChatGroup>>  search( @Query(value="action")  int  action,@Query(value="keyword")  String  keyword,@Query(value="extras",encoded=true)  String  extras );
}