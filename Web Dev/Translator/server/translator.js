
/*
Adam Cunningham
csc337 project 7
summer 2020
This is some javascript which reads translation dictionaries
and creates mappings. Given the url path word+otherword
it will display on the screen a string. The string will be a
(very) rough translation of the end of the url path.

It supports translations:
english to german and spanish
spanish to german and english
german to english and spanish

known issues: german to spanish is not the inverse of spanish to german,
this is not a bug, it is a deliberate design choice. No data was provided
to directly translate the two languages, and so english was used as a transitional
state between the two.
*/

//modules
/*
const http = require('http');
*/
var url = require('url'); //for pathname grabbing
const fr = require('fs'); //import module defining fileReader

//express
const express = require('express');
const e = require('express');
const { send } = require('process');
const app = express();
const port = 3000;

//----------------serverstuff

app.use(express.static('public_html'));

/*
call to app.get
handles a request to translate input
replies with the html file
this serves a dynamic response
*/
app.get('/dynamic/:requestType/:translationType/:untranslatedString',
   (req,res) => {
        if(req.params.requestType === "translate"){
            res.send(translate(req.params.translationType,
                               req.params.untranslatedString));
        }
    }
    
);

app.get('/:requestType/:translationType/:untranslatedString',
   (req,res) => {
        if(req.params.requestType === "translate"){
           fr.readFile('./public_html/index.html', "utf-8",
               function (err,data){
                    if (err) { return console.log(err); }
                    //modify the html textarea values to contain io
                    let input = req.params.untranslatedString;
                    output = translate(req.params.translationType, input)
                    var result = data.replace(/dummyInput/g, input);
                    result = result.replace(/dummyOutput/g, output);
                    res.send(result);
               }
           );
        }
    }
    
);

/*
defines how to reply to the translation request of an empty string
*/
app.get('/translate/:translationType/',(req,res) => {
        res.send('empty translation string');
    }
);

/*
call to listen at port,
while server is running app is listening
*/
app.listen(port,
    function () {
    console.log('Example app listening at http://localhost:${port}');
    }
);

//instantiate translationDictionary
tDict ={"e2s":{}, "s2e":{},
        "e2g":{}, "g2e":{},
        "g2s":{}, "s2g":{}};
//populate with data
populateTranslationDictionary();

/*
populate Translation Dictionary
param: none
return: none
result: four mappings will be created, and stored in tDict
english->spanish
spanish->english
english->german
german->english
*/
function populateTranslationDictionary(){
    createBidirectionalMap("Spanish.txt", tDict["e2s"], tDict["s2e"]);
    createBidirectionalMap("German.txt", tDict["e2g"], tDict["g2e"]);
}

/*create Bidirectional Map
param: a filename (.txt), "map" a reference to an object, 
"reverseMap" a reference to a second object
return: none
result: the passed filename, if able, is open and read
parsed file is read as lowercase
the file is parsed with regex matching "w*\tw*"
a mapping is created in map from first w to second w
a mapping is created in reverseMap from second w to first w
*/
function createBidirectionalMap(fileName, map, reverseMap){
    var text = new fr.readFile(fileName, 'utf8', function(err, data) {
        if (err) throw err;
        //get rid of stuff in parens
        var result = data.replace(/\([^)]*\)|\[[^\]]*\]/g, "");
        //extract array of "word \tab word" (all lower)
        var re = new RegExp("([a-z]|[A-Z]| )*\t([a-z]|[A-Z]| |[^\x00-\x7F])*", "g");
        result = result.toLowerCase();
        var strList = result.match(re);
        var i = 0;
        for(i = 0; i < strList.length; i++){
            //define the word and its translation
            let currLine = strList[i].split('\t');
            let a = currLine[0];
            let b = currLine[1];
            //first results are more commonly better translations, don't overwrite
            //skip ones with weird ascii
            if (!/[^\x00-\x7F]/.test(b)){
                //add a->b in map, and b->a in reverseMap
                map[a] = b;
                reverseMap[b] = a;
            }
        }
    });
}


/*
translate
param: type of translation ie e2s english to spanish, 
text the text to be translated
return: a translated string
determines which language mapping to use,
iteratively takes words from the text, gets val from map[word]
and concatenates val to newString translation
*/
function translate(translationType, text){
    //split on +
    text = text.replace(/\+/g,' ');
    var stringList = text.split(' ');
    var translation = "";
    var i = 0;
    for (i = 0; i < stringList.length; i++){
        let currWord = stringList[i];
        currWord = lookup(translationType, currWord);
        //update translation
        translation += currWord + " ";
    }
    return translation;
}

/*
lookup
param: the type of translation to perform,
a string (lower, no whitespace) to perform it on
return: the translated string
performs a case-switch to determine the type of lookup to perform
*/
function lookup(translationType, word){
    //handle e2e ie no translation
    if (translationType[0] == translationType[2]){
        return word;
    }
    switch(translationType){
        case "g2s":
            word = transitiveLookup(tDict["g2e"], tDict["e2s"], word);
            break;
        case "s2g":
            word = transitiveLookup(tDict["s2e"], tDict["e2g"], word);
            break;
        default:
            word = directLookup(tDict[translationType], word);
    }
    return word;
}

/*
transitiveLookup
params: object dictAB, object dictBC, string word, which should be a property of dictAB
returns: when passed a mapping to string A, returns string C, if A is in dictAB
and B is in dict BC
result: handles the edge case of german to spanish and spanish to german
by translating from one to english, and then from english to the other
*/
function transitiveLookup(dictAB,dictBC,word){
    word = directLookup(dictAB, word);
    word = directLookup(dictBC, word);
    return word;
}

/*
directLookup
param: object, string (possibly property of the object) to get value of
return: the value of the object.string if it exists
looks up a value in an object, with some error handling
*/
function directLookup(dict,word){
    if (/\s/.test(word) || word == ""){
        return word;
    }
    if (!(word in dict)){
        //bad input
        word = '?';
    }else{
        //translate
        word = dict[word];
    }
    return word;
}