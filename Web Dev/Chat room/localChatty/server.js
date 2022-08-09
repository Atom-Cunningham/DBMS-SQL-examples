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
db.on('open', function(){
    db.dropDatabase(function(err, result){;});
});

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

//TODO create messages
//test messages
/*
var m1 = new ChatMessage( { time: 1, alias: "player1", message: "hello world"});
var m2 = new ChatMessage( { time: 2, alias: "player2", message: "hello worlds"});
m1.save(function (err) { if (err) console.log('an error occured saving'); });
*/

//define the location of the html,css,js
app.use(express.static('public_html'));

db.on('error', console.error.bind(console, 'MongoDB connection error:'));

app.get('/chats', (req, res) => {
        //TODO When getting a request,
        // the server should retrieve all
        // messages it has stored in the mongoDB database
        // and return them to the user
        // so that they can be displayed
        var msgSchema = mongoose.model('ChatMessage', ChatMessageSchema);
        msgSchema.find({}).exec(function(error,results) {
            res.send(JSON.stringify(results));
            }
        )
    }
);

app.post('/chats/post', (req, res) => {
        //TODO These requests will be sent from the client,
        //and include an alias and message parameter in
        // the body of the request. The server should save 
        // the alias, message, and a timestamps 
        // (so that order can be preserved) into the database,
        // for later retrieval via a /chats GET request.
        console.log(req.body);
        let msgObject = JSON.parse(req.body.message);
        var msg = new chatMessage(msgObject);
        msg.save(function (err) { if (err) console.log('an error occured saving'); });
        res.end("");
    }
);

//set up the current port
const port = 3000;
app.listen(port, () => {
        console.log('server running at ${port}');
    }
);

