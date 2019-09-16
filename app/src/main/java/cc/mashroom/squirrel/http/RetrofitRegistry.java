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
package cc.mashroom.squirrel.http;

import  cc.mashroom.squirrel.module.common.services.FileService;
import  cc.mashroom.squirrel.parent.Application;
import  cc.mashroom.util.JsonUtils;

import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.Map;
import  lombok.AccessLevel;
import  lombok.Getter;
import  lombok.NoArgsConstructor;
import  retrofit2.Retrofit;
import  retrofit2.converter.jackson.JacksonConverterFactory;

/**
 *  retrofit  service  registry,  which  cache  all  retrofit  service  used.  http  file  uploading  is  a  heavily  time-consuming  io  operation,  so  seperate  it  from  other  data  request  to  avoid  blocking  data  interaction  by  uploading  file  operations  (long-term  occupancy  of  connections  in  okhttp  connection  pool).
 */
@NoArgsConstructor( access=AccessLevel.PRIVATE )
public  class  RetrofitRegistry
{
	private  Retrofit  fileUploadRetrofit;

	private  Retrofit  dataRetrofit;
	@Getter
	private  Retrofit      fileDownloadRetrofit;

	private  Map<Class<?>,Object>  services = new  ConcurrentHashMap<Class<?>,Object>();

	public   final  static  RetrofitRegistry  INSTANCE = new  RetrofitRegistry();

	public  <T>  T  get( Class<T>  clazz )
	{
		return  (T) services.computeIfLackof(clazz,(key) -> clazz == FileService.class ? fileUploadRetrofit.create(clazz) : dataRetrofit.create(clazz) );
	}

	public  void  initialize( Application  application )
	{
		//  set  http  write  timeout  of  1200  seconds  and  file  size  should  be  considered  anyway.

		this.dataRetrofit = new  Retrofit.Builder().baseUrl(application.baseUrl().build()).client(application.getSquirrelClient().okhttpClient(5,5,10)).addConverterFactory(JacksonConverterFactory.create(JsonUtils.mapper)).build();

		this.fileUploadRetrofit = new  Retrofit.Builder().baseUrl(application.baseUrl().build()).client(application.getSquirrelClient().okhttpClient(5,1200,5)).addConverterFactory(JacksonConverterFactory.create(JsonUtils.mapper)).build();

		application.setFileDownloadRetrofit( this.fileDownloadRetrofit = new  Retrofit.Builder().baseUrl(application.baseUrl().build()).client(application.getSquirrelClient().okhttpClient(5,5,1200)).addConverterFactory(JacksonConverterFactory.create(JsonUtils.mapper)).build() );
	}
}