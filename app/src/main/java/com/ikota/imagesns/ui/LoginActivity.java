package com.ikota.imagesns.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.VolleyError;
import com.ikota.imagesns.R;
import com.ikota.imagesns.net.FlickerApiCaller;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kota on 2015/05/26.
 * Login screen Activity.
 */
public class LoginActivity extends ActionBarActivity{

    // UI references.
    private EditText mNameView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private ToggleButton mLoginToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mNameView = (EditText) findViewById(R.id.name);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        final Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mLoginToggle = (ToggleButton) findViewById(R.id.login_toggle);
        mLoginToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {// switch to log in form
                    mNameView.setVisibility(View.GONE);
                    mEmailSignInButton.setText(getResources().getString(R.string.login));
                } else {// switch to register form
                    mNameView.setVisibility(View.VISIBLE);
                    mEmailSignInButton.setText(getResources().getString(R.string.register));
                }
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String name = mNameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if(!mLoginToggle.isChecked() && TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            if(mLoginToggle.isChecked()) {
                // attempt login
                loginAccount(email, password);
            } else {
                // attempt register
                createAccount(name, email, password);
            }
        }
    }
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private void createAccount(final String name, final String email, String password) {
        FlickerApiCaller.getInstance().registerAccount(LoginActivity.this, name, email, password,
                new FlickerApiCaller.ApiListener() {
                    @Override
                    public void onPostExecute(String response) {
                        showProgress(false);
                        Context context = LoginActivity.this;
                        try {
                            JSONObject jo = new JSONObject(response);
                            boolean stat = jo.getBoolean("stat");
                            if(stat) {
                                String user_id = jo.getString("id");
                                Util.saveUserId(context, user_id);
                                Util.saveUserName(context, name);
                                //Util.saveUserEmail(context, email);
                                // start main activity
                                Intent intent = new Intent(LoginActivity.this, ImageListActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in_depth, R.anim.fade_out_depth);
                                finish();
                            } else {
                                Toast.makeText(context, getResources().getString(R.string.failed_crate_account), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, getResources().getString(R.string.failed_crate_account), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onErrorListener(VolleyError error) {
                        showProgress(false);
                        error.printStackTrace();
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.failed_crate_account),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginAccount(String email, String password) {
        FlickerApiCaller.getInstance().checkLoginAccount(LoginActivity.this, email, password, new FlickerApiCaller.ApiListener() {
            @Override
            public void onPostExecute(String response) {
                Context context = LoginActivity.this;
                try {
                    JSONObject jo = new JSONObject(response);
                    boolean stat = jo.getBoolean("stat");
                    if(stat) {
                        String user_id = jo.getString("id");
                        String name = jo.getString("name");
                        Util.saveUserId(context, user_id);
                        Util.saveUserName(context, name);

                        // start main activity
                        Intent intent = new Intent(LoginActivity.this, ImageListActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in_depth, R.anim.fade_out_depth);
                    } else {
                        Toast.makeText(context, getResources().getString(R.string.failed_crate_account),Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, getResources().getString(R.string.failed_crate_account),Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorListener(VolleyError error) {
                showProgress(false);
                error.printStackTrace();
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.failed_login),Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            mLoginToggle.setVisibility(show ? View.GONE : View.VISIBLE);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
