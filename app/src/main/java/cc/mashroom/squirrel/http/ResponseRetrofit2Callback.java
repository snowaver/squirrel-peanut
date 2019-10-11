package cc.mashroom.squirrel.http;

import  com.irozon.sneaker.Sneaker;

import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.util.collection.map.HashMap;
import  cc.mashroom.util.collection.map.Map;
import  lombok.NonNull;
import  retrofit2.Call;
import  retrofit2.Response;

public  class  ResponseRetrofit2Callback<T>  extends  AbstractRetrofit2Callback<T>
{
    public  ResponseRetrofit2Callback( AbstractActivity   context     )
    {
        super( context );
    }

    public  ResponseRetrofit2Callback( AbstractActivity   context ,boolean  isShowWaitingDialog )
    {
        super( context, isShowWaitingDialog );
    }

    protected  Map<Integer,Hint>  hints = new  HashMap<Integer,Hint>();

    protected  Map<Integer,ResponseHandler<T>>   responseHandlers = new  HashMap<Integer,ResponseHandler<T>>();

    public  ResponseRetrofit2Callback addResponseHandler( int code,ResponseHandler<T>  responseHandler )
    {
        responseHandlers.put( code , responseHandler );

        return      this;
    }

    public  ResponseRetrofit2Callback  addHint(@NonNull   Hint   hint )
    {
        this.hints.put( hint.getCode(),hint );

        return      this;
    }
    @Override
    public  void  onResponse( Call<T>  call,Response<T>   response    )
    {
        super.onResponse(  call   ,response );

        ResponseHandler  responseHandler = responseHandlers.get(response.code() );

        if( responseHandler != null )
        {
            responseHandler.onResponse( call,response);
        }
        else
        if( this.hints.containsKey(  response.code()) )
        {
            Hint  hint =  hints.get( response.code() );

            this.context.showSneakerWindow( Sneaker.with(this.context),hint.getIconResId(),hint.getHintResId(),hint.getTextColorResId(),hint.getBackgroundColorResId() );
        }
        else
        {
            this.context.showSneakerWindow( Sneaker.with(this.context),com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red );
        }
    }
}
