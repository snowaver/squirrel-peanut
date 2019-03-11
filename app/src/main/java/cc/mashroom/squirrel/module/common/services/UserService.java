package cc.mashroom.squirrel.module.common.services;

import  java.util.List;

import  cc.mashroom.squirrel.client.storage.model.user.User;
import  okhttp3.MultipartBody;
import  okhttp3.RequestBody;
import  retrofit2.Call;
import  retrofit2.http.Field;
import  retrofit2.http.FormUrlEncoded;
import  retrofit2.http.GET;
import  retrofit2.http.Multipart;
import  retrofit2.http.POST;
import  retrofit2.http.Part;
import  retrofit2.http.Query;

public  interface  UserService
{
	@GET(  value="/user/search" )
	public  Call<List<User>>  search(@Query(value = "action") int action, @Query(value = "keyword") String keyword, @Query(value = "extras") String extras);

	@GET(  value="/user" )
	public  Call<User>  get(@Query(value = "userId") long userId);

	@FormUrlEncoded
	@POST( value="/user/logout" )
	public  Call<Void>  logout(@Field(value = "userId") String userId);

	@Multipart
	@POST( value="/user" )
	public  Call<Void>  register(@Part("user") RequestBody user, @Part MultipartBody.Part portrait);
}
