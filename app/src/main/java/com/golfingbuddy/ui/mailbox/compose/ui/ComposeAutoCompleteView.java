package com.golfingbuddy.ui.mailbox.compose.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.ui.mailbox.AutoCompleteHolderInterface;
import com.golfingbuddy.ui.mailbox.compose.Recipient;
import com.golfingbuddy.ui.mailbox.compose.RecipientInterface;
import com.golfingbuddy.utils.SKRoundedImageView;
import com.squareup.picasso.Callback;

import java.util.ArrayList;

/**
 * Created by kairat on 3/19/15.
 */
public class ComposeAutoCompleteView extends AutoCompleteTextView {

    private static final int MESSAGE_TEXT_CHANGED = 100;
    private static final int DEFAULT_AUTOCOMPLETE_DELAY = 750;

    private int mAutoCompleteDelay = DEFAULT_AUTOCOMPLETE_DELAY;
    private ArrayList<RecipientInterface> mFilterResult;
    private AutoCompleteHolderInterface mHolder;
    private ProgressBar mLoadingIndicator;
    private View mClear;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ArrayList<String> idList = new ArrayList<>();
/*
            if (!mFilterResult.isEmpty()) {
                for (RecipientInterface recipient : mFilterResult) {
                    idList.add(recipient.getId());
                }
            }
*/
            mHolder.suggestionListRequest(msg.obj.toString(), idList);
        }
    };

    public ComposeAutoCompleteView(Context context) {
        super(context);
        init();
    }

    public ComposeAutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ComposeAutoCompleteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mFilterResult = new ArrayList<>();
    }

    public void setHolder(AutoCompleteHolderInterface holder) {
        mHolder = holder;
    }

    public ProgressBar getLoadingIndicator() {
        return mLoadingIndicator;
    }

    public void setLoadingIndicator(ProgressBar progressBar) {
        mLoadingIndicator = progressBar;
    }

    public View getmClear() {
        return mClear;
    }

    public void setClear(View clear) {
        mClear = clear;
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(VISIBLE);
        }

        if (mClear != null) {
            mClear.setVisibility(GONE);
        }

        mHandler.removeMessages(MESSAGE_TEXT_CHANGED);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_TEXT_CHANGED, text), mAutoCompleteDelay);
    }

    public void onComplete() {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(INVISIBLE);
        }

        if (mClear != null) {
            mClear.setVisibility(VISIBLE);
        }
    }

    public void hideClear() {
        if (mClear != null) {
            mClear.setVisibility(GONE);
        }
    }

    public static class AutocompleteAdapter extends ArrayAdapter<RecipientInterface> {

        private LayoutInflater mInflater;
        private ArrayList<RecipientInterface> mFilterResult;

        public AutocompleteAdapter(Context context, int resource) {
            super(context, resource);
            mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mFilterResult = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mFilterResult.size();
        }

        @Override
        public RecipientInterface getItem(int position) {
            return mFilterResult.get(position);
        }

        @Override
        public int getPosition(RecipientInterface item) {
            return mFilterResult.indexOf(item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View conversationView = convertView;

            if ( conversationView == null ) {
                conversationView = mInflater.inflate(R.layout.mailbox_compose_auto_complete_item, null);
                Holder holder = new Holder(getItem(position));
                holder.setView(conversationView);
                conversationView.setTag(holder);
            } else {
                Holder holder = (Holder) conversationView.getTag();
                holder.setData(getItem(position));
            }

            ((Holder) conversationView.getTag()).render();

            return conversationView;
        }

        public ArrayList<RecipientInterface> getFilterResult() {
            return mFilterResult;
        }

        public void setFilterResult(JsonArray list) {
            mFilterResult.clear();

            if (list != null && list.size() != 0) {
                for (JsonElement element : list) {
                    JsonObject tmpObject = element.getAsJsonObject();
                    RecipientInterface recipient = new Recipient();
                    recipient.setOpponentId(tmpObject.get("id").getAsString());
                    recipient.setAvatarUrl(tmpObject.get("avatarUrl").isJsonNull() ? null : tmpObject.get("avatarUrl").getAsString());
                    recipient.setDisplayName(tmpObject.get("displayName").getAsString());
                    mFilterResult.add(recipient);
                }

                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

        private class Holder implements AutoCompleteHolderInterface.ItemHolderInterface {
            private View mView;
            private RecipientInterface mRecipient;

            public Holder(RecipientInterface recipient) {
                mRecipient = recipient;
            }

            @Override
            public void render() {
                if (mView == null) {
                    return;
                }

                ((TextView) mView.findViewById(R.id.mailbox_auto_complete_item_display_name)).setText(mRecipient.getDisplayName());

                if (mRecipient.getAvatarUrl() != null) {
                    ((SKRoundedImageView) mView.findViewById(R.id.mailbox_auto_complete_item_avatar)).setImageUrl(mRecipient.getAvatarUrl(), new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            ((SKRoundedImageView) mView.findViewById(R.id.mailbox_auto_complete_item_avatar)).setImageResource(R.drawable.rounded_rectangle_2_copy_2);
                        }
                    });
                }
            }

            @Override
            public View getView() {
                return null;
            }

            @Override
            public void setView(View view) {
                mView = view;
            }

            @Override
            public void setData(Object data) {
                if (data instanceof RecipientInterface) {
                    mRecipient = (RecipientInterface) data;
                }
            }

            @Override
            public Object getData() {
                return mRecipient;
            }
        }
    }
}
