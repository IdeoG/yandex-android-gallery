package com.ideog.android.gallery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.ideog.android.gallery.models.FlickrAPI;
import com.ideog.android.gallery.models.FlickrResultModel;
import com.ideog.android.gallery.models.Photo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class GalleryActivity extends AppCompatActivity {
    private static String TAG = "GalleryActivity";
    private final String API_KEY = "967e2082cbdb43b27b6c0df3325d1843";

    @BindView(R.id.search_btn) Button search_btn;
    @BindView(R.id.search_edit) EditText search_edit;
    @BindView(R.id.my_recycler_view) RecyclerView recycler_view;
    RecyclerView.Adapter adapter;

    private static ArrayList<String> imageUrls = new ArrayList<>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        initializeUI();
    }

    @OnClick(R.id.search_btn)
    public void Search() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.flickr.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FlickrAPI flickrAPI = retrofit.create(FlickrAPI.class);

        Observable<FlickrResultModel> model = flickrAPI.getPhotos(
                API_KEY,
                search_edit.getText().toString(),
                "interestingness-asc",
                "1",
                "json",
                "url_m"
        );

        model.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( response -> {
                    imageUrls.clear();
                    for (Photo photo : response.getPhotos().getPhoto()) {
                        String url = photo.getUrlM();
                        imageUrls.add(0, url);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void initializeUI() {

        ButterKnife.bind(this);

        recycler_view.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ImageAdapter(
                GalleryActivity.this,
                imageUrls
        );

        recycler_view.setAdapter(adapter);
        search_btn.setEnabled(false);

        RxHelper.searchValidatorObservable(search_edit)
                .subscribe(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        Log.i(TAG, "onNext: " + aBoolean);
                        search_btn.setEnabled(aBoolean);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}
