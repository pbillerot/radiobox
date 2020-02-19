package fr.pbillerot.radiobox;

import android.content.Context;
import android.util.AttributeSet;

/**
 * non utilisé
 */
public class MyEditTextPreference extends androidx.preference.EditTextPreference {

    public MyEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyEditTextPreference(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        if(super.getSummary() == null) return null;

        String summary = super.getSummary().toString();
        return String.format(summary, getText());
    }
}
