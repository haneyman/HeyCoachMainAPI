<%--
  Created by IntelliJ IDEA.
  User: mark
  Date: 11/5/16
  Time: 10:26 AM
  To change this template use File | Settings | File Templates.
--%>

<%--DOCO: https://firechat.firebaseapp.com/docs/--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
        <title>Chat</title>
        <style type="text/css">
        </style>
        <!-- jQuery -->
        <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.0/jquery.min.js"></script>

        <!-- Firebase -->
        <script src="https://www.gstatic.com/firebasejs/3.3.0/firebase.js"></script>

        <!-- Firechat -->
        <link rel="stylesheet" href="https://cdn.firebase.com/libs/firechat/3.0.1/firechat.min.css" />
        <script src="https://cdn.firebase.com/libs/firechat/3.0.1/firechat.min.js"></script>
        <script>
            // Initialize Firebase
            var config = {
                apiKey: "AIzaSyCVX0wytGBXlP0HbVKEqV7A7JtKzC7amNg",
                authDomain: "coachmonsterpoc.firebaseapp.com",
                databaseURL: "https://coachmonsterpoc.firebaseio.com",
                storageBucket: "",
                messagingSenderId: "761893969962"
            };
            firebase.initializeApp(config);
        </script>

        <%--Firebase auth--%>
        <script src="https://www.gstatic.com/firebasejs/ui/live/0.5/firebase-ui-auth.js"></script>
        <link type="text/css" rel="stylesheet" href="https://www.gstatic.com/firebasejs/ui/live/0.5/firebase-ui-auth.css" />

        <script>
            firebase.auth().onAuthStateChanged(function(user) {
                // Once authenticated, instantiate Firechat with the logged in user
                if (user) {
                    //alert("we have a user" + user);
                    initChat(user);
                }
            });

            function initChat(user) {
                // Get a Firebase Database ref
                var chatRef = firebase.database().ref("chat");

                // Create a Firechat instance
                var chat = new FirechatUI(chatRef, document.getElementById("firechat-wrapper"));

                // Set the Firechat user
                chat.setUser(user.uid, user.displayName);
                $('#profile').html(user);
            }

        </script>
    </head>
    <body>
        <h2><a href="welcome">CoachMonster API - Chat</a></h2>
        <div id="profile"></div>
        <p>Chat POC V0.2 - Select "Coaches" chat room and chat away.</p>
        <div style="padding: 0 10% 10% 10%" id="firechat-wrapper"></div>
    </body>
</html>
