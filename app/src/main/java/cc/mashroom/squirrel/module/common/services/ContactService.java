package cc.mashroom.squirrel.module.common.services;

import  java.util.List;

import  cc.mashroom.squirrel.client.storage.model.user.Contact;
import  retrofit2.Call;
import  retrofit2.http.Field;
import  retrofit2.http.FormUrlEncoded;
import  retrofit2.http.GET;
import  retrofit2.http.PATCH;
import  retrofit2.http.POST;
import  retrofit2.http.Query;

public  interface  ContactService
{
	@GET(   value="/contact/search" )
	public  Call<List<Contact>>  search(@Query(value = "action") int action, @Query(value = "keyword") String keyword, @Query(value = "extras", encoded = true) String extras);

	@FormUrlEncoded
	@POST(  value="/contact/status" )
	public  Call<Void>  subscribe(@Field(value = "subscriberId") long subscriberId, @Field(value = "subscribeeId") long subscribeeId, @Field(value = "remark") String remark, @Field(value = "group") String group);

	@FormUrlEncoded
	@PATCH( value="/contact/status" )
	public  Call<Void>  changeSubscribeStatus(@Field(value = "status") int status, @Field(value = "subscriberId") long subscriberId, @Field(value = "subscribeeId") long subscribeeId, @Field(value = "remark") String remark, @Field(value = "group") String group);

	@FormUrlEncoded
	@PATCH( value="/contact"    )
	public  Call<Void>  update(@Field(value = "userId") long userId, @Field(value = "contactId") long contactId, @Field(value = "remark") String remark, @Field(value = "group") String group);

}
