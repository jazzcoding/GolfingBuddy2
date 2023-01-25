package com.golfingbuddy.ui.mailbox.conversation_list;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.model.base.classes.SkMenuItem;
import com.golfingbuddy.ui.base.SkBaseInnerActivity;
import com.golfingbuddy.ui.mailbox.MailboxPingHandler;
import com.golfingbuddy.ui.mailbox.chat.WinkActivity;
import com.golfingbuddy.ui.mailbox.compose.ComposeActivity;
import com.golfingbuddy.ui.mailbox.conversation_list.ui.ConversationListAdapter;
import com.golfingbuddy.ui.mailbox.Constants;
import com.golfingbuddy.ui.mailbox.chat.ChatActivity;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationItemInterface;
import com.golfingbuddy.ui.mailbox.conversation_list.model.ConversationList;
import com.golfingbuddy.ui.mailbox.mail.MailActivity;
import com.golfingbuddy.utils.SKPopupMenu;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by kairat on 2/13/15.
 */
public class MailboxFragment extends Fragment implements MailboxView, SKPopupMenu.MenuItemClickListener {
    public static final int CHAT_RESULT = 1;
    public static final int MAIL_RESULT = 2;
    public static final int WINK_RESULT = 4;
    public static final int COMPOSE_RESULT = MailActivity.COMPOSE_RESULT;

    protected MailboxPresenter mPresenter;

    private View mView;
    private Menu mMenu;

    private ListView mMessageList;

    private ConversationListAdapter mAdapter;
    private SKPopupMenu mPopupMenu;
    private int mModes;
    private Boolean isCountInit = false;

    public static MailboxFragment newInstance() {
        return new MailboxFragment();
    }

    BroadcastReceiver menuListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<SkMenuItem> list = new Gson().fromJson(intent.getStringExtra("data"), new TypeToken<ArrayList<SkMenuItem>>(){}.getType());

            if (list == null) {
                return;
            }

            if (!SkApplication.getConfig().configExists("mailbox", "count_new_messages")) {
                return;
            }

            for (SkMenuItem menuItem : list) {
                if (menuItem.getKey().equals("mailbox")) {
                    menuItem.setCount(SkApplication.getConfig().getIntValue("mailbox", "count_new_messages"));
                    SkApplication.getConfig().removeConfig("mailbox", "count_new_messages");
                    ((SkBaseInnerActivity) getActivity()).updateSidebarData(list, false);

                    break;
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new MailboxPresenterImpl(this);
        mAdapter = new ConversationListAdapter(getActivity(), this);

        Resources resources = getResources();

        SKPopupMenu.MenuParams params = new SKPopupMenu.MenuParams(getActivity());
        params.setWidth(530);
        params.setXoff(-470);

        SKPopupMenu.MenuItem readMenuItem = new SKPopupMenu.MenuItem("read");
        readMenuItem.setLabel(resources.getString(R.string.mailbox_as_read));

        SKPopupMenu.MenuItem unreadMenuItem = new SKPopupMenu.MenuItem("unread");
        unreadMenuItem.setLabel(resources.getString(R.string.mailbox_as_unread));

        SKPopupMenu.MenuItem deleteMenuItem = new SKPopupMenu.MenuItem("delete");
        deleteMenuItem.setLabel(resources.getString(R.string.mailbox_delete));

        params.setMenuItems(new ArrayList<>(Arrays.asList(readMenuItem, unreadMenuItem, deleteMenuItem)));

        mPopupMenu = new SKPopupMenu(params);
        mPopupMenu.setMenuItemClickListener(this);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(menuListener, new IntentFilter(SkBaseInnerActivity.EVENT_ON_MENU_DATA_USE));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        getActivity().getMenuInflater().inflate(R.menu.mailbox_conversation_list_menu, menu);
        mMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mAdapter.hasSelected()) {
            SKPopupMenu.MenuParams params = mPopupMenu.getParams();

            params.setAnchor(getActivity().findViewById(R.id.mailbox_conversation_list_menu));
            params.setTag(null);

            mPopupMenu.show();
        } else {
            Intent intent = new Intent(getActivity(), ComposeActivity.class);
            startActivityForResult(intent, COMPOSE_RESULT);
            getActivity().overridePendingTransition(R.anim.speedmatches_slide_in_right, R.anim.speedmatches_slide_out_left);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.mailbox_view, container, false);

        initView();

        mPresenter.renderConversationList();

        return mView;
    }

