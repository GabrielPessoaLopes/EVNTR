package estgoh.tam2425.gabriellopes.tp03_2425_gabriellopes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private TextView usernameTextView;
    private ListView listView;
    private EventAdapter eventAdapter;
    private List<ApiManager.EventResponse> eventList = new ArrayList<>();
    private int userId;
    private ImageButton backArrowButton;
    private boolean isLoggingOut = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.settings_activity_layout);

        // Retrieve user ID
        userId = getIntent().getIntExtra("USER_ID", ApiManager.getUserIdFromSP());
        if (userId == -1) {
            finish(); // Exit if user ID is invalid
            return;
        }

        // Initialize UI components
        usernameTextView = findViewById(R.id.usernameTextView);
        listView = findViewById(R.id.listView);
        ImageButton editProfileButton = findViewById(R.id.editProfileButton);
        ImageButton logoutButton = findViewById(R.id.settingsButton);
        backArrowButton = findViewById(R.id.backArrowButton);

        // Set username from SharedPreferences
        usernameTextView.setText(ApiManager.getUsernameFromSP());

        // Set button listeners
        editProfileButton.setOnClickListener(v -> openEditProfileActivity());
        logoutButton.setOnClickListener(v -> logout());
        backArrowButton.setOnClickListener(v -> finish());

        // Add click listener for ListView items
        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            // Get the clicked event
            ApiManager.EventResponse selectedEvent = eventList.get(position);

            // Open the DetailEventActivity and pass the event ID
            openEventDetails(selectedEvent.id);
        });

        // Fetch user-created events
        fetchUserCreatedEvents();
    }

    private void fetchUserCreatedEvents() {
        // Retrieve the current username from SharedPreferences
        String currentUsername = ApiManager.getUsernameFromSP();
        if (currentUsername == null) {
            showToast("Informações de utilizador em falta.");
            return;
        }

        // Fetch the user ID using the current username
        ApiManager.getInstance().getUserIdByUsername(currentUsername).enqueue(new Callback<ApiManager.UserIdResponse>() {
            @Override
            public void onResponse(Call<ApiManager.UserIdResponse> call, Response<ApiManager.UserIdResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int userId = response.body().userId;
                    // Fetch events created by the user
                    fetchEventsByCreator(userId);
                } else {
                    showToast(response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiManager.UserIdResponse> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void fetchEventsByCreator(int userId) {
        String token = ApiManager.getAuthToken();
        if (token == null) {
            return;
        }

        ApiManager.getInstance().getEventsByCreator(token, userId).enqueue(new Callback<List<ApiManager.EventResponse>>() {
            @Override
            public void onResponse(Call<List<ApiManager.EventResponse>> call, Response<List<ApiManager.EventResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventList.clear();
                    eventList.addAll(response.body());
                    setupListView();
                } else
                    showToast("Os seus eventos não foram possíveis de obter. Tente novamente mais tarde.");
            }

            @Override
            public void onFailure(Call<List<ApiManager.EventResponse>> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }


    private void openEventDetails(int eventId) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", eventId); // Pass the event ID
        startActivity(intent);
    }

    private void setupListView() {
        if (eventAdapter == null) {
            eventAdapter = new EventAdapter(this, eventList);
            listView.setAdapter(eventAdapter);
        } else {
            eventAdapter.notifyDataSetChanged();
        }
    }

    private void openEditProfileActivity() {
        Intent intent = new Intent(this, EditUserActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivityForResult(intent, 1); // Use a request code to identify this action
    }

    private void logout() {
        ApiManager.clearAuthToken();
        ApiManager.clearUserCredentialsFromSP();
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the back stack
        startActivity(intent);
        isLoggingOut = true;
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLoggingOut)
            return;
        fetchUserCreatedEvents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if the result is from EditUserActivity
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // Get the updated username from the intent
            String updatedUsername = data.getStringExtra("UPDATED_USERNAME");
            if (updatedUsername != null && !updatedUsername.isEmpty())
                usernameTextView.setText(updatedUsername);// Update the username TextView
        }
    }

}
