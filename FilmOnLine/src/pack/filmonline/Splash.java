package pack.filmonline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import bluepixel.filmonlineitaliano.R;

public class Splash extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.splash);
	final int welcomeScreenDisplay=2800;

	Thread welcomeThread=new Thread() {
	    int wait=0;

	    @Override
	    public void run() {
		try {
		    super.run();
		    while (wait < welcomeScreenDisplay) {
			sleep(100);
			wait+=100;
		    }
		}
		catch (Exception e) {
		    startActivity(new Intent(Splash.this, FilmCompletiActivity.class));
		    finish();
		}
		finally {
		    startActivity(new Intent(Splash.this, FilmCompletiActivity.class));
		    finish();
		}
	    }
	};
	welcomeThread.start();
    }

}
