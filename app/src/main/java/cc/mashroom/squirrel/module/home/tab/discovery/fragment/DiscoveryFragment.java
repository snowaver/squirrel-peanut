package cc.mashroom.squirrel.module.home.tab.discovery.fragment;

import  android.content.Intent;
import  android.os.Bundle;
import  androidx.core.app.ActivityCompat;
import  androidx.core.app.ActivityOptionsCompat;
import  android.view.KeyEvent;
import  android.view.LayoutInflater;
import  android.view.View;
import  android.view.ViewGroup;
import  android.view.inputmethod.EditorInfo;
import  android.widget.EditText;
import  android.widget.ListView;
import  android.widget.TextView;
import  android.widget.Toast;

import  com.aries.ui.widget.progress.UIProgressDialog;
import  com.irozon.sneaker.Sneaker;

import  java.util.List;

import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.hedgehog.util.DensityUtils;
import  cc.mashroom.hedgehog.widget.StyleableEditView;
import  cc.mashroom.squirrel.R;
import  cc.mashroom.squirrel.client.storage.model.user.User;
import  cc.mashroom.squirrel.module.home.activity.SubscribeActivity;
import  cc.mashroom.squirrel.parent.AbstractActivity;
import  cc.mashroom.squirrel.parent.AbstractFragment;
import  cc.mashroom.squirrel.http.AbstractRetrofit2Callback;
import  cc.mashroom.squirrel.http.RetrofitRegistry;
import  cc.mashroom.squirrel.module.home.tab.discovery.adapters.DiscoveryUserListAdapter;
import  cc.mashroom.squirrel.module.common.services.UserService;
import  cc.mashroom.util.ObjectUtils;
import  cc.mashroom.util.StringUtils;
import  cc.mashroom.util.stream.Stream;
import  es.dmoral.toasty.Toasty;
import  retrofit2.Call;
import  retrofit2.Response;

public  class   DiscoveryFragment  extends  AbstractFragment   implements  TextView.OnEditorActionListener
{
	public  View  onCreateView( LayoutInflater  inflater,ViewGroup  container,Bundle  savedInstanceState )
	{
		if( contentView == null )
		{
			contentView = inflater.inflate( R.layout.fragment_discovery,container,false );

			ObjectUtils.cast(contentView.findViewById(R.id.discovery_list),ListView.class).setOnItemClickListener( (parent,view,position,id) -> {User  user = ObjectUtils.cast(parent.getAdapter().getItem(position),User.class);  ActivityCompat.startActivity(super.getActivity(),new  Intent(super.getActivity(),SubscribeActivity.class).putExtra("USER",user),ActivityOptionsCompat.makeCustomAnimation(super.getActivity(),R.anim.right_in,R.anim.left_out).toBundle());} );

			ObjectUtils.cast(contentView.findViewById(R.id.keyword_editor).findViewById(R.id.edit_inputor),EditText.class).setOnEditorActionListener( this );
		}

		return  contentView;
	}

	protected  View  contentView;

	public  boolean  onEditorAction( TextView  view,int  editorActionId, KeyEvent  event )
	{
		if( editorActionId == EditorInfo.IME_ACTION_DONE )
		{
			if( StringUtils.isNotBlank(ObjectUtils.cast(contentView.findViewById(R.id.keyword_editor),StyleableEditView.class).getText().toString().trim()) )
			{
				RetrofitRegistry.get(UserService.class).search(0,ObjectUtils.cast(contentView.findViewById(R.id.keyword_editor),StyleableEditView.class).getText().toString().trim(), "{}").enqueue
				(
					new  AbstractRetrofit2Callback<List<User>>( this.getActivity(),new  UIProgressDialog.WeBoBuilder(this.getActivity()).setTextSize(18).setMessage(R.string.waiting).setCanceledOnTouchOutside(false).create().setHeight(DensityUtils.px(this.getActivity(),140)) )
					{
						public  void  onResponse( Call<List<User>>  call, Response<List<User>>  response )
						{
							super.onResponse( call , response );

							if( response.code()   != 200 )
							{
								ObjectUtils.cast(DiscoveryFragment.this.getActivity(),AbstractActivity.class).showSneakerWindow( Sneaker.with(DiscoveryFragment.this.getActivity()),com.irozon.sneaker.R.drawable.ic_error,R.string.network_or_internal_server_error,R.color.white,R.color.red );
							}
							else
							{
                                if( response.body().isEmpty() )
                                {
                                    Toasty.warning(DiscoveryFragment.this.getActivity(),DiscoveryFragment.this.getString(R.string.search_result_empty),Toast.LENGTH_LONG,false).show();
                                }

                                Stream.forEach( response.body(),(user) -> user.addEntry("ID",Long.parseLong(user.get("ID").toString())) );

                                ObjectUtils.cast(contentView.findViewById(R.id.discovery_list),ListView.class).setAdapter( new  DiscoveryUserListAdapter(DiscoveryFragment.this,response.body()) );
							}
						}
					}
				);
			}
			else
			{
				ObjectUtils.cast(this.getActivity(),AbstractActivity.class).showSneakerWindow( Sneaker.with(this.getActivity()),com.irozon.sneaker.R.drawable.ic_error,R.string.content_empty_error,R.color.white,R.color.red );
			}
		}

		return  false;
	}
}
