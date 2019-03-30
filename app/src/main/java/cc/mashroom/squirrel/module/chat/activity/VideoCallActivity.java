package cc.mashroom.squirrel.module.chat.activity;

import  android.Manifest;
import  android.os.Bundle;
import  android.widget.Toast;

import  androidx.annotation.NonNull;

import  com.aries.ui.widget.alert.UIAlertDialog;

import  cc.mashroom.hedgehog.util.ContextUtils;
import  cc.mashroom.squirrel.R;
import  es.dmoral.toasty.Toasty;
import  lombok.SneakyThrows;
import  permissions.dispatcher.NeedsPermission;
import  permissions.dispatcher.OnShowRationale;
import  permissions.dispatcher.PermissionRequest;
import  permissions.dispatcher.PermissionUtils;
import  permissions.dispatcher.RuntimePermissions;

@RuntimePermissions

public  class  VideoCallActivity  extends  CallActivity
{
    protected  void  onCreate(Bundle  savedInstanceStateBundle )
    {
        super.onCreate( savedInstanceStateBundle );
    }

    protected  void  onStart()
    {
        super.onStart();

        super.application().getExecutor().execute( () -> VideoCallActivityPermissionsDispatcher.permissionsGrantedWithPermissionCheck(this) );
    }

    public  void  onRequestPermissionsResult( int  requestCode,@NonNull  String[]  permissions,@NonNull  int[]  grantedResults )
    {
        super.onRequestPermissionsResult(requestCode,permissions,grantedResults);

        VideoCallActivityPermissionsDispatcher.onRequestPermissionsResult( this, requestCode, grantedResults );

        if( !PermissionUtils.verifyPermissions(grantedResults) )
        {
            Toasty.error(this,super.getString(R.string.permission_grant_error),Toast.LENGTH_LONG,false).show();  super.finish();
        }
    }

    @NeedsPermission( value = {Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA} )
    @SneakyThrows
    public  void  permissionsGranted()
    {
        super.permissionsGranted();
    }

    @OnShowRationale( value = {Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA} )

    public  void  showPermissionRationale( PermissionRequest  permissionRequest )
    {
        new  UIAlertDialog.DividerIOSBuilder(this).setBackgroundRadius(15).setTitle(cc.mashroom.hedgehog.R.string.notice).setTitleTextSize(18).setMessage(cc.mashroom.hedgehog.R.string.camcorder_permission_check).setMessageTextSize(18).setCancelable(false).setCanceledOnTouchOutside(false).setNegativeButtonTextSize(18).setNegativeButton(cc.mashroom.hedgehog.R.string.close,(dialog, which) -> {permissionRequest.cancel();  ContextUtils.finish(this);}).setPositiveButtonTextSize(18).setPositiveButton(cc.mashroom.hedgehog.R.string.ok,(dialog,which) -> permissionRequest.proceed()).create().setWidth((int)  (super.getResources().getDisplayMetrics().widthPixels*0.9)).show();
    }
}
