package com.golfingbuddy.ui.base.email_verification;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.golfingbuddy.R;
import com.golfingbuddy.core.SkBaseActivity;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.base.email_verification.fragments.EmailValidationForm;
import com.golfingbuddy.ui.search.service.QuestionService;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jk on 2/18/16.
 */

public class EmailVerification extends SkBaseActivity implements MainView {

    EmailValidationForm mFragment;
    PresenterImp mPresenter;
    public static String PREFIX = "VERIFICATION";
    LinearLayout doneButton;
    LinearLayout sendButton;

    protected BaseServiceHelper baseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_email_activity);

        setTitle(R.string.verify_activity_title);

        baseHelper = new BaseServiceHelper(getApp());

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        mFragment = EmailValidationForm.newInstance();
        mFragment.setPreferencePrefix(PREFIX);

        fragmentTransaction.replace(R.id.questions, mFragment);
        fragmentTransaction.commit();

        mPresenter = new PresenterImp(this, this.getApp());

        mFragment.setPresenter(mPresenter);

        sendButton = (LinearLayout) findViewById(R.id.resetEmail);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmailVerification.this.mPresenter.onResendClicked(getData());
            }
        });

        doneButton = (LinearLayout) findViewById(R.id.done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmailVerification.this.mPresenter.onDoneClicked(getData());
            }
        });
    }

    @Override
    public void clean() {
        QuestionService.getInstance().removeSharedPreferences(this, PREFIX);
    }

    @Override
    public HashMap<String, String> getData() {
        return QuestionService.getInstance().getData(this, PREFIX);
    }

    @Override
    public void drawQuestions(LinkedList<QuestionService.QuestionParams> list) {
        mFragment.drawQuestions(list);
    }

    @Override
    public void drawErrors(Collection<QuestionService.QuestionValidationInfo> errors) {
        mFragment.drawErrors(errors);
    }

    @Override
    public void removeErrors(List<QuestionService.QuestionParams> questions) {
        mFragment.removeErrors(questions);
    }

    @Override
    public void close() {
        this.finish();
    }

    @Override
    public void showMessage(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.email_verification_top_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_log_out) {
            baseHelper.userLogout();
        }

        return super.onOptionsItemSelected(item);
    }
}