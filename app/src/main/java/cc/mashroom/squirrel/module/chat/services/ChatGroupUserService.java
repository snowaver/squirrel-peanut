package cc.mashroom.squirrel.module.chat.services;

import  java.util.List;

import  cc.mashroom.util.collection.map.Map;
import  retrofit2.Call;
import  retrofit2.http.Field;
import  retrofit2.http.FormUrlEncoded;
import  retrofit2.http.POST;

public  interface  ChatGroupUserService
{
	@POST( value="/chat/group/user" )
	@FormUrlEncoded
	public  Call<Map<String,List<Map<String,Object>>>>  add( @Field(value="chatGroupId")  long  chatGroupId,@Field(value="inviteeIds") String  inviteeIds );
}
