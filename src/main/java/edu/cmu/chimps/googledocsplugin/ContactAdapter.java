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

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

import static edu.cmu.chimps.googledocsplugin.Contact.SelectedItemCount;
import static edu.cmu.chimps.googledocsplugin.Contact.contactList;
import static edu.cmu.chimps.googledocsplugin.Contact.toggleFlag;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private List<Contact> mContactList;
    protected Toolbar mToolbar;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View contactView;
        ImageView contactImage;
        TextView contactName;
        LinearLayout contactLayout;
        CheckBox contactCheckBox;

        public ViewHolder(View itemView) {
            super(itemView);
            contactView = itemView;
            contactLayout = itemView.findViewById(R.id.linearLayout);
            contactImage =  itemView.findViewById(R.id.contact_image);
            contactName =  itemView.findViewById(R.id.contact_name);
            contactCheckBox = itemView.findViewById(R.id.contact_checkbox);
        }
    }


    public ContactAdapter(List<Contact> mContactList, Toolbar toolbar) {
        this.mContactList = mContactList;
        this.mToolbar = toolbar;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.contactView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (SelectedItemCount()==contactList.size()){
                    GoogleDocsSettingsActivity.iconChangeListener.onChange(false);
                }
                Contact contact = mContactList.get(position);
                toggleFlag(contact);
                String title = " " + SelectedItemCount() + " selected";
                mToolbar.setSubtitle(title);
                if (SelectedItemCount()== contactList.size()){
                    Log.e("Test", "Listener sent");
                    GoogleDocsSettingsActivity.iconChangeListener.onChange(true);
                }


                SetSelection(holder, contact);
                //Toast.makeText(view.getContext(), "click " + "position:"+position, Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Contact contact = mContactList.get(position);
        holder.contactImage.setImageDrawable(contact.getContactPicture());
        holder.contactName.setText(contact.getName());
        SetSelection(holder, contact);
    }

    @Override
    public int getItemCount() {
        return mContactList.size();
    }


    public  static void SetSelection(ViewHolder holder, Contact contact){
        if (contact.isFlag()){
            holder.contactLayout.setSelected(true);
            holder.contactCheckBox.setChecked(true);
        }else {
            holder.contactLayout.setSelected(false);
            holder.contactCheckBox.setChecked(false);
        }
    }

    public static void SetAllSelection(Boolean selection, RecyclerView recyclerView){
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            Contact.SetAllFlag(selection);
            holder.contactLayout.setSelected(selection);
            holder.contactCheckBox.setChecked(selection);
            Log.i("iiii", "SetAllSelection: ");
        }
    }
    
    public static void SetAllSavedSelection(RecyclerView recyclerView){
        Set<String> set = ContactStorage.getContacts(GoogleDocApplication.getAppContext(), ContactStorage.ALLSELECTSTORAGE);
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            holder.contactLayout.setSelected(false);
            holder.contactCheckBox.setChecked(false);
        }
        Log.i("iiii", "SetAllSavedSelection: enter");
        if (set.size() == 0){
            SetAllSelection(false,recyclerView);
        } else{
            for (String str: set) {
                Log.i("iiii", "SetAllSavedSelection:111 ");
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    ViewHolder holder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                    Log.i("iiii", "SetAllSavedSelection: "+holder.contactName.getText());
                    if (str.equals(holder.contactName.getText())){
                        holder.contactLayout.setSelected(true);
                        holder.contactCheckBox.setChecked(true);
                    }
                    Log.i("iiii", "SetAllSelection:  completed");
                }
            }
        }

    }

}
