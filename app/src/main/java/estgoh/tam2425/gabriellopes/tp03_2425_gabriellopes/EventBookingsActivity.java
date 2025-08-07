package estgoh.tam2425.gabriellopes.tp03_2425_gabriellopes;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class EventBookingsActivity extends AppCompatActivity {

    private LinearLayout tableContainer;
    private TableLayout bookingsTable;
    private TextView noBookingsMessage;
    private String eventName;
    private int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_bookings_activity_layout);

        // Initialize views
        initializeViews();

        // Retrieve event details from Intent
        eventName = getIntent().getStringExtra("EVENT_NAME");
        eventId = getIntent().getIntExtra("EVENT_ID", -1);

        // Back button functionality
        ImageButton backArrowButton = findViewById(R.id.backArrowButton);
        backArrowButton.setOnClickListener(v -> finish());

        // Fetch and populate bookings
        if (eventId != -1)
            fetchEventIdAndBookings();
        else
            showToast("ID de evento inválido.");

    }

    private void initializeViews() {
        tableContainer = findViewById(R.id.tableContainer);
        bookingsTable = findViewById(R.id.bookingsTable);
        noBookingsMessage = findViewById(R.id.noBookingsMessage);

        if (tableContainer == null || bookingsTable == null || noBookingsMessage == null)
            throw new IllegalStateException("Erro a carregar a página. Por favor tente novamente mais tarde. ");

    }

    private void fetchEventIdAndBookings() {
        String token = ApiManager.getAuthToken();
        if (token == null) {
            showToast("Por favor inicie sessão novamente.");
            return;
        }

        ApiManager.getInstance().getEventById(token, eventId).enqueue(new retrofit2.Callback<ApiManager.EventResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ApiManager.EventResponse> call, retrofit2.Response<ApiManager.EventResponse> response) {
                if (response.isSuccessful() && response.body() != null)
                    fetchBookingsForEvent(eventId);
                else
                    showToast(response.message());
            }

            @Override
            public void onFailure(retrofit2.Call<ApiManager.EventResponse> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void fetchBookingsForEvent(int eventId) {
        ApiManager.getBookingsForEvent(eventId, new ApiManager.EventBookingsCallback() {
            @Override
            public void onSuccess(List<ApiManager.BookingResponse> bookings) {
                populateTable(bookings);
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void populateTable(List<ApiManager.BookingResponse> bookings) {
        if (bookingsTable == null || noBookingsMessage == null || tableContainer == null)
            throw new IllegalStateException("Erro ao preencher tabela. Tente novamente mais tarde.");

        // Clear existing rows in the table
        bookingsTable.removeAllViews();

        if (bookings == null || bookings.isEmpty()) {
            // Show "No bookings available" message and hide the table
            noBookingsMessage.setVisibility(View.VISIBLE);
            tableContainer.setVisibility(View.GONE);
            return;
        }

        // Show the table and hide the "No bookings available" message
        noBookingsMessage.setVisibility(View.GONE);
        tableContainer.setVisibility(View.VISIBLE);

        for (ApiManager.BookingResponse booking : bookings) {
            if (booking == null)
                continue; // Skip null entries

            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));
            row.setPadding(8, 8, 8, 8);

            // Username column
            TextView usernameText = new TextView(this);
            usernameText.setText(booking.getUsername() != null ? booking.getUsername() : "Unknown User");
            usernameText.setTextSize(14);
            usernameText.setGravity(Gravity.CENTER);
            usernameText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(usernameText);

            // Booking date column
            TextView bookingDateText = new TextView(this);
            bookingDateText.setText(booking.getBookingDate() != null ? booking.getBookingDate() : "Unknown Date");
            bookingDateText.setTextSize(14);
            bookingDateText.setGravity(Gravity.CENTER);
            bookingDateText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(bookingDateText);

            // Add the row to the table
            bookingsTable.addView(row);
        }
    }

    private void showToast(String message) {
        // Check for null components to avoid crashes
        if (noBookingsMessage == null || tableContainer == null)
            throw new IllegalStateException("Erro de apresentação. Tente novamente mais tarde.");

        // Show the informational message in the `noBookingsMessage` TextView
        noBookingsMessage.setVisibility(View.VISIBLE);
        noBookingsMessage.setText(message);

        // Hide the tableContainer (the table of bookings)
        tableContainer.setVisibility(View.GONE);
    }
}