    private void initView() {
        if ( mView == null ) {
            return;
        }

        mMessageList = (ListView) mView.findViewById(R.id.mailbox_message_list);
        mMessageList.setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(R.string.mailbox_title);
    }

    @Override
    public void onDestroy() {
        MailboxPingHandler.getInstance().setConversationListHolder(null);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(menuListener);

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null || !data.hasExtra(Constants.CONVERSATION_ITEM)) {
            return;
        }

        switch ( resultCode ) {
            case CHAT_RESULT:
            case MAIL_RESULT:
                ConversationList.ConversationItem tmpItem = data.getParcelableExtra(Constants.CONVERSATION_ITEM);
                ConversationList.ConversationItem item = mAdapter.findConversationItem(tmpItem);
                item.update(tmpItem);

                if (data.hasExtra("action")) {
                    switch (data.getStringExtra("action")) {
                        case "unread":
                            item.markUnRead();
                            updateConversationItem(item);
                            break;
                        case "delete":
                            mAdapter.delete(item);
                            break;
                    }
                } else {
                    item.markAsRead();
                    updateConversationItem(item);
                }

                setEmptyMode(mAdapter.isEmpty());
                break;
            case COMPOSE_RESULT:
                ConversationList.ConversationItem newItem = data.getParcelableExtra(Constants.CONVERSATION_ITEM);
                mAdapter.insert(newItem);
                break;

            case WINK_RESULT:
                Integer itemToDeleteId = data.getIntExtra("winkId", 0);
                mAdapter.deleteById(itemToDeleteId);
                break;
        }

