package estgoh.tam2425.gabriellopes.tp03_2425_gabriellopes;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class ApiManager {

    private static final String DATABASE_URL = "https://tam2425tp3apigabriellopes.vercel.app/";
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String TOKEN_KEY = "authToken";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String USER_ID_KEY = "userId";

    private static ApiService apiService;
    private static SharedPreferences sharedPreferences;

    public static void initialize(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .dns(Dns.SYSTEM)
                    .addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(DATABASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
    }

    public static ApiService getInstance() {
        if (apiService == null) {
            throw new IllegalStateException("ApiManager not initialized.");
        }
        return apiService;
    }

    public static void saveAuthToken(String token, String username, int userId) {
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(TOKEN_KEY, token);
            editor.putString(USERNAME_KEY, username);
            editor.putInt(USER_ID_KEY, userId);
            editor.apply();
        }
    }

    public static String getAuthToken() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(TOKEN_KEY, null);
        }
        return null;
    }

    public static void clearAuthToken() {
        if (sharedPreferences != null) {
            sharedPreferences.edit().remove(TOKEN_KEY).apply();
        }
    }

    //----------------------------------------- USER GETTERS -----------------------------------
    public static void login(String username, String password, LoginCallback callback) {
        LoginRequest request = new LoginRequest(username, password);
        getInstance().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveAuthToken(response.body().token, username, response.body().userId);
                    callback.onSuccess(response.body().token, response.body().userId);
                } else {
                    try {
                        callback.onFailure(getAPIErrorMessage(response.errorBody().string()));
                    } catch (IOException e) {
                        callback.onFailure("Runtime error. ");
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public static void checkUsername(String username, EventActionCallback callback) {
        getInstance().checkUsernameExists(username).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean exists = "true".equalsIgnoreCase(response.body().message);
                    callback.onSuccess(exists ? "Username exists" : "Username does not exist");
                } else {
                    try {
                        callback.onFailure(getAPIErrorMessage(response.errorBody().string()));
                    } catch (Exception e) {
                        callback.onFailure("Runtime error. ");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }


    public static int getUserIdFromSP() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt(USER_ID_KEY, -1);
        }
        return -1;
    }

    public static String getUsernameFromSP() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(USERNAME_KEY, null);
        }
        return null;
    }

    //----------------------------------------- USER MODIFIERS ----------------------------------
    public static void registerUser(String username, String password, EventActionCallback callback) {
        RegisterRequest request = new RegisterRequest(username, password);
        getInstance().registerUser(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().message);
                } else {
                    try {
                        callback.onFailure(getAPIErrorMessage(response.errorBody().string()));
                    } catch (Exception e) {
                        callback.onFailure("Runtime error. ");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                callback.onFailure("Connection error. Check internet connection. ");
            }
        });
    }

    public static void updateUser(String username, String password, EventActionCallback callback) {
        // Retrieve the current username from SharedPreferences
        String currentUsername = getUsernameFromSP();
        if (currentUsername == null) {
            callback.onFailure("User information missing.");
            return;
        }

        // Fetch the user ID using the current username
        getInstance().getUserIdByUsername(currentUsername).enqueue(new Callback<UserIdResponse>() {
            @Override
            public void onResponse(Call<UserIdResponse> call, Response<UserIdResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int userId = response.body().userId;

                    String newUsername = null;
                    String newPassword = null;

                    // Check if the username is not null or empty
                    if (username != null && !username.isEmpty())
                        newUsername = username;
                    // Check if the password is not null or empty
                    if (password != null && !password.isEmpty())
                        newPassword = password;

                    // Create the UpdateUserRequest object with the filtered values
                    UpdateUserRequest request = new UpdateUserRequest(newUsername, newPassword);
                    String token = getAuthToken();
                    if (token == null) {
                        callback.onFailure("Authentication error. Please log in again.");
                        return;
                    }

                    // Make the API call to update the user
                    getInstance().updateUser(token, userId, request).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                callback.onSuccess(response.body().message);
                            } else {
                                try {
                                    callback.onFailure(getAPIErrorMessage(response.errorBody().string()));
                                } catch (Exception e) {
                                    callback.onFailure("Runtime error. ");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            callback.onFailure(t.getMessage());
                        }
                    });
                } else {
                    callback.onFailure(response.message());
                }
            }

            @Override
            public void onFailure(Call<UserIdResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public static void removeUser(EventActionCallback callback) {
        // Retrieve the current username from SharedPreferences
        String currentUsername = getUsernameFromSP();
        if (currentUsername == null) {
            callback.onFailure("User information missing.");
            return;
        }

        // Fetch the user ID using the current username
        getInstance().getUserIdByUsername(currentUsername).enqueue(new Callback<UserIdResponse>() {
            @Override
            public void onResponse(Call<UserIdResponse> call, Response<UserIdResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int userId = response.body().userId;

                    // Retrieve the authentication token
                    String token = getAuthToken();
                    if (token == null) {
                        callback.onFailure("Authentication error. Please log in again.");
                        return;
                    }

                    // Make the API call to delete the user
                    getInstance().deleteUser(token, userId).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                callback.onSuccess(response.body().message);
                            } else {
                                try {
                                    callback.onFailure(getAPIErrorMessage(response.errorBody().string()));
                                } catch (Exception e) {
                                    callback.onFailure("Runtime error. ");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            callback.onFailure(t.getMessage());
                        }
                    });
                } else {
                    try {
                        callback.onFailure(getAPIErrorMessage(response.errorBody().string()));
                    } catch (Exception e) {
                        callback.onFailure("Runtime error. ");
                    }
                }
            }

            @Override
            public void onFailure(Call<UserIdResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public static void saveUserCredentialsToSP(Context context, String username, String password) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (username != null)
            editor.putString(USERNAME_KEY, username);
        if (password != null)
            editor.putString(PASSWORD_KEY, password);
        editor.apply();
    }

    public static void clearUserCredentialsFromSP() {
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(USERNAME_KEY);
            editor.remove(PASSWORD_KEY);
            editor.apply();
        }
    }

    //----------------------------------------- EVENT MODIFIERS ----------------------------------
    public static void addEvent(EventRequest eventRequest, EventActionCallback callback) {
        String token = getAuthToken(); // Retrieve authentication token
        if (token == null) {
            callback.onFailure("Authentication error. Please log in again.");
            return;
        }

        getInstance().addEvent(token, eventRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().message);
                } else {
                    try {
                        callback.onFailure(getAPIErrorMessage(response.errorBody().string()));
                    } catch (Exception e) {
                        callback.onFailure("Runtime error. ");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }


    public static void updateEvent(int eventId, EventRequest eventRequest, EventActionCallback callback) {
        String token = getAuthToken(); // Retrieve authentication token
        if (token == null) {
            callback.onFailure("Authentication error. Please log in again.");
            return;
        }

        getInstance().updateEvent(token, eventId, eventRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().message);
                } else {
                    try {
                        callback.onFailure(getAPIErrorMessage(response.errorBody().string()));
                    } catch (Exception e) {
                        callback.onFailure("Runtime error. ");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public static void removeEvent(int eventId, EventActionCallback callback) {
        String token = getAuthToken(); // Retrieve authentication token
        if (token == null) {
            callback.onFailure("Authentication error. Please log in again.");
            return;
        }

        getInstance().deleteEvent(token, eventId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().message);
                } else {
                    try {
                        callback.onFailure(getAPIErrorMessage(response.errorBody().string()));
                    } catch (Exception e) {
                        callback.onFailure("Runtime error. ");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }


    //----------------------------------------- EVENT GETTERS --------------------------------
    public static void getEvents(String eventType, boolean futureEvents, boolean bookedEvents, EventListCallback callback) {
        String token = getAuthToken(); // Retrieve the authentication token
        if (token == null) {
            callback.onFailure("Authentication error. Please log in again.");
            return;
        }

        if (eventType != null && eventType.equals("Qualquer"))
            eventType = null;

        // Make the API request with the query parameters
        getInstance().getEvents(token, eventType, futureEvents, bookedEvents).enqueue(new Callback<List<EventResponse>>() {
            @Override
            public void onResponse(Call<List<EventResponse>> call, Response<List<EventResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    try {
                        callback.onFailure(getAPIErrorMessage(response.errorBody().string()));
                    } catch (IOException e) {
                        callback.onFailure("Runtime error.");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<EventResponse>> call, Throwable t) {
                callback.onFailure("Connection error. Check internet connection.");
            }
        });
    }

    public static void isEventOwner(int eventId, EventOwnershipCallback callback) {
        String token = getAuthToken();
        if (token == null) {
            callback.onFailure("Authentication error. Please log in again.");
            return;
        }

        getInstance().isEventOwner(token, eventId).enqueue(new Callback<OwnershipResponse>() {
            @Override
            public void onResponse(Call<OwnershipResponse> call, Response<OwnershipResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().isOwner);
                } else {
                    try {
                        callback.onFailure(getAPIErrorMessage(response.errorBody().string()));
                    } catch (Exception e) {
                        callback.onFailure("Runtime error. ");
                    }
                }
            }

            @Override
            public void onFailure(Call<OwnershipResponse> call, Throwable t) {
                callback.onFailure("Connection error. Check internet connection. ");
            }
        });
    }

    //----------------------------------------- EVENT BOOKINGS --------------------------------------
    public static void bookEvent(int eventId, EventActionCallback callback) {
        String token = getAuthToken(); // Retrieve authentication token
        if (token == null) {
            callback.onFailure("Authentication error. Please log in again.");
            return;
        }

        getInstance().bookEvent(token, eventId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().message);
                } else {
                    try {
                        callback.onFailure(getAPIErrorMessage(response.errorBody().string()));
                    } catch (Exception e) {
                        callback.onFailure("Runtime error. ");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                callback.onFailure("Connection error. Check internet connection. ");
            }
        });
    }

    public static void cancelBooking(int eventId, EventActionCallback callback) {
        String token = getAuthToken(); // Retrieve authentication token
        if (token == null) {
            callback.onFailure("Authentication error. Please log in again.");
            return;
        }

        getInstance().cancelBooking(token, eventId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().message);
                } else {
                    try {
                        callback.onFailure(getAPIErrorMessage(response.errorBody().string()));
                    } catch (Exception e) {
                        callback.onFailure("Runtime error. ");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public static void getBookingsForEvent(int eventId, EventBookingsCallback callback) {
        String token = getAuthToken();
        if (token == null) {
            callback.onFailure("Authentication error. Please log in again.");
            return;
        }

        getInstance().getBookingsForEvent(token, eventId).enqueue(new Callback<BookingListResponse>() {
            @Override
            public void onResponse(Call<BookingListResponse> call, Response<BookingListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookingResponse> bookings = response.body().getBookings();
                    callback.onSuccess(bookings);
                }  else {
                    try {
                        callback.onFailure(getAPIErrorMessage(response.errorBody().string()));
                    } catch (Exception e) {
                        callback.onFailure("Runtime error. ");
                    }
                }
            }
            @Override
            public void onFailure(Call<BookingListResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }
    //---------------------------------------- other methods ---------------------------------------
    public static String getAPIErrorMessage(String apiResponse) {
        if (apiResponse == null || apiResponse.isEmpty()) {
            return apiResponse;
        }

        //Parses Json message
        try {
            JSONObject json = new JSONObject(apiResponse);
            if (json.has("message"))
                apiResponse = json.getString("message");
        } catch (Exception e) {
            return apiResponse;
        }

        return apiResponse;
    }
    //----------------------------------------- HTTP REQUESTS --------------------------------------
    public interface ApiService {
        @POST("login")
        Call<LoginResponse> login(@Body LoginRequest request);

        @POST("/users")
        Call<ApiResponse> registerUser(@Body RegisterRequest request);

        @PUT("/users/{userId}")
        Call<ApiResponse> updateUser(@Header("Authorization") String token, @Path("userId") int userId, @Body UpdateUserRequest request);

        @DELETE("/users/{userId}")
        Call<ApiResponse> deleteUser(@Header("Authorization") String token, @Path("userId") int userId);

        @GET("/users/id/{username}")
        Call<UserIdResponse> getUserIdByUsername(@Path("username") String username);

        @GET("/users/username/{userId}")
        Call<UsernameResponse> getUsernameByUserId(@Path("userId") int userId);

        @GET("/users/exists/{username}")
        Call<ApiResponse> checkUsernameExists(@Path("username") String username);

        @GET("/events/filtered")
        Call<List<EventResponse>> getEvents(@Header("Authorization") String token, @Query("eventType") String eventType, @Query("futureEvents") boolean future, @Query("booked") boolean booked);

        @GET("/events/{eventId}/is-owner")
        Call<OwnershipResponse> isEventOwner(@Header("Authorization") String token, @Path("eventId") int eventId);

        @POST("/events")
        Call<ApiResponse> addEvent(@Header("Authorization") String token, @Body EventRequest request);

        @PUT("/events/{eventId}")
        Call<ApiResponse> updateEvent(@Header("Authorization") String token, @Path("eventId") int eventId, @Body EventRequest request);

        @DELETE("/events/{eventId}")
        Call<ApiResponse> deleteEvent(@Header("Authorization") String token, @Path("eventId") int eventId);

        @GET("/events/{eventId}")
        Call<EventResponse> getEventById(@Header("Authorization") String token, @Path("eventId") int eventId);

        @POST("/events/{eventId}/book")
        Call<ApiResponse> bookEvent(@Header("Authorization") String token, @Path("eventId") int eventId);

        @DELETE("/events/{eventId}/bookings")
        Call<ApiResponse> cancelBooking(@Header("Authorization") String token, @Path("eventId") int eventId);

        @GET("/events/creator/{userId}")
        Call<List<EventResponse>> getEventsByCreator(@Header("Authorization") String token, @Path("userId") int userId);

        @GET("/events/{eventId}/bookings")
        Call<BookingListResponse> getBookingsForEvent(@Header("Authorization") String token, @Path("eventId") int eventId);

    }

    //----------------------------------------- INTERFACES -----------------------------------
    public interface LoginCallback {
        void onSuccess(String token, int userId);
        void onFailure(String errorMessage);
    }

    public interface UserDetailsCallback {
        void onSuccess(Object response);
        void onFailure(String errorMessage);
    }

    public interface EventListCallback {
        void onSuccess(List<EventResponse> events);
        void onFailure(String errorMessage);
    }

    public interface EventActionCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    // Callback interface for ownership check
    public interface EventOwnershipCallback {
        void onSuccess(boolean isOwner);
        void onFailure(String errorMessage);
    }

    // Callback interface for event bookings
    public interface EventBookingsCallback {
        void onSuccess(List<BookingResponse> bookings);
        void onFailure(String errorMessage);
    }

    //----------------------------------------- CLASSES -------------------------------------
    public static class LoginRequest {
        String username;
        String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class LoginResponse {
        String token;
        int userId;
    }

    public static class RegisterRequest {
        String username;
        String password;

        public RegisterRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class UserIdResponse {
        int userId;
    }

    public static class UsernameResponse {
        String username;
    }

    public static class UpdateUserRequest {
        String username;
        String password;

        public UpdateUserRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class EventRequest {
        String eventType;
        String description;
        String location;
        String eventDate;
        String deadlineDate;
        int seats;
        double price;

        public EventRequest(String eventType, String description, String location, String eventDate, String deadlineDate, int seats, double price) {
            this.eventType = eventType;
            this.description = description;
            this.location = location;
            this.eventDate = eventDate;
            this.deadlineDate = deadlineDate;
            this.seats = seats;
            this.price = price;
        }
    }

    public static class EventResponse {
        int id;
        String eventType;
        String description;
        String location;
        String eventDate;
        String deadlineDate;
        int seats;
        double price;
        boolean isBooked;
    }

    public static class ApiResponse {
        String status;
        String message;
    }

    // OwnershipResponse class
    public static class OwnershipResponse {
        boolean isOwner;
    }
    public static class BookingResponse {
        private String bookingDate;
        private int userId;
        private String username;

        public String getBookingDate() {
            return bookingDate;
        }

        public int getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }
    }

    public static class BookingListResponse {
        private List<BookingResponse> bookings;

        public List<BookingResponse> getBookings() {
            return bookings;
        }
    }

}
