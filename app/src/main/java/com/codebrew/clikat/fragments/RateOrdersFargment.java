package com.codebrew.clikat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.codebrew.clikat.R;
import com.codebrew.clikat.activities.MainActivity;
import com.codebrew.clikat.adapters.RateAdapter;
import com.codebrew.clikat.modal.PojoSignUp;
import com.codebrew.clikat.modal.other.RateMyProductModel;
import com.codebrew.clikat.preferences.DataNames;
import com.codebrew.clikat.preferences.Prefs;
import com.codebrew.clikat.retrofit.RestClient;
import com.codebrew.clikat.utils.ConnectionDetector;
import com.codebrew.clikat.utils.ProgressBarDialog;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;
import com.codebrew.clikat.utils.configurations.TextConfig;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Created by Ankit Jindal on 20/4/16.
 */
public class RateOrdersFargment extends BaseOrderFrag {
    private RateMyProductModel exampleOrderHistory;
    private RateAdapter rateAdapter;

    private TextConfig textConfig= Configurations.strings;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (exampleOrderHistory != null) {
            setdata();
        }
        else if (StaticFunction.INSTANCE.isInternetConnected(getActivity())) {
            getRateMyOrders();
        }else
        {
            StaticFunction.INSTANCE.showNoInternetDialog(getActivity());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void getRateMyOrders() {
        final ProgressBarDialog barDialog = new ProgressBarDialog(getActivity());
        barDialog.show();
        HashMap<String, String> hashMap = new HashMap<>();
        PojoSignUp signUp = Prefs.with(getActivity()).getObject(DataNames.USER_DATA, PojoSignUp.class);
        hashMap.put("accessToken", signUp.data.access_token);
        hashMap.put("languageId", StaticFunction.INSTANCE.getLanguage(getActivity())+"");
        Call<RateMyProductModel> historyCall = RestClient.getModalApiService(getActivity()).rateMyOrder(hashMap);

        historyCall.enqueue(new Callback<RateMyProductModel>() {
            @Override
            public void onResponse(Call<RateMyProductModel> call, Response<RateMyProductModel> response) {
                barDialog.dismiss();
                if (response.body().getStatus() == 200) {
                    exampleOrderHistory = response.body();

                    if(exampleOrderHistory.getData().getOrderList()!=null  && exampleOrderHistory.getData().getOrderList().size()>0)
                    {

                     //   rvOrders.setVisibility(View.VISIBLE);
                    //    noData.setVisibility(View.GONE);
                        List<RateMyProductModel.DataBean.OrderListBean> orderHistory2List = exampleOrderHistory.getData().getOrderList();

                        rateAdapter= new RateAdapter(getActivity(), orderHistory2List);
                        setdata();
                    }
                    else
                    {
                      //  rvOrders.setVisibility(View.GONE);
                      //  noData.setVisibility(View.VISIBLE);
                    }
                }


                else if (response.body().getStatus()==401)
                {
                    new ConnectionDetector(getActivity()).loginExpiredDialog();
                }
            }

            @Override
            public void onFailure(Call<RateMyProductModel> call, Throwable t) {
                barDialog.dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).tbShare.setVisibility(View.GONE);
        ((MainActivity) getActivity()).tvTitleMain.setText(getString(R.string.rate_my_order,textConfig.order));
        ((MainActivity)getActivity()).setSupplierImage(false,"",0,"",0,0);
        ((MainActivity) getActivity()).seticonsSidepnael();
    }

    private void setdata() {
      //  rvOrders.setNestedScrollingEnabled(false);
        setAdapter(rateAdapter, getString(R.string.rate_my_order,textConfig.order), getResources().getString(R.string.no_order_found));
    }

}
