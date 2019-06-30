<html>
    <head>
        <title>Login</title>
        <style type="text/css">
        </style>
        <!-- jQuery -->
        <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.1.0/jquery.min.js"></script>

        <!-- Firebase -->
        <script src="https://www.gstatic.com/firebasejs/3.3.0/firebase.js"></script>
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

        <!-- Firechat -->
        <link rel="stylesheet" href="https://cdn.firebase.com/libs/firechat/3.0.1/firechat.min.css" />
        <script src="https://cdn.firebase.com/libs/firechat/3.0.1/firechat.min.js"></script>

        <%--Firebase auth--%>
        <script src="https://www.gstatic.com/firebasejs/ui/live/0.5/firebase-ui-auth.js"></script>
        <link type="text/css" rel="stylesheet" href="https://www.gstatic.com/firebasejs/ui/live/0.5/firebase-ui-auth.css" />
        <script type="text/javascript">
            // FirebaseUI config.
            var uiConfig = {
                'signInSuccessUrl': "welcome",
                'signInOptions': [
                    // Leave the lines as is for the providers you want to offer your users.
                    firebase.auth.GoogleAuthProvider.PROVIDER_ID,
                    firebase.auth.FacebookAuthProvider.PROVIDER_ID,
                    firebase.auth.TwitterAuthProvider.PROVIDER_ID,
                    firebase.auth.GithubAuthProvider.PROVIDER_ID,
                    firebase.auth.EmailAuthProvider.PROVIDER_ID
                ],
                // Terms of service url.
                'tosUrl': '@RequestMapping("/tos")',
            };

            // Initialize the FirebaseUI Widget using Firebase.
            var ui = new firebaseui.auth.AuthUI(firebase.auth());
            // The start method will wait until the DOM is loaded.
            ui.start('#firebaseui-auth-container', uiConfig);

            firebase.auth().onAuthStateChanged(function(user) {
                // Once authenticated, instantiate Firechat with the logged in user
                if (user) {
                    $('#divLoggedIn').html("Hello " + user.displayName + " you are logged in to CoachMonsterPOC.");
                    $('#divLoggedIn').show();
                    $('#divNotLoggedIn').hide();
                }
            });

            function logout() {

            }
        </script>


    </head>

    <body>
    <h2><a href="welcome">CoachMonster API - Login</a></h2>
        <%--${message}--%>
        <div id="divLoggedIn">
            <div id="sign-in-status"></div>
            <div id="sign-in"></div>
            <div id="account-details"></div>
        </div>
        <div id="divNotLoggedIn">
            Press one of the below buttons to log in.  For now, only Google is working.
        </div>
        <hr>
        <div id="firebaseui-auth-container"></div>
    </body>
</html>
