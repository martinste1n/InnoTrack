package wineme.de.innotrack;

/**
 * Created by stein on 13.01.2016.
 */
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


import com.karumi.dexter.Dexter;
import de.ms.location.Utilities;
public class InnoApplication extends Application {


    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    public void onCreate() {
        super.onCreate();
        Utilities.setContext(this);
        prefs= PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        Dexter.initialize(this);
        Utilities.checkAndInitLocationService();
    }




}
