package pack.filmonline;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import bluepixel.filmonlineitaliano.R;

public class checkNewest extends Activity {

    private Document doc;
    private static String ERROR="ERROR";
    public static final int FO_NOTIFICATION_ID=07112010;
    public static final String NEWEST_TAG="FO_NEWEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	if (addictionsMade()) {
	    // Create the notification
	    Notification notification=new Notification(android.R.drawable.stat_notify_sync_noanim, getResources()
		.getString(R.string.notify_updates), System.currentTimeMillis());

	    // Create intent and add it to the notification
	    Intent intent=new Intent(getBaseContext(), FilmCompletiActivity.class);
	    intent.putExtra(NEWEST_TAG, true);
	    PendingIntent pi=PendingIntent.getActivity(this, 0, intent, 0);
	    notification.setLatestEventInfo(getApplicationContext(), getResources().getString(R.string.notify_updates),
		getResources().getString(R.string.notify_updates_details), pi);

	    // Display the Notification
	    NotificationManager nm=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	    nm.notify(FO_NOTIFICATION_ID, notification);
	}
	finish();
    }

    private String getStoredNewest() {
	SharedPreferences settings=getSharedPreferences(FilmCompletiActivity.PREFS_FILM_ONLINE, 0);
	return settings.getString(FilmCompletiActivity.PREFS_NEWEST_LIST_NAME, ERROR);
    }

    private String getNewNewest() {
	int tot_new=initialize(FilmCompletiActivity.NEW_LIST_URL);
	if (tot_new != 0) {
	    return createChildList();
	}
	return ERROR;
    }

    private Boolean addictionsMade() {
	String newString=getNewNewest();
	String storedString=getStoredNewest();
	if (( ! storedString.equals(ERROR) && ! newString.equals(ERROR)) && ( ! storedString.equals(newString))) {
	    return true;
	}
	return false;
    }

    private int initialize(String urlXml) {
	// Parsing of the remote XML file
	// Returns the total number of movies, 0 if errors occur
	try {
	    URL url=new URL(urlXml);
	    DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
	    DocumentBuilder db=dbf.newDocumentBuilder();
	    doc=db.parse(new InputSource(url.openStream()));
	    doc.getDocumentElement().normalize();
	    return (doc.getElementsByTagName("film")).getLength();
	}
	catch (Exception e) {
	    return 0;
	}
    }

    private String createChildList() {
	String s="";
	// Create the HashMap for the children
	ArrayList resultC=new ArrayList();
	NodeList catList=doc.getElementsByTagName("category");

	// "category" node iteration
	for (int i=0; i < catList.getLength(); ++i) {
	    NodeList filmList=((Element) catList.item(i)).getChildNodes();
	    ArrayList secList=new ArrayList();
	    List globalListNew=new ArrayList();

	    // "film" nodes iteration
	    for (int n=0; n < filmList.getLength(); n++) {
		List<String> film=new ArrayList<String>();
		// populate childrenList
		String title=((Element) filmList.item(n)).getAttribute("id");
		// add "Playlist" tag if needed
		if ( ! ((Element) filmList.item(n)).getAttribute("list").equals("")) {
		    title=title + FilmCompletiActivity.PLAYLIST_TAG;
		}
		HashMap child=new HashMap();
		child.put("Sub Item", title);
		secList.add(child);
		// populate globalList
		film.add("" + ((Element) filmList.item(n)).getAttribute("id"));
		film.add("" + ((Element) filmList.item(n)).getAttribute("v"));
		film.add("" + ((Element) filmList.item(n)).getAttribute("list"));
		globalListNew.add(film);
	    }
	    resultC.add(secList);
	}
	return s+=resultC.toString();
    }

}
