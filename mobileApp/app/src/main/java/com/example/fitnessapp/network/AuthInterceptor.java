package com.example.fitnessapp.network;

import com.example.fitnessapp.utils.SessionManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private SessionManager session;

    public AuthInterceptor(SessionManager session) {
        this.session = session;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = session.getAccessToken();
        Request original = chain.request();

        if (token == null) return chain.proceed(original);

        Request request = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(request);
    }
}