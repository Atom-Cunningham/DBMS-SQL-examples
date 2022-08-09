/**Adam Cunningham
Final project
csc337 web programming
connect 4
summer 2020
 * 
  handles server request for the page where players play the game
  sends and recieves moves and messages,
  updating both periodically.
  when the game is over, it makes the game board unclickable
  and provides the user with a button to return to the lobby
 */


//this should be set to non-0 on page load via update()
gameID = 0;     //used to avoid the hassle of nested lookups on the server
gameOver = false;   //changes when game is over, used in update

/**update
 * 
 * gets the state of the board from the server
 * and calls "build board" which creates the dom
 * object board state
 */
function update(){
    //create request
    var httpRequest = new XMLHttpRequest();
    if (!httpRequest) { return false; }

    //define ready state behavior
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {
                if(httpRequest.responseText == "TIMEOUT"){
                    window.location.href = "index.html";
                }else{
                    game = JSON.parse(httpRequest.responseText);
                    setVSmsg(game.player1,game.player2);
                    gameID = game._id;
                    var table = document.getElementById("board");
                    table.innerHTML = createTable(game.board);
                    checkWinner();
                    //var game = JSON.parse(httpRequest.responseText);
                    //gui.innerHTML = game.player1 + game.player2;
                }
            } else { alert('Response failure');}
        }
    }
    let url = '/getBoard/';
    httpRequest.open('GET', url, true);
    httpRequest.send();
}

/**move
 * 
 * sends a post to server letting it know that the board has been clicked,
 * and asking it to change the game state
 * 
 * @param {*} col a string refering to the col where the piece was dropped
 */
function move(col){
    //create request
    if (!gameOver){
        var httpRequest = new XMLHttpRequest();
        if (!httpRequest) { return false; }
    
        httpRequest.onreadystatechange = () => {
            if (httpRequest.readyState === XMLHttpRequest.DONE) {
                if (httpRequest.status === 200) {
                    update();
                    var header = document.getElementById("header");
                } else { alert('Response failure');}
            }
        }
        let url = `/move/${gameID}/${col}`;
        httpRequest.open('POST', url, true);
        httpRequest.send("col =" + col);
    }
}


/**create table
 * 
 * writes html to reflect the current state of the board
 * recieved from server
 * @param Array a 7 col 6 row array
 * stored in col major order
 */
function createTable(board){
    console.log("creating table: "+board.length);
    var table = "";
    for (c in board){
        var col = board[c].split("").reverse();
        //create a function with the col as parameter
        var func = `onclick='move("${c}");'`;
        table += "<tr ";
        //unclickable after win,lose or draw
        if (!gameOver){
            table+= func;
        }
        table += ">";
        for (r in board[c]){
            var color = "white";
            if(col[r]=="1"){
                color = "red";
            }else if(col[r]=="2"){
                color = "yellow";
            }
            table+=`<td style="background-color:${color}"></td>`;
        }
        table += "</tr>"
    }
    return table;
}

/**check winner
 * 
 * sends a request to the server to see
 * if this client has won.
 */
function checkWinner(){
    //create request
    var httpRequest = new XMLHttpRequest();
    if (!httpRequest) { return false; }

    //define ready state behavior
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {
                var winStatus = httpRequest.responseText;
                if (winStatus != "OK"){
                    gameOver = true;
                    setWinLossText(winStatus);
                    clearInterval(interval);
                }
                //TODO tell the user what happened
            } else { alert('Response failure');}
        }
    }
    let url = '/getWinner/' + gameID;
    httpRequest.open('GET', url, true);
    httpRequest.send();
}

/**wet win loss text
 * 
 * displays a message letting the user
 * know if the won or lost,
 * and allows the player to return to the lobby
 * 
 */
function setWinLossText(status){
    var text = "";
    var msg = document.getElementById("gameOver");
    if (status == "WIN"){
        text += "You won! Congradulations!";
    } else if(status == "LOSE") {
        text += "You lost, Better luck next time!";
    } else if(status == "DRAW"){
        text += "Tied game, good effort!";
    }
    text += "<br>";
    text += `<button id="send" onclick="goToLobby();">
    return to lobby
    </button>`;
    msg.innerHTML = text;
}

function goToLobby(){
    window.location.href = "home.html";
}

/**set vs message
 * sets a message player1 vs player2
 * and makes it clear which player is which color
 * 
 * @param {*} player1 string, the red player
 * @param {*} player2 string, the yellow player
 */
function setVSmsg(player1, player2){
    var vsMsg = document.getElementById("player1").innerHTML = player1;
    var vsMsg = document.getElementById("player2").innerHTML = player2;
}

/*
sendMessage
param:none
return:none
sends a post request (JSON type) to the server
*/
function sendMessage() {
    //create a new httpRequest
    var httpRequest = new XMLHttpRequest();
    if (!httpRequest) { return false; }

    //define ready state behavior
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {
                updateChat();
            } else { alert('Response failure'); }
        }
    }
    let inputField = document.getElementById("messageInput");
    let input = inputField.value;
    var msg = {message: input, gameID: gameID}

    let url = "/sendMessage/";
    httpRequest.open('POST', url, true);
    httpRequest.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    httpRequest.send("message=" + JSON.stringify(msg));

    //clear input field
    inputField.value = "";
}

function updateChat(){
        //create a new httpRequest
        var httpRequest = new XMLHttpRequest();
        if (!httpRequest) { return false; }
    
        //define ready state behavior
        httpRequest.onreadystatechange = () => {
            if (httpRequest.readyState === XMLHttpRequest.DONE) {
                if (httpRequest.status === 200) {
                    if(httpRequest.responseText == "TIMEOUT"){
                        window.location.href = "index.html";
                    }else{
                        var chat = document.getElementById("chat");
                        var results = JSON.parse(httpRequest.responseText);
                        var text = "";
                        for (msg in results){
                            var msgObject = results[msg];
                            text+= msgObject.username;
                            text+= ": ";
                            text+= msgObject.message;
                            text += "<br>";
                        }
                        chat.innerHTML = text;
                    }
                } else { alert('Response failure'); }
            }
        }

        let url = "/getChat/" + gameID;
        httpRequest.open('GET', url, true);
        httpRequest.send();
}

update();
updateChat();
interval = setInterval(update, 1000);
setInterval(updateChat, 3000);
