package cl.ucn.disc.dsm.cafa.quakemap;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Date;
import java.util.List;

import cl.ucn.disc.dsm.cafa.quakemap.controllers.EarthquakeCatalogController;
import cl.ucn.disc.dsm.cafa.quakemap.models.EarthquakeData;

public class MainActivity extends AppCompatActivity {

    MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_main);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // Configurar mapa:
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(16);


        // Punto por defecto: UCN.
        GeoPoint initialPoint = new GeoPoint(-23.6812, -70.4102, 16);
        map.getController().setCenter(initialPoint);


        // Marcador de ejemplo:
        Marker initialMarker = new Marker(map);
        initialMarker.setTitle("Este es un Titulo");
        initialMarker.setSnippet("Este es un Snippet");
        initialMarker.setSubDescription("Esta es una SubDescripcion");

        initialMarker.setPosition(initialPoint);
        initialMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Anadir marcador al mapa.
        map.getOverlays().add(initialMarker);

    }


    @Override
    protected void onStart() {
        super.onStart();

        downloadData();
    }

    private void downloadData()
    {
        AsyncTask.execute(() -> {

            Log.d("TAG", "-------------------");
            Log.d("TAG", "Descargando informacion...");
            Log.d("TAG", "-------------------");

            List<EarthquakeData> earthquakesData = null;

            try {
                earthquakesData = EarthquakeCatalogController.getEarthquakeCatalog();
            } catch (Exception e) {
                // Ocurrio un error.
                Log.d("TAG", "ERROR: " + e.getMessage() + "\n" + e.getStackTrace());
            }

            if (earthquakesData != null) {
                for (EarthquakeData earthquakeData : earthquakesData) {
                    Log.d(".", "..............................................");
                    Log.d("EQ", "Title: "+earthquakeData.properties.title);
                    Log.d("EQ", "Date: "+new Date(earthquakeData.properties.time));
                    Log.d("EQ", "Coordinates: " + earthquakeData.geometry.toString());
                }
                Log.d(".", "..............................................");
            }
        });
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    //earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2014-01-01&endtime=2014-01-02

}
