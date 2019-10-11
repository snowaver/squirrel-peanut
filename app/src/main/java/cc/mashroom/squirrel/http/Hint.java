package cc.mashroom.squirrel.http;

import  androidx.annotation.ColorRes;
import  androidx.annotation.DrawableRes;
import  androidx.annotation.StringRes;

import  lombok.AllArgsConstructor;
import  lombok.Data;
import  lombok.experimental.Accessors;

@Accessors(chain=true )
@Data
@AllArgsConstructor
public  class  Hint
{
    private  int  code;
    @StringRes
    private  int  hintResId;
    @ColorRes
    private  int  textColorResId;
    @DrawableRes
    private  int  iconResId;
    @ColorRes
    private  int  backgroundColorResId;
}
