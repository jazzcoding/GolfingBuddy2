package com.golfingbuddy.core;

import android.app.Application;

import com.golfingbuddy.model.base.BaseServiceHelper;

/**
 * Created by sardar on 10/20/14.
 */
//@Module(injects = com.skadatexapp.ui.base.SkBaseInnerActivity.class)
public class SKInjectModule {

    public static Application app;

    public SKInjectModule(){

    }

//    @Singleton
//    @Provides
    BaseServiceHelper provideBaseServiceHelper(){
        return BaseServiceHelper.getInstance(app);
    }
}