        mAdapter.notifyDataSetChanged();
        getActivity().overridePendingTransition(R.anim.speedmatches_slide_in_left, R.anim.speedmatches_slide_out_right);
    }

    @Override
    public void feedback(String msg) {
        if (msg == null || msg.isEmpty()) {
            return;
        }

        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setLoadingMode(Boolean mode) {
        if (mode != null && mode) {
            mView.findViewById(R.id.mailbox_list).setVisibility(View.GONE);
        } else {
            mView.findViewById(R.id.mailbox_list).setVisibility(View.VISIBLE);
        }

        mView.findViewById(R.id.mailbox_conversation_list_no_items).setVisibility(View.GONE);
    }

    @Override
    public void setEmptyMode(Boolean mode) {
        if (mode != null && mode) {
            mView.findViewById(R.id.mailbox_list).setVisibility(View.GONE);
            mView.findViewById(R.id.mailbox_conversation_list_no_items).setVisibility(View.VISIBLE);
        } else {
            mView.findViewById(R.id.mailbox_list).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.mailbox_conversation_list_no_items).setVisibility(View.GONE);
        }
    }

    @Override
    public void setModes(int mode) {
        mModes = mode;

        if ((mModes & Constants.MODE_MAIL) >= 1) {
            setHasOptionsMenu(true);
        } else {
            setHasOptionsMenu(false);
        }
    }

    @Override
    public Boolean isSingleMode() {
        return mModes <= 0 || (mModes & Constants.MODE_BOTH) != 3;
    }

    @Override
    public void setConversationList(ConversationList conversationList) {
        try {
            mAdapter.setConversation(conversationList);
            mAdapter.notifyDataSetChanged();
        } catch (ConversationListAdapter.ConversationListException e) {
            // Nothing
        }

        setEmptyMode(mAdapter.isEmpty());

        if (MailboxPingHandler.getInstance().getConversationListHolder() == null) {
            MailboxPingHandler.getInstance().setConversationListHolder(this);
        }
    }

    @Override
    public void initListView() {
        if (mAdapter.isEmpty()) {
            mMessageList.setOnScrollListener(null);
        } else {
            mMessageList.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (visibleItemCount != totalItemCount && firstVisibleItem + visibleItemCount == totalItemCount) {
                        View view = listView.getChildAt(visibleItemCount - 1);

                        if (view.getBottom() <= listView.getBottom()) {
                            mPresenter.loadOffsetConversationList(mAdapter.getCount());
                        }
                    }
                }
            });
        }
    }

    @Override
    public void disableScrollListener() {
        mMessageList.setOnScrollListener(null);
    }

    @Override
    public void onConversationItemClick(ConversationItemInterface conversationItem) {
        ConversationList.ConversationItem item = conversationItem.getItem();
        Intent intent;
        int result = CHAT_RESULT;

        if( item.getConversationId() < 0 ) {
            intent = new Intent(getActivity(), WinkActivity.class);
        }
        else if (item.isChatMode()) {
            intent = new Intent(getActivity(), ChatActivity.class);
        } else {
            intent = new Intent(getActivity(), MailActivity.class);
            result = MAIL_RESULT;
        }

        intent.putExtra(Constants.CONVERSATION_ITEM, item);
        startActivityForResult(intent, result);
        getActivity().overridePendingTransition(R.anim.speedmatches_slide_in_right, R.anim.speedmatches_slide_out_left);
    }

    @Override
    public void onConversationContextClick(ConversationItemInterface conversationItem) {
        SKPopupMenu.MenuParams params = mPopupMenu.getParams();

        params.setAnchor(conversationItem.getView().findViewById(R.id.mailbox_conversation_action));
        params.setTag(conversationItem.getItem());

        mPopupMenu.show();
    }

    @Override
    public void onConversationAvatarClick(ConversationItemInterface conversationItem) {
        conversationItem.setSelected(!mAdapter.isSelected(conversationItem.getItem()));
        SkBaseInnerActivity activity = (SkBaseInnerActivity)getActivity();

        if (mAdapter.hasSelected()) {
            if (isCountInit) {
                activity.setCheckedItemsCount(mAdapter.getSelectedItems().size());
            } else {
                activity.setActionBarCheckCounterEnable(true, mAdapter.getSelectedItems().size());
                isCountInit = true;
            }

            mMenu.findItem(R.id.mailbox_conversation_list_menu).setIcon(R.drawable.more);
        } else {
            isCountInit = false;
            activity.setActionBarCheckCounterEnable(false);
            mMenu.findItem(R.id.mailbox_conversation_list_menu).setIcon(R.drawable.ic_newmessage);
        }
    }

    @Override
    public void updateConversationItem(ConversationList.ConversationItem conversationItem) {
        if (conversationItem == null || mAdapter.isEmpty()) {
            return;
        }

        for (ConversationList.ConversationItem item : mAdapter.getList()) {
            if (item.update(conversationItem)) {
                for (int i = 0, j = mMessageList.getChildCount(); i < j; i++) {
                    ConversationItemInterface holder = (ConversationItemInterface) mMessageList.getChildAt(i).getTag();
                    ConversationList.ConversationItem _item = holder.getItem();

                    if (_item.getConversationId() == conversationItem.getConversationId()) {
                        holder.setItem(item);
                        holder.render();

                        break;
                    }
                }

                break;
            }
        }
    }

    @Override
    public Boolean onClick(SKPopupMenu.MenuItem menuItem, int position, long id, ArrayList<SKPopupMenu.MenuItem> list) {
        SKPopupMenu.MenuParams params = mPopupMenu.getParams();

        if (mAdapter.hasSelected()) {
            String[] idList = mAdapter.getSelectedItems().getSelectedIdList();

            switch (menuItem.getKey()) {
                case "read":
                    mPresenter.markConversationAsRead(idList);
                    mAdapter.setAsReadSelectedItems();
                    break;
                case "unread":
                    mPresenter.markConversationUnRead(idList);
                    mAdapter.setUnReadSelectedItems();
                    break;
                case "delete":
                    mPresenter.deleteConversation(idList);
                    mAdapter.deleteSelectedItems();
                    break;
            }

            mAdapter.clearSelectedList();
            mAdapter.notifyDataSetChanged();
            mMenu.findItem(R.id.mailbox_conversation_list_menu).setIcon(R.drawable.ic_newmessage);
            isCountInit = false;
            ((SkBaseInnerActivity) getActivity()).setActionBarCheckCounterEnable(false);
        } else {
            ConversationList.ConversationItem item = (ConversationList.ConversationItem)params.getTag();
            String[] idList = {item.getStrConversationId()};

            switch (menuItem.getKey()) {
                case "read":
                    mPresenter.markConversationAsRead(idList);
                    item.markAsRead();
                    item.setNewMsgCount(0);
                    updateConversationItem(item);
                    break;
                case "unread":
                    mPresenter.markConversationUnRead(idList);
                    item.markUnRead();
                    updateConversationItem(item);
                    break;
                case "delete":
                    mPresenter.deleteConversation(idList);
                    mAdapter.delete(item);
                    break;
            }
        }

        setEmptyMode(mAdapter.isEmpty());

        return true;
    }
}
