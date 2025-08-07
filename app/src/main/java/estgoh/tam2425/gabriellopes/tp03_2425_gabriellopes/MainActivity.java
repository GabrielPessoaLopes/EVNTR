package estgoh.tam2425.gabriellopes.tp03_2425_gabriellopes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "EventFilters";
    private static final String KEY_FILTER_TYPE = "filterType";
    private static final String KEY_FILTER_FUTURE_EVENTS = "filterFutureEvents";
    private static final String KEY_FILTER_REGISTERED_EVENTS = "filterRegisteredEvents";

    private ListView listView;
    private FloatingActionButton addEventButton;
    private ImageButton infoButton;
    private ImageButton settingsButton;
    private List<ApiManager.EventResponse> eventList = new ArrayList<>();
    private EventAdapter eventAdapter;
    private Spinner eventTypeSpinner;
    private CheckBox futureEventsCheckbox, bookedEventsCheckbox;
    private int userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Retrieve the user ID from the intent
        userId = getIntent().getIntExtra("USER_ID", -1);
        if (userId == -1) {
            // Handle missing user ID (e.g., log out the user)
            finish();
            return;
        }
        // Retrieve user ID from intent or shared preferences
        userId = getIntent().getIntExtra("USER_ID", ApiManager.getUserIdFromSP());
        if (userId == -1) {
            // Handle missing user ID (e.g., log out the user)
            finish();
            return;
        }

        // Check if user session is valid, otherwise redirect to login
        String token = ApiManager.getAuthToken();
        if (token == null || userId == -1) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        initializeViews();
        setupMainActivity();
    }

    private void initializeViews() {
        setContentView(R.layout.main_activity_layout);
        listView = findViewById(R.id.listView);
        addEventButton = findViewById(R.id.addEventButton);
        infoButton = findViewById(R.id.infoButton);
        settingsButton = findViewById(R.id.settingsButton);
        eventTypeSpinner = findViewById(R.id.eventTypeSpinner);
        futureEventsCheckbox = findViewById(R.id.checkboxFutureEvents);
        bookedEventsCheckbox = findViewById(R.id.checkboxRegisteredEvents);
    }

    private void setupMainActivity() {
        // Initialize event adapter
        eventAdapter = new EventAdapter(this, eventList);
        listView.setAdapter(eventAdapter);

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.event_types,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(adapter);

        // Restore saved filter states
        restoreFilterStates();

        // Set listeners for filters
        eventTypeSpinner.setOnItemSelectedListener(new FilterListener());
        futureEventsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveFilterStates();
            fetchEvents();
        });

        bookedEventsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveFilterStates();
            fetchEvents();
        });

        // ListView item click listener
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ApiManager.EventResponse event = eventList.get(position);
            Intent intent = new Intent(MainActivity.this, EventDetailsActivity.class);
            intent.putExtra("EVENT_ID", event.id);
            startActivityForResult(intent, 2); // Request code 2 for DetailEventActivity
        });

        // Floating button to add events
        addEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ModifyEventsActivity.class);
            startActivityForResult(intent, 1);
        });

        // Info button click listener
        infoButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        });

        // Logout button click listener
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("USER_ID", userId); // Pass userId to LogoutActivity
            startActivity(intent);
        });

        // Fetch initial list of events
        fetchEvents();
    }

    private void fetchEvents() {
        String selectedType = (eventTypeSpinner.getSelectedItem() != null)
                ? eventTypeSpinner.getSelectedItem().toString()
                : "Qualquer";

        switch (selectedType) {
            case "Académico":
                selectedType = "academico";
                break;
            case "Gastronómico":
                selectedType = "gastronomico";
                break;
            case "Cultural":
                selectedType = "cultural";
                break;
            case "Profissional":
                selectedType = "profissional";
                break;
            case "Desportivo":
                selectedType = "desportivo";
                break;
            default:
                break;
        }

        ApiManager.getEvents(selectedType, futureEventsCheckbox.isChecked(), bookedEventsCheckbox.isChecked(), new ApiManager.EventListCallback() {
            @Override
            public void onSuccess(List<ApiManager.EventResponse> events) {
                eventList.clear();
                eventList.addAll(events);
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private class FilterListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            saveFilterStates();
            fetchEvents();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    private void saveFilterStates() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_FILTER_TYPE, eventTypeSpinner.getSelectedItem() != null
                ? eventTypeSpinner.getSelectedItem().toString()
                : "Qualquer");
        editor.putBoolean(KEY_FILTER_FUTURE_EVENTS, futureEventsCheckbox.isChecked());
        editor.putBoolean(KEY_FILTER_REGISTERED_EVENTS, bookedEventsCheckbox.isChecked());
        editor.apply();
    }

    private void restoreFilterStates() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedType = sharedPreferences.getString(KEY_FILTER_TYPE, "Qualquer");
        boolean savedFutureEvents = sharedPreferences.getBoolean(KEY_FILTER_FUTURE_EVENTS, false);
        boolean savedRegisteredEvents = sharedPreferences.getBoolean(KEY_FILTER_REGISTERED_EVENTS, false);

        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) eventTypeSpinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(savedType);
            if (position >= 0) {
                eventTypeSpinner.setSelection(position);
            }
        }
        futureEventsCheckbox.setChecked(savedFutureEvents);
        bookedEventsCheckbox.setChecked(savedRegisteredEvents);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchEvents(); // Refresh the event list whenever the activity is resumed
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == 1 || requestCode == 2)) { // 1 for AddEventActivity, 2 for DetailEventActivity
            fetchEvents(); // Refresh the event list
        }
    }
}
