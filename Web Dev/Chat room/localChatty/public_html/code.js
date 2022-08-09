setInterval(update, 1000);

//retrieve chat information
function update() {
    console.log("update called");
    //create a new httpRequest
    var httpRequest = new XMLHttpRequest();
    if (!httpRequest) { return false; }

    //define ready state behavior
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {
                var result = "";
                var results = JSON.parse(httpRequest.responseText);
                //sort by time
                results.sort((a, b) => (a.time > b.time) ? 1 : -1);
                for (i in results){
                    result += '<b>'+results[i].alias+'</b>' + ': ';
                    result += results[i].message + "</br>";
                }
                let chat = document.getElementById("chat");
                chat.innerHTML = result;
            } else { alert('Response failure'); }
        }
    }
    
    //send a get request to /chats
    let url = '/chats';
    httpRequest.open('GET', url);
    httpRequest.send();
}

//sends a message to the server
//TODO SEND, Currently identical to update
function sendMessage() {
        //create a new httpRequest
        var httpRequest = new XMLHttpRequest();
        if (!httpRequest) { return false; }

    //define ready state behavior
    httpRequest.onreadystatechange = () => {
        if (httpRequest.readyState === XMLHttpRequest.DONE) {
            if (httpRequest.status === 200) {
                console.log('attempted to add message');
            } else { alert('Response failure'); }
        }
    }

    //set up request message
    let name = document.getElementById("aliasInput");
    let input = document.getElementById("messageInput");
    let date = new Date();
    let result = {time: date.getTime(), alias: name.value, message:input.value}

    //send a get request to /chats
    let url = "/chats/post";
    httpRequest.open('POST', url, true);
    httpRequest.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    httpRequest.send("message=" + JSON.stringify(result));
}

