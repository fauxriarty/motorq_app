package com.example.motorq

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class ManagerMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_main)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.navigation_drivers -> selectedFragment = DriversFragment()
                R.id.navigation_vehicles -> selectedFragment = VehiclesFragment()
                R.id.navigation_assignments -> selectedFragment = AssignmentsFragment()
            }
            selectedFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, it)
                    .commit()
            }
            true
        }

        bottomNavigation.selectedItemId = R.id.navigation_drivers
    }
}
