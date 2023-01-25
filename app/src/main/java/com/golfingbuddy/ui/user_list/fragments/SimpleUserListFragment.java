package com.golfingbuddy.ui.user_list.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.golfingbuddy.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.profile.ProfileViewActivity;
import com.golfingbuddy.ui.user_list.classes.ListData;
import com.golfingbuddy.utils.SKImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * interface.
 */
public abstract class SimpleUserListFragment extends Fragment implements AbsListView.OnItemClickListener, AbsListView.OnScrollListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    protected static final String ARG_CONTENT = "content";
    protected static final String ARG_CONTENT_ORDER = "contentOrder";
    protected static final String ARG_LOAD_MORE = "loadMore";
    protected static final String ARG_SHOWED_ITEMS = "showedItems";

    protected final int ARG_ITEMS_PER_PAGE = 50;
    protected final int ARG_ITEMS_PER_LOAD= 150;

    // TODO: Rename and change types of parameters
    //protected ArrayList<Integer> userList = new ArrayList();
    protected HashMap<String,HashMap<String,String>> content = new HashMap();
    protected ArrayList<String> contentOrder = new ArrayList<String>();
    protected ArrayList<String> showedItems = new ArrayList<String>();

    protected ProgressDialog preloader = null;

    protected int requestId = -1;

    protected boolean loadMore = true;
    protected boolean sendRequest = false;
    protected boolean forceRenderShowedItems = false;
    //private [] usersData;

