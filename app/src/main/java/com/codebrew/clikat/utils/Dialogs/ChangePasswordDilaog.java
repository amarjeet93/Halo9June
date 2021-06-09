package com.codebrew.clikat.utils.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.ExampleCommon;
import com.codebrew.clikat.modal.PojoSignUp;
import com.codebrew.clikat.preferences.DataNames;
import com.codebrew.clikat.preferences.Prefs;
import com.codebrew.clikat.retrofit.RestClient;
import com.codebrew.clikat.utils.ClikatConstants;
import com.codebrew.clikat.utils.GeneralFunctions;
import com.codebrew.clikat.utils.ProgressBarDialog;
import com.codebrew.clikat.utils.StaticFunction;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Created by cbl80 on 18/5/16.
 */
public class ChangePasswordDilaog extends Dialog {


    public OnOkClickListener mListener;
    private Boolean mIsDismiss;
    private Context context;
    private TextInputEditText etOldpassword;
    private TextInputEditText etnewpassword;
    private TextInputEditText etConfirmpassword;


    private TextInputLayout inputOldpassword,inputnewpassword,inputConfirmpassword;

    public ChangePasswordDilaog(Context context
            , boolean isDismiss, OnOkClickListener onClick) {
        super(context, R.style.TransparentDilaog);
        mListener = onClick;
        mIsDismiss = isDismiss;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_change_password);
        setCancelable(mIsDismiss);
        ImageView ivCross = findViewById(R.id.ivCross);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setTypeface(AppGlobal.regular);

        etOldpassword = findViewById(R.id.etOldPassword);
        etnewpassword = findViewById(R.id.etNewPassword);
        etConfirmpassword = findViewById(R.id.etConfirmPassword);

        inputOldpassword = findViewById(R.id.inputOldpassword);
        inputnewpassword = findViewById(R.id.inputnewpassword);
        inputConfirmpassword = findViewById(R.id.inputConfirmpassword);
        assert ivCross != null;
        ivCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        TextView tvGo = findViewById(R.id.tvGo);
        assert tvGo != null;
        tvGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StaticFunction.INSTANCE.isInternetConnected(context)) {
                    GeneralFunctions.hideKeyboard(etOldpassword, context);
                    if (etOldpassword.getText().toString().trim().equals("")) {
                        inputnewpassword.setError(null);
                        inputConfirmpassword.setError(null);
                        inputOldpassword.requestFocus();
                        inputOldpassword.setError(context.getString(R.string.empty));
                    } else if (etnewpassword.getText().toString().trim().equals("")) {
                        inputOldpassword.setError(null);
                        inputConfirmpassword.setError(null);
                        inputnewpassword.requestFocus();
                        inputnewpassword.setError(context.getString(R.string.empty));
                    } else if (etnewpassword.getText().toString().trim().length() < 6) {
                        inputOldpassword.setError(null);
                        inputConfirmpassword.setError(null);
                        inputnewpassword.requestFocus();
                        inputnewpassword.setError(context.getString(R.string.passwrd_lenght));
                    } else if (etConfirmpassword.getText().toString().trim().equals("")) {
                        inputOldpassword.setError(null);
                        inputnewpassword.setError(null);
                        inputConfirmpassword.requestFocus();
                        inputConfirmpassword.setError(context.getString(R.string.empty));
                    } else if (!etConfirmpassword.getText().toString().trim().equals(etnewpassword.getText().toString().trim())) {
                        inputOldpassword.setError(null);
                        inputnewpassword.setError(null);
                        inputConfirmpassword.requestFocus();
                        inputConfirmpassword.setError(context.getString(R.string.password_match_error));
                    } else {
                        inputConfirmpassword.setError(null);
                        change_passwordApi();
                        if (mListener != null) {
                            mListener.onButtonClick();
                        }
                    }


                } else {
                    StaticFunction.INSTANCE.showNoInternetDialog(context);
                }
            }


        });

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                GeneralFunctions.hideKeyboard(etOldpassword, context);
            }
        });

    }

    private void change_passwordApi() {
        final ProgressBarDialog barDialog=new ProgressBarDialog(context);
        barDialog.show();
        PojoSignUp signUp = Prefs.with(context).getObject(DataNames.USER_DATA, PojoSignUp.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("accessToken", signUp.data.access_token);
        hashMap.put("oldPassword", etOldpassword.getText().toString().trim());
        hashMap.put("newPassword", etnewpassword.getText().toString().trim());
        hashMap.put("languageId", "" + StaticFunction.INSTANCE.getLanguage(context));
        Call<ExampleCommon> changepassword = RestClient.getModalApiService(context).changePassword(hashMap);

        changepassword.enqueue(new Callback<ExampleCommon>() {
            @Override
            public void onResponse(Call<ExampleCommon> call, Response<ExampleCommon> response) {
                barDialog.dismiss();
                if (response.code() == 200) {
                    ExampleCommon pojoSignUp = response.body();
                    if (pojoSignUp.getStatus() == ClikatConstants.STATUS_SUCCESS) {
                        GeneralFunctions.showSnackBar(getCurrentFocus(), pojoSignUp.getMessage(), context);
                        dismiss();

                    } else
                        GeneralFunctions.showSnackBar(getCurrentFocus(), pojoSignUp.getMessage(), context);
                }
            }

            @Override
            public void onFailure(Call<ExampleCommon> call, Throwable t) {
                GeneralFunctions.showSnackBar(getCurrentFocus(), t.getMessage(), context);
                barDialog.dismiss();
            }
        });
    }

    @Override
    public void setOnKeyListener(OnKeyListener onKeyListener) {
        super.setOnKeyListener(onKeyListener);
    }


    public interface OnOkClickListener {
        void onButtonClick();
    }
}
