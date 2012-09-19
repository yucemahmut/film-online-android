package pack.filmonline;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import bluepixel.filmonlineitaliano.R;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) { 
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.preferences);
    }

}
