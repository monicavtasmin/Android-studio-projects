// Data
// DatabaseHelper.java
package com.example.trucksharingapp.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.trucksharingapp.model.User;
import com.example.trucksharingapp.util.Util;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(@Nullable Context context) {
        super(context, Util.DATABASE_NAME, null, Util.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_USER_TABLE = "CREATE TABLE " +
                Util.TABLE_NAME + "(" +
                Util.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Util.COLUMN_POST_TYPE + " TEXT, " +
                Util.COLUMN_NAME + " TEXT, " +
                Util.COLUMN_PHONE + " TEXT," +
                Util.COLUMN_DESCRIPTION + " TEXT," +
                Util.COLUMN_DATE + " TEXT," +
                Util.COLUMN_LOCATION + " TEXT" + ")";
        sqLiteDatabase.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Util.TABLE_NAME);
        onCreate(sqLiteDatabase);


    }

    public long insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Util.COLUMN_POST_TYPE, user.post_type);
        contentValues.put(Util.COLUMN_NAME, user.name);
        contentValues.put(Util.COLUMN_PHONE, user.phone);
        contentValues.put(Util.COLUMN_DESCRIPTION, user.description);
        contentValues.put(Util.COLUMN_DATE, user.date);
        contentValues.put(Util.COLUMN_LOCATION, user.location);
        long newRowId = db.insert(Util.TABLE_NAME, null, contentValues);
        db.close();
        return newRowId;
    }

    public User fetchUser(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT * FROM " + Util.TABLE_NAME + " WHERE " + Util.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        if (cursor != null)
            cursor.moveToFirst();

        assert cursor != null;
        return new User(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6));
    }

    public List<User> fetchAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectAll = " SELECT * FROM " + Util.TABLE_NAME;
        Cursor cursor = db.rawQuery(selectAll, null);

        if (cursor.moveToFirst()) {
            do {
                User user = new User(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6));
                userList.add(user);
            } while (cursor.moveToNext());

        }

        return userList;
    }

    public void deleteItems(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Util.TABLE_NAME, Util.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}

// ItemDetails.java
package com.example.trucksharingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.trucksharingapp.data.DatabaseHelper;
import com.example.trucksharingapp.model.User;

public class ItemDetails extends AppCompatActivity {

    DatabaseHelper db;

    public TextView postNameTxt, descTxt, dateTxt, locationTxt, phoneTxt;
    public Button removeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        postNameTxt = findViewById(R.id.post_name_txt);
        descTxt = findViewById(R.id.description_txt);
        dateTxt = findViewById(R.id.date_txt);
        locationTxt = findViewById(R.id.location_txt);
        phoneTxt = findViewById(R.id.phone_txt);
        removeBtn = findViewById(R.id.removeItem);

        db = new DatabaseHelper(this);

        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);

        User user = db.fetchUser(id);


        postNameTxt.setText(user.post_type + " " + user.description);
        descTxt.setText("Name : " + user.name);
        dateTxt.setText("Date : " + user.date);
        locationTxt.setText("Location : " + user.location);
        phoneTxt.setText("Phone Number : " + user.phone);

        // to remove notes
        removeBtn.setOnClickListener(view -> {
            db.deleteItems(id);
            setResult(RESULT_OK);
            finish();
        });
    }
}

// ListViewAdapter.java
package com.example.trucksharingapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.trucksharingapp.model.User;

import java.util.List;

public class ListViewAdapter extends ArrayAdapter<User> {

    public List<User> users;
    public int resource;
    public OnClicked onClicked;

    public ListViewAdapter(@NonNull Context context,int resource,List<User> users, OnClicked onClicked) {
        super(context,R.layout.item_list,resource,users);
        this.users = users;
        this.resource = resource;
        this.onClicked = onClicked;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resource, null);
        } else {
            view = convertView;
        }

        TextView post_type = view.findViewById(R.id.post_type);
        TextView description = view.findViewById(R.id.description);
        ConstraintLayout listLayout = view.findViewById(R.id.list_layout);

        User user = users.get(position);
        post_type.setText(user.post_type);
        description.setText(user.description);

        listLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClicked.onClicked(user);
            }
        });

        return view;
    }
}

// MainActivity.java
package com.example.trucksharingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button showlfButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button signupButton = findViewById(R.id.signUpButton);
        Button showButton = findViewById(R.id.showButton);
        showlfButton = findViewById(R.id.showlfButton);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signupIntent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(signupIntent);
            }
        });

        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showIntent = new Intent(MainActivity.this, ShowUsers.class);
                startActivity(showIntent);
            }
        });

        showlfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }
}

// MapsActivity.java
package com.example.trucksharingapp;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.example.trucksharingapp.data.DatabaseHelper;
import com.example.trucksharingapp.model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.trucksharingapp.databinding.ActivityMapsBinding;

import java.util.List;

