package estgoh.tam2425.gabriellopes.tp03_2425_gabriellopes;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.about_activity_layout);

        // Initialize back arrou button
        ImageButton backArrowButton = findViewById(R.id.backArrowButton);

        // Set click listener to go back to the previous activity
        backArrowButton.setOnClickListener(v -> finish());
    }
}
