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

exports.notifyOwnerInterest = functions.firestore
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
          // Notification details.
          let payload = {
              "data": {
                "op": "notifyOwnerInterest",
                "fullname": `${fullname}`,
                "item": `${title}`
              }
          };
          return db.collection('utenti').doc(owner).get().then(async (doc) => {
            const tokens = doc.data().notificationTokens
            console.log('tokens are', tokens);
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

exports.notifyOwnerBuy = functions.firestore
    .document('items/{itemId}/buyers/{pushId}')
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
          // Notification details.
          let payload = {
              "data": {
                "op": "notifyOwnerBuy",
                "fullname": `${fullname}`,
                "item": `${title}`
              }
          };
          return db.collection('utenti').doc(owner).get().then(async (doc) => {
            const tokens = doc.data().notificationTokens
            console.log('tokens are', tokens);
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
    .onUpdate(async (change, context) => {
        const newState = change.after.data().state
        const oldState = change.before.data().state
        const title = change.after.data().title

        const buyer = change.after.data().buyerId

        if(newState == oldState){
            return;
        }

        // Notification details.
        let payload = {
            "data": {
              "op": "sendFollowerNotification",
              "item": `${title}`,
              "state": `${newState}`
            }
        };

        let payload1 = {
            "data": {
              "op": "onSuccessfulBuy",
              "item": `${title}`
            }
        };

        let interested = []
        let buyers = []

        await db.collection('items').doc(context.params.itemId).collection('users').get().then( async (col) => {
            col.forEach(async (user) => {
                console.log(user.data().id, 'added to interested')
                interested.push(user.data().id)
            })
        })
        await db.collection('items').doc(context.params.itemId).collection('buyers').get().then( async (col) => {
            col.forEach(async (user) => {
                console.log(user.data().id, 'added to buyers')
                buyers.push(user.data().id)
            })
        })

        console.log('interested are ', interested)
        console.log('buyers are', buyers)

        let buyersOnly = buyers.filter(x => !interested.includes(x))
        let union = buyersOnly.concat(interested)
        console.log('union is ', union)

        return union.forEach( async (follower) => {
            return db.collection('utenti').doc(follower).get().then(async (doc) => {
                const tokens = doc.data().notificationTokens
                console.log('tokens are', tokens);

                // Send notifications to all tokens.
                let message = payload;
                if (follower == buyer && newState == 2) message = payload1
                const response = await admin.messaging().sendToDevice(tokens, message);
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

