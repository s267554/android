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
          return db.collection('utenti').doc(owner).get().then(async (doc) => {
            const tokens = doc.data().notificationTokens
            console.log('tokens are', tokens);
            // Notification details.
            const payload = {
              notification: {
                title: 'Your item got some love!',
                body: `${fullname} has expressed interest in ${title}`
              }
            };
            // Send notifications to all tokens.
            const response = await admin.messaging().sendToDevice(tokens, payload);
            // For each message check if there was an error.
            const tokensToRemove = [];
            response.results.forEach((result, index) => {
              const error = result.error;
              if (error) {
                console.error('Failure sending notification to', tokens[index], error);
                // Cleanup the tokens who are not registered anymore.
                if (error.code === 'messaging/invalid-registration-token' ||
                    error.code === 'messaging/registration-token-not-registered') {
                  tokensToRemove.push(tokens[index]);
                }
              }
            });
            await Promise.all(tokensToRemove);
            console.log('tokens to remove are ', tokensToRemove);
            let difference = tokens.filter(x => !tokensToRemove.includes(x));
            console.log('tokens are now', difference);
            return db.collection('utenti').doc(owner).update({
              notificationTokens: difference
            });
          });
      });
    });

exports.sendFollowerNotification = functions.firestore.document('items/{itemId}')
    .onUpdate((change, context) => {
        const newState = change.after.data().state
        const oldState = change.before.data().state
        const title = change.after.data().title

        if(newState == oldState){
            return;
        }

        return db.collection('items').doc(context.params.itemId).collection('users').get().then( async (col) => {
            col.forEach(async (user) => {
                  const follower = user.data().id
                  return db.collection('utenti').doc(follower).get().then(async (doc) => {
                    const tokens = doc.data().notificationTokens
                    console.log('tokens are', tokens);
                    // Notification details.
                    const payload = {
                      notification: {
                        title: 'Update on your loved item!',
                        body: `${title} is now ${newState}`
                      }
                    };
                    // Send notifications to all tokens.
                    const response = await admin.messaging().sendToDevice(tokens, payload);
                    // For each message check if there was an error.
                    const tokensToRemove = [];
                    response.results.forEach((result, index) => {
                      const error = result.error;
                      if (error) {
                        console.error('Failure sending notification to', tokens[index], error);
                        // Cleanup the tokens who are not registered anymore.
                        if (error.code === 'messaging/invalid-registration-token' ||
                            error.code === 'messaging/registration-token-not-registered') {
                          tokensToRemove.push(tokens[index]);
                        }
                      }
                    });
                    await Promise.all(tokensToRemove);
                    console.log('tokens to remove are ', tokensToRemove);
                    let difference = tokens.filter(x => !tokensToRemove.includes(x));
                    console.log('tokens are now', difference);
                    return db.collection('utenti').doc(follower).update({
                      notificationTokens: difference
                    });
                  });
            })

        });
    });

