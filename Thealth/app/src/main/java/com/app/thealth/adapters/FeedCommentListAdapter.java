package com.app.thealth.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.app.thealth.R;
import com.app.thealth.classes.DateMain;
import com.app.thealth.main.HomeActivity;
import com.app.thealth.models.FeedComment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringEscapeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedCommentListAdapter extends BaseAdapter {

    private Context _context;
    private ArrayList<FeedComment> _datas = new ArrayList<>();
    private ArrayList<FeedComment> _alldatas = new ArrayList<>();

    public FeedCommentListAdapter(Context context){

        super();
        this._context = context;
    }

    public void setDatas(ArrayList<FeedComment> datas) {

        _alldatas = datas;
        _datas.clear();
        _datas.addAll(_alldatas);
    }

    @Override
    public int getCount(){
        return _datas.size();
    }

    @Override
    public Object getItem(int position){
        return _datas.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        CustomHolder holder;

        if (convertView == null) {
            holder = new CustomHolder();

            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.item_comment, parent, false);

            holder.picture = (CircleImageView) convertView.findViewById(R.id.userPicture);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.comment = (TextView) convertView.findViewById(R.id.comment);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final FeedComment entity = (FeedComment) _datas.get(position);

        holder.name.setText(entity.getUserName());
        holder.date.setText(DateMain.getDateStr(_context, Long.parseLong(entity.getCommentedTime())));
        holder.comment.setText(StringEscapeUtils.unescapeJava(entity.getComment()));
        Picasso.with(_context)
                .load(entity.getUserPhoto())
                .error(R.drawable.user)
                .placeholder(R.drawable.user)
                .into(holder.picture, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.progressBar.setVisibility(View.INVISIBLE);
                    }
                    @Override
                    public void onError() {
                        holder.progressBar.setVisibility(View.INVISIBLE);
                    }
                });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return convertView;
    }

    public void filter(String charText){

        charText = charText.toLowerCase();
        _datas.clear();

        if(charText.length() == 0){
            _datas.addAll(_alldatas);
        }else {
            for (FeedComment comment : _alldatas){

                if (comment instanceof FeedComment) {

                    String value = ((FeedComment) comment).getUserName().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(comment);
                    }else {
                        value = ((FeedComment) comment).getComment().toLowerCase();
                        if (value.contains(charText)) {
                            _datas.add(comment);
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {

        CircleImageView picture;
        TextView name, comment, date;
        ProgressBar progressBar;
    }
}


