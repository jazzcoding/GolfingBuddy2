package com.golfingbuddy.ui.mailbox;

import com.google.gson.JsonArray;
import com.golfingbuddy.ui.mailbox.compose.RecipientInterface;

import java.util.ArrayList;

/**
 * Created by kairat on 3/19/15.
 */
public interface AutoCompleteHolderInterface {
    void suggestionListRequest(String constraint, ArrayList<String> idList);
    void onSuggestionListResponse(JsonArray list);
    void onSuggestionClick(RecipientInterface recipient);
    void onSuggestionCancel();
    void onSuggestionClear();

    interface ItemHolderInterface extends RenderInterface {
        void setData(Object data);
        Object getData();
    }
}
