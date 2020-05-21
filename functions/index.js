// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

exports.notifyOwner = functions.firestore
    .document('items/{itemId}/users/{pushId}')
    .onCreate((snap, context) => {
      // Get an object representing the document
      // e.g. {'name': 'Marie', 'age': 66}
      const newValue = snap.data();

      // access a particular field as you would any JS property
      const fullname = newValue.fullname;
      const nickname = newValue.nickname
      const id = newValue.id

      // perform desired operations ...
      console.log('Someone got interest for', fullname, context.params.itemId);
      return db.doc('utenti/$id').get().then(doc => {
        const token = doc.data().notificationToken
        console.log('token is', token);
        // Notification details.
        const payload = {
          notification: {
            title: 'Someone got interested!',
            body: `$fullname has expressed interest`
          }
        };
        // Send notifications to all tokens.
        return admin.messaging().sendToDevice(token, payload);
      });
    });
