# ChitChat-mobile-messaging-app
ChitChat is an online messaging app for mobile phones with Android OS. It is fully written in Kotlin and is using Google Firebase functions such as Firestore, Authentication, Cloud Functions and Cloud Messaging.

## Logging:
Upon running the app, user is presented with two choices. He can either use his google or facebook account for creating user's profile in the app. After choosing one of the options a profile is created with Firebase Authentication and more detailed user description appears in the database.
It consists of unique in app id, username and photo url both taken from linked google or facebook acount and also a unique registration token of the device. 

<img src=https://i.imgur.com/RErQN9o.png width=200/>


## Main menu:
User's menu is meant to display current conversations and groups that user is part of. At the bottom there are two tabs which change the display from conversations to groups and the other way around. 
From the menu user can go directly to the conversation, create new group or conversation and see his in app profile.

<img src=https://i.imgur.com/bptvzxh.png width=200/> <img src=https://i.imgur.com/CQ1fd7n.png width=200/>


## Managing profile:
Here the user can change his nickname used in the app. Other users will immediately see the change if it occurs. The user can also log out from the app and delete his account completely. The first option will only redirect to the authentication screen and force user to once agin choose desired logging option.
Deleting account means deleting all of the data connected to the user in the database and also removing his linked google or facebook account.

<img src=https://i.imgur.com/vgHBpqe.png width=200/>

## Creating new groups and conversations:
To create new conversations user has to provide a full nickname and than click add on the element in the list. If these conversation already exists the user will see a proper message.
Creating groups works similar. The user who is creating the group can set its name and add members.

<img src=https://i.imgur.com/t80ynHR.png width=200/> <img src=https://i.imgur.com/sTRqMmA.png width=200/>


## Sending messages:
In ChitChat users can send both text and image messages. Image can be chosen from phone gallery or taken using the in app camera. Every user can delete their messages. After deletion the message will look differentely for other users.
The user can see if their last message was read by other users. This is indicated by the small circles with other users' profile images.

<img src=https://i.imgur.com/5ncDdrm.png width=200/> <img src=https://i.imgur.com/kH3P1wg.png width=200/>   <img src=https://i.imgur.com/F3g756M.png width=200/>


## Notifications:
If the user is not in the app and he will receive a message, a suitable notification will appear, stating the sender, the content and the name of the group if needed. 

<img src=https://i.imgur.com/9QXTqne.png width=200/> 

## Managing groups:
Every user can leave the group. His messages will still remain in the group but he won't be able to see the group. Every group member can add new user to the group using similar search method as in the conversation creation.

<img src=https://i.imgur.com/Iucwh8g.png width=200/> 
