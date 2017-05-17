package ch.trvlr.trvlr.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import ch.trvlr.trvlr.AppController;
import ch.trvlr.trvlr.R;
import ch.trvlr.trvlr.model.Chat;
import ch.trvlr.trvlr.model.Traveler;

public class BaseDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{


    // ----- Static.

    public static final String TAG = BaseDrawerActivity.class.getSimpleName();


    // ----- State.

    protected DrawerLayout mDrawerLayout;
    protected FrameLayout mFrameLayout;
    protected NavigationView mNavigationView;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected Menu menu;
    protected int travelerId = -1;
    protected int chatId = -1;
    protected Traveler currentUser = null;
    protected AppController appController = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base_drawer);

        appController = (AppController) this.getApplicationContext();

        mFrameLayout = (FrameLayout) findViewById(R.id.content_frame);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        menu = mNavigationView.getMenu();
        rebuildMenu();
        loadTravelerId();
    }

    @Override
    protected void onResume() {
        super.onResume();
        appController.setCurrentActivity(this);
        rebuildMenu();
    }

    @Override
    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void loadTravelerId() {
        if (travelerId > 0) return;

        Map<String, String> params = new HashMap();
        params.put("firebaseId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        params.put("firebaseToken", FirebaseAuth.getInstance().getCurrentUser().getToken(false).getResult().getToken());
        JSONObject parameters = new JSONObject(params);

        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(
                Request.Method.POST,
                "http://trvlr.ch:8080/api/traveler/auth",
                parameters,
                loadTravelerIdSuccess(),
                loadError()
        ));
    }

    private Response.Listener<JSONObject> loadTravelerIdSuccess() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    travelerId = response.getInt("id");

                    currentUser = new Traveler(
                        response.getInt("id"),
                        response.getString("firstName"),
                        response.getString("lastName"),
                        response.getString("email"),
                        response.getString("uid")
                    );

                    // Make the current user for other activities available.
                    AppController.getInstance().setCurrentUser(currentUser);

                    // Load the private chats of this user.
                    AppController.getInstance().addToRequestQueue(new JsonArrayRequest(Request.Method.GET,
                            "http://trvlr.ch:8080/api/private-chats/list/" + currentUser.getId(),
                            null,
                            loadTravelerChatsSuccess(AppController.CHATROOM_TYPE_PRIVATE),
                            loadError()
                    ));

                    // Load the public chats of this user.
                    AppController.getInstance().addToRequestQueue(new JsonArrayRequest(Request.Method.GET,
                            "http://trvlr.ch:8080/api/public-chats/list/" + currentUser.getId(),
                            null,
                            loadTravelerChatsSuccess(AppController.CHATROOM_TYPE_PUBLIC),
                            loadError()
                    ));
                } catch (JSONException e) {
                    Toast.makeText(BaseDrawerActivity.this, TAG + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private Response.Listener<JSONArray> loadTravelerChatsSuccess(final int chatType) {
        return new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    if (response.length() == 0) {
                        // TODO: check if empty json array is also response.length() == 0
//                        Toast.makeText(BaseDrawerActivity.this, "Can not chats (Type: " + chatType + ")", Toast.LENGTH_LONG).show();
                    } else {
                        for (int i = 0; i < response.length(); i++) {
                            Chat bo = null;

                            if (chatType == AppController.CHATROOM_TYPE_PRIVATE) {
                                JSONArray travelers = response.getJSONObject(i).getJSONArray("allTravelers");

                                if (travelers.length() > 1) {
                                    int uId1 = travelers.getJSONObject(0).getInt("id");
                                    int uId2 = travelers.getJSONObject(1).getInt("id");
                                    int indexToLoad = uId1 != currentUser.getId() ? 0 : 1;
                                    Traveler chatPartner = new Traveler(
                                            travelers.getJSONObject(indexToLoad).getInt("id"),
                                            travelers.getJSONObject(indexToLoad).getString("firstName"),
                                            travelers.getJSONObject(indexToLoad).getString("lastName"),
                                            travelers.getJSONObject(indexToLoad).getString("email"),
                                            travelers.getJSONObject(indexToLoad).getString("uid")
                                    );
                                    bo = new Chat(response.getJSONObject(i).getInt("id"), chatPartner.getFullname(), chatPartner);
                                }
                            } else {
                                String from = response.getJSONObject(i).getJSONObject("from").getString("name");
                                String to = response.getJSONObject(i).getJSONObject("to").getString("name");

                                bo = new Chat(response.getJSONObject(i).getInt("id"), from + " - " + to, Chat.CHATROOM_TYPE_PUBLIC);
                            }

                            if (bo != null) {
                                AppController.getInstance().addChat(chatType, bo);
                            }
                        }

                        rebuildMenu();
                    }
                } catch (JSONException e) {
                    Toast.makeText(BaseDrawerActivity.this, TAG + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    /**
     * Handle back button
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Chat activeChat = appController.getCurrentActiveChat();
            Chat activePublicChat = appController.getCurrentActivePublicChat();
            Activity currentActivity = appController.getCurrentActivity();
            String currentActivityClass = "";

            if (currentActivity != null) {
                currentActivityClass = appController.getCurrentActivity().getLocalClassName();
            }

            switch (currentActivityClass) {
                case "ui.ChatActivity":
                    if (activeChat.getChatType() == AppController.CHATROOM_TYPE_PRIVATE && activePublicChat != null) {
                        appController.setCurrentActiveChat(AppController.CHATROOM_TYPE_PRIVATE, null);
                        appController.setCurrentActiveChat(AppController.CHATROOM_TYPE_PUBLIC, activePublicChat);
                        startActivity(new Intent(getApplicationContext(), ChatActivity.class));
                    } else {
                        appController.setCurrentActiveChat(AppController.CHATROOM_TYPE_PUBLIC, null);
                        appController.setCurrentActiveChat(AppController.CHATROOM_TYPE_PRIVATE, null);
                        startActivity(new Intent(getApplicationContext(), FindConnectionActivity.class));
                    }

                    break;

                case "ui.ListPublicChatMembersActivity":
                    if (activePublicChat != null) {
                        appController.setCurrentActiveChat(AppController.CHATROOM_TYPE_PUBLIC, activePublicChat);
                        startActivity(new Intent(getApplicationContext(), ChatActivity.class));
                    } else {
                        appController.setCurrentActiveChat(AppController.CHATROOM_TYPE_PUBLIC, null);
                        appController.setCurrentActiveChat(AppController.CHATROOM_TYPE_PRIVATE, null);
                        startActivity(new Intent(getApplicationContext(), FindConnectionActivity.class));
                    }

                    break;

                default:
                    appController.setCurrentActiveChat(AppController.CHATROOM_TYPE_PUBLIC, null);
                    appController.setCurrentActiveChat(AppController.CHATROOM_TYPE_PRIVATE, null);
                    startActivity(new Intent(getApplicationContext(), FindConnectionActivity.class));
                    break;
            }
        }
    }

    /**
     * Handle for navigation action
     *
     * @param item MenuItem
     * @return Boolean
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (item.isChecked()){
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return false;
        }

        uncheckItems(); // TODO maybe there's a better way...
        Intent i;
        Bundle b;
        switch (id) {
            case R.layout.activity_login:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;
            case R.layout.activity_findconn:
                appController.setCurrentActiveChat(AppController.CHATROOM_TYPE_PUBLIC, null);
                appController.setCurrentActiveChat(AppController.CHATROOM_TYPE_PRIVATE, null);
                startActivity(new Intent(getApplicationContext(), FindConnectionActivity.class));
                break;
            case R.layout.activity_chat:
                i = new Intent(getApplicationContext(), ChatActivity.class);
                // Get the right bo of this chat room.
                Chat bo = ((AppController) getApplication()).getChat(item.getTitle().toString());

                if (bo.isPublicChat()) {
                    appController.setCurrentActivePublicChat(bo);
                    appController.setCurrentActiveChatTypeToPublic();
                } else {
                    appController.setCurrentActivePrivateChat(bo);
                    appController.setCurrentActiveChatTypeToPrivate();
                }

                startActivity(i);
                break;
            default:
                Toast.makeText(BaseDrawerActivity.this, TAG + ": " + "Activity unavailable", Toast.LENGTH_SHORT).show();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void uncheckItems() {
        Menu menu = mNavigationView.getMenu();
        for (int i = 0; i < menu.size(); i++)
            menu.getItem(i).setChecked(false);
    }

    public Response.ErrorListener loadError() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(BaseDrawerActivity.this, TAG + ": " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    /**
     * Rebuild the options menu
     *
     * @param menu Menu
     * @return Boolean
     */
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        rebuildMenu();

        return super.onCreateOptionsMenu(menu);
    }

    protected void rebuildMenu() {
        // Clear menu.
        menu.clear();

        // Add default menu items.
        SubMenu startChattingMenu = menu.addSubMenu("Get started");
        startChattingMenu.add(Menu.NONE, R.layout.activity_findconn, Menu.NONE, "Find connection");

        // Add public chat menu items.
        SubMenu publicChatsMenu = menu.addSubMenu("Public chats");
        LinkedList<Chat> publicChats = ((AppController) this.getApplication()).getPublicChats();
        for (Chat bo : publicChats) {
            publicChatsMenu.add(Menu.NONE, R.layout.activity_chat, Menu.NONE, bo.getChatName());
        }

        // Add private chat menu items.
        SubMenu privateChatsMenu = menu.addSubMenu("Private chats");
        LinkedList<Chat> privateChats = ((AppController) this.getApplication()).getPrivateChats();
        for (Chat bo : privateChats) {
            privateChatsMenu.add(Menu.NONE, R.layout.activity_chat, Menu.NONE, bo.getChatName());
        }
    }

    /**
     * Handler after onCreate is completed
     *
     * This hook is used to sync the state of the navigation drawer
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    /**
     * Handler for configuration changes
     *
     * @param newConfig Configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Initialize options menu
     *
     * @param menu Menu
     * @return Boolean
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);

        Chat activeChat = appController.getCurrentActiveChat();
        Activity currentActivity = appController.getCurrentActivity();
        String currentActivityClass = "";

        if (currentActivity != null) {
            currentActivityClass = appController.getCurrentActivity().getLocalClassName();
        }

        boolean isChat = currentActivityClass.equals("ui.ChatActivity");
        boolean isPublicChat = isChat && activeChat.isPublicChat();

        if (!isChat) {
            menu.removeItem(R.id.leave_chat_room);
        }

        if (!isPublicChat) {
            menu.removeItem(R.id.list_travelers);
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handler for options item action
     *
     * @param item MenuItem
     * @return Boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        Bundle b;

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.list_travelers:
                i = new Intent(getApplicationContext(), ListPublicChatMembersActivity.class);
                b = new Bundle();
                b.putInt("chatId", chatId);
                i.putExtras(b);
                startActivity(i);
                break;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;

            case R.id.leave_chat_room:
                leaveChat();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void leaveChat() {
        try {
            Chat bo = AppController.getInstance().getCurrentActiveChat();
            String chat = bo.isPublicChat() ? "public-chats" : "private-chats";
            int chatId = bo.getChatId();
            final String json = new JSONObject().put("travelerId", currentUser.getId()).toString();

            AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST,
                    "http://trvlr.ch:8080/api/" + chat + "/" + chatId + "/leave",
                    leaveChatSuccess(),
                    loadError()) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return json == null ? null : json.getBytes("utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Response.Listener leaveChatSuccess() {
        return new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                AppController controller = AppController.getInstance();
                Chat bo = controller.getCurrentActiveChat();
                controller.removeChat(bo.getChatId(), controller.getCurrentActiveChatType());
                finish();

                // Reset current active chat.
                controller.setCurrentActiveChat(bo.getChatType(), null);

                // Show find connection after leaving a chat room.
                startActivity(new Intent(getApplicationContext(), FindConnectionActivity.class));
            }
        };
    }

    protected void clearReferences() {
        Activity currentActivity = appController.getCurrentActivity();

        if (this.equals(currentActivity)) {
            appController.setCurrentActivity(null);
        }
    }
}

