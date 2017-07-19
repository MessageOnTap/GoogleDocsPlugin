package edu.cmu.chimps.googledocsplugin;

import android.os.RemoteException;
import android.util.Log;

import edu.cmu.chimps.messageontap_api.PluginData;
import edu.cmu.chimps.messageontap_api.MessageData;
import edu.cmu.chimps.messageontap_api.MessageOnTapPlugin;


public class GoogleDocsPlugin extends MessageOnTapPlugin {

    @Override
    protected PluginData iPluginData() {
        Log.e("plugin", "getting plugin data");
        return new PluginData().trigger("test trigger");
    }

    @Override
    protected void analyzeMessage(MessageData data) {
        Log.e("plugin", "got messagedata: " + data);
        try {
            mManager.sendResponse(data.response("test response"));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