public class  MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    public double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        List<User> locationList = databaseHelper.fetchAllUsers();
        for (User userLocation : locationList) {
            // https://stackoverflow.com/questions/20249274/find-and-replace-android-studio
            String lat_lng = String.valueOf(userLocation.location);
            lat_lng = lat_lng.replaceAll("lat/lng: ", "");
            lat_lng = lat_lng.replace("(", "");
            lat_lng = lat_lng.replace(")", "");
            String[] split = lat_lng.split(",");
            latitude = Double.parseDouble(split[0]);
            longitude = Double.parseDouble(split[1]);

            googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(userLocation.description));

        }
    }
}

// Model
// User.java
package com.example.trucksharingapp.model;

public class User {

    public int id;
    public String post_type;
    public String name;
    public String phone;
    public String description;
    public String date;
    public String location;

    public User(int id, String post_type ,String name, String phone, String description, String date, String location) {
        this.id = id;
        this.post_type = post_type;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.date = date;
        this.location = location;
    }

    public User(String post_type ,String name, String phone, String description, String date, String location) {
        this.post_type = post_type;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.date = date;
        this.location = location;
    }
}

// ShowUsers.java

package com.example.trucksharingapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.example.trucksharingapp.data.DatabaseHelper;
import com.example.trucksharingapp.model.User;

import java.util.List;

public class ShowUsers extends AppCompatActivity implements OnClicked {

    ListView usersListView ;
    ListViewAdapter adapter;

    ActivityResultLauncher<Intent> startActivityForResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_users);
        usersListView = findViewById(R.id.usersListView);
        DatabaseHelper db = new DatabaseHelper(ShowUsers.this);


        startActivityForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            List<User> userList = db.fetchAllUsers();

            adapter = new ListViewAdapter(this, R.layout.item_list, userList, this);
            usersListView.setAdapter(adapter);
        });

        List<User> userList = db.fetchAllUsers();

        adapter = new ListViewAdapter(this, R.layout.item_list, userList, this);
        usersListView.setAdapter(adapter);
    }

    @Override
    public void onClicked(User user) {
        Intent intent = new Intent(this, ItemDetails.class);
        intent.putExtra("id", user.id);
        startActivityForResult.launch(intent);
    }
}

// SignupActivity.java
{

    public EditText name, phone ,description, date;
    public Button save;
    public RadioButton lost, found;
    public RadioGroup radioGroup;
    EditText locationedit;
    LocationManager locationManager;
    Button currentLoc;
    LocationListener locationListener;

    final Calendar myCalendar = Calendar.getInstance();

    public static String post_type = "", show_loc = "";
    DatabaseHelper db;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        name = findViewById(R.id.name_edt);
        phone = findViewById(R.id.phone_edt);
        description = findViewById(R.id.desc_edt);
        save = findViewById(R.id.save_btn);
        lost = findViewById(R.id.lostRB);
        found = findViewById(R.id.foundRB);
        radioGroup = findViewById(R.id.radioGr);
        date = findViewById(R.id.date_edt);
        locationedit = findViewById(R.id.location_edt);
        currentLoc = findViewById(R.id.currentLoc);

        db = new DatabaseHelper(this);

        // to initialize places and enter API key
        Places.initialize(getApplicationContext(), "AIzaSyBAf7rpSKl19V2rMIHMDQM-tUZUm3LSnw0");

        // non focusable
        locationedit.setFocusable(false);
        locationedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent i = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fieldList).build(SignupActivity.this);
                startActivityForResult(i, 100);
            }
        });

        DatePickerDialog.OnDateSetListener datePick = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String myFormat = "dd/MM/yyyy";
            SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
            date.setText(dateFormat.format(myCalendar.getTime()));
        };

        // https://stackoverflow.com/questions/14327412/set-focus-on-edittext
        date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus){
                    new DatePickerDialog(SignupActivity.this, datePick, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();                }
            }
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.lostRB:
                    post_type = "lost";
                    break;
                case R.id.foundRB:
                    post_type = "found";
                    break;
            }
        });

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        currentLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        locationedit.setText("lat/lng: (" + location.getLatitude() + ", " + location.getLongitude() + ")");
                        show_loc = locationedit.getText().toString();
                    }
                };

                if (ActivityCompat.checkSelfPermission(SignupActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                        (SignupActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    ActivityCompat.requestPermissions(SignupActivity.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
                else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name_str = name.getText().toString();
                String phone_str = phone.getText().toString();
                String location_str = show_loc;
                String description_str = description.getText().toString();
                String date_str = date.getText().toString();

                if (name_str.isEmpty() || phone_str.isEmpty() || location_str.isEmpty() || description_str.isEmpty() || date_str.isEmpty() || post_type.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "PLEASE FILL-IN THE DATA", Toast.LENGTH_SHORT).show();
                } else {
                    db.insertUser(new User(post_type, name_str, phone_str, description_str, date_str, location_str));
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class );
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK){
            Place place = Autocomplete.getPlaceFromIntent(data);
            locationedit.setText(place.getAddress());
            show_loc = place.getLatLng().toString();
        }
        else if (resultCode == AutocompleteActivity.RESULT_ERROR){
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(),status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

// Util.java
package com.example.trucksharingapp.util;

public class Util {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "user_db";
    public static final String TABLE_NAME = "users";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_POST_TYPE = "post_type";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_LOCATION = "location";
}