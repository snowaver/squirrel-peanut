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

import  cc.mashroom.squirrel.client.SquirrelClient;
import  cc.mashroom.squirrel.module.common.services.FileService;
import  cc.mashroom.squirrel.parent.Application;
import  cc.mashroom.util.JsonUtils;

import  java.util.concurrent.TimeUnit;

import  cc.mashroom.util.NoopHostnameVerifier;
import  cc.mashroom.util.NoopX509TrustManager;
import  cc.mashroom.util.collection.map.ConcurrentHashMap;
import  cc.mashroom.util.collection.map.Map;
import  okhttp3.OkHttpClient;
import  retrofit2.Retrofit;
import  retrofit2.converter.jackson.JacksonConverterFactory;

/**
 *  retrofit  service  registry,  which  cache  all  retrofit  service  used.  http  file  uploading  is  a  heavily  time-consuming  io  operation,  so  seperate  it  from  other  data  request  to  avoid  blocking  data  interaction  by  uploading  file  operations  (long-term  occupancy  of  connections  in  okhttp  connection  pool).
 */
public  class  RetrofitRegistry
{
	private  final  static  Map<Class<?>,Object>  services = new  ConcurrentHashMap<Class<?>,Object>();

	public  static  void  install( Application  application )
	{
		//  set  http  write  timeout  of  120  seconds  while  considering  uploading  files  and  file  size  should  be  considered  anyway.
		RetrofitRegistry.FILE_UPLOAD_RETROFIT = new  Retrofit.Builder().client(new  OkHttpClient.Builder().hostnameVerifier(new  NoopHostnameVerifier()).sslSocketFactory(SquirrelClient.SSL_CONTEXT.getSocketFactory(),new  NoopX509TrustManager()).connectTimeout(2,TimeUnit.SECONDS).writeTimeout(120,TimeUnit.SECONDS).readTimeout(8,TimeUnit.SECONDS).addInterceptor((chain) -> chain.proceed(chain.request().newBuilder().header("SECRET_KEY",application.getSquirrelClient().getUserMetadata().getString("SECRET_KEY") == null ? "" : application.getSquirrelClient().getUserMetadata().getString("SECRET_KEY")).build())).build()).baseUrl(application.baseUrl().toString()).addConverterFactory(JacksonConverterFactory.create(JsonUtils.mapper)).build();

		RetrofitRegistry.COMMON_RETROFIT = new  Retrofit.Builder().client(new  OkHttpClient.Builder().hostnameVerifier(new  NoopHostnameVerifier()).sslSocketFactory(SquirrelClient.SSL_CONTEXT.getSocketFactory(),new  NoopX509TrustManager()).connectTimeout(2,TimeUnit.SECONDS).writeTimeout(2,TimeUnit.SECONDS).readTimeout(8,TimeUnit.SECONDS).addInterceptor((chain) -> chain.proceed(chain.request().newBuilder().header("SECRET_KEY",application.getSquirrelClient().getUserMetadata().getString("SECRET_KEY") == null ? "" : application.getSquirrelClient().getUserMetadata().getString("SECRET_KEY")).build())).build()).baseUrl(application.baseUrl().toString()).addConverterFactory(JacksonConverterFactory.create(JsonUtils.mapper)).build();
	}

	private  static  Retrofit  FILE_UPLOAD_RETROFIT;

	private  static  Retrofit  COMMON_RETROFIT;

	public  static  <T>  T  get( Class< T >  clazz )
	{
		return  (T)  services.computeIfLackof( clazz,(key) -> clazz == FileService.class ? FILE_UPLOAD_RETROFIT.create(clazz) : COMMON_RETROFIT.create(clazz) );
	}
}