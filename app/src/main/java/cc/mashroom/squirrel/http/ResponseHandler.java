package cc.mashroom.squirrel.http;

import  retrofit2.Call;
import  retrofit2.Response;

public  interface  ResponseHandler
{
    public  void  onResponse( Call  call,Response  response );
}
