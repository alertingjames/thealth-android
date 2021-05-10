package com.app.thealth.main;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.app.thealth.R;
import com.app.thealth.base.BaseActivity;
import com.app.thealth.classes.MapWrapperLayout;
import com.app.thealth.commons.Commons;
import com.firebase.client.annotations.Nullable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PickLocationActivity extends BaseActivity implements OnMapReadyCallback, View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLng latLngG = null;
    MapWrapperLayout mapWrapperLayout;
    String info, countryName = "", cityName = "", stateName = "", address = "";
    SearchView inputBox;
    AVLoadingIndicatorView _progressDlg;

    private final int[] MAP_TYPES = {
            GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};
    private int curMapTypeIndex = 2;

    String pickLocationMessage = "",
            title = "",
            hint = "",
            speechError = "",
            selectLocationMessage = "",
            confirmTitle = "",
            confirmLocation = "",
            yes = "",
            no = "",
            ok = "",
            locationInfo = "",
            typeAddress = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickLocationMessage = getString(R.string.location_text);
        title = "Select a location";
        hint = getString(R.string.hint);
        speechError = getString(R.string.speech_error);
        selectLocationMessage = getString(R.string.select_location);
        confirmTitle = getString(R.string.confirm);
        confirmLocation = getString(R.string.confirm_select_location);
        yes = getString(R.string.yes);
        no = getString(R.string.no);
        ok = getString(R.string.ok);
        locationInfo = getString(R.string.location_info);

        setContentView(R.layout.activity_pick_location);

        _progressDlg = (AVLoadingIndicatorView) findViewById(R.id.loading_bar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);
        mapFragment.getMapAsync(this);
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, 39 + 20));

        mapFragment.setHasOptionsMenu(true);

        @SuppressLint("ResourceType") View myLocationButton = mapFragment.getView().findViewById(0x2);

        if (myLocationButton != null && myLocationButton.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            // location button is inside of RelativeLayout
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) myLocationButton.getLayoutParams();

            // Align it to - parent BOTTOM|LEFT
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 150);

            // Update margins, set to 10dp
            final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                    getResources().getDisplayMetrics());
            params.setMargins(margin, margin, margin, margin);

            myLocationButton.setLayoutParams(params);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        inputBox = (SearchView) findViewById(R.id.searchView);
        inputBox.setQuery(typeAddress, false);
        inputBox.setFocusable(true);
