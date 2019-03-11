package cc.mashroom.squirrel.module.common.services;

import  java.util.List;

import  okhttp3.MultipartBody;
import  retrofit2.Call;
import  retrofit2.http.Multipart;
import  retrofit2.http.POST;
import  retrofit2.http.Part;

public  interface  FileService
{
	@Multipart
	@POST( value="/file" )
	public  Call<Void>  add(@Part List<MultipartBody.Part> files);
}
