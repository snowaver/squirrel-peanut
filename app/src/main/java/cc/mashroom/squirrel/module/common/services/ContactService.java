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
package cc.mashroom.squirrel.module.common.services;

import  java.util.List;

import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  retrofit2.Call;
import  retrofit2.http.DELETE;
import  retrofit2.http.Field;
import  retrofit2.http.FormUrlEncoded;
import  retrofit2.http.GET;
import  retrofit2.http.POST;
import  retrofit2.http.PUT;
import  retrofit2.http.Query;

public  interface  ContactService
{
	@GET(  value="/contact/search" )
	public  Call<List<Contact>>  search( @Query(value="action")  int  action,@Query(value="keyword")  String  keyword,@Query(value="extras",encoded=true)  String  extras );

	@FormUrlEncoded
	@POST( value="/contact/status" )
	public  Call<Contact>  subscribe( @Field(value="subscribeeId")  long  subscribeeId,@Field(value="remark")  String  remark,@Field(value = "group")  String  group );

	@FormUrlEncoded
	@PUT(  value="/contact/status" )
	public  Call<Contact>  changeSubscribeStatus( @Field(value="status")  int  status,@Field(value="subscriberId")  long  subscriberId,@Field(value="remark")  String  remark,@Field(value="group")  String  group );

	@DELETE(  value="/contact/status" )
	public  Call<Contact>  unsubscribe( @Field(value="unsubscribeeId")  long  unsubscribeeId );

	@FormUrlEncoded
	@PUT( value="/contact" )
	public  Call<Contact>  update( @Field(value="contactId")  long  contactId,@Field(value="remark")  String  remark,@Field(value="group")  String  group );

}