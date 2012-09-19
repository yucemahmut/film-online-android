package pack.filmonline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
	Intent check=new Intent(context, checkNewest.class);
	check.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	context.startActivity(check);
    }
}