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
import android.graphics.Color;

import com.amulyakhare.textdrawable.TextDrawable;
import com.github.privacystreams.core.Item;
import com.github.privacystreams.core.UQI;
import com.github.privacystreams.core.exceptions.PSException;
import com.github.privacystreams.core.purposes.Purpose;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class Contact {
    private String Name;
    private boolean isFlag;
    public static ArrayList<Contact> contactList =  new ArrayList<>();

    public Contact(String Name){
        this.Name = Name;
    }

    public String getName() {
        return Name;
    }

    public boolean isFlag(){
        return isFlag;
    }

    public void setFlag(boolean flag){
        isFlag = flag;
    }

    public static ArrayList<Contact> getWhatsAppContacts(Context context) throws PSException {

        UQI uqi = new UQI(context);
        ArrayList<Contact> result = new ArrayList<>();
        List<Item> whatsAppC= uqi.getData(com.github.privacystreams.communication.Contact.getWhatAppAll(), Purpose.UTILITY("get whatsapp contacts"))
                .asList();
        for(int i = 0; i < whatsAppC.size();i++){
            Contact contact = new Contact(whatsAppC.get(i).getValueByField(com.github.privacystreams.communication.Contact.NAME)
                    .toString());
            result.add(i, contact);

        }
        return result;
    }

    public char getFirstC(){
        return this.Name.charAt(0);
    }
    public TextDrawable getContactPicture(){
        return TextDrawable.builder()
                .buildRound(String.valueOf(getFirstC()), Color.GRAY);
    }

    public static int SelectedItemCount(){
        int count = 0;
        for (int i=0; i<contactList.size(); i++){
            if (contactList.get(i).isFlag()){
                count++;
            }
        }
        return count;
    }

    public  static void SetAllFlag(Boolean flag){
        for (int i = 0; i < contactList.size(); i++) {
            contactList.get(i).setFlag(flag);
        }
    }

    public static void toggleFlag(Contact contact){
        if (contact.isFlag()){
            contact.setFlag(false);
        } else {
            contact.setFlag(true);
        }
    }

    public static ArrayList<String> getSavedContactList(){
        ArrayList<String> savedContactList = new ArrayList<>();
        for (int i = 0; i < Contact.contactList.size(); i++){
            if (Contact.contactList.get(i).isFlag()){
                savedContactList.add(Contact.contactList.get(i).getName());
            }
        }
        return savedContactList;
    }

    public static void InitFlag(Context context, String filename){
        Set<String> set = ContactStorage.getContacts(context, filename);
        SetAllFlag(false);
        if (set.size() != 0){
            for (String str: set){
                for (Contact contact: Contact.contactList){
                    if (str.equals(contact.getName())){
                        contact.setFlag(true);
                        //Toast.makeText(context, "selecte completed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

}
