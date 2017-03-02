package com.mb.mapbox_flight;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private List<Position> routeCoordinates;
    private MapboxMap map;
    private LocationServices locationServices;
    private FloatingActionButton fButton;

    private static final int PERMISSIONS_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get access token. !
        MapboxAccountManager.start(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        // MapView found or created by new MapView.
        mapView = (MapView) findViewById(R.id.mapview);

        locationServices = LocationServices.getLocationServices(MainActivity.this);

//        MapboxMapOptions opts = new MapboxMapOptions()
//                .styleUrl(Style.OUTDOORS)
//                .camera(new CameraPosition.Builder()
//                        .target(new LatLng(120.11, 30.11))
//                        .zoom(12)
//                        .build());
//        mapView = new MapView(this, opts);

        // what ..always see savedInstanceState.
        mapView.onCreate(savedInstanceState);

        // Add a MapboxMap
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                // Customize map with markers, polylines, etc.
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(30.800, 120.911))
                        .title("这里是华东")
                        .snippet("欢迎来到这里"));

                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(13.371, 103.85))
                        .title("Combodia")
                        .snippet("欢迎来到这里 Siem Reap."));

                drawPolygon(mapboxMap);
                map = mapboxMap;

                // add data from Hard Code!! Create LineString Geometry from list of coords
                // then make GeoJSON FeatureCollection so add the line to map as linelayer.
                LineString lineString = LineString.fromCoordinates(addRoute(new ArrayList<Position>()));

                FeatureCollection featureCollection = FeatureCollection.
                        fromFeatures(new Feature[]{Feature.fromGeometry(lineString)});
                Source geoJsonSource = new GeoJsonSource("line-source", featureCollection);
                mapboxMap.addSource(geoJsonSource);
                LineLayer lineLayer = new LineLayer("linelayer", "line-source");
                // The layer properties for our line. This is where we make the line dotted, set the
                // color, etc.
                lineLayer.setProperties(
                        PropertyFactory.lineDasharray(new Float[]{0.02f, 4f}),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                        PropertyFactory.lineWidth(4f),
                        PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
                );

                final int lengthShort = Toast.LENGTH_SHORT;
                mapboxMap.addLayer(lineLayer);
                Toast.makeText(MainActivity.this, "添加geojson线图层...", lengthShort).show();

                // When user click the map ,animate to new position..
                mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {
                        // fly2Point(mapboxMap, new LatLng(13.361, 121.30));
                    }
                });

                mapboxMap.setOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(@NonNull LatLng point) {
                        fly2Point(mapboxMap, new LatLng(13.371, 103.85), 10);
                    }
                });

                fButton = (FloatingActionButton)findViewById(R.id.floatingActionButton);
                fButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        Intent intent = new Intent(MainActivity.this, Popup.class);
//                        MainActivity.this.startActivity(intent);
                        if (map != null) {
                            toggleGps(!map.isMyLocationEnabled());
                        }
                    }
                });

            }
        });
//        setContentView(mapView);

