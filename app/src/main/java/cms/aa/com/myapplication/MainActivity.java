package cms.aa.com.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.BreakIterator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {


    private Handler handler = new Handler();
    double oldLat = 0;
    double oldLong = 0;
    static boolean stream = false;
    JSONObject params;
    //static String url = "http://192.168.43.82:8080/api/v1/lte/stats";
    static String url = "https://cnamapp01.azurewebsites.net/api/v1/lte/stats";

    static int UPDATE_DATA_TIMER = 15000;
    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        params = new JSONObject();
        context = this;
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }


        ListView myListView = findViewById(R.id.myListView);

        final ArrayList<String> dataList = new ArrayList<>(10);
        initDataList(dataList);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);
                TextView itemTextView = (TextView) itemView;
                itemTextView.setBackgroundColor(Color.WHITE);
                if (getItem(position).contains("SINR: ")) {
                    setSINRColors(position, itemTextView);
                } else if (getItem(position).contains("LTE_RSRP: ")) {
                    setRSRPColors(position, itemTextView);
                } else if (getItem(position).contains("LTE_RSRQ: ")) {
                    setRSRQColor(position, itemTextView);
                } else {
                    itemTextView.setTextColor(Color.BLACK);
                    itemTextView.setBackgroundColor(Color.WHITE);
                }
                return itemView;
            }

            private void setRSRQColor(int position, TextView itemTextView) {
                String[] str = getItem(position).split("LTE_RSRQ: ");
                double rsrq = Double.parseDouble(str[1]);
                itemTextView.setBackgroundColor(Color.WHITE);
                if (rsrq < -16) {
                    itemTextView.setTextColor(Color.BLACK);
                } else if (-16 <= rsrq && rsrq < -14) {
                    itemTextView.setTextColor(Color.rgb(5, 0, 248));
                } else if (-14 <= rsrq && rsrq < -10) {
                    itemTextView.setTextColor(Color.RED);
                } else if (-10 <= rsrq && rsrq < -7) {
                    itemTextView.setBackgroundColor(Color.BLACK);
                    itemTextView.setTextColor(Color.rgb(255, 247, 25));
                } else if (-7 <= rsrq && rsrq < -4) {
                    itemTextView.setTextColor(Color.rgb(0, 255, 0));
                } else {
                    itemTextView.setTextColor(Color.rgb(5, 121, 5));
                }
            }

            private void setRSRPColors(int position, TextView itemTextView) {
                String[] str = getItem(position).split("LTE_RSRP: ");
                double rsrp = Double.parseDouble(str[1]);
                itemTextView.setBackgroundColor(Color.WHITE);

                if (rsrp < -105) {
                    itemTextView.setTextColor(Color.BLACK);
                } else if (-105 <= rsrp && rsrp < -100) {
                    itemTextView.setTextColor(Color.rgb(134, 134, 134));
                } else if (-100 <= rsrp && rsrp < -95) {
                    itemTextView.setTextColor(Color.rgb(231, 102, 212));
                } else if (-95 <= rsrp && rsrp < -90) {
                    itemTextView.setTextColor(Color.rgb(3, 10, 232));
                } else if (-90 <= rsrp && rsrp < -85) {
                    //itemTextView.setBackgroundColor(Color.BLACK);
                    itemTextView.setTextColor(Color.rgb(48, 109, 225));
                } else if (-85 <= rsrp && rsrp < -80) {
                    itemTextView.setBackgroundColor(Color.BLACK);
                    itemTextView.setTextColor(Color.rgb(165, 206, 63));
                } else if (-80 <= rsrp && rsrp < -75) {
                    itemTextView.setTextColor(Color.RED);
                } else if (-75 <= rsrp && rsrp < -70) {
                    itemTextView.setTextColor(Color.rgb(252, 172, 10));
                } else if (-70 <= rsrp && rsrp < -65) {
                    itemTextView.setBackgroundColor(Color.BLACK);
                    itemTextView.setTextColor(Color.rgb(255, 253, 37));
                } else {
                    itemTextView.setTextColor(Color.rgb(0, 124, 0));
                }
            }

            private void setSINRColors(int position, TextView itemTextView) {
                String[] str = getItem(position).split("SINR: ");
                double snr = Double.parseDouble(str[1]);
                if (snr < 5) {
                    itemTextView.setTextColor(Color.RED);
                } else if (5 <= snr && snr < 10) {
                    itemTextView.setTextColor(Color.rgb(245, 128, 32));
                } else if (10 <= snr && snr < 15) {
                    itemTextView.setTextColor(Color.rgb(254, 192, 17));
                } else if (15 <= snr && snr < 20) {
                    itemTextView.setTextColor(Color.rgb(246, 235, 4));
                } else if (20 <= snr && snr < 25) {
                    itemTextView.setBackgroundColor(Color.BLACK);
                    itemTextView.setTextColor(Color.rgb(213, 229, 154));
                } else if (25 <= snr && snr < 30) {
                    itemTextView.setTextColor(Color.rgb(165, 206, 63));
                } else {
                    itemTextView.setTextColor(Color.rgb(123, 193, 66));
                }
            }
        };
        myListView.setAdapter(arrayAdapter);

        class PhoneStateListener extends android.telephony.PhoneStateListener {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                try {
                    double snr = (int) SignalStrength.class.getMethod("getLteRssnr").invoke(signalStrength) / 10D;
                    dataList.set(2, "SINR: " + snr);
                    double rssi = (int) SignalStrength.class.getMethod("getLteSignalStrength").invoke(signalStrength);
                    dataList.set(14, "LTE_RSSI: " + rssi);

                    if (Build.VERSION.SDK_INT >= 26) {
                        params.put("srlNo", "" + android.os.Build.getSerial());
                    } else {
                        params.put("srlNo", "" + android.os.Build.SERIAL);
                        params.put("rsrp", (int) SignalStrength.class.getMethod("getLteRsrp").invoke(signalStrength));
                        params.put("rsrq", (int) SignalStrength.class.getMethod("getLteRsrq").invoke(signalStrength));

                        dataList.set(0, "LTE_RSRP: " + (int) SignalStrength.class.getMethod("getLteRsrp").invoke(signalStrength));
                        dataList.set(1, "LTE_RSRQ: " + + (int) SignalStrength.class.getMethod("getLteRsrq").invoke(signalStrength));
                    }


                    params.put("snr",   snr);
                    params.put("rssi", "" + rssi);

                    arrayAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        PhoneStateListener mPhoneStateListener = new PhoneStateListener();
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);


        class MyLocationListener implements LocationListener {


            @Override
            public void onLocationChanged(Location loc) {

                String longitude = "Longitude: " + loc.getLongitude();
                String latitude = "Latitude: " + loc.getLatitude();

                String cityName = "-";
                Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                List<Address> addresses;
                try {
                    addresses = gcd.getFromLocation(loc.getLatitude(),
                            loc.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        System.out.println(addresses.get(0).getLocality());
                        cityName = addresses.get(0).getLocality();
                    }

                    dataList.set(9, "lat: " + loc.getLatitude());
                    dataList.set(10, "long: " + loc.getLongitude());
                    dataList.set(11, "alt: " + loc.getAltitude());
                    dataList.set(12, "city: " + cityName);
                    params.put("lat", "" + loc.getLatitude());
                    params.put("lon", "" + loc.getLongitude());
                    params.put("city", "" + cityName);

                    arrayAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        }

        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        Runnable runnable = new Runnable() {

            private double getDistance(double lat1, double lon1, double lat2, double lon2) {
                double R = 6371000; // for haversine use R = 6372.8 km instead of 6371 km
                double dLat = lat2 - lat1;
                double dLon = lon2 - lon1;

                if (dLat == 0 && dLon == 0) return 0D;

                double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(lat1) * Math.cos(lat2) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                return c;

            }

            @Override
            public void run() {

                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                try {

                    NetworkInfo netInfo = cm.getActiveNetworkInfo();
                    //should check null because in airplane mode it will be null
                    NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
                    int downSpeed = nc.getLinkDownstreamBandwidthKbps();
                    int upSpeed = nc.getLinkUpstreamBandwidthKbps();
                    dataList.set(7, "D/L speed (Kbps): " + nc.getLinkDownstreamBandwidthKbps());
                    dataList.set(8, "U/L speed (Kbps): " + nc.getLinkUpstreamBandwidthKbps());

                    params.put("dlSpd", "" + nc.getLinkDownstreamBandwidthKbps());
                    params.put("ulSpd", "" + nc.getLinkUpstreamBandwidthKbps());

                    arrayAdapter.notifyDataSetChanged();

                    List<CellInfo> cellInfoList = tm.getAllCellInfo();
                    if (cellInfoList != null) {
                        for (final CellInfo info : cellInfoList) {
                            if (info.isRegistered())
                                if (info instanceof CellInfoGsm) {
                                    Log.w("CellInfo", "getting GSM data");
                                } else if (info instanceof CellInfoCdma) {
                                    Log.w("CellInfo", "getting Cdma Data");
                                } else if (info instanceof CellInfoLte) {
                                    final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                                    final CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
                                    if (Build.VERSION.SDK_INT >= 26) {
                                        dataList.set(0, "LTE_RSRP: " + lte.getRsrp());
                                        dataList.set(1, "LTE_RSRQ: " + lte.getRsrq());
                                        params.put("rsrp",  lte.getRsrp());
                                        params.put("rsrq",  lte.getRsrq());
                                    }
                                    dataList.set(3, "PCI: " + identityLte.getPci());
                                    dataList.set(4, "Phone no: " + tm.getLine1Number());
                                    dataList.set(5, "Network Operator: " + tm.getNetworkOperatorName());
                                    dataList.set(6, "Network Country ISO: " + tm.getNetworkCountryIso());
                                    dataList.set(15, "imei: " + tm.getImei());
                                    dataList.set(16, "EARFCN:" + identityLte.getEarfcn());



                                    params.put("pci",  identityLte.getPci());
                                    params.put("phnNo", "" + tm.getLine1Number());
                                    params.put("nwOpr", "" + tm.getNetworkOperatorName());
                                    params.put("nwCon", "" + tm.getNetworkCountryIso());
                                    params.put("imei",  tm.getImei());
                                    params.put("efcn", "" + identityLte.getEarfcn());
                                    params.put("insert", "" +  Instant.now().toString());

                                    arrayAdapter.notifyDataSetChanged();
                                }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.i("test", "in here");

                if (locationManager != null) {
                    String cityName = "-";
                    Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(loc == null)
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                    List<Address> addresses;
                    try {
                        addresses = gcd.getFromLocation(loc.getLatitude(),
                                loc.getLongitude(), 1);
                        if (addresses.size() > 0) {
                            System.out.println(addresses.get(0).getLocality());
                            cityName = addresses.get(0).getLocality();
                        }
                        dataList.set(9, "lat: " + loc.getLatitude());
                        dataList.set(10, "long: " + loc.getLongitude());
                        dataList.set(11, "alt: " + loc.getAltitude());
                        dataList.set(12, "city: " + cityName);
                        dataList.set(13, "Speed(m/s): " + getDistance(oldLat, oldLong, loc.getLatitude(), loc.getLongitude()));

                        params.put("lat", "" + loc.getLatitude());
                        params.put("lon", "" + loc.getLongitude());
                        params.put("city", "" + cityName);
                        params.put("speed", "" + getDistance(oldLat, oldLong, loc.getLatitude(), loc.getLongitude()));

                        oldLat = loc.getLatitude();
                        oldLong = loc.getLongitude();
                        arrayAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (stream) {
                    HttpUtils.postByUrl(context,url, params);
                }
                handler.postDelayed(this, UPDATE_DATA_TIMER);
            }
        };

        handler.postDelayed(runnable, 500);


        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                stream = isChecked;
                Log.i("checked ", "" + stream);
            }
        });
    }

    private void initDataList(ArrayList<String> dataList) {
        dataList.add("LTE_RSRP:" + "-");
        dataList.add("LTE_RSRQ:" + "-");
        dataList.add("LTE_SINR:" + "-");
        dataList.add("PCI:" + "-");
        dataList.add("Phone no:" + "-");
        dataList.add("Network Operator:" + "-");
        dataList.add("Network Country ISO:" + "-");
        dataList.add("D/L speed (Kbps):" + "-");
        dataList.add("U/L speed (Kbps):" + "-");
        dataList.add("lat:" + "-");
        dataList.add("long:" + "-");
        dataList.add("alt:" + "-");
        dataList.add("city:" + "-");
        dataList.add("speed:" + "-");
        dataList.add("LTE_RSSI: " + "-");
        dataList.add("imei: " + "-");
        dataList.add("DbM: " + "-");
    }
}
