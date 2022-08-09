/*
Adam Cunningham
Csc337 project 8
this is a simple chatroom server
it uses MongoDB to store chat comments from users
with middleman transfer via JSON
current live server @http://161.35.62.182/
listening @80
*/
//imported/linked modules
const mongoose = require('mongoose')
const express = require('express')
const parser = require('body-parser');
const { Http2ServerRequest } = require('http2');

const app = express();
app.use(parser.json());
app.use(parser.urlencoded({ extended: true }));

//set up default mongoose connection
const db = mongoose.connection;
const mongoDBURL = 'mongodb://127.0.0.1/auto';

mongoose.connect(mongoDBURL, { useNewUrlParser: true });

//clear the database on startup!
//comment this code out to save mongo data between shutdowns
/*
db.on('open', function(){
    db.dropDatabase(function(err, result){;});
});
*/

//TODO setup schema
//define schema type as mongoose.schema
var Schema = mongoose.Schema;
/*
schema design
a js object with a timestamp
username
and message
*/
var ChatMessageSchema = new Schema({
  time: Number,
  alias: String,
  message: String
});
var chatMessage = mongoose.model('ChatMessage', ChatMessageSchema );

//define the location of the html,css,js
app.use(express.static('public_html'));

//define database error behavior
db.on('error', console.error.bind(console, 'MongoDB connection error:'));

/*
When getting a request,
the server retrieves all
messages it has stored in the mongoDB database
and returns them to the user
so that they can be displayed
*/
app.get('/chats', (req, res) => {
        var msgSchema = mongoose.model('ChatMessage', ChatMessageSchema);
        msgSchema.find({}).exec(function(error,results) {
            res.send(JSON.stringify(results));
            }
        )
    }
);

/*
These requests will be sent from the client,
and include an alias and message parameter in
the body of the request. The server should save 
the alias, message, and a timestamps 
(so that order can be preserved) into the database,
for later retrieval via a /chats GET request.
*/
app.post('/chats/post', (req, res) => {
        console.log(req.body);
        let msgObject = JSON.parse(req.body.message);
        var msg = new chatMessage(msgObject);
        msg.save(function (err) { if (err) console.log('an error occured saving'); });
        res.end("");
    }
);

//set up the current port
const port = 80;
app.listen(port, () => {
        console.log(`server running at ${port}`);
    }
);