//        mapView.setOnLongClickListener();

        final int layerNumber = 3;
        final String[] styles = new String[layerNumber];
        styles[0] = Style.DARK;
        styles[1] = Style.SATELLITE;
        styles[2] = Style.MAPBOX_STREETS;
        final int[] layerIndex = {0};

        Button btn = (Button)findViewById(R.id.Layers);
        Double distance = 1.21212;
        btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View v) {
                // View v means this view Object Button!!
                final int lengthShort = Toast.LENGTH_SHORT;
                Toast.makeText(MainActivity.this, "切换图层...", lengthShort).show();
                System.out.println("switching layers..");
                if (layerIndex[0] < layerNumber) {
                mapView.setStyleUrl(styles[layerIndex[0]]);
                layerIndex[0]++;

                } else {
                    layerIndex[0] = 0;
                }
            }
        });

        Button jsonBtn = (Button)findViewById(R.id.Addjson);
        jsonBtn.setOnClickListener(addJSON);

    }

    // LatLng is basic Point Geometry of Mapbox
    private void fly2Point(MapboxMap map, LatLng point, int zoom) {
        CameraPosition position = new CameraPosition.Builder()
                .target(point)
                .zoom(zoom)
                .bearing(45)
                .tilt(30)
                .build(); // Creates a CameraPosition from the builder

        map.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 4000);
    }

    // must called in map.onCreate where scope has mapboxMap Instance!!!
    private void addPolygons (MapboxMap mapboxMap) {
        List<LatLng> polygon = new ArrayList<>();
        polygon.add(new LatLng(30.885699,121.522585));
        polygon.add(new LatLng(30.885699,121.622585));
        polygon.add(new LatLng(30.985699,121.622585));
        polygon.add(new LatLng(30.985699,121.522585));
        polygon.add(new LatLng(30.885699,121.522585));
        mapboxMap.addPolygon(new PolygonOptions()
                .addAll(polygon)
                .fillColor(Color.parseColor("#ff4080"))
                .alpha(0.4f));
    }

    private void String2Polygons (String jsonContent, MapboxMap mapboxMap, String sourceName) {
        FeatureCollection featureCollection = FeatureCollection.fromJson(jsonContent);
        GeoJsonSource source = new GeoJsonSource(sourceName, featureCollection);
        mapboxMap.addSource(source);

        FillLayer polygonLayer = new FillLayer(sourceName, sourceName);

        // The layer properties.
        polygonLayer.setProperties(
                PropertyFactory.fillOpacity(0.4f),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(4f),
                PropertyFactory.fillColor(Color.parseColor("#e55e5e")),
                PropertyFactory.fillOutlineColor(Color.parseColor("#e55e5e"))
        );

        final int lengthShort = Toast.LENGTH_SHORT;
        mapboxMap.addLayer(polygonLayer);
        Toast.makeText(MainActivity.this, "添加面图层...", lengthShort).show();
    }

    View.OnClickListener addJSON = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String content = addGeoJSON(getString(R.string.cache_json));
            String2Polygons(content, map, "Japan");
            fly2Point(map, new LatLng(34.00, 134.22), 8);
            System.out.println(content);
        }
    };

    private ArrayList<Position> addRoute(ArrayList<Position> routeCoordinates) {
        // Create a list to store our line coordinates.
        routeCoordinates = new ArrayList<Position>();
        routeCoordinates.add(Position.fromCoordinates(121.3439114221236, 31.0676454651766));
        routeCoordinates.add(Position.fromCoordinates(121.3421054012902, 31.069799454838));
        routeCoordinates.add(Position.fromCoordinates(121.3408583869053, 31.061901490136));
        routeCoordinates.add(Position.fromCoordinates(121.3388373635917, 31.0228225582285));
        routeCoordinates.add(Position.fromCoordinates(121.3372033447427, 31.018514560042));
        routeCoordinates.add(Position.fromCoordinates(121.330882271826, 31.046875508861));
        routeCoordinates.add(Position.fromCoordinates(121.328216241072, 31.059029501192));
        return routeCoordinates;
    }

    private String addGeoJSON(String fileName) {
        try {
            InputStream is = getAssets().open(fileName);
            int length = is.available();
            byte[] buffer = new byte[length];
            is.read(buffer);
            String result = new String(buffer, "utf8");
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    };

    View.OnLongClickListener addMarker = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            final int lengthShort = Toast.LENGTH_SHORT;
            Toast.makeText(MainActivity.this, "Long click event...", lengthShort).show();
            return false;
        }
    };

    private void drawPolygon(MapboxMap mapboxMap) {
        List<LatLng> polygon = new ArrayList<>();
        polygon.add(new LatLng(30.885699,121.522585));
        polygon.add(new LatLng(30.885699,121.622585));
        polygon.add(new LatLng(30.985699,121.622585));
        polygon.add(new LatLng(30.985699,121.522585));
        polygon.add(new LatLng(30.885699,121.522585));
        mapboxMap.addPolygon(new PolygonOptions()
            .addAll(polygon)
            .fillColor(Color.parseColor("#ff4080"))
            .alpha(0.4f));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    private void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            if (!locationServices.areLocationPermissionsGranted()) {
//                ActivityCompat.requestPermissions(this, new String[]{
//                        Manifest.permission.ACCESS_COARSE_LOCATION,
//                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
//                Toast.makeText(this, "location not permitted..", Toast.LENGTH_SHORT).show();
            } else {
                enableLocation(true);
            }
        } else {
            enableLocation(false);
        }
    }

    private void enableLocation(boolean enabled) {
        if (enabled) {
            // If we have the last location of the user, we can move the camera to that position.
            Location lastLocation = locationServices.getLastLocation();
            if (lastLocation != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
            }

            locationServices.addLocationListener(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        // Move the map camera to where the user location is and then remove the
                        // listener so the camera isn't constantly updating when the user location
                        // changes. When the user disables and then enables the location again, this
                        // listener is registered again and will adjust the camera once again.
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
                        locationServices.removeLocationListener(this);
                    }
                }
            });
            fButton.setImageResource(R.drawable.ic_disable_24dp);
        } else {
            fButton.setImageResource(R.drawable.ic_my_location_black_24dp);
        }
        // Enable or disable the location layer on the map
        map.setMyLocationEnabled(enabled);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation(true);
            }
        }
    }

}
