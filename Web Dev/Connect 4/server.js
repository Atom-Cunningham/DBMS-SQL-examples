/*
Adam Cunningham
Final project
csc337 web programming
connect 4
summer 2020

this is a simple server/database
which holds user information including passwords 0.o
as well as item information which may be put up for sale
by a user.
Handles logic for creating games, joining other user's games,
and the game logic itself
The game is connect4
it uses express for node, mongoose for mongodb
and jquery for ajax
*/

//imported/linked modules
const mongoose = require('mongoose')
const express = require('express')
const parser = require('body-parser');
const cookieParser = require('cookie-parser');
const crypto = require('crypto');
const { Http2ServerRequest } = require('http2');
const { request } = require('http');

const app = express();
app.use(cookieParser());
app.use(parser.urlencoded({ extended: true }));
app.use(parser.json());

//set up default mongoose connection
const db = mongoose.connection;
const mongoDBURL = 'mongodb://127.0.0.1/auto';
mongoose.connect(mongoDBURL, { useNewUrlParser: true });

var sessionKeys = {};
const SessionTime = 60*60*1000 //1 hour
const cryptIterations = 1000;

//clear database on server startup
//TODO comment this out in final sub

db.on('open', function(){
    db.dropDatabase(function(err, result){;});
    }
);




//define schema type as mongoose.schema
var Schema = mongoose.Schema;

setInterval(updateSessions, 5000);

/*
UserSchema
contains a username, password
*/
var UserSchema = new Schema(
    // User
    { username: String,
      salt: String,
      hash: String,
      currGame: String} //reference to the _id of the current game
);
var User = mongoose.model('User', UserSchema);

/*
GameSchema
keeps track of the two players playing the game
valid is true if players can join the game from the lobby
board is a list of strings representing columns
turn is the player whos turn it is
*/
var GameSchema = new Schema(
    {player1: String,   //username
     player2: String,   //username
     valid: Boolean,    //game is waiting or being played
     board: Array,
     turn: String}
);
var Game = mongoose.model('Game', GameSchema);

/*
ChatSchema
tracks the user that made the message,
the message itself,
and the game id that this chat its attached to
*/
var ChatSchema = new Schema(
    {username: String,
     message: String,
     gameID: String}
);
var Chat = mongoose.model('Chat', ChatSchema);


//define the location of the html,css,js
app.use(express.static('public_html'));

db.on('error', console.error.bind(console, 'MongoDB connection error:'));

/**************************************************
 * login, auth, sessions, and account creation
 **************************************************/

function authenticate(req, res, next) {
    //make sure cookies isnt undefined
    if (Object.keys(req.cookies).length > 0){
        let u = req.cookie.login.username;
        let key = req.cookie.login.key;
        //validate session key
        if (sessionKeys[u].key == key) {
            next();
        }
        else {
            res.send('BAD AUTH');
        }
    }
    else {
        res.send('BAD AUTH');
    }
}

function updateSessions() {
    let now = Date.now();
    for (e in sessionKeys) {
        if (sessionKeys[e].time < (now - SessionTime)){
        delete sessionKeys[e];
        }
    }
}

/*
add a user to the database. 
the username and password should be sent as POST parameter(s)
*/
app.post('/add/user/', (req,res) => {
    var userObject = {};
    //sometimes JSON are already parsed
    try {
        userObject = JSON.parse(req.body.user);
    } catch (error) {
        userObject = req.body.user;
    }
    name = userObject.username;
    //TODO find other users, ensure that name does not already exist
    User.find( {username : name} ).exec(
        function(error, results){
            if (results.length == 0) {
                var password = userObject.password;
                var salt = crypto.randomBytes(64).toString('base64');
                crypto.pbkdf2(password, salt, cryptIterations, 64, 'sha512',
                    (err,hash) => {
                        if (err) throw err;
                        var user = new User({username: name,
                                             salt: salt,
                                             hash: hash,
                                             currGame: ""});
                        user.save(function (err) { if (err) console.log('an error occured saving'); });
                        res.send("OK");
                    });
            }else{
                res.send("EXISTS");
            }
        }
    );

}
);

