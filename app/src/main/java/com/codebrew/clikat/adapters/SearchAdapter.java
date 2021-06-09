package com.codebrew.clikat.adapters;

import android.content.Context;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codebrew.clikat.R;
import com.codebrew.clikat.databinding.ItemMutliSearchBinding;
import com.codebrew.clikat.modal.AppGlobal;
import com.codebrew.clikat.modal.SearchItem;
import com.codebrew.clikat.utils.configurations.Configurations;

import java.util.List;

/*
 * Created by cbl80 on 6/5/16.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.View_holder> {
    private Context mContext;
    private List<SearchItem> list;

    public SearchAdapter(Context context, List<SearchItem> list) {
        this.mContext = context;
        this.list=list;
    }

    @Override
    public View_holder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemMutliSearchBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_mutli_search, parent, false);
        binding.setColor(Configurations.colors);
        return new View_holder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(View_holder holder, int position) {
        SearchItem searchItem=list.get(position);
        holder.tvTitle.setText(mContext.getString(R.string.results_for)+"\""+searchItem.name+"\"");
        holder.recyclerview.setAdapter(new SearchSubAdapter(mContext,searchItem.result));
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class View_holder extends RecyclerView.ViewHolder {
        RecyclerView recyclerview;
        TextView tvTitle;


        public View_holder(View itemView) {
            super(itemView);

            tvTitle=itemView.findViewById(R.id.tvTitle);
            recyclerview=itemView.findViewById(R.id.recyclerview);

            recyclerview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        /*    tvTitle.setTypeface(AppGlobal.semi_bold);*/


        }
    }
}