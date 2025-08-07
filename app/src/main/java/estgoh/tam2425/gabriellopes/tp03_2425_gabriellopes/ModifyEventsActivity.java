package estgoh.tam2425.gabriellopes.tp03_2425_gabriellopes;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.Calendar;

public class ModifyEventsActivity extends AppCompatActivity {

    private EditText descriptionEditText, locationEditText, dateEditText, timeEditText, seatsEditText, priceEditText, deadlineDateEditText, deadlineTimeEditText;
    private Spinner typeSpinner;
    private CheckBox freeCheckbox;
    private Button saveButton;
    private ImageButton backArrowButton;
    private boolean isEditMode = false;
    private int eventID = -1;
    private ApiManager.EventResponse existingEvent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        initializeViews();

        // Determine if the activity is in edit mode
        Intent intent = getIntent();
        eventID = intent.getIntExtra("EVENT_ID", -1);
        isEditMode = (eventID != -1);

        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        if (isEditMode) {
            toolbarTitle.setText("Editar Evento");
            fetchEventDetails();
        } else
            toolbarTitle.setText("Adicionar Evento");

        backArrowButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> {
            if (validateFields()) {
                if (isEditMode) {
                    updateEvent();
                } else {
                    addEvent();
                }
            }
        });
    }

    private void initializeViews() {
        setContentView(R.layout.modify_events_activity_layout);

        descriptionEditText = findViewById(R.id.descriptionText);
        locationEditText = findViewById(R.id.locationText);
        dateEditText = findViewById(R.id.dateText);
        timeEditText = findViewById(R.id.timeText);
        seatsEditText = findViewById(R.id.seatsText);
        freeCheckbox = findViewById(R.id.paidCheckbox);
        priceEditText = findViewById(R.id.priceText);
        deadlineDateEditText = findViewById(R.id.deadlineDateText);
        deadlineTimeEditText = findViewById(R.id.deadlineTimeText);
        typeSpinner = findViewById(R.id.typeSpinner);
        saveButton = findViewById(R.id.saveButton);
        backArrowButton = findViewById(R.id.backArrowButton);

        setupSpinner();
        setupPickers();

        freeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                priceEditText.setVisibility(View.GONE);
                priceEditText.setText("");
            } else {
                priceEditText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupSpinner() {
        String[] eventTypes = {"cultural", "profissional", "academico", "desportivo", "gastronomico", "outro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
    }

    private void setupPickers() {
        dateEditText.setOnClickListener(v -> showDatePicker(dateEditText));
        timeEditText.setOnClickListener(v -> showTimePicker(timeEditText));
        deadlineDateEditText.setOnClickListener(v -> showDatePicker(deadlineDateEditText));
        deadlineTimeEditText.setOnClickListener(v -> showTimePicker(deadlineTimeEditText));
    }

    private void fetchEventDetails() {
        String token = ApiManager.getAuthToken();
        ApiManager.getInstance().getEventById(token, eventID).enqueue(new retrofit2.Callback<ApiManager.EventResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ApiManager.EventResponse> call, retrofit2.Response<ApiManager.EventResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    existingEvent = response.body();
                    populateEventDetails();
                } else
                    finish();
            }

            @Override
            public void onFailure(retrofit2.Call<ApiManager.EventResponse> call, Throwable t) {
                showToast(t.getMessage());
                finish();
            }
        });
    }

    private void populateEventDetails() {
        descriptionEditText.setText(existingEvent.description);
        locationEditText.setText(existingEvent.location);

        String[] dateTime = existingEvent.eventDate.split(" ");
        dateEditText.setText(dateTime[0]);
        timeEditText.setText(dateTime[1]);

        seatsEditText.setText(String.valueOf(existingEvent.seats));
        freeCheckbox.setChecked(existingEvent.price == 0);

        if (existingEvent.price > 0) {
            priceEditText.setVisibility(View.VISIBLE);
            priceEditText.setText(String.valueOf(existingEvent.price));
        } else {
            priceEditText.setVisibility(View.GONE);
        }

        String[] deadlineDateTime = existingEvent.deadlineDate.split(" ");
        deadlineDateEditText.setText(deadlineDateTime[0]);
        deadlineTimeEditText.setText(deadlineDateTime[1]);

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) typeSpinner.getAdapter();
        int position = adapter.getPosition(existingEvent.eventType);
        if (position >= 0) {
            typeSpinner.setSelection(position);
        }
    }

    private void addEvent() {
        ApiManager.EventRequest request = buildEventRequest();

        ApiManager.addEvent(request, new ApiManager.EventActionCallback() {
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

    private void updateEvent() {
        ApiManager.EventRequest request = buildEventRequest();

        ApiManager.updateEvent(eventID, request, new ApiManager.EventActionCallback() {
            @Override
            public void onSuccess(String message) {
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private ApiManager.EventRequest buildEventRequest() {
        return new ApiManager.EventRequest(
                typeSpinner.getSelectedItem().toString(),
                descriptionEditText.getText().toString(),
                locationEditText.getText().toString(),
                dateEditText.getText().toString() + " " + timeEditText.getText().toString(),
                deadlineDateEditText.getText().toString() + " " + deadlineTimeEditText.getText().toString(),
                Integer.parseInt(seatsEditText.getText().toString()),
                freeCheckbox.isChecked() ? 0.0 : Double.parseDouble(priceEditText.getText().toString())
        );
    }

    private boolean validateFields() {
        boolean isValid = true;
        int emptyToast = 0;
        String seatsText = seatsEditText.getText().toString();
        String priceText = priceEditText.getText().toString();

        // Reset previous errors
        resetFieldErrors();

        if (descriptionEditText.getText().toString().isEmpty()) {
            descriptionEditText.setError("Campo obrigatório");
            emptyToast++;
            isValid = false;
        }
        if (locationEditText.getText().toString().isEmpty()) {
            locationEditText.setError("Campo obrigatório");
            emptyToast++;
            isValid = false;
        }
        if (dateEditText.getText().toString().isEmpty()) {
            dateEditText.setError("Campo obrigatório");
            emptyToast++;
            isValid = false;
        }
        if (timeEditText.getText().toString().isEmpty()) {
            timeEditText.setError("Campo obrigatório");
            emptyToast++;
            isValid = false;
        }
        if (deadlineDateEditText.getText().toString().isEmpty()) {
            deadlineDateEditText.setError("Campo obrigatório");
            emptyToast++;
            isValid = false;
        }
        if (deadlineTimeEditText.getText().toString().isEmpty()) {
            deadlineTimeEditText.setError("Campo obrigatório");
            emptyToast++;
            isValid = false;
        }

        if (seatsText.isEmpty()) {
            deadlineTimeEditText.setError("Campo obrigatório");
            emptyToast++;
            isValid = false;
        }
        if (!freeCheckbox.isChecked()) {
            if (priceText.isEmpty()) {
                deadlineTimeEditText.setError("Campo obrigatório");
                emptyToast++;
                isValid = false;
            }
        }
        if (emptyToast>0)
            showToast("Por favor, preencha os campos obrigatórios.");
        // Validate seats
        try {
            if (Integer.parseInt(seatsText) <= 0) {
                seatsEditText.setError("Deve ser um número maior que 0");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            seatsEditText.setError("Deve ser um número válido");
            isValid = false;
        }

        // Validate price if the event is not free
        if (!freeCheckbox.isChecked()) {
            try {
                if (priceText.isEmpty() || Float.parseFloat(priceText) <= 0) {
                    priceEditText.setError("Deve ser maior que 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                priceEditText.setError("Deve ser um valor numérico válido");
                isValid = false;
            }
        }

        return isValid;
    }


    private void resetFieldErrors() {
        descriptionEditText.setHintTextColor(Color.GRAY);
        locationEditText.setHintTextColor(Color.GRAY);
        dateEditText.setHintTextColor(Color.GRAY);
        timeEditText.setHintTextColor(Color.GRAY);
        deadlineDateEditText.setHintTextColor(Color.GRAY);
        deadlineTimeEditText.setHintTextColor(Color.GRAY);
        seatsEditText.setTextColor(Color.GRAY);
        priceEditText.setTextColor(Color.GRAY);
    }

    private void showDatePicker(EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear);
                    editText.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker(EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    editText.setText(selectedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