/*
login
req: JSON user object
res: incorrect login or "OK"
verifies username and login, and creates a session
*/
app.post('/login/', (req,res) => {
    console.log("verifying user");
    loginCreds = {};
    //sometimes JSON are already parsed
    try {
        loginCreds = JSON.parse(req.body.user);
    } catch (error) {
        loginCreds = req.body.user;
    }
    var u = loginCreds.username;
    var p = loginCreds.password;
    User.find({username:u}).exec(
        function(error,results) {
            //no duplicate users should ever exist
            //if duplicates exist, verify add/user
            if (results.length == 1){
                //verify password
                user = results[0];
                salt = user.salt;
                crypto.pbkdf2(p, salt, cryptIterations, 64, 'sha512',
                (err,hash) => {
                    if (err) throw err;
                    if (user.hash == hash){
                        let sessionKey = Math.floor(Math.random() * 10000);
                        sessionKeys[u] = {key:sessionKey, time:Date.now()};
                        res.cookie("login",
                                    {username: u, key:sessionKey}, 
                                    {maxAge: SessionTime});
                        res.send("OK");
                    }
                });
            } else if (results.length > 1){
                res.send("duplicate users in the database");
            } else {
                res.send("Invalid login");
            }
        });
    });

//TODO use this cookie type to validate sessions
app.get('/username/', (req,res) => {
    var login = req.cookies.login;
    if (login == void(0)){
        res.send("TIMEOUT");
    }else{
        res.send(JSON.stringify(req.cookies.login.username));
        }
    }
);

//set up the current port
const port = 3000;
app.listen(port, () => {
        console.log(`server running at ${port}`);
    }
);
/*****************************************************
 * LOBBY AND GAME CREATION
 *****************************************************/

/**create game request
 * 
 * uses cookies to get client username
 * creates a new game, links it to the client and vice versa
 * responds with the newly created game id
 */
app.post('/createGame/', (req,res) => {
    var login = req.cookies.login;
    if (login == void(0)){
        res.send("TIMEOUT");
        }else{
        var name = req.cookies.login.username;
        //invalidate previous game
        User.find({username:name}).exec(function(error, results){
            if(results.length == 0){
                res.send("TIMEOUT")
            } else {
                var user = results[0];
                if (user.currGame != ""){
                    Game.find({_id:user.currGame}).exec(function(error, results){

                        if (results.length >0){
                            results[0].valid = false;
                            results[0].save(function (err) {
                                if (err) console.log('an error occured saving'); 
                                });
                        }
                    });
                }
            }
        });

        var list = ["000000","000000","000000","000000",
                    "000000","000000","000000"];
        var game = new Game({player1: name, 
                            player2: "",   
                            valid: true,
                            board: list,
                            turn: name
                            });
        game.save(function (err) { if (err) console.log('an error occured saving'); });
        User.find({username:name}).exec(function(error,results) {
            var user = results[0];
            //update the user's current game
            user.currGame = game._id;
            console.log(game._id);
            user.save(function (err) { if (err) console.log('an error occured saving'); });
        });
        res.send("OK");
        }
});

/**get lobby
 * returns a list of all valid games still waiting for players
 */
app.get('/getLobby/', (req, res) => {
    Game.find({valid:true}).exec(function(error,results) {
        res.send(JSON.stringify(results));
    });
});

app.post('/join/:id', (req, res) => {
    var login = req.cookies.login;
    if (login == void(0)){
        res.send("TIMEOUT");
    }else{
        var name = req.cookies.login.username;
        //get the current game and link it to the user
        var id = req.params.id;
        Game.find({_id:id}).exec(function(error, results){
            if (results.length == 0){
                res.send("ERROR");  //game not found
            } else {
                var game = results[0];
                //don't let players join their own game
                if (game.player1 == name){
                    res.send("JOINER IS SAME AS HOST");
                } else {
                    game.player2 = name;
                    game.save(function (err) { if (err) console.log('an error occured saving'); });
                    //invalidate users previous game
                    User.find({username:name}).exec(function(error,results) {
                        //update the user's current game if it exists
                        user = results[0];
                        //invalidate previous game
                        if (user.currGame != ""){
                            Game.find({_id:user.currGame}).exec(function(error, results){
                                if (results.length >0){
                                    results[0].valid = false;
                                    results[0].save(function (err) {
                                        if (err) console.log('an error occured saving'); 
                                        });
                                }
                            });
                        }
                    user.currGame = game._id;
                    user.save(function (err) { if (err) console.log('an error occured saving'); });
                    });
                    res.send("OK");
                }
            }
        });
    }
});

