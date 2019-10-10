package cc.mashroom.squirrel.http;

import  com.irozon.sneaker.Sneaker;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  retrofit2.Call;
import  retrofit2.Response;

public  class  ResponseRetrofit2Callback  extends  AbstractRetrofit2Callback
{
    protected  Map<Integer,ResponseHandler>      responseHandlers = new  HashMap<Integer,ResponseHandler>();

    public  ResponseRetrofit2Callback( AbstractActivity   context )
    {
        super( context );
    }

    public  ResponseRetrofit2Callback addResponseHandler( int  code      ,ResponseHandler  responseHandler )
    {
        responseHandlers.put( code , responseHandler );

        return      this;
    }

    public  ResponseRetrofit2Callback( AbstractActivity   context , boolean  isShowWaitingDialog )
    {
        super( context,isShowWaitingDialog );
    }
    @Override
    public  void  onResponse( Call  call    ,  Response   response)
    {
        super.onResponse(  call  ,response );

        ResponseHandler  responseHandler = this.responseHandlers.get( response.code() );

        if( responseHandler != null )
        {
            responseHandler.onResponse(call,response);
        }
        else
        {
            this.context.showSneakerWindow( Sneaker.with(this.context),com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red );
        }
    }
}
