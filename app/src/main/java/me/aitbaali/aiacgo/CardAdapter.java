package me.aitbaali.aiacgo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class CardAdapter extends ArrayAdapter<Card>{

    public CardAdapter(@NonNull Context context, int resource, @NonNull List<Card> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.activityadddate, parent, false);
        }
        TextView datetextView = (TextView) convertView.findViewById(R.id.the_date);
        TextView placeTextView = (TextView) convertView.findViewById(R.id.the_place);
        TextView timehegoTextView = (TextView) convertView.findViewById(R.id.timeHeGo);
        TextView priceTextView = (TextView) convertView.findViewById(R.id.the_price);
        CircleImageView photoImageView = (CircleImageView) convertView.findViewById(R.id.photo) ;
        //note in the card
        ImageView ColorImageView = (ImageView) convertView.findViewById(R.id.the_note) ;

        ColorImageView.setBackgroundResource(colorRand());

        //position
        Card card = getItem(position);
        //setdata
        datetextView.setText(card.getMdate());
        placeTextView.setText(card.getMplace());
        timehegoTextView.setText(card.getMtimeHeGo());
        priceTextView.setText(card.getMprice());

        //profil pic
        Glide.with(photoImageView.getContext())
                .load(card.getUser().getProfilePic())
                .into(photoImageView);



        return convertView;
    }

    public int colorRand(){
        Random rand = new Random();
        int randomNum = rand.nextInt((7) + 1);

        switch (randomNum){
            case 1: return R.color.Red;
            case 2: return R.color.GREEN;
            case 3: return R.color.BLUE;
            case 4: return R.color.colorPrimary;
            case 5: return R.color.CYAN;
            //case 6: return R.color.mauve;
            //case 7: return R.color.orange;

            default: return R.color.GREEN;
        }
    }

}