/**redirect
 * 
 * when the user is connected to a game, either waiting
 * in the lobby joining someone else's game from the lobby,
 * both players are redirected, and the game is removed
 * from the lobby list;
 */
app.get('/redirect/', (req, res) => {
    var login = req.cookies.login;
    if (login == void(0)){
        res.send("TIMEOUT");
    }else{
        var name = req.cookies.login.username;
        //get user object
        User.find({username : name}).exec(function(error,results){
            user = results[0];
            //if user has a current game, find the game
            if (user.currGame != ""){
                Game.find({_id : results[0].currGame}).exec(function(gerror,gresults){
                    //if the game exists and has 2 players, res ready
                    if ((gresults.length != 0) && isReady(gresults[0])){
                        game = gresults[0];
                        //if redirecting player1, player 2
                        //has already joined. remove from ready set
                        if (game.player1 == name){
                            game.valid = false;
                        }
                        game.save(function (err) { if (err) console.log('an error occured saving');});
                        res.send("READY");
                    }else{
                        res.send("NOT READY");
                    }
                });
            }else{
                res.send("NOT READY");
            }
        });
    }
});

/**is ready
 * 
 * a small helper function for cleaner redirect code
 * checks if game has two users, in order to
 * redirect them both
 * 
 * @param {*} game a game object
 * @return a boolean, true if game is ready
 */
function isReady(game){
    return (game.valid) && (game.player1 != "") && (game.player2 != "");
}

/*
function timeOutGames(){
    Game.find({}).exec(function(error,results){
        for(game in results){
            var name = results[game].player1;
            User.find({username:name}).exec(function(uerror,uresults){
                if(uresults.length)
            });
        }
    });
}
*/


/******************************************************
 * Below this is game logic!
 * ****************************************************/

 /**get request for the game that the user is currently playing
  * 
  * looks up user with cookie data,
  * looks up the game with user data,
  * then sends the game back to the client
  */
app.get('/getBoard/', (req, res) => {
    var login = req.cookies.login;
    console.log(req.cookies.login);
    if (login == void(0)){
        res.send("TIMEOUT");
    }
    else{
        var name = req.cookies.login.username;
        User.find({username:name}).exec(function(error,results) {
            var user = results[0];
            Game.find({_id : user.currGame}).exec(function(error,results) {
                res.send(JSON.stringify(results[0]));
            });
        });
    }
});

//TODO ensure turns
/**move
 * 
 * verifies the client is in the game,
 * and that it is the clients turn.
 * modifies the game board based on the
 * column number and user passed in the url request
 */
app.post('/move/:id/:col', (req, res) => {
    var login = req.cookies.login;
    if (login == void(0)){
        res.send("TIMEOUT");
    }else{
        var id = req.params.id;
        var col = req.params.col;
        var name = req.cookies.login.username;
        Game.find({_id:id}).exec(function(error,results){
            //ensure valid id
            if (results.length != 0){
                var game = results[0];
                //get the number of the player making the move
                var playerNum = "";
                //check if player matches a current game player,
                //and that it is that players turn
                //if so make a move
                if ((game.player1 == name)
                && (game.player1 == game.turn)){
                    playerNum = "1";
                    game.board = modifyBoard(game.board, parseInt(col), playerNum);
                    game.turn = game.player2;
                    res.send("OK");
                } else if ((game.player2 == name)
                && (game.player2 == game.turn)){
                    playerNum = "2";
                    game.board = modifyBoard(game.board, parseInt(col), playerNum);
                    game.turn = game.player1;
                    res.send("OK");
                } else {
                    res.send("INVALID USER");
                }
                game.save(function (err) { if (err) console.log('an error occured saving'); });
            }else{
                res.send("ERROR");
            }
        });
    }
});

