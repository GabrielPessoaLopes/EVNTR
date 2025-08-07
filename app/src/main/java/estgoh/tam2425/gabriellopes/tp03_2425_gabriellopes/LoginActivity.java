package estgoh.tam2425.gabriellopes.tp03_2425_gabriellopes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Check if the user is already logged in
        String token = ApiManager.getAuthToken();
        int userId = ApiManager.getUserIdFromSP();
        if (token != null && userId != -1) {
            openMainActivity(userId);  // Redirect to MainActivity if logged in
            return;
        }

        setContentView(R.layout.login_activity_layout);

        // Start ApiManager
        ApiManager.initialize(this);

        // Initialize UI components
        EditText usernameField = findViewById(R.id.usernameField);
        EditText passwordField = findViewById(R.id.passwordField);
        Button loginButton = findViewById(R.id.loginButton);
        TextView registerLink = findViewById(R.id.registerLink);

        loginButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty())
                performLogin(username, password);
            else {
                if (username.isEmpty())
                    usernameField.setError("Username required");
                if (password.isEmpty())
                    passwordField.setError("Password required");
            }
        });

        registerLink.setOnClickListener(v -> openRegisterActivity());
    }

    protected void performLogin(String username, String password) {
        ApiManager.login(username, password, new ApiManager.LoginCallback() {
            @Override
            public void onSuccess(String token, int userId) {
                runOnUiThread(() -> {
                    // Save credentials to SP only if login is successful
                    ApiManager.saveUserCredentialsToSP(LoginActivity.this, username, password);
                    openMainActivity(userId);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Credenciais inválidas", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void openMainActivity(int userId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }

    private void openRegisterActivity() {
        Intent intent = new Intent(this, AddUserActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            int userId = ApiManager.getUserIdFromSP();
            if (userId != -1)
                openMainActivity(userId);
            else
                Toast.makeText(this, "Utilizador não encontrado.", Toast.LENGTH_SHORT).show();
        }
    }
}
