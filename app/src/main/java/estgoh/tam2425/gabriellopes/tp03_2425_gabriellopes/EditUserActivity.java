package estgoh.tam2425.gabriellopes.tp03_2425_gabriellopes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class EditUserActivity extends AppCompatActivity {

    private EditText usernameField;
    private EditText passwordField;
    private EditText confirmPasswordField;
    private Button saveButton;
    private Button deleteAccountButton;
    private ImageButton backArrowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.edit_user_activity_layout);

        initializeViews();
        setupActivity();
    }

    private void initializeViews() {
        // Initialize UI components
        usernameField = findViewById(R.id.usernameEdit);
        passwordField = findViewById(R.id.passwordEdit);
        confirmPasswordField = findViewById(R.id.confirmPasswordEdit);
        saveButton = findViewById(R.id.saveButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
        backArrowButton = findViewById(R.id.backArrowButton);
    }

    private void setupActivity() {
        // Back button functionality
        backArrowButton.setOnClickListener(v -> finish());

        // Handle save button click
        saveButton.setOnClickListener(v -> {
            validateInputs(usernameField.getText().toString().trim(), passwordField.getText().toString().trim(), confirmPasswordField.getText().toString().trim());
        });

        // Handle delete account button click
        deleteAccountButton.setOnClickListener(v -> confirmAccountRemoval());
    }

    private void validateInputs(String username, String password, String confirmPassword) {
        if (!password.isEmpty() && confirmPassword.isEmpty()) {
            showToast("Por favor, confirme a palavra-passe.");
            return;
        }
        if (!password.isEmpty() && !password.equals(confirmPassword)) {
            showToast("Palavras-passe não coincidem.");
            return;
        }
        if (!username.isEmpty())
            checkUsername(username, password); // Checks if the username is already in use
        else
            updateUser(null, password);// Directly updates if username is not being changed
    }

    // Checks if the username is already in use
    private void checkUsername(String username, String password) {
        ApiManager.checkUsername(username, new ApiManager.EventActionCallback() {
            @Override
            public void onSuccess(String message) {
                if ("Username exists".equals(message))
                    runOnUiThread(() -> showToast("Nome de utilizador já existe."));
                else
                    updateUser(username, password);
                }
            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> showToast(errorMessage));
            }
        });
    }

    private void updateUser(String newUsername, String newPassword) {
        int userId = ApiManager.getUserIdFromSP();
        if (userId == -1) {
            showToast("Erro ao identificar utilizador. Faça login novamente.");
            return;
        }

        ApiManager.updateUser(newUsername, newPassword, new ApiManager.EventActionCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(EditUserActivity.this, "Utilizador atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    if (newUsername != null || newPassword != null)
                        ApiManager.saveUserCredentialsToSP(EditUserActivity.this, newUsername, newPassword);

                    // Send the updated username back to SettingsActivity
                    Intent resultIntent = new Intent();
                    if (newUsername != null)
                        resultIntent.putExtra("UPDATED_USERNAME", newUsername);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> showToast(errorMessage));
            }
        });
    }

    private void confirmAccountRemoval() {
        new AlertDialog.Builder(this)
                .setTitle("Remover Conta")
                .setMessage("Remover a sua conta? Esta ação é irreversível.")
                .setPositiveButton("Sim", (dialog, which) -> removeUser())
                .setNegativeButton("Não", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void removeUser() {
        ApiManager.removeUser(new ApiManager.EventActionCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(EditUserActivity.this, "Conta removida com sucesso!", Toast.LENGTH_SHORT).show();
                    // Clear stored credentials and redirect to login screen
                    ApiManager.clearAuthToken();
                    Intent intent = new Intent(EditUserActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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
