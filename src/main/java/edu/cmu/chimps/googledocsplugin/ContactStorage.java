/*
  Copyright 2017 CHIMPS Lab, Carnegie Mellon University
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package edu.cmu.chimps.googledocsplugin;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class ContactStorage {
    public static final String KEY_POSITION = "send_contacts_position";
    public static final String KEY_STORAGE = "save_contacts_file";
    public static final String KEY_ALL_SELECT_STORAGE = "save_contacts_file";

    public static void storeSendUsers(Context context, Set<String> set, String filename){
        SharedPreferences.Editor editor = context.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        editor.putStringSet(KEY_POSITION, set);
        editor.apply();
    }

    public static Set<String> getContacts(Context context, String filename){
        SharedPreferences pref = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        return pref.getStringSet(KEY_POSITION, new HashSet<String>());
    }


}
