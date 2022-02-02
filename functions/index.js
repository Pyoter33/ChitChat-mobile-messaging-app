const functions = require("firebase-functions");

const admin = require("firebase-admin");
admin.initializeApp();

exports.notifyNewMessageGroup = functions.firestore
  .document('groups/{group}/messages/{message}')
  .onCreate((docSnapshot, context) => {
    const message = docSnapshot.data();
    const currentConversationId = message["currentConversationId"];
    const senderId = message["senderId"];
    const content = message["content"];
    const type = message["type"]


    return admin.firestore().doc('users/' + senderId).get().then(userSnapshot => {
      const name = userSnapshot.get("name");

      return admin.firestore().doc('groups/' + currentConversationId).get().then(groupSnapshot => {

        const groupName = groupSnapshot.get("name");

        let payload = {
          notification: {
            title: "New message in group " + groupName,
            body: name + ": " + content,
            sound: "default"
          }
        };

        if (type === "IMAGE") {
          payload = {
            notification: {
              title: "New message in group " + groupName,
              body: name + " sent an image!",
              sound: "default"
            }
          };
        }



        const options = {
          priority: "high",
          timeToLive: 60 * 60 * 24
        };

        return admin.messaging().sendToTopic(currentConversationId, payload, options)
      })
    })
  });



exports.notifyNewMessageConversation = functions.firestore
  .document('conversations/{conversation}/messages/{message}')
  .onCreate((docSnapshot, context) => {
    const message = docSnapshot.data();
    const senderId = message["senderId"];
    const content = message["content"];
    const receiverId = message["receiverId"]
    const type = message["type"]


    return admin.firestore().doc("users/" + receiverId).get().then(snapshot => {
      const registrationTokens = snapshot.get("registrationTokens");

      return admin.firestore().doc('users/' + senderId).get().then(userSnapshot => {
        const userName = userSnapshot.get("name");

        let payload = {
          notification: {
            title: userName + " sent you a message!",
            body: content,
            sound: "default"
          }
        };

        if (type === "IMAGE") {
          payload = {
            notification: {
              title: userName + " sent you a message!",
              body: "Image message",
              sound: "default"
            }
          };
        }

        const options = {
          priority: "high",
          timeToLive: 60 * 60 * 24
        };

        return admin.messaging().sendToDevice(registrationTokens, payload, options).then(response => {
          const stillRegisteredTokens = registrationTokens;

          response.results.forEach((result, index) => {
            const error = result.error;
            if (error) {
              const failedRegistrationToken = registrationTokens[index]
              if (error.code === "messaging/registration-token-not-registered" || error.code === "messaging/invalid-registration-token") {
                const failedIndex = stillRegisteredTokens.indexOf(failedRegistrationToken);
                if (failedIndex > -1) {
                  stillRegisteredTokens.splice(failedIndex, 1)
                }

              }
            }
          })

          return admin.firestore().doc("users/" + id).update({
            registrationTokens: stillRegisteredTokens
          })
        })
      })
    })
  });


  exports.checkToDeleteGroup = functions.firestore
  .document('groups/{group}')
  .onUpdate((docSnapshot, context) => {
    const group = docSnapshot.after.data();
    const membersIds = group["membersIds"];
    
    const path = docSnapshot.path;

    if(membersIds.length == 0){
      return docSnapshot.after.ref.delete();
    }

    return null;
  });

  