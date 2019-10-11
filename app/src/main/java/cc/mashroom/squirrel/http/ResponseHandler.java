package cc.mashroom.squirrel.http;

import  retrofit2.Call;
import  retrofit2.Response;

public  interface  ResponseHandler<T>
{
    public  void  onResponse( Call<T>  call,Response<T>  response );
}
