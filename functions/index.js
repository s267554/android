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
      //const id = newValue.id

      // perform desired operations ...
      console.log('Someone got interest for', fullname, context.params.itemId);

      return db.collection('items').doc(context.params.itemId).get().then(item => {
          const owner = item.data().userId
          const title = item.data().title
          console.log('ownerId is', owner);
          return db.collection('utenti').doc(owner).get().then(doc => {
            const token = doc.data().notificationToken
            console.log('token is', token);
            // Notification details.
            const payload = {
              notification: {
                title: 'Your item got some love!',
                body: `${fullname} has expressed interest in ${title}`
              },
              token: token
            };
            // Send notifications to all tokens.
            admin.messaging().send(payload).then((response) => {
                console.log("Message sent successfully:", response);
                return response;
            })
            .catch((error) => {
                console.log("Error sending message: ", error, payload);
            });
          });
      });
    });

exports.sendFollowerNotification = functions.firestore.document('items/{itemId}')
    .onUpdate((change, context) => {
        const newState = change.after.data().state
        const oldState = change.before.data().state
        const title = change.after.data().title

        if(newState == oldState){return;}

        const promises = []
        const tokens = []

        return db.collection('items').doc(context.params.itemId).collection('users').get().then( async (col) => {
            col.forEach(async (item) => {
                promises.push(
                    db.collection('utenti').doc(item.data().id).get().then( async (utente) => {
                          return tokens.push(utente.data().notificationToken);
                      })
                    );
            })
            await Promise.all(promises);
            console.log("Tokens:", tokens);
            const payload = {
              notification: {
                title: 'Update on an item you were following',
                body: `${title} is now ${newState}!`
              }
            };
            return admin.messaging().sendToDevice(tokens, payload).then((response) => {
                console.log("Message sent successfully:", response);
                return response;
            })
            .catch((error) => {
                console.log("Error sending message: ", error, payload);
            });
        });
    });

