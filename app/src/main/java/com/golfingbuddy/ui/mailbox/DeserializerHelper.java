package com.golfingbuddy.ui.mailbox;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.golfingbuddy.ui.mailbox.chat.model.ConversationHistory;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.ui.mailbox.mail.image.MailImage;

import java.lang.reflect.Type;

/**
 * Created by kairat on 4/21/15.
 */
public class DeserializerHelper {
    private static DeserializerHelper mInstance;

    public static DeserializerHelper getInstance() {
        if (mInstance == null) {
            synchronized (DeserializerHelper.class) {
                mInstance = new DeserializerHelper();
            }
        }

        return mInstance;
    }

    public ConversationList getConversationList(JsonObject object) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        // adding winks to the list of conversations
        gsonBuilder.registerTypeAdapter(ConversationList.class, new WinksDeserializer());
        Gson gson = gsonBuilder.create();
        ConversationList list = gson.fromJson(object, ConversationList.class);

        gsonBuilder.registerTypeAdapter(ConversationList.class, new ConversationDeserializer(list));
        gson = gsonBuilder.create();

        list = gson.fromJson(object, ConversationList.class);

        return list;
    }

    public ConversationHistory getConversationHistory(JsonObject object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ConversationHistory.class, new ConversationHistoryDeserializer());
        Gson gson = gsonBuilder.create();

        return gson.fromJson(object, ConversationHistory.class);
    }

    public MailImage getMailImage(JsonObject object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MailImage.class, new MailDeserializer());

        return gsonBuilder.create().fromJson(object, MailImage.class);
    }

    private class ConversationDeserializer implements JsonDeserializer<ConversationList> {

        private ConversationList conversationList;

        public ConversationDeserializer(ConversationList conversationList) {
            this.conversationList = conversationList;
        }

        @Override
        public ConversationList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject tmpJsonObject = json.getAsJsonObject();
            JsonArray list;

            if (!tmpJsonObject.has("list") || !tmpJsonObject.get("list").isJsonArray() || (list = tmpJsonObject.getAsJsonArray("list")).size() == 0) {
                return conversationList;
            }

            if( conversationList == null )
            {
                conversationList = new ConversationList();
            }

            Gson gson = new Gson();

            for (JsonElement jsonElement : list) {
                try {
                    ConversationList.ConversationItem item = gson.fromJson(jsonElement, ConversationList.ConversationItem.class);
                    conversationList.addOrUpdateConversationItem(item);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }

            return conversationList;
        }
    }

    private class WinksDeserializer implements JsonDeserializer<ConversationList> {
        @Override
        public ConversationList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject tmpJsonObject = json.getAsJsonObject();
            JsonArray list;

            if (!tmpJsonObject.has("winks") || !tmpJsonObject.get("winks").isJsonArray() || (list = tmpJsonObject.getAsJsonArray("winks")).size() == 0) {
                return null;
            }

            ConversationList conversationList = new ConversationList();
            Gson gson = new Gson();

            for (JsonElement jsonElement : list) {
                try {
                    ConversationList.ConversationItem item = gson.fromJson(jsonElement, ConversationList.ConversationItem.class);
                    conversationList.addOrUpdateConversationItem(item);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }

            conversationList.size();

            return conversationList;
        }
    }

    private class ConversationHistoryDeserializer implements JsonDeserializer<ConversationHistory> {
        @Override
        public ConversationHistory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject tmpJsonObject = json.getAsJsonObject();
            JsonArray list;

            if (!tmpJsonObject.has("list") || !tmpJsonObject.get("list").isJsonArray() || (list = tmpJsonObject.getAsJsonArray("list")).size() == 0) {
                return null;
            }

            ConversationHistory conversationHistory = new ConversationHistory();
            Gson gson = new Gson();

            for (JsonElement jsonElement : list) {
                try {
                    JsonObject tmpMessage = jsonElement.getAsJsonObject();
                    String date = tmpMessage.get("dateLabel").getAsString();
                    ConversationHistory.DailyHistory dailyHistory = conversationHistory.getDailyHistoryByDate(date);

                    if (dailyHistory == null) {
                        dailyHistory = new ConversationHistory.DailyHistory(date);
                        conversationHistory.addHistory(dailyHistory);
                    }

                    ConversationHistory.Messages.Message message = gson.fromJson(jsonElement, ConversationHistory.Messages.Message.class);
                    dailyHistory.addMessage(message);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }

            return conversationHistory;
        }
    }

    private class MailDeserializer implements JsonDeserializer<MailImage> {
        @Override
        public MailImage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject tmpJsonObject = json.getAsJsonObject();
            JsonArray list;


            if (!tmpJsonObject.has("list") || !tmpJsonObject.get("list").isJsonArray() || (list = tmpJsonObject.getAsJsonArray("list")).size() == 0) {
                return null;
            }

            MailImage  mailImage = new MailImage();
            Gson gson = new Gson();

            for (JsonElement jsonElement : list) {
                try {
                    MailImage.Message message = gson.fromJson(jsonElement, MailImage.Message.class);
                    mailImage.addMessage(message);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }

            return mailImage;
        }
    }
}
