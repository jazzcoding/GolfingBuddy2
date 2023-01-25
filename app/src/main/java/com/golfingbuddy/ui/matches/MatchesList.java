package com.golfingbuddy.ui.matches;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.golfingbuddy.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.ui.matches.classes.SlideData;
import com.golfingbuddy.ui.matches.fragments.UserSlide;
import com.golfingbuddy.ui.matches.service.MatchesListService;
import com.golfingbuddy.utils.SKPopupMenu;

import java.util.ArrayList;

/**
 * Created by kairat on 10/7/14.
 */
public class MatchesList extends Fragment {

    private int mCommand;
    private int mPage;
    private String mSort;
    private UserSlide mUserSlide;
    private MatchesListService mMatchesService;
    private SkServiceCallbackListener mListener;
    private SKPopupMenu mPopupMenu;

    private enum SortType {
        KEY {
            @Override
            public String toString() {
                return "sort";
            }
        },
        NEWEST {
            @Override
            public String toString() {
                return "newest";
            }
        },
        COMPATIBLE {
            @Override
            public String toString() {
                return "compatible";
            }
        }
    }

    public static MatchesList newInstance() {
        return new MatchesList();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.matches_titlet));
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.matches_list, container, false);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        mUserSlide = new UserSlide();

        mUserSlide.setDrawPhotoIndex(true);
        mUserSlide.setUserSlideListener(new UserSlide.UserSlideListener(){
            @Override
            public Boolean loadMore(int position, ArrayList<SlideData> list) {
                super.loadMore(position, list);

                if (mMatchesService.isPending(mCommand)) {
                    return true;
                }

                mPage++;
                mCommand = mMatchesService.getMatchesList(mSort, mPage, new SkServiceCallbackListener() {
                    @Override
                    public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                        mUserSlide.addSlideData(parse(data.getString("data")));
                    }
                });

                return true;
            }
        });
        fragmentTransaction.replace(R.id.matches_list, mUserSlide);
        fragmentTransaction.commit();

        mMatchesService = new MatchesListService(getActivity().getApplication());
        mListener = new SkServiceCallbackListener() {
            @Override
            public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                mUserSlide.setSlideData(parse(data.getString("data")));
            }
        };

        mPage = 1;
        mSort = SortType.NEWEST.toString();
        mCommand = mMatchesService.getMatchesList(mListener);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.matches_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.matches_menu:
                if (mMatchesService.isPending(mCommand)) {
                    mMatchesService.cancelCommand(mCommand);
                }

                if (mPopupMenu == null) {
                    ArrayList<SKPopupMenu.MenuItem> maps = new ArrayList<>();
                    SKPopupMenu.MenuItem sortMenu = new SKPopupMenu.MenuItem(SortType.KEY.toString());
                    sortMenu.setPresentation(SKPopupMenu.PRESENTATION.SIMPLE);
                    sortMenu.setIsClickable(false);
                    sortMenu.setLabel(getString(R.string.matches_sort_lagel));
                    sortMenu.setLabelColor(getResources().getColor(R.color.matches_divider_color));
                    maps.add(sortMenu);

                    SKPopupMenu.MenuItem dividerMenu = new SKPopupMenu.MenuItem("divider");
                    dividerMenu.setPresentation(SKPopupMenu.PRESENTATION.DIVIDER);
                    dividerMenu.setDividerColor(getResources().getColor(R.color.matches_divider_color));
                    maps.add(dividerMenu);

                    SKPopupMenu.MenuItem newestMenu = new SKPopupMenu.MenuItem(SortType.NEWEST.toString());
                    newestMenu.setPresentation(SKPopupMenu.PRESENTATION.SIMPLE);
                    newestMenu.setGroup(1);
                    newestMenu.setLabel(getString(R.string.matches_sort_newest));
                    newestMenu.setIcon(R.drawable.mark_menu_item);
                    maps.add(newestMenu);

                    SKPopupMenu.MenuItem compatibilityMenu = new SKPopupMenu.MenuItem(SortType.COMPATIBLE.toString());
                    compatibilityMenu.setPresentation(SKPopupMenu.PRESENTATION.SIMPLE);
                    compatibilityMenu.setGroup(1);
                    compatibilityMenu.setLabel(getString(R.string.matches_sort_compatibility));
                    maps.add(compatibilityMenu);

                    SKPopupMenu.MenuParams params = new SKPopupMenu.MenuParams(getActivity());
                    params.setContext(getActivity());
                    params.setMenuItems(maps);
                    params.setWidth(500);
                    params.setAnchor(getActivity().findViewById(R.id.matches_menu));
                    params.setXoff(-350);
                    params.setYoff(0);

                    mPopupMenu = new SKPopupMenu(params);
                    mPopupMenu.setMenuItemClickListener(new SKPopupMenu.MenuItemClickListenerImpl(){
                        @Override
                        public Boolean onClick(SKPopupMenu.MenuItem menuItem, int position, long id, ArrayList<SKPopupMenu.MenuItem> list) {
                            super.onClick(menuItem, position, id, list);

                            if (menuItem.getIcon() == 0) {
                                mUserSlide.hide();
                                menuItem.setIcon(R.drawable.mark_menu_item);
                                mPage = 1;
                                mSort = menuItem.getKey();
                                mCommand = mMatchesService.getMatchesList(menuItem.getKey(), mListener);
                            }

                            return true;
                        }
                    });
                }

                mPopupMenu.show();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<SlideData> parse(String jsonString)
    {
        JsonParser jsonParser = new JsonParser();
        JsonObject json = jsonParser.parse(jsonString).getAsJsonObject().getAsJsonObject("data");
        JsonArray jsonUserList = json.getAsJsonArray("list");

        if (jsonUserList == null || jsonUserList.size() == 0) {
            return null;
        }

        ArrayList<SlideData> speedmatcheses = new ArrayList<>();
        Gson gson = new GsonBuilder().create();

        for (JsonElement jsonElement : jsonUserList) {
            speedmatcheses.add(gson.fromJson(jsonElement, SlideData.class));
        }

        return speedmatcheses;
    }
}