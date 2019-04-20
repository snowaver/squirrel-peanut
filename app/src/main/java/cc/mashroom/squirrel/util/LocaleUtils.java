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
package cc.mashroom.squirrel.util;

import  android.content.Context;
import  android.content.SharedPreferences;
import  android.content.res.Configuration;
import  android.content.res.Resources;
import  android.os.Build;

import  java.util.Locale;

import  cc.mashroom.hedgehog.system.LocaleChangeEventDispatcher;
import  cc.mashroom.util.StringUtils;

public  class  LocaleUtils
{
    /**
     *  change  the  application  i18n  language.  store  the  language  tag  and  dispatch  the  language  event  after  language  changed.  use  latest  stored  language  tag  or  default  language  tag  (english)  if  language  tag  parameter  is  blank.
     */
    public  static  void  change( Context  context,String  languageTag )
    {
        Configuration  configuration = context.getResources().getConfiguration();

        Resources  resources = context.getResources();

        SharedPreferences  configurationSharedPreferences = context.getSharedPreferences( "CONFIGURATION",Context.MODE_PRIVATE );

        Locale  locale = Locale.forLanguageTag( StringUtils.isNotBlank(languageTag) ? languageTag : configurationSharedPreferences.getString("LOCALE",Locale.ENGLISH.toLanguageTag()) );

        if( Build.VERSION.SDK_INT < Build.VERSION_CODES.N )
        {
            configuration.locale = locale;
        }
        else
        {
            configuration.setLocale( locale );
        }

        context.getResources().updateConfiguration( configuration,resources.getDisplayMetrics() );

        configurationSharedPreferences.edit().putString("LOCALE",locale.toLanguageTag()).commit();

        LocaleChangeEventDispatcher.onChange(     locale );
    }
}
