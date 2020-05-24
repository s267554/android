package it.polito.mad.team19lab2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import it.polito.mad.team19lab2.repository.UserRepository
import it.polito.mad.team19lab2.ui.StateVO
import it.polito.mad.team19lab2.viewModel.UserViewModel
import kotlinx.android.synthetic.main.activity_main.*

private const val RC_SIGN_IN=123

class MainActivity : AppCompatActivity() {

    private val TAG = "MAIN_ACTIVITY"

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var storage: FirebaseStorage
    private val userVm: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = Firebase.storage
        val u=FirebaseAuth.getInstance().currentUser
        Log.d("xxxx", u.toString())
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
            userVm.getOrCreateUser().observe(this, Observer{ item ->
                if(item != null) {
                    nav_view.getHeaderView(0).findViewById<TextView>(R.id.header_title_textView).text = item.fullname
                    nav_view.getHeaderView(0).findViewById<TextView>(R.id.header_subtitle_textView).text = item.nickname
                    if (item.imagePath.isNotEmpty()) {
                        val storageRef = storage.reference
                        storageRef.child(item.imagePath).downloadUrl.addOnSuccessListener {
                            Picasso.get().load(it).noFade()
                                .placeholder(R.drawable.progress_animation)
                                .into(
                                    nav_view.getHeaderView(0).findViewById(R.id.header_imageView),
                                    object : com.squareup.picasso.Callback {
                                        override fun onSuccess() {
                                        }

                                        override fun onError(e: java.lang.Exception?) {
                                        }
                                    })
                        }.addOnFailureListener {
                            Log.e("IMAGE", "Error in download image")
                        }
                    } else {
                        nav_view.getHeaderView(0).findViewById<ImageView>(R.id.header_imageView)
                            .setImageBitmap(null)
                    }
                }
                else{
                    signOut(view = View(applicationContext))
                }
            })
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }
                    // Get new Instance ID token
                    val token = task.result?.token

                    val repo = UserRepository()
                    if(repo.user != null) {
                        Log.d(TAG,"firstRegistrationToken($token)")
                        repo.getProfile().update("notificationTokens", FieldValue.arrayUnion(token))
                            .addOnSuccessListener { Log.d(TAG, "firstRegistrationToken succeeded") }
                            .addOnFailureListener { Log.d(TAG, "firstRegistrationToken failed") }
                    }
                })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            Log.d("xxxx", resultCode.toString())
            IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Log.d("xxxx", "result ok")
                setContentView(R.layout.activity_main)
                val toolbar: Toolbar = findViewById(R.id.toolbar)
                setSupportActionBar(toolbar)
                val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
                val navView: NavigationView = findViewById(R.id.nav_view)
                val navController = findNavController(R.id.nav_host_fragment)
                appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home), drawerLayout)
                setupActionBarWithNavController(navController, appBarConfiguration)
                navView.setupWithNavController(navController)
                userVm.getOrCreateUser().observe(this, Observer { item ->
                    if(item != null) {
                        nav_view.getHeaderView(0).findViewById<TextView>(R.id.header_title_textView).text = item.fullname
                        nav_view.getHeaderView(0).findViewById<TextView>(R.id.header_subtitle_textView).text = item.nickname
                        val storageRef = storage.reference
                        if (item.imagePath.isNotEmpty()) {
                            storageRef.child(item.imagePath).downloadUrl.addOnSuccessListener {
                                Picasso.get().load(it).noFade()
                                    .placeholder(R.drawable.progress_animation)
                                    .into(
                                        nav_view.getHeaderView(0)
                                            .findViewById(R.id.header_imageView),
                                        object : com.squareup.picasso.Callback {
                                            override fun onSuccess() {
                                            }

                                            override fun onError(e: java.lang.Exception?) {
                                            }
                                        })
                            }.addOnFailureListener {
                                Log.d("image", "error in download image")
                            }
                        }
                    }
                })
                FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w(TAG, "getInstanceId failed", task.exception)
                            return@OnCompleteListener
                        }
                        // Get new Instance ID token
                        val token = task.result?.token

                        val repo = UserRepository()
                        if(repo.user != null) {
                            Log.d(TAG,"firstRegistrationToken($token)")
                            repo.getProfile().update("notificationTokens", FieldValue.arrayUnion(token))
                                .addOnSuccessListener { Log.d(TAG, "firstRegistrationToken succeeded") }
                                .addOnFailureListener { Log.d(TAG, "firstRegistrationToken failed") }
                        }
                    })

            } else {
               Log.e("signIn", "Sign in failed")
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
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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
        findViewById<AutoCompleteTextView>(R.id.interestsDropdown).setText(interests, false)
    }

    override fun onBackPressed() {

            val navController = findNavController(R.id.nav_host_fragment)
            //destination means source!

        when(navController.currentDestination?.id){
                R.id.nav_home -> {
                    val x = findViewById<DrawerLayout>(R.id.drawer_layout)
                    if(x.isDrawerOpen(GravityCompat.START))
                        x.closeDrawers()
                    else
                        finish()
                }
                else ->
                    super.onBackPressed()
             }
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
