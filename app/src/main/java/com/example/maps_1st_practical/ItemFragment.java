package com.example.maps_1st_practical;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.maps_1st_practical.dummy.DummyContent;
import com.example.maps_1st_practical.dummy.DummyContent.DummyItem;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ItemFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private MyItemRecyclerViewAdapter adapter;

    private static final String API_URL = "https://api3.themonetizr.com/api/offers";
    private static final String AUTHORIZATION_HEADER = "Bearer 0EC739B6CA9AC49961216F280FB8BE7MHASGGYWER";

    public ItemFragment() {
    }

    public static ItemFragment newInstance(int columnCount) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        adapter = new MyItemRecyclerViewAdapter(DummyContent.ITEMS, mListener);

        fetchDataFromApi();
    }

    private void fetchDataFromApi() {
        RequestQueue queue = Volley.newRequestQueue(requireActivity());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, API_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleApiResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleApiError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", AUTHORIZATION_HEADER);
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");
                return params;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void handleApiResponse(JSONObject response) {
        try {
            JSONArray articles = response.getJSONArray("results");
            for (int i = 0; i < articles.length(); i++) {
                JSONObject article = articles.getJSONObject(i);
                DummyContent.DummyItem item = new DummyContent.DummyItem(
                        article.getString("id"),
                        article.getString("name"),
                        article.getString("description"),
                        article.getString("offer_thumbnail"),
                        article.getString("is_active")
                );
                DummyContent.addItem(item);
            }
        } catch (Exception e) {
            Log.d("Praktiskais exception", e.toString());
        }
        adapter.notifyDataSetChanged();
    }

    private void handleApiError(VolleyError error) {
        try {
            Log.d("Praktiskais exception, json", "response body:" + error.toString());
            String responseBody = new String(error.networkResponse.data, "utf-8");
            Log.d("Praktiskais exception, json", "response body:" + responseBody);
            JSONObject data = new JSONObject(responseBody);
            JSONArray errors = data.getJSONArray("errors");
            JSONObject jsonMessage = errors.getJSONObject(0);
            String message = jsonMessage.getString("message");
            Log.d("Praktiskais exception", message + " the status code: " + error.networkResponse.statusCode);
        } catch (JSONException | UnsupportedEncodingException e) {
            Log.d("Praktiskais exception, json", "error" + e.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(adapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(DummyItem item);
    }
}