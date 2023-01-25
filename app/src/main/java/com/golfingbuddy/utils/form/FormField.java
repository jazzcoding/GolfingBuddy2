package com.golfingbuddy.utils.form;

/**
 * Created by jk on 1/12/15.
 */
public interface FormField {

    public void setError(String value);

    public void setValue(String value);

    public String getValue();

    public void setTitle(CharSequence title);

    public CharSequence getTitle();

    public void setKey(String key);

    public String getKey();
}
