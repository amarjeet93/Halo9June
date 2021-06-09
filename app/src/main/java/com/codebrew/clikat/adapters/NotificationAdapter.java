package com.codebrew.clikat.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.activities.MainActivity;
import com.codebrew.clikat.databinding.ItemPromotionsBinding;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.ExampleCommon;
import com.codebrew.clikat.modal.GetTimeAgo;
import com.codebrew.clikat.modal.Notification;
import com.codebrew.clikat.module.order_detail.OrderDetailActivity;
import com.codebrew.clikat.retrofit.RestClient;
import com.codebrew.clikat.utils.StaticFunction;
import com.codebrew.clikat.utils.configurations.Configurations;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Created by cbl80 on 4/5/16.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.View_holder> {
    private Context mContext;
    private List<Notification> list;

    public NotificationAdapter(Context context, List<Notification> list) {
        this.mContext = context;
        this.list = list;
    }

    @Override
    public View_holder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemPromotionsBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_promotions, parent, false);
        binding.setColor(Configurations.colors);
        return new View_holder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(View_holder holder, int position) {
        Notification notification = list.get(position);


        StaticFunction.INSTANCE.loadImage(list.get(position).getLogo(),holder.sdvProduct,false);

 /*       Glide.with(mContext)
                .load(list.get(position).getLogo())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder_product)
                        .fitCenter().override(90,90))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.sdvProduct);*/
        holder.tvDesc.setText(notification.getNotification_message());
        holder.tvTime.setText(GetTimeAgo.getTimeAgo(getTime(notification.getCreated_on()), mContext));
    }

    private Long getTime(String dateString) {
        String s = dateString.replace("T", " ");
        String s1 = s.replace("Z", " ");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mm:sss", Locale.getDefault());/*2016-06-06T13:00:53.000Z*/
        Date myDate = new Date(); // Default Value.
        try {
            myDate = sdf.parse(s1);
        } catch (ParseException e) {
            // Do Something on Error.
        }
        return myDate.getTime();
    }

    private void clearSingleNoificationApi(int id, final int pos) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("accessToken", StaticFunction.INSTANCE.getAccesstoken(mContext));
        hashMap.put("notificationId", "" + id);

        Call<ExampleCommon> notificationCall = RestClient.getModalApiService(mContext).clearOneNotification(hashMap);


        notificationCall.enqueue(new Callback<ExampleCommon>() {
            @Override
            public void onResponse(Call<ExampleCommon> call, Response<ExampleCommon> response) {
                if (response.code() == 200) {
                    list.remove(pos);
                    notifyItemRemoved(pos);
                    notifyItemChanged(getItemCount());
                }
            }

            @Override
            public void onFailure(Call<ExampleCommon> call, Throwable t) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class View_holder extends RecyclerView.ViewHolder {
        TextView tvDesc;
        TextView tvTime;
        ImageView sdvProduct;

        public View_holder(View itemView) {
            super(itemView);

            sdvProduct=itemView.findViewById(R.id.sdvProduct);
            tvTime=itemView.findViewById(R.id.tvTime);
            tvDesc=itemView.findViewById(R.id.tvDesc);

       /*     tvDesc.setTypeface(AppGlobal.regular);
            tvTime.setTypeface(AppGlobal.semi_bold);*/

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearSingleNoificationApi(list.get(getAdapterPosition()).getId(), getAdapterPosition());
                    mContext.startActivity(new Intent(mContext, OrderDetailActivity.class)
                            .putExtra("OrderId", list.get(getAdapterPosition()).getOrder_id()));
                    ((MainActivity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });


        }
    }
}
