package it.polito.mad.team19lab2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.File

private const val RC_SIGN_IN=123

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val u=FirebaseAuth.getInstance().currentUser
        if(u == null) {
            // Choose authentication providers
            val providers = arrayListOf(
                AuthUI.IdpConfig.GoogleBuilder().build()
            )
            // Create and launch sign-in intent
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setLogo(R.drawable.ic_shop_black_48dp)
                    .build(),
                RC_SIGN_IN
            )
        }
        else {
            setContentView(R.layout.activity_main)
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)

            val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
            val navView: NavigationView = findViewById(R.id.nav_view)
            val navController = findNavController(R.id.nav_host_fragment)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_home
                ), drawerLayout
            )
            setupActionBarWithNavController(
                navController,
                appBarConfiguration
            )//To add navigation support to the default action bar
            navView.setupWithNavController(navController)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                Log.d("signIn", user.toString())
                setContentView(R.layout.activity_main)
                val toolbar: Toolbar = findViewById(R.id.toolbar)
                setSupportActionBar(toolbar)
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
                val navView: NavigationView = findViewById(R.id.nav_view)
                val navController = findNavController(R.id.nav_host_fragment)
                appBarConfiguration = AppBarConfiguration(setOf(
                    R.id.nav_home), drawerLayout) // decidiamo le schermate root e connettiamole al drawer
                setupActionBarWithNavController(navController, appBarConfiguration)//To add navigation support to the default action bar
                navView.setupWithNavController(navController)
            } else {
               Log.d("signIn", "Sign in failed")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        //destination means source!
        if(navController.currentDestination!!.id==R.id.nav_item_detail){//arrivo da item detail
            navController.navigate(R.id.action_nav_item_detail_to_nav_home)
            return false
        }

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        //SHARED PREFERENCES

        val sharedPref = getSharedPreferences(
            "it.polito.mad.team19lab2.profile", 0)
        if(sharedPref!=null) {
            val profile = sharedPref.getString("profile", "notFound")
            if (profile != "notFound") {
                val jo = JSONObject(profile)
                nav_view.getHeaderView(0).findViewById<TextView>(R.id.header_title_textView).text = jo.getString("FULL_NAME")
                nav_view.getHeaderView(0).findViewById<TextView>(R.id.header_subtitle_textView).text = jo.get("NICK_NAME") as String
                val file = File(filesDir, "myimage.png")
                if (file.exists()) {
                    nav_view.getHeaderView(0).findViewById<ImageView>(R.id.header_imageView).setImageURI(file.toUri())
                }
            }
        }
    }


    fun setInterestsDropdown(listState: ArrayList<StateVO>) {
        var interests = ""
        for (element in listState) {
            if (element.isSelected) {
                interests += if (interests.isEmpty()) {
                    element.title
                } else {
                    ", ${element.title}"
                }
            }
        }
        findViewById<AutoCompleteTextView>(R.id.interestsDropdown)?.setText(interests, false)
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment)
        //destination means source!
        if(navController.currentDestination!!.id==R.id.nav_item_detail){//arrivo da item detail
            navController.navigate(R.id.action_nav_item_detail_to_nav_home)
        }
        else
            super.onBackPressed()
    }

    fun signOut(view: View) {

        AuthUI.getInstance()
        .signOut(this)
        .addOnCompleteListener {
            val i = baseContext.packageManager
                .getLaunchIntentForPackage(baseContext.packageName)
            i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(i)
        }
    }
}
