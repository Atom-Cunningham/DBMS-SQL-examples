
/*
adam cunningham
csc337
translator function
this controlls how the html elements in the translator
are updated. It uses express, and sends requests to a
server. the server does the actual logic
*/

//globals
var inputType = "e";
var outputType = "e";

/*
set defaults
param: none
return: none
sets the default langage types to whatever they are currently at
english by default
sets the outputText to ?
*/
function setDefaults(){
    inputType = document.getElementById("inputLanguage").value;
    outputType = document.getElementById("outputLanguage").value;
    document.getElementById("outputText").value = '?';
    document.getElementById("inputText").value = '';
}
setDefaults();

/*
change input language
param: none
return: none
gets the value of the drop down selection, and sets
the 
*/
function changeInputLanguage(){
    inputType = document.getElementById("inputLanguage").value;
    updateOutput();
}

/*
change output language
param: none
return: none
gets the value of the drop down selection, and sets
the 
*/
function changeOutputLanguage(){
    outputType = document.getElementById("outputLanguage").value;
    updateOutput();
}

/*
update output
param: none
return: none
gets the value in the input box and tests it for
empty length, if empty, output box is set to ?
if the length is not empty, then a server request
is made to translate the string
the server request function will update the value
*/
function updateOutput(){
    let inputText = document.getElementById("inputText").value;
    let outputText = document.getElementById("outputText");
    if (inputText.length != 0){
        setupRequest();
    }else{
        outputText.value = '?';
    }
    
}

/*
setuprequest function from the lecture

*/
function setupRequest() {
    var httpRequest = new XMLHttpRequest();
    if (!httpRequest) {
        alert('Error');
        return false;
    }

    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {
                var output = document.getElementById("outputText");
                output.value = httpRequest.responseText;
            } else { alert('ERROR'); }
        }
    }

    let url = 'http://localhost:3000' + getRequestString();
    console.log(url);
    httpRequest.open('GET', url);
    httpRequest.send();
}

/*
get request string
builds a request string to dymaically recieve the
translation of text from the server
param: none
return: a path of the form
/translate/?2?/untranslated+string
*/
function getRequestString(){
    //build the requestString
    //translate type request
    let requestString = "/translate";
    //add translation type
    requestString += '/' + inputType + "2" + outputType;
    //add untranslated text
    let inputText = document.getElementById("inputText").value;
    inputText.toLowerCase()
    inputText = inputText.replace(/\s/g, '+');
    requestString += '/' + inputText;
    console.log("request string: "+requestString);
    //add dynamic tag
    return '/dynamic' + requestString;
}