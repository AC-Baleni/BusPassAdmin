package com.example.buspassadmin.Home;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import com.example.buspassadmin.Auth.Login;
import com.example.buspassadmin.BookedTickets.BookedTicketsFragment;
import com.example.buspassadmin.Profile.CompanyProfileFragment;
import com.example.buspassadmin.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private final Fragment profileFragment = new BookedTicketsFragment();
    private final Fragment ticketFragment = new TicketsFragment();
    private final Fragment historyFragment = new UpdatesFragment();
    // Initialize Firebase Authentication
    private FirebaseAuth mAuth;

    private final Fragment[] fragments = {profileFragment, ticketFragment, historyFragment};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        mAuth = FirebaseAuth.getInstance();
        setSupportActionBar(toolbar);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(navItemSelectedListener);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Show the HomeFragment by default
        displayFragment(new UpdatesFragment());
    }
    private NavigationView.OnNavigationItemSelectedListener navItemSelectedListener =
            new NavigationView.OnNavigationItemSelectedListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment fragment = null;

                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_company_profile) {
                        fragment = new CompanyProfileFragment();
                    }
                    if (fragment != null) {
                        displayFragment(fragment);
                        // Close the navigation drawer after selecting an item
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    }
                    else if (itemId == R.id.nav_logout) {
                        // Handle logout
                        logout();
                        // Then close the drawer
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                    return false;
                }
            };


    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment fragment = null;

                    int itemId = item.getItemId();
                    if (itemId == R.id.profile) {
                        fragment = new BookedTicketsFragment();
                    } else if (itemId == R.id.tickets) {
                        fragment = new TicketsFragment();
                    } else if (itemId == R.id.updates) {
                        fragment = new UpdatesFragment();
                    }

                    if (fragment != null) {
                        displayFragment(fragment);
                        return true;
                    }

                    return false;
                }
            };

    private void displayFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    private void logout() {
        mAuth.signOut();
        // Navigate to Login activity
        Intent intent = new Intent(MainActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Optional: This will finish the current activity to prevent the user from returning to it with the back button
    }

}
