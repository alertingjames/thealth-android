package com.app.thealth.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.app.thealth.R;
import com.app.thealth.commons.Commons;
import com.app.thealth.models.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class LikeListAdapter extends BaseAdapter {

    private Context _context;
    private ArrayList<User> _datas = new ArrayList<>();
    private ArrayList<User> _alldatas = new ArrayList<>();

    public LikeListAdapter(Context context){

        super();
        this._context = context;
    }

    public void setDatas(ArrayList<User> datas) {

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
            convertView = inflater.inflate(R.layout.item_user_list, parent, false);

            holder.pictureBox = (CircleImageView) convertView.findViewById(R.id.pictureBox);
            holder.nameBox = (TextView) convertView.findViewById(R.id.nameBox);
            holder.addressBox = (TextView) convertView.findViewById(R.id.addressBox);
            holder.connectButton = (TextView) convertView.findViewById(R.id.connectButton);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final User entity = (User) _datas.get(position);

        holder.nameBox.setText(entity.get_name());
        holder.addressBox.setText(entity.get_address());
        Picasso.with(_context)
                .load(entity.get_photoUrl())
                .error(R.drawable.noresult)
                .placeholder(R.drawable.noresult)
                .into(holder.pictureBox, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.progressBar.setVisibility(View.INVISIBLE);
                    }
                    @Override
                    public void onError() {
                        holder.progressBar.setVisibility(View.INVISIBLE);
                    }
                });

//        if(entity.is_isFriend()){
//            holder.connectButton.setBackgroundResource(R.drawable.green_round_stroke);
//            holder.connectButton.setTextColor(_context.getResources().getColor(R.color.green_color_picker));
//        }else {
//            holder.connectButton.setBackgroundResource(R.drawable.green_round_fill);
//            holder.connectButton.setTextColor(Color.WHITE);
//        }

        holder.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(!entity.is_isFriend()){
//                    _context.requestFriend(entity);
//                }
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.user = entity;
//                Intent intent = new Intent(_context, UserProfileActivity.class);
//                _context.startActivity(intent);
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
            for (User user : _alldatas){
                if (user instanceof User) {
                    String value = ((User) user).get_name().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(user);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {

        CircleImageView pictureBox;
        TextView nameBox, addressBox, connectButton;
        ProgressBar progressBar;
    }
}





