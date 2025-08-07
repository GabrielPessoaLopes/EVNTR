package estgoh.tam2425.gabriellopes.tp03_2425_gabriellopes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {

    private TextView descriptionTextView;
    private TextView locationTextView;
    private TextView eventDateTextView;
    private TextView eventTimeTextView;
    private TextView deadlineDateTextView;
    private TextView deadlineTimeTextView;
    private TextView seatsTextView;
    private TextView priceTextView;
    private ImageButton backArrowButton;
    private ImageButton editButton;
    private ImageButton deleteButton;
    private Button bookButton;

    private ApiManager.EventResponse selectedEvent;
    private int eventID;
    private boolean isOwner; // Flag to determine if the user can edit or remove the event

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.event_details_layout);

        // Initialize views
        initializeViews();

        // Get the event ID from the Intent
        Intent intent = getIntent();
        eventID = intent.getIntExtra("EVENT_ID", -1);

        if (eventID != -1) {
            fetchEventDetails();
            fetchOwnershipStatus(); // Fetch ownership after loading details
        }

        // Set visibility and clickability of edit and delete buttons based on ownership
        configureOwnershipUI();

        // Back button functionality
        backArrowButton.setOnClickListener(v -> finish());

        // Edit button functionality
        editButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(EventDetailsActivity.this, ModifyEventsActivity.class);
            editIntent.putExtra("EVENT_ID", eventID);
            startActivityForResult(editIntent, 2);
        });

        // Delete button functionality
        deleteButton.setOnClickListener(v -> {
            // Show confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Remover evento")
                    .setMessage("Tem a certeza que pretende remover este evento permanentemente?")
                    .setPositiveButton("Sim", (dialog, which) -> removeEvent())
                    .setNegativeButton("Não", null)
                    .show();
        });

        // Book button functionality
        bookButton.setOnClickListener(v -> bookButtonAction());

    }

    private void initializeViews() {
        descriptionTextView = findViewById(R.id.descriptionTextView);
        locationTextView = findViewById(R.id.locationTextView);
        eventDateTextView = findViewById(R.id.eventDateTextView);
        eventTimeTextView = findViewById(R.id.eventTimeTextView);
        deadlineDateTextView = findViewById(R.id.deadlineDateTextView);
        deadlineTimeTextView = findViewById(R.id.deadlineTimeTextView);
        seatsTextView = findViewById(R.id.seatsTextView);
        priceTextView = findViewById(R.id.priceTextView);
        backArrowButton = findViewById(R.id.backArrowButton);
        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);
        bookButton = findViewById(R.id.bookButton);
    }

    private void configureOwnershipUI() {
        if (!isOwner) {
            // Hide edit and delete buttons if the user is not the owner
            editButton.setVisibility(ImageButton.GONE);
            deleteButton.setVisibility(ImageButton.GONE);
            editButton.setClickable(false);
            deleteButton.setClickable(false);
        } else {
            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
        }
    }

    private void fetchEventDetails() {
        String token = ApiManager.getAuthToken();
        ApiManager.getInstance().getEventById(token, eventID).enqueue(new retrofit2.Callback<ApiManager.EventResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ApiManager.EventResponse> call, retrofit2.Response<ApiManager.EventResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    selectedEvent = response.body();
                    populateEventDetails();
                } else
                    showToast("Erro ao carregar os detalhes do evento.");
            }

            @Override
            public void onFailure(retrofit2.Call<ApiManager.EventResponse> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void fetchOwnershipStatus() {
        ApiManager.isEventOwner(eventID, new ApiManager.EventOwnershipCallback() {
            @Override
            public void onSuccess(boolean isOwnerFlag) {
                isOwner = isOwnerFlag;
                configureOwnershipUI(); // Configure the UI after determining ownership
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void populateEventDetails() {
        descriptionTextView.setText(selectedEvent.description);
        locationTextView.setText(selectedEvent.location);

        // Handle Event Date
        if (selectedEvent.eventDate != null && !selectedEvent.eventDate.isEmpty()) {
            String[] eventDateTime = selectedEvent.eventDate.split(" ");
            eventDateTextView.setText(eventDateTime[0]); // Date
            eventTimeTextView.setText(eventDateTime.length > 1 ? eventDateTime[1] : ""); // Time
        } else {
            eventDateTextView.setText("Data não disponível");
            eventTimeTextView.setText("");
        }

        // Handle Deadline Date
        if (selectedEvent.deadlineDate != null && !selectedEvent.deadlineDate.isEmpty()) {
            String[] deadlineDateTime = selectedEvent.deadlineDate.split(" ");
            deadlineDateTextView.setText(deadlineDateTime[0]); // Date
            deadlineTimeTextView.setText(deadlineDateTime.length > 1 ? deadlineDateTime[1] : ""); // Time
        } else {
            deadlineDateTextView.setText("Data limite não disponível");
            deadlineTimeTextView.setText("");
        }

        seatsTextView.setText(selectedEvent.seats + " lugares livres");
        priceTextView.setText(selectedEvent.price == 0 ? "Grátis" : selectedEvent.price + "€");

        updateBookButtonText();

        // Set click listener to open EventBookingsActivity if the user is the event owner
        if (isOwner)
            descriptionTextView.setOnClickListener(v -> openEventBookingsActivity());
    }

    private void bookButtonAction() {
        if (selectedEvent != null) {
            if (selectedEvent.isBooked) {
                // Show confirmation dialog for canceling booking
                new AlertDialog.Builder(this)
                        .setTitle("Cancelar Inscrição")
                        .setMessage("Tem a certeza que pretende cancelar a sua inscrição neste evento?")
                        .setPositiveButton("Sim", (dialog, which) -> cancelBooking())
                        .setNegativeButton("Não", null)
                        .show();
            } else if (isDeadlinePassed()) {
                // Show deadline passed message
                new AlertDialog.Builder(this)
                        .setTitle("Inscrição impossível")
                        .setMessage("A data limite de inscrição foi ultrapassada.")
                        .setPositiveButton("Ok", null)
                        .show();
            } else {
                // Show confirmation dialog for booking
                new AlertDialog.Builder(this)
                        .setTitle("Confirmar Inscrição")
                        .setMessage("Tem a certeza que pretende inscrever-se neste evento?")
                        .setPositiveButton("Sim", (dialog, which) -> bookEvent())
                        .setNegativeButton("Não", null)
                        .show();
            }
        }
    }

    private void bookEvent() {
        ApiManager.bookEvent(eventID, new ApiManager.EventActionCallback() {
            @Override
            public void onSuccess(String message) {
                selectedEvent.isBooked = true;
                updateBookButtonText();
                fetchEventDetails();
                setResult(RESULT_OK);
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void cancelBooking() {
        ApiManager.cancelBooking(eventID, new ApiManager.EventActionCallback() {
            @Override
            public void onSuccess(String message) {
                selectedEvent.isBooked = false;
                updateBookButtonText();
                fetchEventDetails();
                setResult(RESULT_OK);
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private boolean isDeadlinePassed() {
        try {
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            Date deadlineDate = outputFormat.parse(selectedEvent.deadlineDate);
            return deadlineDate != null && deadlineDate.before(new Date());
        } catch (ParseException e) {
            e.printStackTrace();
            return true;
        }
    }

    private void updateBookButtonText() {
        if (selectedEvent != null && selectedEvent.isBooked)
            bookButton.setText("Desinscrever");
        else
            bookButton.setText("Inscrever");

    }

    private void removeEvent() {
        ApiManager.removeEvent(eventID, new ApiManager.EventActionCallback() {
            @Override
            public void onSuccess(String message) {
                showToast(message);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    // Method to open EventBookingsActivity
    private void openEventBookingsActivity() {
        Intent bookingsIntent = new Intent(EventDetailsActivity.this, EventBookingsActivity.class);
        bookingsIntent.putExtra("EVENT_ID", eventID);
        startActivity(bookingsIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            int updatedEventID = data.getIntExtra("EVENT_ID", -1);
            if (updatedEventID == eventID)
                fetchEventDetails(); // Refresh the event details
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
