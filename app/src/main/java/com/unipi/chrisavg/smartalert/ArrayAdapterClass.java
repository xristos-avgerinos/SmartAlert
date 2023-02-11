package com.unipi.chrisavg.smartalert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


class ArrayAdapterClass extends android.widget.ArrayAdapter<String> {

    Context context;

    List<String> rTitle = new ArrayList<>();
    List<String> rDescription = new ArrayList<>();
    List<Integer> rImgs = new ArrayList<>();
    ArrayList<String> temp_rTitle =new ArrayList<>();
    ArrayList<String> temp_rDescription = new ArrayList<>();
    ArrayList<Integer> temp_rImgs = new ArrayList<>();

    ArrayAdapterClass(Context c, List<String> t, List<String> d, List<Integer> i) {
        super(c, R.layout.row, R.id.textView1, t);
        this.context = c;
        this.rTitle = t;
        this.rDescription = d;
        this.rImgs = i;

        this.temp_rTitle.addAll(t);
        this.temp_rImgs.addAll(i);
        this.temp_rDescription.addAll(d);

    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.row, parent, false);
        ImageView images = row.findViewById(R.id.image);
        TextView myTitle = row.findViewById(R.id.textView1);
        TextView myDescription = row.findViewById(R.id.textView2);

        // now set our resources on views
        images.setImageResource(rImgs.get(position));
        myTitle.setText(rTitle.get(position));
        myDescription.setText(rDescription.get(position));

        return row;
    }

    //filter for search-view(search by title)
    public void filter(String charText){
        charText = charText.toLowerCase(Locale.getDefault());
        rTitle.clear();
        rImgs.clear();
        rDescription.clear();
        if (charText.length()==0){
            rTitle.addAll(temp_rTitle);
            rImgs.addAll(temp_rImgs);
            rDescription.addAll(temp_rDescription);
        }
        else {
            int counter = 0;
            for ( String title : temp_rTitle){
                if (title.toLowerCase(Locale.getDefault())
                        .contains(charText)){
                    rTitle.add(title);
                    rImgs.add(temp_rImgs.get(counter));
                    rDescription.add(temp_rDescription.get(counter));
                }
                counter++;
            }
        }
        notifyDataSetChanged();
    }
}