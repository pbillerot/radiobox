package fr.pbillerot.radiobox;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

class MyListAdapter extends ArrayAdapter<AudioItem> {
    private static final String TAG = "MyListAdapter";

    // Constructor
    public MyListAdapter(Context context,List<AudioItem> files) {
        super(context, 0, files);
        Log.i(TAG, "Create");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        AudioItem audioItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            // Création de la ViewHolder qui gère le remplissage des éléments d'un item de la listview
            ViewHolder viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        // Lookup view for data population
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        // Remplissage des éléments de l'item
        viewHolder.audio_name.setText(audioItem.audio_name);
        // Return the completed view to render on screen
        return convertView;
    }

    /**
     * Gestion des éléments d'un item de la ListView via une classe
     */
    private class ViewHolder {
        TextView audio_name;
        public ViewHolder(View view) {
            audio_name = view.findViewById(R.id.audio_name);
        }
    }

}
