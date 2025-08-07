package estgoh.tam2425.gabriellopes.tp03_2425_gabriellopes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class EventAdapter extends ArrayAdapter<ApiManager.EventResponse> {

    private final Context context;
    private final List<ApiManager.EventResponse> events;

    public EventAdapter(Context context, List<ApiManager.EventResponse> events) {
        super(context, R.layout.event_item, events);
        this.context = context;
        this.events = events;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the current event
        ApiManager.EventResponse event = events.get(position);

        // Populate view
        if (convertView == null) {
            // Inflate the layout and create a new view hierarchy rooted at this layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.event_item, parent, false);
        }

        // Get references to the UI components
        ImageView eventTypeImage = convertView.findViewById(R.id.eventTypeImage);
        ImageView eventFreeImage = convertView.findViewById(R.id.eventFreeImage);
        ImageView eventBookedImage = convertView.findViewById(R.id.eventBooked);
        TextView eventDescription = convertView.findViewById(R.id.eventDescription);
        ImageView locationIcon = convertView.findViewById(R.id.locationIcon);
        TextView eventLocation = convertView.findViewById(R.id.eventLocation);
        ImageView dateIcon = convertView.findViewById(R.id.dateIcon);
        TextView eventDate = convertView.findViewById(R.id.eventDate);
        TextView eventTime = convertView.findViewById(R.id.eventTime);

        // Load images based on event type
        switch (event.eventType.toLowerCase()) {
            case "cultural":
                eventTypeImage.setImageResource(R.drawable.cultural);
                break;
            case "profissional":
                eventTypeImage.setImageResource(R.drawable.professional);
                break;
            case "academico":
                eventTypeImage.setImageResource(R.drawable.academic);
                break;
            case "desportivo":
                eventTypeImage.setImageResource(R.drawable.sports);
                break;
            case "gastronomico":
                eventTypeImage.setImageResource(R.drawable.food);
                break;
            default:
                eventTypeImage.setImageResource(R.drawable.default_image);
                break;
        }

        // Set description
        eventDescription.setText(event.description);

        // Set location
        locationIcon.setImageResource(R.drawable.ic_location);
        eventLocation.setText(event.location);

        String[] dateTime = event.eventDate.split(" ");
        eventDate.setText(dateTime[0]);
        eventTime.setText(dateTime.length > 1 ? dateTime[1] : "");

        // Set the booked event image
        if (event.isBooked)
            eventBookedImage.setImageResource(R.drawable.booked);
        else
            eventBookedImage.setImageResource(R.drawable.notbooked);

        // Set the free/paid image
        if (event.price == 0)
            eventFreeImage.setImageResource(R.drawable.free);
        else
            eventFreeImage.setImageResource(R.drawable.paid);

        return convertView;
    }
}
