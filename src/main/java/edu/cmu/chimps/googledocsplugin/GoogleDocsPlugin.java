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

import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.cmu.chimps.messageontap_api.JSONUtils;
import edu.cmu.chimps.messageontap_api.MessageOnTapPlugin;
import edu.cmu.chimps.messageontap_api.MethodConstants;
import edu.cmu.chimps.messageontap_api.ParseTree;
import edu.cmu.chimps.messageontap_api.SemanticTemplate;
import edu.cmu.chimps.messageontap_api.ServiceAttributes;
import edu.cmu.chimps.messageontap_api.Tag;

import static edu.cmu.chimps.googledocsplugin.GoogleDocUtils.getHtml;
import static edu.cmu.chimps.googledocsplugin.GoogleDocUtils.getTimeString;
import static edu.cmu.chimps.messageontap_api.ParseTree.Direction;



public class GoogleDocsPlugin extends MessageOnTapPlugin {

    public static final String TAG = "GoogleDoc plugin";
    private HashMap<Long, Long> mTidFindAllDocName = new HashMap<>();
    private HashMap<Long, Long> mTidFindDocName = new HashMap<>();
    private HashMap<Long, Long> mTidFindUrl1 = new HashMap<>();
    private HashMap<Long, Long> mTidFindUrl2 = new HashMap<>();
    private HashMap<Long, Long> mTidBubble = new HashMap<>();
    private HashMap<Long, Long> mTidDetails = new HashMap<>();
    private HashMap<Long, Long> mTidDocSend = new HashMap<>();

    private HashMap<Long, ParseTree> mTree1 = new HashMap<>();
    private HashMap<Long, ParseTree> mTree2 = new HashMap<>();
    private HashMap<Long, ParseTree> mTreeForSearch1 = new HashMap<>();
    private HashMap<Long, ParseTree> mTreeForSearch2 = new HashMap<>();
    private HashMap<Long, String> mDocTime1 = new HashMap<>();
    private HashMap<Long, String> mDocTime2 = new HashMap<>();
    private HashMap<Long, StringBuilder> mSelectedDocUrl = new HashMap<>();

    public static final String SEMANTIC_TEMPLATE_DOC_REQUEST = "doc_request";


    @Override
    protected Set<SemanticTemplate> semanticTemplates() {
        Set<SemanticTemplate> templates = new HashSet<>();
        /*
         * Semantic template I: incoming document request.
         */
        Set<Tag> tags = new HashSet<>();
        Set<String> reSet = new HashSet<>();
        reSet.add("share");
        reSet.add("send");
        reSet.add("give");
        reSet.add("show");
        tags.add(new Tag("tag_send", reSet, Tag.Type.MANDATORY));

        reSet.clear();
        reSet.add("file");
        reSet.add("doc");
        reSet.add("document");
        tags.add(new Tag("tag_document", reSet, Tag.Type.MANDATORY));
        tags.add(new Tag(ServiceAttributes.Internal.TAG_TIME, new HashSet<String>(), Tag.Type.OPTIONAL));
        templates.add(new SemanticTemplate().name(SEMANTIC_TEMPLATE_DOC_REQUEST)
                .tags(tags)
                .direction(Direction.INCOMING));

        /*
         * Semantic template II: outgoing document request.
         */
        tags.clear();
        reSet.clear();
        reSet.add("share");
        reSet.add("send");
        reSet.add("give");
        reSet.add("show");
        tags.add(new Tag("tag_send", reSet, Tag.Type.MANDATORY));

        reSet.clear();
        reSet.add("file");
        reSet.add("doc");
        reSet.add("document");
        tags.add(new Tag("tag_document", reSet, Tag.Type.MANDATORY));

        templates.add(new SemanticTemplate().name(SEMANTIC_TEMPLATE_DOC_REQUEST)
                .tags(tags)
                .direction(Direction.OUTGOING));


        reSet.clear();
        tags.clear();


        return templates;
    }

