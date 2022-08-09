/*
requests the server for the user name of the
current user, based on local cookies
sets the welcome message based on the username
*/
username = "";
function setWelcomeMessage(){
    var httpRequest = new XMLHttpRequest();
    if (!httpRequest) { return false; }

    //define ready state behavior
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {
                name = httpRequest.responseText;
                username = name;
                console.log(name);
                welcome = document.getElementById("welcome");
                msg = "Welcome ";
                msg+= name;
                msg+= ", what would you like to do?";
                welcome.innerHTML = msg;
            } else { alert('Response failure'); }
        }
    }
    let url = "/username/";
    httpRequest.open('GET', url, true);
    httpRequest.send();
}

/**create game
 * <p>
 * creates a table entry, where a user can join
 * a game that has not yet begun. Players can create
 * a table entry to wait for another user to click, and
 * join the game.
 * @param none;
 * @return none;
 */
function createGame(){
    var httpRequest = new XMLHttpRequest();
    if (!httpRequest) { return false; }
    //define ready state behavior
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {
                updateTable();
            } else { alert('Response failure'); }
        }
    }
    let url = '/createGame/';
    httpRequest.open('POST', url, true);
    httpRequest.send("");
}

/**update table
 * <p>
 * 
 * creates an HTTPrequest,
 * and if valid, returns all the valid games in the
 * database
 */
 //TODO sort by timestamp
function updateTable(){
    var httpRequest = new XMLHttpRequest();
    if (!httpRequest) { return false; }
    //define ready state behavior
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {
                if(httpRequest.responseText == "TIMEOUT"){
                    window.location.href = "index.html";
                }else{
                    var inner = "";
                    var games = JSON.parse(httpRequest.responseText);
                    //construct html string to populate table
                    for(i in games.reverse()){
                        inner += createGameTableEntry(games[i].player1, games[i]._id);
                    }
                    var table = document.getElementById("gameTable");
                    table.innerHTML = inner;
                    console.log(games);
                    //when constructing, use the gameID as a parameter for createGameTableEntry
                }
            } else {  window.location.href = "index.html";  }
        }
    }
    let url = '/getLobby/';
    httpRequest.open('GET', url, true);
    httpRequest.send();
}

/**create game table entry
 * <p>
 * creates a table entry other players can click on
 * to join a game
 * 
 * @param {*} username the name of the user creating the game
 * @return String, html to construct a table row
 */
//TODO pass gameID to use as parameter for joinGame
function createGameTableEntry(username, gameID){
    var row = "<tr>";
    //a label for the game
    row += "<td>" + username +" ";
    //allows the server to respond with the game that spawned this button
    func = "\"joinGame(\'" + gameID + "\');\"";
    row += "<button class=\"joinButton\" onclick=" + func + ">";
    row += "Join";
    row += "</button></td>"
    row += "</tr>";
    return row;
}

/**join game
 * 
 * takes the gameID of a game object stored in the database
 * adds the client as one of the two players, and
 * redirects to the gameplay page 
 */
function joinGame(gameID){
    var httpRequest = new XMLHttpRequest();
    if (!httpRequest) { return false; }
    //define ready state behavior
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {
                var res = httpRequest.responseText;
                if(res =="OK"){
                    redirect();
                }
            } else { alert('Response failure'); }
        }
    }
    let url = '/join/' + gameID;
    httpRequest.open('POST', url, true);
    httpRequest.send();
}

/**redirect
 * 
 * pings server to see if a match is ready,
 * when the game connected to the client has
 * two members, and the game is valid, redirects to the
 * game page where players can play each other.
 * 
 */
function redirect(){
    var httpRequest = new XMLHttpRequest();
    if (!httpRequest) { return false; }
    //define ready state behavior
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {
                if(httpRequest.responseText == "TIMEOUT"){
                    window.location.href = "index.html";
                }else{
                    console.log(httpRequest.responseText);
                    if (httpRequest.responseText == "READY"){
                        window.location.href = "game.html";
                    }
                }
            } else { window.location.href = "index.html"; }
        }
    }
    let url = '/redirect/';
    httpRequest.open('GET', url, true);
    httpRequest.send();
}

/*updates the page once every 5 seconds.
*/
function update(){
    updateTable();
}

setInterval(update, 5000);
setInterval(redirect, 1000);

updateTable();
//setWelcomeMessage();