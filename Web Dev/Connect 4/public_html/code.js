/*
login
param,ret, none
creates login credentials with the login field
makes a post request
gets a response from the server and displays it as an alert;
*/
function login(){
    //set up user type
    var name = $('#usernameLogin').val();
    var pswd = $('#passwordLogin').val();
    //create a new httpRequest
    var httpRequest = new XMLHttpRequest();
    if (!httpRequest) { return false; }

    //define ready state behavior
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {
                if(httpRequest.responseText == "OK"){
                    window.location.href = "home.html";
                }else{
                    alert(httpRequest.responseText);
                }
            } else { alert('Response failure'); }
        }
    }

    //check if valid input
    if (name.length == 0 || pswd.length == 0){
        alert("Invalid User");
    }else{
        let loginCreds = {username: name,
                          password: pswd}
        //send a post request to add user
        let url = "/login/";
        httpRequest.open('POST', url, true);
        httpRequest.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
        httpRequest.send('user=' + JSON.stringify(loginCreds));
        console.log('user=' + JSON.stringify(loginCreds));
    }
}

/*
adduser
param: none
return: none
collects the fields from the html
and compiles them into an object
sends an ajax request to add a new user
to the server to add a new user in the system
*/
function addUser(){
    //create a new httpRequest
    var httpRequest = new XMLHttpRequest();
    if (!httpRequest) { return false; }

    //define ready state behavior
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {
                if(httpRequest.responseText == "EXISTS"){
                    alert("Username already taken");
                }if(httpRequest.responseText == "OK"){
                    alert("account created");
                }
            } else { alert('Response failure'); }
        }
    }

    //set up user type
    let name = $('#username').val();
    let pswd = $('#password').val();
    if (name.length == 0 || pswd.length == 0){
        alert("please enter something");
    }else{
        let user = {username: name,
                    password: pswd,}
        //send a post request to add user
        let url = "/add/user/";
        httpRequest.open('POST', url, true);
        httpRequest.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
        httpRequest.send('user=' + JSON.stringify(user));
    }
}