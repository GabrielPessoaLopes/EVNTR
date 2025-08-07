package estgoh.tam2425.gabriellopes.tp03_2425_gabriellopes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class AddUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.add_user_activity_layout);

        // Initialize UI components
        EditText usernameField = findViewById(R.id.usernameInput);
        EditText passwordField = findViewById(R.id.passwordInput);
        EditText confirmPasswordField = findViewById(R.id.confirmPasswordInput);
        Button registerButton = findViewById(R.id.registerButton);
        ImageButton backArrowButton = findViewById(R.id.backArrowButton);
        backArrowButton.setOnClickListener(v -> finish());

        // Handle register button click
        registerButton.setOnClickListener(v -> {
            validateInputs(usernameField.getText().toString().trim(), passwordField.getText().toString().trim(), confirmPasswordField.getText().toString().trim());
        });
    }

    private void validateInputs(String username, String password, String confirmPassword) {
        if (username.isEmpty()) {
            showToast("Nome the utilizador deve ser preenchido");
            return;
        }
        if (password.isEmpty()) {
            showToast("Palavra-passe deve ser preenchida");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showToast("Palavras-passe não coincidem");
            return;
        }
        addUser(username, password);
    }

    private void addUser(String username, String password) {
        ApiManager.registerUser(username, password, new ApiManager.EventActionCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(AddUserActivity.this, "Utilizador registado!", Toast.LENGTH_SHORT).show();
                    // Navigate back to LoginActivity
                    Intent intent = new Intent(AddUserActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> showToast(errorMessage));
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
