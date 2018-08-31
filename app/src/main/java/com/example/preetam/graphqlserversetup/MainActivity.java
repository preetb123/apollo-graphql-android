package com.example.preetam.graphqlserversetup;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloMutationCall;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.example.preetam.graphqlserversetup.type.VideoInput;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";
  private MainApplication application;
  private ListView listView;
  private VideoAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    application = (MainApplication) getApplication();

    Button createVideo = findViewById(R.id.add_button);
    final EditText input = findViewById(R.id.input);
    listView = findViewById(R.id.list);
    createVideo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addVideo(input.getText().toString());
      }
    });
    adapter = new VideoAdapter(this, android.R.layout.simple_list_item_1,
        android.R.id.text1, new ArrayList<String>());
    listView.setAdapter(adapter);
    loadVideosList();
    subscribeToNewVideos();
  }

  public class VideoAdapter extends ArrayAdapter<String> {
    public VideoAdapter(Context context, int resource, int text1, ArrayList<String> strings) {
      super(context, resource, text1, strings);
    }
  }

  private void subscribeToNewVideos() {
    Rx2Apollo.from(application.apolloClient().subscribe(OnVideoAddedSubscription.builder().build()))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {
          final String title = response.data().videoAdded.title;
          Toast.makeText(application, "New video received", Toast.LENGTH_SHORT).show();
          adapter.add(title);
        }, throwable -> {
          Toast.makeText(application, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        });
  }

  private void loadVideosList() {
    Log.d(TAG, "loadVideosList() called");
    Rx2Apollo.from(application.apolloClient().query(AllVideosQuery.builder().build()))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {
          loadData(response.data().videos);
        }, throwable -> {
          Toast.makeText(application, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        });
  }

  private void loadData(List<AllVideosQuery.Video> videos) {
    List<String> videoTitles = videos.stream().map(video -> video.title).collect(Collectors.toList());
    adapter.addAll(videoTitles);
  }

  public void addVideo(String input) {
    VideoInput videoInput = VideoInput.builder()
        .title(input)
        .duration(400)
        .released(true)
        .build();
    ApolloMutationCall<AddVideoMutation.Data> addVideoMutation = application.apolloClient().mutate(AddVideoMutation.builder().video(videoInput).build());

    Rx2Apollo.from(addVideoMutation)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {
          Toast.makeText(application, "Video added successfully", Toast.LENGTH_SHORT).show();
        }, throwable -> {
          Toast.makeText(application, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        });
  }
}
