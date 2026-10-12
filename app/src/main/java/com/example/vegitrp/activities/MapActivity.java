package com.example.vegitrp.activities;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.vegitrp.R;
import com.example.vegitrp.models.Order;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MapActivity extends BaseActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;

    private double originLat = 0.0;
    private double originLng = 0.0;
    private double destLat = 0.0;
    private double destLng = 0.0;
    private boolean hasOrigin = false;
    private boolean hasDest = false;
    private boolean isRoutingMode = false;

    private String pickupName = "";
    private String dropoffName = "";
    private String pinCode = "";

    private View cardRouteInfo;
    private TextView tvPickupPoint, tvDropoffPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.map_toolbar_title);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        cardRouteInfo = findViewById(R.id.cardRouteInfo);
        tvPickupPoint = findViewById(R.id.tvPickupPoint);
        tvDropoffPoint = findViewById(R.id.tvDropoffPoint);

        if (getIntent().hasExtra("ORIGIN_LAT") && getIntent().hasExtra("ORIGIN_LNG")) {
            originLat = getIntent().getDoubleExtra("ORIGIN_LAT", 0.0);
            originLng = getIntent().getDoubleExtra("ORIGIN_LNG", 0.0);
            if (originLat != 0.0 && originLng != 0.0) {
                hasOrigin = true;
                isRoutingMode = true;
            }
        }

        if (getIntent().hasExtra("DEST_LAT") && getIntent().hasExtra("DEST_LNG")) {
            destLat = getIntent().getDoubleExtra("DEST_LAT", 0.0);
            destLng = getIntent().getDoubleExtra("DEST_LNG", 0.0);
            if (destLat != 0.0 && destLng != 0.0) {
                hasDest = true;
                isRoutingMode = true;
            }
        }

        if (getIntent().hasExtra("PICKUP_LOCATION")) {
            pickupName = getIntent().getStringExtra("PICKUP_LOCATION");
            isRoutingMode = true;
        } else if (getIntent().hasExtra("pickupAddress")) {
            pickupName = getIntent().getStringExtra("pickupAddress");
            isRoutingMode = true;
        }

        if (getIntent().hasExtra("DROPOFF_LOCATION")) {
            dropoffName = getIntent().getStringExtra("DROPOFF_LOCATION");
            isRoutingMode = true;
        } else if (getIntent().hasExtra("deliveryAddress")) {
            dropoffName = getIntent().getStringExtra("deliveryAddress");
            isRoutingMode = true;
        }

        if (getIntent().hasExtra("pinCode")) {
            pinCode = getIntent().getStringExtra("pinCode");
        } else if (getIntent().hasExtra("PIN_CODE")) {
            pinCode = getIntent().getStringExtra("PIN_CODE");
        }

        String orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) orderId = getIntent().getStringExtra("tripId");
        if (orderId != null) {
            isRoutingMode = true;
            loadOrderLocations(orderId);
        } else if (isRoutingMode) {
            if (hasOrigin && hasDest) {
                updateLocationLabels();
            } else {
                resolveCoordinatesAndDraw();
            }
        }

        if (isRoutingMode) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.map_toolbar_route_title);
            }
            if (cardRouteInfo != null) {
                cardRouteInfo.setVisibility(View.VISIBLE);
            }
            updateLocationLabels();
        } else {
            if (cardRouteInfo != null) {
                cardRouteInfo.setVisibility(View.GONE);
            }
        }

        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void updateLocationLabels() {
        if (tvPickupPoint != null && pickupName != null && !pickupName.isEmpty()) {
            tvPickupPoint.setText(pickupName);
        }
        if (tvDropoffPoint != null && dropoffName != null && !dropoffName.isEmpty()) {
            tvDropoffPoint.setText(dropoffName);
        }
    }

    private void loadOrderLocations(String orderId) {
        FirebaseDatabase.getInstance().getReference("AllOrders").child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Order order = snapshot.getValue(Order.class);
                        if (order != null) {
                            if (order.getPickupAddress() != null && !order.getPickupAddress().trim().isEmpty()) {
                                pickupName = order.getPickupAddress().trim();
                            }
                            if (order.getDeliveryAddress() != null && !order.getDeliveryAddress().trim().isEmpty()) {
                                dropoffName = order.getDeliveryAddress().trim();
                            } else if (order.getPinCode() != null && !order.getPinCode().trim().isEmpty()) {
                                pinCode = order.getPinCode().trim();
                                if (dropoffName.isEmpty()) {
                                    dropoffName = "PIN: " + pinCode;
                                }
                            }

                            if (order.getPickupLatitude() != 0.0 && order.getPickupLongitude() != 0.0) {
                                originLat = order.getPickupLatitude();
                                originLng = order.getPickupLongitude();
                                hasOrigin = true;
                            }
                            if (order.getDeliveryLatitude() != 0.0 && order.getDeliveryLongitude() != 0.0) {
                                destLat = order.getDeliveryLatitude();
                                destLng = order.getDeliveryLongitude();
                                hasDest = true;
                            }

                            Log.d(TAG, "Loaded order: pickup=" + pickupName + ", drop=" + dropoffName + ", hasOrigin=" + hasOrigin + ", hasDest=" + hasDest);
                            updateLocationLabels();

                            if (hasOrigin && hasDest) {
                                if (mMap != null) {
                                    drawRoute();
                                }
                            } else {
                                resolveCoordinatesAndDraw();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load order location data: " + error.getMessage());
                    }
                });
    }

    private void resolveCoordinatesAndDraw() {
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean geocoderAvailable = Geocoder.isPresent();
            Log.d(TAG, "resolveCoordinatesAndDraw: geocoderAvailable=" + geocoderAvailable + ", pickup=" + pickupName + ", drop=" + dropoffName + ", pin=" + pinCode);
            if (geocoderAvailable) {
                Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                try {
                    if (!hasOrigin && pickupName != null && !pickupName.trim().isEmpty()) {
                        String query = pickupName.toLowerCase().contains("india") ? pickupName : pickupName + ", India";
                        List<Address> addresses = geocoder.getFromLocationName(query, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            Address addr = addresses.get(0);
                            originLat = addr.getLatitude();
                            originLng = addr.getLongitude();
                            hasOrigin = true;
                            Log.d(TAG, "Resolved pickup coordinates: (" + originLat + ", " + originLng + ")");
                        }
                    }

                    if (!hasDest) {
                        String query = null;
                        if (dropoffName != null && !dropoffName.trim().isEmpty() && !dropoffName.startsWith("PIN:")) {
                            query = dropoffName.toLowerCase().contains("india") ? dropoffName : dropoffName + ", India";
                        } else if (pinCode != null && !pinCode.trim().isEmpty()) {
                            query = pinCode + ", India";
                        }
                        if (query != null) {
                            List<Address> addresses = geocoder.getFromLocationName(query, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address addr = addresses.get(0);
                                destLat = addr.getLatitude();
                                destLng = addr.getLongitude();
                                hasDest = true;
                                Log.d(TAG, "Resolved destination coordinates: (" + destLat + ", " + destLng + ")");
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Geocoding error: " + e.getMessage());
                }
            }

            runOnUiThread(() -> {
                if (mMap != null) {
                    if (hasOrigin && hasDest) {
                        drawRoute();
                    } else if (hasOrigin) {
                        mMap.clear();
                        LatLng loc = new LatLng(originLat, originLng);
                        mMap.addMarker(new MarkerOptions()
                                .position(loc)
                                .title("PICKUP: " + (pickupName.isEmpty() ? "Farm Produce Point" : pickupName))
                                .snippet("Agricultural Pickup Point")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));
                    } else if (hasDest) {
                        mMap.clear();
                        LatLng loc = new LatLng(destLat, destLng);
                        mMap.addMarker(new MarkerOptions()
                                .position(loc)
                                .title("DROP: " + (dropoffName.isEmpty() ? "Market Yard" : dropoffName))
                                .snippet("Agricultural Delivery / Market Point")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));
                    } else {
                        Log.d(TAG, "Coordinates could not be resolved from address text.");
                    }
                }
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }

        if (isRoutingMode) {
            if (hasOrigin && hasDest) {
                drawRoute();
            } else {
                resolveCoordinatesAndDraw();
            }
        } else {
            LatLng defaultIndiaCenter = new LatLng(20.5937, 78.9629);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultIndiaCenter, 5));
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (mMap != null) {
                        mMap.setMyLocationEnabled(true);
                    }
                }
            } else {
                Toast.makeText(this, "Location permission is required to show your current location on the map.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void drawRoute() {
        if (mMap == null || !hasOrigin || !hasDest) return;
        mMap.clear();

        LatLng origin = new LatLng(originLat, originLng);
        LatLng destination = new LatLng(destLat, destLng);

        String pickupTitle = (pickupName != null && !pickupName.isEmpty()) ? "PICKUP: " + pickupName : "PICKUP POINT";
        String dropTitle = (dropoffName != null && !dropoffName.isEmpty()) ? "DROP: " + dropoffName : "DELIVERY POINT";

        mMap.addMarker(new MarkerOptions()
                .position(origin)
                .title(pickupTitle)
                .snippet("Agricultural Pickup Point")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        mMap.addMarker(new MarkerOptions()
                .position(destination)
                .title(dropTitle)
                .snippet("Agricultural Delivery / Market Point")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Fit camera to see both points
        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(origin);
            builder.include(destination);
            LatLngBounds bounds = builder.build();
            int padding = 160; // offset from edges of the map in pixels
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } catch (Exception e) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 10));
        }

        // Get API Key securely
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.startsWith("YOUR_")) {
            Log.w(TAG, "API Key Missing or Placeholder. Cannot query Google Directions API.");
            return;
        }

        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                originLat + "," + originLng + "&destination=" +
                destLat + "," + destLng + "&key=" + apiKey;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String status = response.optString("status", "");
                        Log.d(TAG, "Directions API response status: " + status);
                        if ("OK".equalsIgnoreCase(status)) {
                            JSONArray routes = response.getJSONArray("routes");
                            if (routes.length() > 0) {
                                JSONObject route = routes.getJSONObject(0);
                                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                                String encodedString = overviewPolyline.getString("points");
                                List<LatLng> decodedPath = PolyUtil.decode(encodedString);

                                PolylineOptions polylineOptions = new PolylineOptions()
                                        .addAll(decodedPath)
                                        .width(12)
                                        .color(ContextCompat.getColor(this, R.color.brand_green_primary))
                                        .geodesic(true);

                                mMap.addPolyline(polylineOptions);
                                Log.d(TAG, "Added polyline with " + decodedPath.size() + " points to map.");
                            }
                        } else {
                            String errorMsg = response.optString("error_message", "None");
                            Log.w(TAG, "Directions API non-OK status: " + status + ", message: " + errorMsg);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parse Error: " + e.getMessage(), e);
                    }
                },
                error -> {
                    Log.e(TAG, "Volley Request Error: " + error.getMessage(), error);
                });

        requestQueue.add(jsonObjectRequest);
    }

    private String getApiKey() {
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                return appInfo.metaData.getString("com.google.android.geo.API_KEY");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
