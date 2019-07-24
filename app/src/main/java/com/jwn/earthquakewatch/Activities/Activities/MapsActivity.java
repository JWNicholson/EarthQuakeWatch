package com.jwn.earthquakewatch.Activities.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jwn.earthquakewatch.Activities.Model.EarthQuake;
import com.jwn.earthquakewatch.Activities.QuakesListActivity;
import com.jwn.earthquakewatch.Activities.UI.CustomInfoWindow;
import com.jwn.earthquakewatch.Activities.Utils.Constants;
import com.jwn.earthquakewatch.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import static com.jwn.earthquakewatch.Activities.Utils.Constants.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RequestQueue queue;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private BitmapDescriptor[] iconColors;
    private Button showListBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        showListBtn = findViewById(R.id.showListBtn);

        showListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapsActivity.this, QuakesListActivity.class));
            }
        });


        iconColors = new BitmapDescriptor[] {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                //BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
        };

        queue = Volley.newRequestQueue(this);

        getEarthQuakes();
    }



    /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                //return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Ask for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location location =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
    }

    // get all Earthquake objects
    public void getEarthQuakes() {

        final EarthQuake earthQuake = new EarthQuake();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features = response.getJSONArray("features");
                            for (int i = 0;i < Constants.LIMIT; i++) {
                                    JSONObject properties =
                                            features.getJSONObject(i).getJSONObject("properties");

                                    //get geometry object
                                JSONObject geometry = features.getJSONObject(i).getJSONObject(
                                        "geometry");

                                //get coordinates array
                                JSONArray coordinates = geometry.getJSONArray("coordinates");

                                double lon = coordinates.getDouble(0);
                                double lat = coordinates.getDouble(1);

                                earthQuake.setPlace(properties.getString("place"));
                                earthQuake.setType(properties.getString("type"));
                                earthQuake.setTime(properties.getLong("time"));
                                earthQuake.setLat(lat);
                                earthQuake.setLon(lon);
                                earthQuake.setMagnitude(properties.getDouble("mag"));
                                earthQuake.setDetailLink(properties.getString("detail"));

                                java.text.DateFormat dateFormat =
                                        java.text.DateFormat.getDateInstance();
                               String formattedDate =
                                       dateFormat.format(new Date(Long.valueOf(properties.getLong("time")))
                                .getTime());

                               MarkerOptions markerOptions = new MarkerOptions();

                              markerOptions.icon(iconColors[Constants.randomInt(iconColors
                                 .length, 0)]);//return a random number each time this runs
                               markerOptions.title(earthQuake.getPlace());
                               markerOptions.position(new LatLng(earthQuake.getLat(), earthQuake.getLon()));


                               markerOptions.snippet("Magnitude: " + earthQuake.getMagnitude(properties.getDouble("mag")) + "\n" + "Date: " + formattedDate);


                               //Add a circle to markers with mag > x
                                if (earthQuake.getMagnitude(properties.getDouble("mag")) >= 2.0) {
                                    CircleOptions circleOptions = new CircleOptions();
                                    circleOptions.center(new LatLng(earthQuake.getLat(),
                                            earthQuake.getLon()));
                                    circleOptions.radius(30000);
                                    circleOptions.strokeWidth(3.6f);
                                    circleOptions.fillColor(Color.RED);
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                                    mMap.addCircle(circleOptions);

                                }


                               Marker marker = mMap.addMarker(markerOptions);
                               marker.setTag(earthQuake.getDetailLink());
                               mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon),
                                       1));



                            }



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        //get the needed JSON objects to get to the details (cities) response
        getQuakeDetails(marker.getTag().toString());


        Toast.makeText(getApplicationContext(), marker.getTitle().toString(), Toast.LENGTH_LONG)
         .show();

    }

    private void getQuakeDetails(String url) {


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        String detailsUrl= "";


                        try {
                            JSONObject properties = response.getJSONObject("properties");
                            JSONObject products = properties.getJSONObject("products");
                            JSONArray geoserve = products.getJSONArray("geoserve");

                            for (int i = 0; i < geoserve.length(); i++) {
                                JSONObject geoserveObj = geoserve.getJSONObject(i);

                                JSONObject contentObj = geoserveObj.getJSONObject("contents");
                                JSONObject geoJsonObj = contentObj.getJSONObject("geoserve.json");

                                  detailsUrl = geoJsonObj.getString("url");


                            }

                            Log.d("URL: ", detailsUrl);

                            getMoreDetails(detailsUrl);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);
    }

   public void getMoreDetails(String url){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        dialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                        View view = getLayoutInflater().inflate(R.layout.popup,null);

                        Button dismissButton = view.findViewById(R.id.dismissPop);
                        Button dismissButtonTop = view.findViewById(R.id.dismissPopTop);
                        TextView popList = view.findViewById(R.id.popList);
                        WebView htmlPop =  view.findViewById(R.id.htmlWebview );

                        StringBuilder stringBuilder = new StringBuilder();


                        try {

                            if (response.has("tectonicSummary") && response.getString(
                                    "tectonicSummary") != null) {
                                JSONObject tectonic = response.getJSONObject("tectonicSummary");

                                if (tectonic.has("text") && tectonic.getString("text") !=null) {

                                    String text = tectonic.getString("text");

                                    htmlPop.loadDataWithBaseURL(null, text, "text/html",
                                            "UTF-8",null);
                                }
                            }

                            JSONArray cities = response.getJSONArray("cities");

                            for (int i = 0; i < cities.length(); i++) {

                                JSONObject citiesObj = cities.getJSONObject(i);

                                stringBuilder.append("City: " + citiesObj.getString("name")
                                        + "\n" + "Distance: " + citiesObj.getString("distance")
                                        + "\n" + "Population: " + citiesObj.getString("population"
                                        + "\n" + "Latitude: " + citiesObj.getString("latitude")));

                                stringBuilder.append("\n\n");

                            }


                            popList.setText(stringBuilder);


                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });


                            dismissButtonTop.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });

                            dialogBuilder.setView(view);
                            dialog = dialogBuilder.create();
                            dialog.show();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

            queue.add(jsonObjectRequest);

   }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}// end MapsActivity



