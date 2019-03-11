package cc.mashroom.squirrel.module.chat.services;

import  java.util.List;

import  cc.mashroom.squirrel.client.storage.model.chat.group.ChatGroup;
import  cc.mashroom.util.collection.map.Map;
import  retrofit2.Call;
import  retrofit2.http.Field;
import  retrofit2.http.FormUrlEncoded;
import  retrofit2.http.GET;
import  retrofit2.http.POST;
import  retrofit2.http.Query;

public  interface  ChatGroupService
{
	@FormUrlEncoded
	@POST( value="/chat/group" )
	public  Call<Map<String,List<Map<String,Object>>>>  add(@Field(value = "userId") long userId, @Field(value = "name") String name);

	@GET(  value="/chat/group/search" )
	public  Call<List<ChatGroup>>  search(@Query(value = "action") int action, @Query(value = "keyword") String keyword, @Query(value = "extras", encoded = true) String extras);
}