/**modify board
 * 
 * creates a new board based on the previous board
 * state, and the changes to the board
 * params: the preivous array, the idx of column to modify,
 * and the player making the move
 * returns: a new array
 */
function modifyBoard(board, colNum, playerNum){
    var newArray = [];
    for (i in board){
        var col = board[i];
        if (i == colNum){
            newArray.push(insertMove(col,playerNum));
        }else{
            newArray.push(col);
        }
    }
    return newArray;
}

/**insert move
 * 
 * modifies a string to change the
 * leftmost non-"0" to the playerNum
 * 
 * takes two strings, the column representation,
 * and the player number to insert
 */
function insertMove(col, playerNum){
    //if column is already full, return
    if (!(col.includes('0'))){
        return col;
    }
    //find the first 0
    i = col.indexOf('0');
    return col.substring(0,i) + playerNum + col.substring(i+1);
}

/**get Winner
 * 
 * checks to see if the client who sent the
 * request has won the game
 */
app.get('/getWinner/:id', (req, res) => {
    var login = req.cookies.login;
    if (login == void(0)){
        res.send("TIMEOUT");
    }else{
        var name = req.cookies.login.username;
        var id = req.params.id;
        Game.find({_id : id}).exec(function(error,results) {
            var game = results[0];
            //get the players number identifier
            player1Win = checkWinner(game.board, "1");
            player2Win = checkWinner(game.board, "2");
            if (game.player1 == name){
                if(player1Win){
                    res.send("WIN");
                }else if(player2Win){
                    res.send("LOSE");
                }else if(boardIsFull(game.board)){
                    res.send("DRAW");
                }
                else{
                    res.send("OK");
                }
            } 
            else if (game.player2 == name){
                if(player2Win){
                    res.send("WIN");
                }else if(player1Win){
                    res.send("LOSE");
                }else if(boardIsFull(game.board)){
                    res.send("DRAW");
                }
                else{
                    res.send("OK");
                }
            } 
        });
    }
});

/*****************************************************
 * check for four in a row (difficult to deduplicate)
 *****************************************************/

 /**checkWinner
  * 
  * checks rows, cols, and diagonals for four in a row
  * 
  * @param {*} board a list of Strings representing cols
  * @param {*} playerNum String, the player in question
  */
function checkWinner(board, playerNum){
    //check every possible place 4 in a row could be
    if(checkCols(board,playerNum)
    || checkRows(board,playerNum)
    || checkTopDown(board,playerNum)
    || checkLeftDown(board,playerNum)
    || checkBottomUp(board,playerNum)
    || checkLeftUp(board,playerNum)){
        return true;
    }
    return false;
}
/**board is full
 * 
 * checks to see if any col contains a 0.
 * @param {*} board a list of strings representing cols
 * if players can still make moves, return false
 * else true
 */
function boardIsFull(board){
    for (col in board){
        if (board[col].includes("0")){
            return false;
        }
    }
    return true;
}

/**check cols
 * 
 * checks for the index of four in a row in
 * each column
 * 
 * @param {*} board a list of strings representing columns
 * @param {*} playerNum String the player in question of winning
 * @return boolean, true if four in a row found
 */
function checkCols(board, playerNum){
    //four in a row string
    var playerString = playerNum.repeat(4);
    for(col in board){
        if(board[col].indexOf(playerString) >= 0){
            return true;
        }
    }return false;
}

/**check rows
 * 
 * gathers a list of rows
 * and check for four in a row
 * 
 * @param {*} board a list of strings representing columns
 * @param {*} playerNum String the player in question of winning
 * 
 * @return boolean, true if four in a row found
 */
function checkRows(board, playerNum){
    //four in a row string
    var playerString = playerNum.repeat(4);
    var rows = [];
    var row = 0;
    //gather the rows
    for(row; row < board[0].length; row++){
        var rowStr = "";
        for (col in board){
            rowStr += board[col][row];
        }
        rows.push(rowStr);
    }
    //if any row contains four in a row str
    for (i=0; i<rows.length; i++){
        if (rows[i].toString().indexOf(playerString) >= 0){
            return true;
        }
    }
    return false;
}

