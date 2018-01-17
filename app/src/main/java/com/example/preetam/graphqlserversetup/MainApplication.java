package com.example.preetam.graphqlserversetup;

import android.app.Application;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;

import okhttp3.OkHttpClient;

public class MainApplication extends Application {

  private static final String BASE_URL = "http://10.0.2.2:3002/graphql/";
  private static final String SUBSCRIPTIONS_URL = "ws://10.0.2.2:3002/subscriptions";
  private ApolloClient apolloClient;

  @Override
  public void onCreate() {
    super.onCreate();
    OkHttpClient okHttpClient = new OkHttpClient.Builder()
        .build();
    apolloClient = ApolloClient.builder()
        .serverUrl(BASE_URL)
        .okHttpClient(okHttpClient)
        .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(SUBSCRIPTIONS_URL, okHttpClient))
        .build();
  }

  public ApolloClient apolloClient() {
    return apolloClient;
  }
}
