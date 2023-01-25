package com.golfingbuddy.ui.base;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.golfingbuddy.R;
import com.golfingbuddy.core.SkBaseActivity;
import com.golfingbuddy.ui.search.fragments.EditFormFragment;

public class LatestList extends SkBaseActivity {

//    ArrayList<HashMap<String,String>> userlist;

    public LatestList() {

//        userlist = new ArrayList<HashMap<String,String>>();
//
//        for ( int i = 0; i < 10000; i++ )
//        {
//            HashMap<String,String> map;
//            map = new HashMap<String,String>();
//
//            map.put("id", Integer.toString(i));
//            map.put("displayName", "Grey " + Integer.toString(i));
//            map.put("role","LAMO");
//            map.put("onlineStatus", "true");
//            map.put("lastActivity", "2 hours ago");
//
//            if ( i % 2 > 0 ) {
//                map.put("avatar", "http://white.staticfly.net/ow_userfiles/2027/plugins/base/avatars/avatar_124_1381376832.jpg");
//            }
//            /* else if ( i % 2  == 1 )
//            {
//                map.put("avatar", "");
//            }
//            else
//            {
//                map.put("avatar", "http://white.staticfly.net/");
//            } */
//
//            map.put("avatar", "http://white.staticfly.net/ow_userfiles/2027/plugins/base/avatars/avatar_124_1381376832.jpg");
//
//
//            userlist.add(map);
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latest_list);
        this.setTitle(R.string.title_latest_user_list);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        EditFormFragment myFragment = new EditFormFragment();
        //SearchFormFragment myFragment = new SearchFormFragment();

        fragmentTransaction.replace(R.id.user_list, myFragment);
        fragmentTransaction.commit();
    }



//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.latest_list, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

//    @Override
//    public View onCreateView(String name, Context context, AttributeSet attrs) {
//
//        return super.onCreateView(name, context, attrs);
//    }
//    @Override
//    public void onFragmentInteraction(int id) {
//
//    }

//    @Override
//    public getData(ArrayList<String> excludeList) {
//
//        BaseServiceHelper helper = new BaseServiceHelper(getApp());
//
//        helper.runRestRequest(BaseRestCommand.ACTION_TYPE.PROFILE_INFO, new SkServiceCallbackListener() {
//            @Override
//            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
//
//            }
//        });
//
//        if ( excludeList == null )
//        {
//            excludeList = new ArrayList<String>();
//        }
//
//        int count = 0;
//
//        ArrayList<HashMap<String,String>> list;
//        list = new ArrayList<HashMap<String,String>>();
//
//        for ( HashMap<String,String> entry:userlist )
//        {
//            String id = entry.get("id");
//
//            int hasElement = excludeList.indexOf(id);
//
//            if ( hasElement == -1 ) {
//                count++;
//                list.add(entry);
//            }
//
//            if ( count > 50 )
//            {
//                break;
//            }
//        }
//
//        return list;
//    }
}
