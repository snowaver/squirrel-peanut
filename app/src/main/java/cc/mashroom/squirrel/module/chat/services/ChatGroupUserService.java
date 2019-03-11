package cc.mashroom.squirrel.module.chat.services;

import  java.util.List;

import  cc.mashroom.util.collection.map.Map;
import  retrofit2.Call;
import  retrofit2.http.Field;
import  retrofit2.http.FormUrlEncoded;
import  retrofit2.http.POST;

public  interface  ChatGroupUserService
{
	@FormUrlEncoded
	@POST( value="/chat/group/user" )
	public  Call<List<Map<String,Object>>>  invite(@Field(value = "chatGroupId") long chatGroupId, @Field(value = "contactIds") String contactIds);
}
