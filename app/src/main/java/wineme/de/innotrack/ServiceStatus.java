package wineme.de.innotrack;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import de.ms.location.locationtools.LocationService;
import de.ms.location.locationtools.PTNMELocationManager;
import de.schildbach.pte.dto.Location;

public class ServiceStatus extends AppCompatActivity {
    private ServiceStatus me;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_status);

    }
    @Override
    protected void onStart() {
        super.onStart();



        me= this;
        PTNMELocationManager.LocationListener listener = new PTNMELocationManager.LocationListener() {
            @Override
            public void onNewLocationFound(Location x) {
                Toast.makeText(me,x.toString(),Toast.LENGTH_LONG );
            }

            @Override
            public void onLocationError(String cause) {

            }
        };

        PTNMELocationManager.registerListener(listener);
    }
}
