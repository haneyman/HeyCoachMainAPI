<html>
    <head>
        <meta charset="UTF-8">
        <title>CM API - Welcome</title>

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


        <script type="text/javascript">
            initApp = function() {
                console.log("initApp start...");
                firebase.auth().onAuthStateChanged(function(user) {
                    if (user) {
                        console.log("user is signed in:" + user.displayName);
                        // User is signed in.
                        var displayName = user.displayName;
                        var email = user.email;
                        var emailVerified = user.emailVerified;
                        var photoURL = user.photoURL;
                        var uid = user.uid;
                        var providerData = user.providerData;
                        user.getToken().then(function(accessToken) {
                            $('#divSignedInMessage').html('Hello ' + displayName + ' you are Signed in');
                            $('#divSignedIn').show();
                            $('#divSignedOut').hide();

                            //document.getElementById('sign-in').textContent = 'Sign out';
                            $('#accountDetails').text(JSON.stringify({
                                displayName: displayName,
                                email: email,
                                emailVerified: emailVerified,
                                photoURL: photoURL,
                                uid: uid,
                                accessToken: accessToken,
                                providerData: providerData
                            }, null, '  '));
                        });
                    } else {
                        console.log("user is signed out");
                        // User is signed out.
                        $('#divSignedIn').hide();
                        $('#divSignedOut').show();

/*
                        document.getElementById('sign-in-status').textContent = 'Signed out';
                        document.getElementById('sign-in').textContent = 'Sign in';
                        document.getElementById('account-details').textContent = 'null';
*/
                    }
                }, function(error) {
                    console.log(error);
                });
                console.log("initApp end...");
            };

            window.addEventListener('load', function() {
                initApp()
            });

            function logout() {
                firebase.auth().signOut().then(function() {
                    console.log('Signed Out');
                }, function(error) {
                    console.error('Sign Out Error', error);
                });
            }
        </script>

    </head>
    <body>
    <h1>Welcome to the CoachMonsterPOC API</h1>
    <div id="divSignedIn">
        <div id="divSignedInMessage">
        </div>
        <div style="margin-top: 20px;zborder-style: solid;zborder-width: 1px;">
            <div style=" float:left;"><a href="#" onclick="logout(); return false;">Log Off</a></div>
            <div style="margin-left: 20px; float:left;"><a href="chat">Chat</a></div>
        </div>
        <div style="margin-top:100px;" id="accountDetails"></div>
    </div>

    <div id="divSignedOut">
        <div id="divSignedOutMessage">
        </div>
        <div><a href="login">Log In</a></div>
    </div>

<%--
    <div id="sign-in"></div>
    <div id="account-details"></div>
    <a href="login">Login</a>
--%>
</body>
</html>