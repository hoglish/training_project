package com.example.fitnessapp.network;

import com.example.fitnessapp.models.AuthResponse;
import com.example.fitnessapp.models.RefreshRequest;
import com.example.fitnessapp.utils.SessionManager;
import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {
    private final SessionManager session;
    private final ApiService refreshApi;

    public TokenAuthenticator(SessionManager session, ApiService refreshApi) {
        this.session = session;
        this.refreshApi = refreshApi;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        if (responseCount(response) >= 2) {
            session.logout();
            return null;
        }

        String refreshToken = session.getRefreshToken();
        if (refreshToken == null) return null;

        Call<AuthResponse> call = refreshApi.refresh(new RefreshRequest(refreshToken));

        retrofit2.Response<AuthResponse> refreshResponse = call.execute();

        if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
            String newAccess = refreshResponse.body().getAccessToken();
            String newRefresh = refreshResponse.body().getRefreshToken();
            session.saveTokens(newAccess, newRefresh);

            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newAccess)
                    .build();
        } else {
            session.logout();
            return null;
        }
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) result++;
        return result;
    }
}