    @Override
    protected void initNewSession(long sid, HashMap<String, Object> params) throws Exception {
        Log.e(TAG, "Session created here!");
        Log.e(TAG, JSONUtils.hashMapToString(params));
        // TID is something we might need to implement stateflow inside a plugin.

        /*
         * Divide all triggers into two groups, include those whose message contains a whole DocName
         * and those whose message only contains terms like doc or file.
         * No matter which group was triggered, plugin is requested to query twice. In the first time
         * the root is DocName, and in the second time the root is DocUrl.
         * The difference between two groups is, if the message contains DocName, plugin have to
         * query all the user's DocNames, and judge whether the message contains one of them, after that can
         * the plugin step forward.
         */
        if ((ContactsReceiver.contactList==null)
                || !ContactsReceiver.contactList.contains((String)params.get(ServiceAttributes.Internal.CURRENT_MESSAGE_CONTACT_NAME))){
            //Toast.makeText(this, ContactsReceiver.contactList.toString(), Toast.LENGTH_SHORT).show();
            try{
                Log.e(TAG, " contact not matched ..... contactList is " + ContactsReceiver.contactList.toString());
            } catch (Exception e){
                Log.e(TAG, " contact not matched ..... contactlist is empty");
            }
            endSession(sid);
        }
        Log.e(TAG, "initNewSession: contact matched");
        if (params.get(ServiceAttributes.Internal.TRIGGER_SOURCE).equals("doc_trigger_one")||
                params.get(ServiceAttributes.Internal.TRIGGER_SOURCE).equals("doc_trigger_two")){

            mTree1.put(sid, (ParseTree)JSONUtils.jsonToSimpleObject((String)params
                    .get(ServiceAttributes.Internal.PARSE_TREE), ParseTree.class));

            try{
                mDocTime1.put(sid, getTimeString(params));
            } catch (Exception e){
                mDocTime1.put(sid, "");
            }

//            mTreeForSearch1.put(sid, addNameRoot(mTree1.get(sid), ALL_DOC_NAME_ROOT_ID, mDocTime1.get(sid), tag_time));
            params.remove(ServiceAttributes.Internal.PARSE_TREE);

            params.put(ServiceAttributes.Internal.PARSE_TREE,
                    JSONUtils.simpleObjectToJson(mTreeForSearch1.get(sid), ParseTree.class));

            mTidFindAllDocName.put(sid, createTask(sid, MethodConstants.GRAPH_TYPE,
                    MethodConstants.GRAPH_METHOD_RETRIEVE, params));
        } else {
            mTree2.put(sid, (ParseTree)JSONUtils.jsonToSimpleObject((String)params
                    .get(ServiceAttributes.Internal.PARSE_TREE), ParseTree.class));

            Log.e(TAG, "initNewSession:    original mTree2 is : " + params.get(ServiceAttributes.Internal.PARSE_TREE).toString());

            try{
                mDocTime2.put(sid, getTimeString(params));
            } catch (Exception e){
                mDocTime2.put(sid, "");
            }

            final int timeNodeID = 1567;
            final int nameNodeID = 3726;
            ParseTree.Node timeNode = new ParseTree.Node();
            timeNode.setWord(mDocTime2.get(sid));
            Log.e(TAG,getTimeString(params));
            Set<String> set = new HashSet<>();
            set.add(ServiceAttributes.Graph.Document.CREATED_TIME);
            timeNode.setTagList(set);
            timeNode.setId(timeNodeID);
            timeNode.setParentId(nameNodeID);
            ParseTree.Node nameNode = new ParseTree.Node();


            Set<String> set2 = new HashSet<>();
            set2.add(ServiceAttributes.Graph.Document.TITLE);
            nameNode.setTagList(set2);
            nameNode.setId(nameNodeID);
            nameNode.setParentId(-1);

            Set<Integer> set3 = new HashSet<>();
            set3.add(timeNodeID);
            nameNode.setChildrenIds(set3);

            SparseArray<ParseTree.Node> array = new SparseArray<>();
            array.put(timeNodeID, timeNode);
            array.put(nameNodeID, nameNode);
            mTree2.get(sid).setNodeList(array);

//            mTreeForSearch2.put(sid, AddNameRoot(mTree2.get(sid), FILTERED_DOCNAME_ROOT_ID, mDocTime2.get(sid), tag_time));
//            params.remove(ServiceAttributes.PMS.PARSE_TREE);

            params.put(ServiceAttributes.Internal.PARSE_TREE,
                    JSONUtils.simpleObjectToJson(mTree2.get(sid), ParseTree.class));

            mTidFindDocName.put(sid, createTask(sid, MethodConstants.GRAPH_TYPE,
                    MethodConstants.GRAPH_METHOD_RETRIEVE, params));
        }
    }

