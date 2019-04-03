package cc.mashroom.squirrel.module.chat.activity;

import  android.Manifest;
import  android.os.Bundle;

import  androidx.annotation.NonNull;

import  com.aries.ui.widget.alert.UIAlertDialog;
import  com.irozon.sneaker.Sneaker;

import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.squirrel.R;
import  lombok.SneakyThrows;
import  permissions.dispatcher.NeedsPermission;
import  permissions.dispatcher.OnShowRationale;
import  permissions.dispatcher.PermissionRequest;
import  permissions.dispatcher.PermissionUtils;
import  permissions.dispatcher.RuntimePermissions;

@RuntimePermissions

public  class  AudioCallActivity  extends  CallActivity
{
    protected  void  onCreate(Bundle  savedInstanceStateBundle )
    {
        super.onCreate( savedInstanceStateBundle );
    }

    protected  void  onStart()
    {
        super.onStart();

        super.application().getExecutor().execute( () -> AudioCallActivityPermissionsDispatcher.permissionsGrantedWithPermissionCheck(this) );
    }

    public  void  onRequestPermissionsResult( int  requestCode,@NonNull  String[]  permissions,@NonNull  int[]  grantedResults )
    {
        super.onRequestPermissionsResult(requestCode,permissions,grantedResults);

        AudioCallActivityPermissionsDispatcher.onRequestPermissionsResult( this, requestCode, grantedResults );

        if( !PermissionUtils.verifyPermissions(grantedResults) )
        {
            super.showSneakerWindow( new Sneaker(this).setOnSneakerDismissListener(() -> ContextUtils.finish(this)),com.irozon.sneaker.R.drawable.ic_error,cc.mashroom.hedgehog.R.string.permission_denied,R.color.white,R.color.red );
        }
    }

    @NeedsPermission(value={ Manifest.permission.RECORD_AUDIO} )
    @SneakyThrows
    public  void  permissionsGranted()
    {
        super.permissionsGranted();
    }

    @OnShowRationale(value={ Manifest.permission.RECORD_AUDIO} )

    public  void  showPermissionRationale( PermissionRequest  permissionRequest )
    {
        new  UIAlertDialog.DividerIOSBuilder(this).setBackgroundRadius(15).setTitle(cc.mashroom.hedgehog.R.string.notice).setTitleTextSize(18).setMessage(cc.mashroom.hedgehog.R.string.permission_denied).setMessageTextSize(18).setCancelable(false).setCanceledOnTouchOutside(false).setNegativeButtonTextSize(18).setNegativeButton(cc.mashroom.hedgehog.R.string.close,(dialog, which) -> {permissionRequest.cancel();  ContextUtils.finish(this);}).setPositiveButtonTextSize(18).setPositiveButton(cc.mashroom.hedgehog.R.string.ok,(dialog,which) -> permissionRequest.proceed()).create().setWidth((int)  (super.getResources().getDisplayMetrics().widthPixels*0.9)).show();
    }
}
