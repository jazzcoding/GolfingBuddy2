package com.golfingbuddy.ui.speedmatch.form;

import android.app.Fragment;

/**
 * Created by kairat on 1/22/15.
 */
public class Form extends Fragment {
    public static enum PRESENTATION {
        TEXT, TEXTAREA, SELECT, DATE, LOCATION, CHECKBOX, MULTICHECKBOX, RADIO, URL, PASSWORD, AGE, BIRTHDATE, RANGE
    }

    public static class FormElement {
        protected String mName;
        protected String mLabel;
        protected PRESENTATION mPresentation;
    }
}