//    protected OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    protected AbsListView mListView;
    protected LinearLayout emptyListTextView;
    protected ProgressBar mProgressbar;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    protected ArrayAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SimpleUserListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( savedInstanceState != null && !savedInstanceState.isEmpty() ) {
            if( savedInstanceState.containsKey(ARG_CONTENT) )
            {
                ListData pContent = savedInstanceState.getParcelable(ARG_CONTENT);
                content = pContent.getData();
            }

            if( savedInstanceState.containsKey(ARG_CONTENT_ORDER) )
            {
                contentOrder = savedInstanceState.getStringArrayList(ARG_CONTENT_ORDER);
            }

            if( savedInstanceState.containsKey(ARG_SHOWED_ITEMS) )
            {
                showedItems = savedInstanceState.getStringArrayList(ARG_SHOWED_ITEMS);

                if ( showedItems != null && showedItems.size() > 0 ) {
                    forceRenderShowedItems = true;
                }
            }

            if( savedInstanceState.containsKey(ARG_LOAD_MORE) )
            {
                loadMore = savedInstanceState.getBoolean(ARG_LOAD_MORE);
            }
        }

        // TODO: Change Adapter to display your content
        mAdapter = new SimpleUserListAdapter((Context) getActivity(), R.layout.simple_user_list_item, new ArrayList<HashMap<String, String>>() );
    }

    protected ProgressDialog getPreloader()
    {
        if ( preloader == null ) {
            preloader = ProgressDialog.show(getActivity(), "", getString(R.string.user_list_preloader_text), true);
        }

        return preloader;
    }

    protected void loadData()
    {
        loadData(true);
    }

    protected abstract void loadData( final boolean renderList );

    protected ArrayList<HashMap<String, String>> getNextItems()
    {
        ArrayList<HashMap<String, String>> next = new ArrayList<HashMap<String, String>>();

        for ( String id:contentOrder )
        {
            if( !showedItems.contains(id) )
            {
                if ( content.containsKey(id) ) {
                    next.add(content.get(id));
                }
                showedItems.add(id);
            }

            if ( next.size() >= ARG_ITEMS_PER_PAGE )
            {
                break;
            }
        }

        return next;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_user_list_item_list, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setClickable(true);
        emptyListTextView = (LinearLayout) view.findViewById(R.id.empty_list);
        mProgressbar = (ProgressBar) view.findViewById(R.id.simplelist_progressBar);

        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);


        if ( forceRenderShowedItems )
        {
            renderShowedItems();
            forceRenderShowedItems = false;
        }

        if ( loadMore && content.size() < ARG_ITEMS_PER_LOAD || Integer.parseInt(String.valueOf(content.size()/2)) <= showedItems.size() ) {
            loadData();
        }

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);

        return view;
    }

    private void renderShowedItems()
    {
        ArrayList<HashMap<String, String>> userList = new ArrayList<HashMap<String, String>>();

        if ( showedItems != null && showedItems.size() > 0 )
        {
            for ( String userId:contentOrder )
            {
                if ( showedItems.contains(userId) && content.containsKey(userId) )
                {
                    HashMap<String, String> map = content.get(userId);
                    userList.add(map);
                }
            }
        }

        renderList(userList);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    public void onSaveInstanceState(Bundle state) {

        ListData pContent  = new ListData();
        pContent.setData(content);

        state.putParcelable(ARG_CONTENT, pContent);
        state.putStringArrayList(ARG_CONTENT_ORDER, contentOrder);
        state.putStringArrayList(ARG_SHOWED_ITEMS, showedItems);
        state.putBoolean(ARG_LOAD_MORE, loadMore);

        if( requestId > -1 )
        {
            BaseServiceHelper helper = BaseServiceHelper.getInstance(getActivity().getApplication());
            helper.cancelCommand(requestId);
        }

        if ( preloader != null )
        {
            preloader.dismiss();
            preloader = null;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            String userID = contentOrder.get(position);
            HashMap<String, String> data = content.get(userID);

            Intent intent = new Intent(getActivity(), ProfileViewActivity.class);
            intent.putExtra("userId", Integer.parseInt(userID));
            startActivity(intent);
//            mListener.onFragmentInteraction(Integer.parseInt(data.get("id")));
//        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i2, int i3) {
        if (loadMore == true && null != mAdapter && sendRequest == false ) {
            if ( content.size() < ARG_ITEMS_PER_LOAD || Integer.parseInt(String.valueOf(content.size()/2)) <= i3 ) {
                loadData();
            }
        }
    }

    public void renderList()
    {
        ArrayList<HashMap<String, String>> data = getNextItems();

        renderList(data);
    }

    public void show()
    {
        int duration = 200;

        if ( mProgressbar != null && mListView != null ) {
            mProgressbar.setVisibility(View.GONE);
            mProgressbar.animate()
                    .alpha(1f)
                    .setDuration(duration)
                    .setListener(null);
            mListView.setVisibility(View.VISIBLE);
            mListView.animate()
                    .alpha(1f)
                    .setDuration(duration)
                    .setListener(null);
        }
    }

    public void showEmptyList()
    {
        int duration = 200;

        if ( mProgressbar != null && mListView != null && emptyListTextView != null ) {
            mProgressbar.setVisibility(View.GONE);
            mProgressbar.animate()
                    .alpha(1f)
                    .setDuration(duration)
                    .setListener(null);
            mListView.setVisibility(View.GONE);
            mListView.animate()
                    .alpha(1f)
                    .setDuration(duration)
                    .setListener(null);
            emptyListTextView.setVisibility(View.VISIBLE);
            emptyListTextView.animate()
                    .alpha(1f)
                    .setDuration(duration)
                    .setListener(null);
        }
    }

    public void renderList( ArrayList<HashMap<String, String>> data )
    {
        if ( data != null && data.size() == 0 && content.size() == 0 && showedItems.size() == 0 )
        {
            showEmptyList();
            return;
        }
        else
        {
            show();
        }

        mAdapter.addAll(data);
        mAdapter.notifyDataSetInvalidated();
    }

    class SimpleUserListAdapter extends ArrayAdapter<HashMap<String,String>> {

        Context context;
        int layoutResourceId;
        ArrayList<HashMap<String,String>> data = null;


        public SimpleUserListAdapter(Context context, int layoutResourceId, ArrayList<HashMap<String, String>> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;

            this.data = data;
        }


        @Override
        public View getView(int position, View row, ViewGroup parent) {

            SimpleListHolder holder = null;

            if(row == null)
            {
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new SimpleListHolder();
                holder.userListItem = (LinearLayout)row.findViewById(R.id.user_list_item_layout);
                holder.displayName = (TextView)row.findViewById(R.id.display_name);
                holder.lastActivity = (TextView)row.findViewById(R.id.last_activity);
                holder.role = (TextView)row.findViewById(R.id.role);
                holder.avatar = (SKImageView) row.findViewById(R.id.avatar);
                holder.avatarProgressBar = (ProgressBar) row.findViewById(R.id.avatarProgressBar);
                holder.onlineStatus = (ImageView) row.findViewById(R.id.online_status_icon);
                holder.bookmark = (ImageView) row.findViewById(R.id.bookmark);

                row.setTag(holder);
            }
            else
            {
                holder = (SimpleListHolder)row.getTag();
            }

            final HashMap<String,String> user = data.get(position);

            if ( user != null )
            {
                holder.setData(user);
            }

            return row;
        }
    }

    class SimpleListHolder
    {
        LinearLayout userListItem;
        SKImageView avatar;
        ProgressBar avatarProgressBar;
        TextView displayName;
        TextView role;
        TextView lastActivity;
        ImageView onlineStatus;
        ImageView bookmark;

        public void setData(HashMap<String, String> data)
        {
            this.displayName.setText("");
            if ( data.containsKey("displayName") ) {
                this.displayName.setText(data.get("displayName").toString());
            }

            this.role.setText("");
            if ( data.containsKey("label") ) {

                if ( data.get("label").toString().isEmpty() )
                {
                    this.role.setVisibility(View.GONE);
                }
                else
                {
                    this.role.setVisibility(View.VISIBLE);
                }
                this.role.setText(data.get("label").toString());
            }

            GradientDrawable bgShape = (GradientDrawable)this.role.getBackground();
            if ( data.containsKey("labelColor") ) {
                int color = Integer.parseInt((String) data.get("labelColor"));
                bgShape.setColor(color);
            }
            else
            {
                // default membership label color
                bgShape.setColor(Color.parseColor("#99999"));
            }
            this.role.setBackground(bgShape);


            this.onlineStatus.setVisibility(ImageView.INVISIBLE);
            if ( data.containsKey("online") ) {
                if ( data.get("online") == "true" ) {
                    this.onlineStatus.setVisibility(ImageView.VISIBLE);
                }
            }

            this.lastActivity.setText("");
            if ( data.containsKey("time") ) {
                this.lastActivity.setText(data.get("time").toString());
            }

            avatar.placeholder(R.drawable.rounded_rectangle_2_copy_2);
            if ( data.containsKey("avatarUrl") ) {
                avatar.setImageUrl(data.get("avatarUrl").toString());
            }

//            if ( data.containsKey("avatarUrl") ) {
//                if ( SimpleUserListFragment.this.getActivity() != null ) {
//                    Picasso.with(SimpleUserListFragment.this.getActivity())
//                            .load(data.get("avatarUrl").toString()).into(new Target() {
//                        @Override
//                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
//                            avatar.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
//                            avatarProgressBar.setVisibility(View.GONE);
//                            avatar.setVisibility(View.VISIBLE);
//                        }
//
//                        @Override
//                        public void onBitmapFailed(Drawable drawable) {
//
//                        }
//
//                        @Override
//                        public void onPrepareLoad(Drawable drawable) {
//                            avatar.setVisibility(View.GONE);
//                            avatarProgressBar.setVisibility(View.VISIBLE);
//                        }
//                    });
//                }
//            }

            this.bookmark.setVisibility(ImageView.INVISIBLE);
            if ( data.containsKey("bookmarked") ) {
                if ( data.get("bookmarked") == "true" ) {
                    this.bookmark.setVisibility(ImageView.VISIBLE);
                }
            }

            this.userListItem.setBackgroundResource(R.drawable.user_list_item_border);
            if ( data.containsKey("viewed") ) {
                if ( "0".equals(data.get("viewed")) || "fasle".equals(data.get("viewed"))  ) {
                    this.userListItem.setBackgroundResource(R.color.user_list_background_color);
                }
            }
        }
    }

    class SimpleListJsonParcer
    {
        protected ArrayList<String> existingItems = new ArrayList<String>();
        protected HashMap<String,HashMap<String,String>> content = new HashMap();
        protected ArrayList<String> order = new ArrayList<String>();

        public SimpleListJsonParcer(HashMap<String,HashMap<String,String>> existingItems) {

            if ( existingItems != null && existingItems.keySet().size() > 0 ) {
                Set<String> keySet = existingItems.keySet();
                this.existingItems.addAll(keySet);
            }
        }

        public void parce( JsonArray items ) {
            HashMap<String,HashMap<String, String>> result = new HashMap<String,HashMap<String, String>>();


            if ( items == null || !items.isJsonArray() || items.size() == 0 )
            {
                return;
            }

            for ( JsonElement element:items )
            {
                if ( null != element && element.isJsonNull() || !element.isJsonObject() )
                {
                    continue;
                }

                JsonObject item = element.getAsJsonObject();

                if ( !item.has("userId") )
                {
                    continue;
                }

                HashMap map = parseItem(item);

                if ( map.size() > 0 && map.containsKey("userId") ) {

                    if ( !existingItems.contains((String) map.get("userId")) ) {
                        String userId = (String) map.get("userId");
                        content.put(userId, map);
                        order.add(userId);
                    }
                }
            }
        }

        protected HashMap parseItem( JsonObject item )
        {
            HashMap map = new HashMap();

            map.put("userId", "");
            if ( item.has("userId") ) {
                JsonElement tmp  = item.get("userId");

                if ( null != tmp  && tmp.isJsonPrimitive() )
                {
                    map.put("userId", tmp.getAsString());
                }
            }

            map.put("displayName", "");
            if ( item.has("displayName") ) {
                JsonElement tmp  = item.get("displayName");

                if ( null != tmp  && tmp.isJsonPrimitive() )
                {
                    map.put("displayName", tmp.getAsString());
                }
            }

            map.put("avatarUrl", "");
            if ( item.has("avatarUrl") ) {
                JsonElement tmp  = item.get("avatarUrl");

                if ( null != tmp  && tmp.isJsonPrimitive() )
                {
                    map.put("avatarUrl", tmp.getAsString());
                }
            }

            map.put("label", "");
            if ( item.has("label") ) {
                JsonElement tmp  = item.get("label");

                if ( null != tmp  && tmp.isJsonPrimitive() )
                {
                    map.put("label", tmp.getAsString());
                }
            }

            map.put("labelColor", "");
            if ( item.has("labelColor") ) {
                JsonElement tmp  = item.get("labelColor");

                if ( null != tmp  && tmp.isJsonObject() )
                {
                    if ( tmp.getAsJsonObject().has("r") && tmp.getAsJsonObject().has("g") && tmp.getAsJsonObject().has("b") ) {
                        int color = Color.rgb(tmp.getAsJsonObject().get("r").getAsInt(), tmp.getAsJsonObject().get("g").getAsInt(), tmp.getAsJsonObject().get("b").getAsInt());
                        map.put("labelColor", String.valueOf(color));
                    }
                }
            }

            map.put("online", "");
            if ( item.has("online") ) {
                JsonElement tmp  = item.get("online");

                if ( null != tmp  && tmp.isJsonPrimitive() )
                {
                    map.put("online", tmp.getAsString());
                }
            }

            map.put("bookmarked", "");
            if ( item.has("bookmarked") ) {
                JsonElement tmp  = item.get("bookmarked");

                if ( null != tmp  && tmp.isJsonPrimitive() )
                {
                    map.put("bookmarked", tmp.getAsString());
                }
            }

            map.put("time", "");
            if ( item.has("time") ) {
                JsonElement tmp  = item.get("time");

                if ( null != tmp  && tmp.isJsonPrimitive() )
                {
                    map.put("time", tmp.getAsString());
                }
            }

            map.put("viewed", "");
            if (item.has("viewed")) {
                JsonElement tmp = item.get("viewed");

                if (null != tmp && tmp.isJsonPrimitive()) {
                    map.put("viewed", tmp.getAsString());
                }

            }

            return map;
        }

        public HashMap<String,HashMap<String,String>> getData() {
            return content;
        }

        public ArrayList<String> getOrder() {
            return order;
        }
    }
}
