package com.codebrew.clikat.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.codebrew.clikat.R;
import com.codebrew.clikat.data.constants.AppConstants;
import com.codebrew.clikat.databinding.ItemRateOrderBinding;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.ExampleCommon;
import com.codebrew.clikat.modal.PojoSignUp;
import com.codebrew.clikat.modal.other.RateMyProductModel;
import com.codebrew.clikat.module.order_detail.OrderDetailActivity;
import com.codebrew.clikat.preferences.DataNames;
import com.codebrew.clikat.preferences.Prefs;
import com.codebrew.clikat.retrofit.RestClient;
import com.codebrew.clikat.utils.GeneralFunctions;
import com.codebrew.clikat.utils.ProgressBarDialog;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;
import com.codebrew.clikat.utils.configurations.TextConfig;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Created by cbl80 on 20/4/16.
 */
public class RateAdapter extends RecyclerView.Adapter<RateAdapter.View_holder> {
    private Context mContext;
    private List<RateMyProductModel.DataBean.OrderListBean> list;

    private int textHead= Color.parseColor(Configurations.colors.textListHead);

    private TextConfig textConfig=Configurations.strings;

    public RateAdapter(Context context, List<RateMyProductModel.DataBean.OrderListBean> list) {
        this.mContext = context;
        this.list = list;
    }

    @Override
    public View_holder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemRateOrderBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_rate_order, parent, false);
        binding.setColor(Configurations.colors);
        return new View_holder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(View_holder holder, int position) {
        RateMyProductModel.DataBean.OrderListBean orderHistory = list.get(position);
        holder.rvImages.setAdapter(new ImagesAdapter( list.get(position).getProduct(),mContext));

        String s = (orderHistory.getNet_amount() + orderHistory.getHandling_admin() + orderHistory.getDelivery_charges() + orderHistory.getHandling_supplier()) + " " + AppConstants.Companion.getCURRENCY_SYMBOL() + " . " + orderHistory.getProduct().size() + " " + mContext.getString(R.string.items);
        holder.tvPrice.setText(s);
        holder.etComment.setText("");
        holder.ratingBar.setRating(0);
        String dd=orderHistory.getCreated_on().replace("T"," ");
        dd=dd.replace("Z","");
        SpannableString deliveryDate = SpannableString.valueOf("");
        try {
            deliveryDate = setColor(mContext.getString(R.string.placed_on) + "\n"
                    + GeneralFunctions.getFormattedDateSide(dd));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvPlaced.setText(deliveryDate);


        SpannableString order = setColor(mContext.getString(R.string.order_no,textConfig.order) + "\n" + orderHistory.getOrder_id());
        holder.tvOrderNo.setText(order);
         //dd=orderHistory.getDeliveredOn().replace("T"," ");
        dd=dd.replace("Z","");
         deliveryDate = SpannableString.valueOf("");
        try {
            deliveryDate = setColor(mContext.getString(R.string.delivered_on) + "\n"
                    + GeneralFunctions.getFormattedDateSide(dd));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvDeliveryDate.setText(deliveryDate);

        holder.tvStatus.setText(StaticFunction.INSTANCE.statusProduct(list.get(position).getStatus(),0,-1, mContext,""));
        StaticFunction.INSTANCE.colorStatusProduct(holder.tvStatus,
                list.get(position).getStatus(), mContext, false);
    }

    private SpannableString setColor(String string) {
        SpannableString newString = new SpannableString(string);
        int index = string.indexOf("\n") + 1;
        newString.setSpan(new ForegroundColorSpan(textHead), index, string.length(),
                0);
        return newString;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class View_holder extends RecyclerView.ViewHolder {
        RecyclerView rvImages;
        TextView tvStatus;
        TextView tvPrice;
        TextView tvPlaced;
        TextView tvOrderNo;
        TextView tvDeliveryDate;
        RatingBar ratingBar;
        EditText etComment;
        TextView tvSubmit;


        public View_holder(final View itemView) {
            super(itemView);

            rvImages=itemView.findViewById(R.id.rvImages);
            tvStatus=itemView.findViewById(R.id.tvStatus);
            tvPrice=itemView.findViewById(R.id.tv_total_prod);
            tvPlaced=itemView.findViewById(R.id.tvPlaced);
            tvOrderNo=itemView.findViewById(R.id.tvOrderNo);
            tvDeliveryDate=itemView.findViewById(R.id.tvDeliveryDate);
            ratingBar=itemView.findViewById(R.id.ratingBar);
            etComment=itemView.findViewById(R.id.etComment);
            tvSubmit=itemView.findViewById(R.id.tvSubmit);


            rvImages.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

      /*      tvStatus.setTypeface(AppGlobal.semi_bold);
            tvPrice.setTypeface(AppGlobal.semi_bold);
            etComment.setTypeface(AppGlobal.regular);
            tvPlaced.setTypeface(AppGlobal.regular);
            tvOrderNo.setTypeface(AppGlobal.regular);
            tvDeliveryDate.setTypeface(AppGlobal.regular);
            tvSubmit.setTypeface(AppGlobal.regular);*/

            itemView.setOnClickListener(v -> {

                ArrayList<Integer> orderId=new ArrayList<>();

                orderId.add(list.get(getAdapterPosition()).getOrder_id());

                Prefs.with(mContext).save(DataNames.ORDER_DETAIL, list.get(getAdapterPosition()));
                mContext.startActivity(new Intent(mContext, OrderDetailActivity.class)
                        .putExtra(DataNames.REORDER_BUTTON, true).putIntegerArrayListExtra("orderId",orderId));

            });

            tvSubmit.setOnClickListener(v -> {
                if (!etComment.getText().toString().equals("")
                        || ratingBar.getRating() != 0f) {
                    orderRateApi(ratingBar.getRating(), itemView, getAdapterPosition()
                            , etComment.getText().toString().trim());
                } else {
                    etComment.setError(mContext.getString(R.string.empty));
                }
            });


        }
    }

    private void orderRateApi(float rating, final View itemView, final int adapterPosition, String trim) {
        PojoSignUp signUp = Prefs.with(mContext).getObject(DataNames.USER_DATA, PojoSignUp.class);
        final ProgressBarDialog barDialog = new ProgressBarDialog(mContext);
        barDialog.show();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("languageId", "" + StaticFunction.INSTANCE.getLanguage(mContext));
        hashMap.put("orderId", "" + list.get(adapterPosition).getOrder_id());
        hashMap.put("accessToken", signUp.data.access_token);
        hashMap.put("rating", "" + rating);
        hashMap.put("comment",""+trim);
        Call<ExampleCommon> call = RestClient.getModalApiService(mContext).orderRating(hashMap);

        call.enqueue(new Callback<ExampleCommon>() {
            @Override
            public void onResponse(Call<ExampleCommon> call, Response<ExampleCommon> response) {
                barDialog.dismiss();
                if (response.code() == 200) {

                    ExampleCommon success = response.body();
                    list.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                    notifyItemRangeChanged(adapterPosition, getItemCount());
                    GeneralFunctions.showSnackBar(itemView, success.getMessage(), mContext);

                }
            }

            @Override
            public void onFailure(Call<ExampleCommon> call, Throwable t) {
                barDialog.show();
            }
        });
    }
}
