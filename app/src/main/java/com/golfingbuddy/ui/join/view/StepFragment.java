package com.golfingbuddy.ui.join.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.golfingbuddy.R;
import com.golfingbuddy.ui.fbconnect.TermsOfUseActivity;
import com.golfingbuddy.ui.join.interfaces.OnTmpAvatarUploaded;
import com.golfingbuddy.ui.join.interfaces.Presenter;
import com.golfingbuddy.ui.search.classes.QuestionObject;
import com.golfingbuddy.ui.search.classes.SectionObject;
import com.golfingbuddy.ui.search.fragments.QuestionFormFragment;
import com.golfingbuddy.ui.search.service.QuestionService;
import com.golfingbuddy.utils.form.AvatarField;
import com.golfingbuddy.utils.form.FormField;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jk on 1/22/16.
 */
public class StepFragment extends QuestionFormFragment implements View.OnClickListener {
    protected String prefix;
    protected Presenter presenter;
    private Uri tmpImageUri;
    AvatarField avatar;
    CheckBoxPreference termsOfUse;

    public static StepFragment newInstance(String prefix) {

        StepFragment fragment = new StepFragment();
        Bundle args = new Bundle();
        args.putString("prefix", prefix);
        fragment.setArguments(args);

        return fragment;
    }
    //
    public StepFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if ( bundle != null && bundle.containsKey("prefix") ) {
            prefix = bundle.getString("prefix");
        }

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
        setPreferenceScreen(screen);
    }

    public void setPreferencePrefix(String prefix) {
        this.prefix = prefix;
    }


    public Uri getTmpImageUri() {
        return this.tmpImageUri;
    }

    @Override
    protected String getPreferencePrefix() {
        return this.prefix;
    }

    protected void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected void loadData() {
        // unused
    }


    protected AvatarField getAvatar() {
        return avatar;
    }

    public void drawQuestions(SectionObject[] sections, QuestionObject[] questions)
    {
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();

        if( sections == null || sections.length == 0)
        {
            renderQuestions(screen, questions);
        }
        else {
            for (SectionObject section : sections) {
                renderSection(screen, section, questions);
            }

            // render questions without section
            LinkedList<QuestionObject> list = new LinkedList<QuestionObject>();

            for (QuestionObject question : questions) {
                if ( question.getSection() == null ) {
                    list.add(question);
                }
            }

            if ( list.size() > 0 ) {
                QuestionObject[] array = new QuestionObject[0];
                renderQuestions(screen, list.toArray(array));
            }
        }
    }

    public void removeErrors(List<QuestionObject> questions)
    {
        if ( questions == null || questions.size() == 0 )
        {
            return;
        }

        PreferenceScreen screen = getPreferenceScreen();

        for (QuestionObject question:questions)
        {
            Preference preference = screen.findPreference(prefix + question.getName());

            if ( preference != null )
            {
                ( (FormField) preference).setError(null);
            }
        }
    }

    public void drawErrors(Collection<QuestionService.QuestionValidationInfo> errors)
    {
        if ( errors == null || errors.size() == 0 )
        {
            return;
        }

        PreferenceScreen screen = getPreferenceScreen();

        for (QuestionService.QuestionValidationInfo error:errors)
        {
            if ( error != null && !error.getIsValid()) {

                Preference preference = screen.findPreference(prefix + error.getName());

                if ( preference != null && error.getMessage() != null )
                {
                    ( (FormField) preference).setError(error.getMessage());
                }
            }
        }
    }

    protected void renderSection(PreferenceGroup screen, SectionObject section, QuestionObject[] questions) {

        if ( section == null )
        {
            return;
        }

        if ( questions == null || questions.length == 0 )
        {
            return;
        }

        PreferenceCategory category = new PreferenceCategory(getActivity());
        category.setTitle((String) section.getLabel());

        screen.addPreference(category);

        List<QuestionObject> sectionQuestions = new LinkedList<QuestionObject>();

        for ( QuestionObject question:questions ) {
            if (question == null || question.getSection() == null || !question.getSection().equals(section.getName())) {
                continue;
            }

            sectionQuestions.add(question);
        }

        if ( sectionQuestions.size() > 0 ) {

            QuestionObject[] array = new QuestionObject[sectionQuestions.size()];
            renderQuestions(screen, sectionQuestions.toArray(array));
        }
    }

    protected void renderQuestions(PreferenceGroup screen, QuestionObject[] questions) {

        if ( questions == null || questions.length == 0 )
        {
            return;
        }

        for ( QuestionObject question:questions )
        {
            Preference preference = QuestionService.getInstance().getFormField(getActivity(),
                    new QuestionService.QuestionParams(question), getPreferencePrefix(), screen);

            if ( preference != null ) {
                screen.addPreference(preference);
                ((FormField) preference).setValue(question.getValue());

                if ( QuestionService.getInstance().QUESTION_PRESENTATION_TERMS_OF_USE.equals(question.getPresentation()) )
                {
                    termsOfUse = (CheckBoxPreference) preference;
                }
                if ( preference instanceof AvatarField ) {
                    avatar = (AvatarField) preference;
                    avatar.setOnDialogButtonClickListener(new AvatarField.OnClickListener() {

                        public void onCameraClick(DialogInterface arg0, int arg1) {

                            File f = null;
                            try {
                                f = createTmpImageFile();
                            } catch (IOException ex) {
                                Log.i(" Error on create tmp file  ", ex.getMessage(), ex);
                            }

                            if (f != null) {
                                Intent pictureActionIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                pictureActionIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTmpImageUri());
                                startActivityForResult(pictureActionIntent, Crop.REQUEST_CAMERA);
                            }
                        }

                        public void onGallaryClick(DialogInterface arg0, int arg1) {
                            Intent pictureActionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            pictureActionIntent.setType("image/*");
                            pictureActionIntent.putExtra("return-data", true);
                            startActivityForResult(pictureActionIntent, Crop.REQUEST_PICK);
                        }
                    });
                }

                preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {

                        if (StepFragment.this.presenter != null) {
                            HashMap<String, String> value = QuestionService.getInstance().getData(StepFragment.this.getActivity(), StepFragment.this.getPreferencePrefix(), preference.getKey(), newValue);
                            StepFragment.this.presenter.validate(value, false);
                        }

                        return true;
                    }
                });
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);

        if (resultCode == getActivity().RESULT_OK) {

            switch (requestCode) {
                case Crop.REQUEST_PICK:

                    if ( result == null )
                    {
                        return;
                    }

                    beginCrop(result.getData());
                    break;
                case Crop.REQUEST_CAMERA:
                    Uri imageUri = getTmpImageUri();
                    Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped.jpg"));
                    Crop.of(Uri.fromFile(new File(imageUri.getPath())), destination).asSquare().start(getActivity(), this, Crop.REQUEST_CROP);
                    break;
                case Crop.REQUEST_CROP:

                    if ( result == null )
                    {
                        return;
                    }

                    handleCrop(resultCode, result);
                    break;
            }
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped.jpg"));
        Crop.of(source, destination).asSquare().start(getActivity(), this, Crop.REQUEST_CROP);
    }

    private void handleCrop(int resultCode, final Intent result) {
        if (resultCode == getActivity().RESULT_OK) {


            if ( getAvatar() != null && getActivity() != null )
            {
                Uri uri = Crop.getOutput(result);
                this.presenter.uploadTmpAvatar(uri, new OnTmpAvatarUploaded() {
                    @Override
                    public void onSuccess(String message) {
                        getAvatar().setValue(Crop.getOutput(result).toString());
                        getAvatar().setError(null);
                    }

                    @Override
                    public void onFailed(String message) {
                        if( message == null )
                        {
                            message = StepFragment.this.getResources().getString(R.string.upload_tmp_avatar_failed);
                        }

                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Exception ex) {
                        Toast.makeText(getActivity(), StepFragment.this.getResources().getString(R.string.error_on_upload_avatar), Toast.LENGTH_LONG).show();
                    }
                });
            }

            //new AvatarChangeTask(Crop.getOutput(result)).execute();
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(getActivity(), Crop.getError(result).getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private File getTmpDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "skadatexapp");

            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    Toast.makeText(getActivity(), "Failed to create directory", Toast.LENGTH_LONG).show();

                    return null;
                }
            }

        } else {
            Toast.makeText(getActivity(), "External storage is not mounted READ/WRITE.", Toast.LENGTH_LONG).show();
        }

        return storageDir;
    }

    private File createTmpImageFile() throws IOException {
        File tmpDir = getTmpDir();
        File f = File.createTempFile("join_temp_avatar", ".jpg", tmpDir);

        tmpImageUri = Uri.fromFile(f);

        return f;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.terms_agree) {

            if ( StepFragment.this.getActivity() == null || termsOfUse == null )
            {
                return;
            }

            termsOfUse.setIcon(null);
            termsOfUse.setIcon(new ColorDrawable(Color.TRANSPARENT));
            termsOfUse.setChecked(true);

            Intent in = new Intent(StepFragment.this.getActivity(), TermsOfUseActivity.class);
            startActivity(in);
        }
    }
}
