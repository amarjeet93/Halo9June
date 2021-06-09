package com.codebrew.clikat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.modal.other.ProductDataBean;
import com.codebrew.clikat.modal.other.RateMyProductModel;
import com.codebrew.clikat.modal.other.TrackOrderList;
import com.codebrew.clikat.module.rate_order.RatingRegions;
import com.codebrew.clikat.utils.GeneralFunctions;
import com.codebrew.clikat.utils.StaticFunction;

import java.util.ArrayList;
import java.util.List;


/*
 * Created by cbl80 on 20/4/16.
 */
public class RatingItemAdapter extends RecyclerView.Adapter<RatingItemAdapter.View_holder> {

    private Context mContext;
    private List<RatingRegions> list;
    ItemClickListener itemClickListener;

    public RatingItemAdapter(Context context, List<RatingRegions> list, ItemClickListener itemClickListener) {
        this.mContext = context;
        this.list = list;
        this.itemClickListener=itemClickListener;
    }


    @Override
    public View_holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_item_rating, parent, false);
        return new View_holder(view);
    }

    @Override
    public void onBindViewHolder(View_holder holder, int position) {
       holder.tv_item.setText(list.get(position).getName());
       holder.tv_item.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
            if(list.get(position).isSelected())
            {
                holder.tv_item.setBackgroundResource(R.drawable.background_grey);
                holder.tv_item.setTextColor(ContextCompat.getColor(mContext,R.color.black));
              list.get(position).setSelected(false);
            }else {
                holder.tv_item.setTextColor(ContextCompat.getColor(mContext,R.color.white));
                holder.tv_item.setBackgroundResource(R.drawable.back_black);
                list.get(position).setSelected(true);
            }
           }
       });
    }

    @Override
    public int getItemCount() {

            return list.size();

    }

    public interface ItemClickListener {

        void onItemClicked();
    }

    public class View_holder extends RecyclerView.ViewHolder {
        TextView tv_item;


        public View_holder(View itemView) {
            super(itemView);
            tv_item = itemView.findViewById(R.id.tv_item);
            }
    }
}