    @Override
    protected void newTaskResponded(long sid, long tid, HashMap<String, Object> params) throws Exception {
        Log.e(TAG, "Got task response!");
        Log.e(TAG, "params is : " + JSONUtils.hashMapToString(params));

        ArrayList<Doc> DocList = new ArrayList<>();
        if (tid == mTidFindAllDocName.get(sid)) {
            //getCardMessage and put it into params
            try {
//                ArrayList<HashMap<String, Object>> cardList = (ArrayList<HashMap<String, Object>>)
//                        JSONUtils.jsonToSimpleObject((String)params.get(ServiceAttributes.Graph.CARD_LIST), ) ;
//                for (HashMap<String, Object> card : cardList) {
//                    for (int i = 0; i < mTree1.get(sid).getNodeList().size(); i++) {
//                        Doc doc = new Doc();
//                        doc.setDocName((String) card.get(ServiceAttributes.Graph.Document.TITLE));
//                        doc.setCreatedTime((Long) card.get(ServiceAttributes.Graph.Document.CREATED_TIME));
//                        DocList.add(doc);
//                    }
//                }
//                if (!DocList.isEmpty()) {
//                    mTree1.put(sid, addUrlRoot(mTree1.get(sid), ALL_URL_ROOT_ID, mDocTime1.get(sid), tag_time));
//                    params.remove(ServiceAttributes.Internal.PARSE_TREE);
//                    params.put(ServiceAttributes.Internal.PARSE_TREE, mTree1);
//                    mTidFindUrl1.put(sid, createTask(sid, MethodConstants.GRAPH_TYPE,
//                            MethodConstants.GRAPH_METHOD_RETRIEVE, params));
//                }
            } catch (Exception e) {
                e.printStackTrace();
                endSession(sid);
            }
        } else if (tid == mTidFindDocName.get(sid)) {
            try {
                ArrayList<HashMap<String, Object>> cardList =
                        (ArrayList<HashMap<String, Object>>) params.get(ServiceAttributes.Graph.CARD_LIST);
                for (HashMap<String, Object> card : cardList) {
                    Doc doc = new Doc();
                    doc.setDocName((String) card.get(ServiceAttributes.Graph.Document.TITLE));
                    doc.setCreatedTime((Long) card.get(ServiceAttributes.Graph.Document.CREATED_TIME));
                    DocList.add(doc);
                }
                if (!DocList.isEmpty()) {
//                    mTree2.put(sid, addUrlRoot(mTree2.get(sid), FILTERED_URL_ROOT_ID, mDocTime2.get(sid), tag_time));
                    params.remove(ServiceAttributes.Internal.PARSE_TREE);
                    params.put(ServiceAttributes.Internal.PARSE_TREE, mTree2);
                    mTidFindUrl2.put(sid, createTask(sid, MethodConstants.GRAPH_TYPE,
                            MethodConstants.GRAPH_METHOD_RETRIEVE, params));
                }
            } catch (Exception e) {
                e.printStackTrace();
                endSession(sid);
            }
        }

        if ((tid == mTidFindUrl1.get(sid))||(tid == mTidFindUrl2.get(sid))){
            try{
                ArrayList<HashMap<String, Object>> cardList =
                        (ArrayList<HashMap<String, Object>>) params.get(ServiceAttributes.Graph.CARD_LIST);
                for (HashMap<String, Object> card : cardList) {
                    for (Doc doc : DocList){
                        if (doc.getCreatedTime().equals(card.get(ServiceAttributes.Graph.Document.CREATED_TIME))){
                            doc.setDocUrl((String)card.get(ServiceAttributes.Graph.Document.TITLE));           //Todo:change to URL
                        }
                    }
                }
                if (!DocList.isEmpty()) {
                    //params.put(BUBBLE_FIRST_LINE, "Show GoogleDocs name");
                    mTidBubble.put(sid, createTask(sid, MethodConstants.UI_TYPE,
                            MethodConstants.UI_METHOD_SHOW_BUBBLE, params));
                }
            } catch (Exception e) {
                e.printStackTrace();
                endSession(sid);
            }
        }

        if (tid == mTidBubble.get(sid)) {
            if ((Integer)params.get(ServiceAttributes.UI.STATUS) == 1) {
                try {
                    params.put("HTML Details", getHtml(DocList));
                    mTidDetails.put(sid, createTask(sid, MethodConstants.UI_TYPE,
                    MethodConstants.UI_METHOD_LOAD_WEBVIEW, params));
                } catch (Exception e) {
                    e.printStackTrace();
                    endSession(sid);
                }
            } else {
                endSession(sid);
            }
        } else if (tid == mTidDetails.get(sid)){
            //get selected URL
            for (Doc doc:DocList){
            String status = (String) params.get(doc.getDocName());
                if (status.equals("on")){
                    mSelectedDocUrl.get(sid).append(doc.getDocUrl());
                }
            }
            params.put(ServiceAttributes.Action.SET_TEXT_EXTRA_MESSAGE, mSelectedDocUrl.toString());                      //send URL
            mTidDocSend.put(sid, createTask(sid, MethodConstants.ACTION_TYPE,
            ServiceAttributes.Action.SET_TEXT_EXTRA_MESSAGE, params));
        } else if (tid == mTidDocSend.get(sid)) {
            Log.e(TAG, "Ending session (triggerListShow)");
            endSession(sid);
            Log.e(TAG, "Session ended");
        }

    }

    @Override
    protected void endSession(long sid) {
        mTidFindAllDocName.remove(sid); mTidFindDocName.remove(sid); mTidFindUrl1.remove(sid);
        mTidFindUrl2.remove(sid); mTidBubble.remove(sid); mTidDetails.remove(sid); mTidDocSend.remove(sid);
        mTree1.remove(sid); mTree2.remove(sid); mTreeForSearch1.remove(sid); mTreeForSearch2.remove(sid);
        mDocTime1.remove(sid); mDocTime2.remove(sid); mSelectedDocUrl.remove(sid);
        super.endSession(sid);
    }
}


