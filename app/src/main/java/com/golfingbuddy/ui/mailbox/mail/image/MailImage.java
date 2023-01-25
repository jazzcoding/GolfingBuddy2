package com.golfingbuddy.ui.mailbox.mail.image;

import com.golfingbuddy.ui.mailbox.chat.model.ConversationHistory;

import java.util.ArrayList;

/**
 * Created by kairat on 3/14/15.
 */
public class MailImage {

    private ArrayList<Message> mMessages;

    public MailImage() {
        mMessages = new ArrayList<>();
    }

    public void addMessage(Message message) {
        mMessages.add(message);
    }

    public Boolean isEmpty() {
        return mMessages.isEmpty();
    }

    public ArrayList<Message> getMessages() {
        return mMessages;
    }

    public Message getLastMessage() {
        return mMessages.get(mMessages.size() - 1);
    }

    public static class Message extends ConversationHistory.Messages.Message implements Comparable {

        @Override
        public int compareTo(Object another) {
            Message item = (Message)another;

            return item.getId() < item.getId() ? -1 : 1;
        }
    }
}
