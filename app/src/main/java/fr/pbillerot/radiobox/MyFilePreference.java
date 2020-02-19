package fr.pbillerot.radiobox;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.ListPreference;

/**
 * Non utilisé pour l'instant
 */
public class MyFilePreference extends ListPreference {
    public MyFilePreference(Context context) { super(context); }
    public MyFilePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEntries(entries());
        setEntryValues(entryValues());
    }
    private CharSequence[] entries() {
        //action to provide entry data in char sequence array for list
        String myEntries[] = {"one", "two", "three", "four", "five"};

        return myEntries;
    }

    private CharSequence[] entryValues() {
        //action to provide value data for list

        String myEntryValues[] = {"ten", "twenty", "thirty", "forty", "fifty"};
        return myEntryValues;
    }

}
