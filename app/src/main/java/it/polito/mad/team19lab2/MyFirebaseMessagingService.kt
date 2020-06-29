package it.polito.mad.team19lab2


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.firestore.FieldValue
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.polito.mad.team19lab2.repository.UserRepository

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

            var title = ""
            var body = ""

            when (remoteMessage.data["op"]) {
                "notifyOwnerInterest" -> {
                    title = resources.getString(R.string.notifyOwnerInterest)
                    body = "${remoteMessage.data["fullname"]} " +
                            "${resources.getString(R.string.notifyOwnerInterestText)} " +
                            "${remoteMessage.data["item"]}"
                }
                "notifyOwnerBuy" -> {
                    title = resources.getString(R.string.notifyOwnerBuy)
                    body = "${remoteMessage.data["fullname"]} " +
                            "${resources.getString(R.string.notifyOwnerBuyText)} " +
                            "${remoteMessage.data["item"]}"
                }
                "sendFollowerNotification" -> {
                    if (remoteMessage.data.containsKey("state")) {
                        title = resources.getString(R.string.sendFollowerNotification)
                        body = "${remoteMessage.data["item"]} " +
                                "${resources.getString(R.string.sendFollowerNotificationText)} " +
                                resources.getStringArray(R.array.item_state)[remoteMessage.data["state"]!!.toInt()]
                    }
                }
                "onSuccessfulBuy"-> {
                    title = resources.getString(R.string.onSuccessfulBuy)
                    body = "${remoteMessage.data["item"]} " +
                            resources.getString(R.string.onSuccessfulBuyText)
                }
            }

            val itemId: String = remoteMessage.data["item_id"].toString()

            if ( title.isNotBlank() && body.isNotBlank() && itemId.isNotBlank())
                sendNotification(body, title, itemId)

        }


        // Check if message contains a notification payload.
        /*remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            it.body?.run { sendNotification(this, title) }
        }*/

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        val repo = UserRepository()
        if(repo.user != null) {
            Log.d(TAG, "sendRegistrationTokenToServer($token)")
            repo.getProfile().update("notificationTokens", FieldValue.arrayUnion(token))
                .addOnSuccessListener { Log.d(TAG, "sendRegistrationTokenToServer succeeded") }
                .addOnFailureListener { Log.d(TAG, "sendRegistrationTokenToServer failed") }
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(
        messageBody: String,
        title: String,
        itemId: String
    ) {
        val myPendingIntent = NavDeepLinkBuilder(applicationContext)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.nav_item_detail)
            .setArguments(bundleOf("item_id1" to itemId))
            .createPendingIntent()

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_shop_black_48dp)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(myPendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "MADketPlace notifications channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Notification's id is generated form body message.
        notificationManager.notify(messageBody.hashCode(), notificationBuilder.build())
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"
    }
}