//        inputBox.requestFocus();
        inputBox.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocationOnAddress(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        ((LinearLayout) findViewById(R.id.lyt_speech)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVoiceRecognitionActivity();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                ((LinearLayout)findViewById(R.id.hintLayout)).setAnimation(animation);
                ((LinearLayout)findViewById(R.id.hintLayout)).setVisibility(View.VISIBLE);
            }
        }, 3000);

        setupUI(findViewById(R.id.activity), this);
    }

    public void dismissDialog(View view){
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        ((LinearLayout)findViewById(R.id.hintLayout)).setAnimation(animation);
        ((LinearLayout)findViewById(R.id.hintLayout)).setVisibility(View.GONE);
    }

    private static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST = 7001;

    private void checkForLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);

        } else {
            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                try {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                    Commons.thisUser.setLatLng(latLng);
                    latLngG = latLng;
                    initCamera(latLng);
                    mMap.setMapType(MAP_TYPES[Commons.curMapTypeIndex]);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            address = getAddressFromLatLng(latLng);
                        }
                    }, 800);

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                mMap.setMapType(MAP_TYPES[Commons.curMapTypeIndex]);
            }
        }
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mMap.setMapType(MAP_TYPES[Commons.curMapTypeIndex]);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getApplicationContext(), getString(R.string.service_connection_suspended), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@Nullable ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), getString(R.string.service_connection_failed), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        LatLng latLng = marker.getPosition();
//        getFullAddressFromLocation(latLng, false);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        getFullAddressFromLocation(latLng);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(Commons.curMapTypeIndex == 2){
            mMap.setMapType(MAP_TYPES[curMapTypeIndex=0]);
        }
        else if(curMapTypeIndex == 0)mMap.setMapType(MAP_TYPES[curMapTypeIndex=2]);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initListeners();
        checkForLocationPermission();
    }

    private void initListeners() {

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled( true );
        mMap.setBuildingsEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude, 1);
            // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            address = addresses.get(0).getAddressLine(0); // If any additional quetripAddress line present than only, check with max available quetripAddress lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            String zip = addresses.get(0).getPostalCode();
            String url= addresses.get(0).getUrl();
            countryName = country;
            cityName = city == null? state:city;
            stateName = state;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    private void initCamera(LatLng location) {
        CameraPosition position = CameraPosition.builder()
                .target(location)
                .zoom(16f)
                .bearing(30)                // Sets the orientation of the camera to east
                .tilt(30)
                .build();

        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), null);


        mMap.setMapType(MAP_TYPES[Commons.curMapTypeIndex]);
        mMap.setTrafficEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled( true );
    }

    public void getFullAddressFromLocation(LatLng latLng){
        mMap.clear();
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        address = addresses.get(0).getAddressLine(0); // If any additional quetripAddress line present than only, check with max available quetripAddress lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
        String zip = addresses.get(0).getPostalCode();
        String url= addresses.get(0).getUrl();

        countryName = country;
        cityName = city == null? state:city; stateName = state;
        latLngG = latLng;

        if(postalCode == null)postalCode = "";
        else postalCode = "Postal Code: "+postalCode+"\n";
        if(zip == null) zip = "";
        else zip = "Zip Code: "+zip+"\n";
        if(knownName == null) knownName="";
        else knownName = "Public Name: " + knownName + "\n";

        info = "Country" + ": " + country + "\n" + "State" + ": " + state;

        try {
            MarkerOptions options = new MarkerOptions().position(latLng);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(options);
            inputBox.onActionViewExpanded();
            inputBox.setQuery(address, false);
            inputBox.clearFocus();
        }catch (NullPointerException e){

        }

        MapsInitializer.initialize(this);
        initCamera(latLng);

        try{
            if(info != null){
                new AlertDialog.Builder(PickLocationActivity.this)
                        .setTitle(locationInfo)
                        .setMessage(address + "\n" + getString(R.string.city) + ": " + city + "\n" + getString(R.string.country) + ": " + country)
                        .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
            }
        }catch (NullPointerException e){}
    }

    private void searchLocationOnAddress(String addr) {
        mMap.clear();
        address = addr;
        List<Address> addresses =null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {

            addresses = geocoder.getFromLocationName(addr, 1);

            if(addresses.size() > 0){
                double latitude= addresses.get(0).getLatitude();
                double longitude= addresses.get(0).getLongitude();

                String address = addresses.get(0).getAddressLine(0);

                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                String zip = addresses.get(0).getPostalCode();
                String url= addresses.get(0).getUrl();

                latLngG = new LatLng(latitude,longitude);
                MarkerOptions options = new MarkerOptions().position(latLngG);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                mMap.addMarker(options);
                initCamera(latLngG);
                getAddressFromLatLng(latLngG);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final int REQ_CODE_SPEECH_INPUT = 100;

    public void startVoiceRecognitionActivity() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,

                "AndroidBite Voice Recognition...");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            showToast(speechError);
        }catch (NullPointerException a) {

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK) {

            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            searchLocationOnAddress(matches.get(0));
        }
    }

    public void ok(View view){
        if(address.length() == 0){
            showToast(selectLocationMessage);
            return;
        }
        String locMsg = address + "\n" + getString(R.string.city) + ": " + cityName + "\n" + getString(R.string.country) + ": " + countryName;
        new AlertDialog.Builder(PickLocationActivity.this)
                .setTitle(confirmTitle)
                .setMessage(confirmLocation + "\n" + locMsg)
                .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if(Commons.registerProfileActivity != null){
                            Commons.registerProfileActivity.locationBox.setText(cityName + ", " + countryName);
                            Commons.thisUser.set_address(cityName + ", " + countryName);
                            Commons.thisUser.setLatLng(latLngG);
                        }else if(Commons.createPostActivity != null){
                            Commons.createPostActivity.locationBox.setText(cityName + ", " + countryName);
                            Commons.createPostActivity.latLng = latLngG;
                        }else if(Commons.editFeedActivity != null){
                            Commons.editFeedActivity.locationBox.setText(cityName + ", " + countryName);
                            Commons.editFeedActivity.latLng = latLngG;
                        }

                        finish();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    public void back(View view){
        onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}






