/**check top down
 * 
 * gathers all the diagonals from the top, down to the right
 * "rows" and checks if there is 4 in a row
 * 
 * @param {*} board a list of strings representing columns
 * @param {*} playerNum String the player in question of winning
 * 
 * @return boolean, true if four in a row found
 */
function checkTopDown(board, playerNum){
    //four in a row string
    var playerString = playerNum.repeat(4);

    var diags = [];
    startCol = 0;
    startRow = 0;
    while (startCol < board.length){
        col = startCol;
        row = startRow;
        diagString = "";
        while (col< board.length && row < board[0].length){
            diagString += board[col][row];
            col++;
            row++;
        }
        diags.push(diagString);
        startCol++;
    }
    for (diag in diags){
        if (diags[diag].toString().indexOf(playerString) >= 0){
            return true;
        }
    }
    return false;
}

/**check left down
 * 
 * gathers all the diagonals from the first col, down to the right
 * "rows" and checks if there is 4 in a row
 * 
 * @param {*} board a list of strings representing columns
 * @param {*} playerNum String the player in question of winning
 * 
 * @return boolean, true if four in a row found
 */
function checkLeftDown(board, playerNum){
    //four in a row string
    var playerString = playerNum.repeat(4);

    var diags = [];
    startCol = 0;
    startRow = 0;
    while (startRow < board[0].length){
        col = startCol;
        row = startRow;
        diagString = "";
        while (col< board.length && row < board[0].length){
            diagString += board[col][row];
            col++;
            row++;
        }
        diags.push(diagString);
        startRow++;
    }
    for (diag in diags){
        if (diags[diag].toString().indexOf(playerString) >= 0){
            return true;
        }
    }
    return false;
}

/**check bottom up
 * 
 * gathers all the diagonals from the last row, up to the right
 * and checks if there is 4 in a row
 * 
 * @param {*} board a list of strings representing columns
 * @param {*} playerNum String the player in question of winning
 * 
 * @return boolean, true if four in a row found
 */
function checkBottomUp(board, playerNum){
    //four in a row string
    var playerString = playerNum.repeat(4);

    var diags = [];
    startCol = 0;
    startRow = board[0].length-1;
    while (startCol < board.length){
        col = startCol;
        row = startRow;
        diagString = "";
        while (col< board.length && row >= 0){
            diagString += board[col][row];
            col++;
            row--;
        }
        diags.push(diagString);
        startCol++;
    }
    for (diag in diags){
        if (diags[diag].toString().indexOf(playerString) >= 0){
            return true;
        }
    }
    return false;
}

/**check left up
 * 
 * gathers all the diagonals from the first col, up to the right
 * and checks if there is 4 in a row
 * 
 * @param {*} board a list of strings representing columns
 * @param {*} playerNum String the player in question of winning
 * 
 * @return boolean, true if four in a row found
 */
function checkLeftUp(board, playerNum){
    //four in a row string
    var playerString = playerNum.repeat(4);

    var diags = [];
    startCol = 0;
    startRow = board[0].length-1;
    while (startRow >= 0){
        col = startCol;
        row = startRow;
        diagString = "";
        while (col< board.length && row >= 0){
            diagString += board[col][row];
            col++;
            row--;
        }
        diags.push(diagString);
        startRow--;
    }
    for (diag in diags){
        if (diags[diag].toString().indexOf(playerString) >= 0){
            return true;
        }
    }
    return false;
}

/**********************************************************
 * CHAT FUNCTION
 *********************************************************/

/**send Message
 * 
 * creates a new chat message
 */
app.post('/sendMessage/', (req,res) => {
    var login = req.cookies.login;
    if (login == void(0)){
        res.send("TIMEOUT");
    }else{
        var name = req.cookies.login.username;
        let msgObject = JSON.parse(req.body.message);
        var msg = new Chat({username: name,
                            message: msgObject.message,
                            gameID: msgObject.gameID});
        msg.save(function (err) { if (err) console.log('an error occured saving'); });
        console.log(msg);
        res.send();
    }
});

/**get chat
 * 
 * return all chat messages associated with the
 * game id passed in the url request
 */
app.get('/getChat/:id', (req,res) => {
    var id = req.params.id;
    Chat.find({gameID: id}).exec(function(error,results){
        res.send(JSON.stringify(results));
    });
});