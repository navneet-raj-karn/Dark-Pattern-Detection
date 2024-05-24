

document.addEventListener('DOMContentLoaded', function () {
    var clickCount = 0;
    document.getElementById("b1").onclick = function () {
        if (clickCount % 2 === 0) {
            chrome.tabs.executeScript({
                file: 'highlighter.js'
            });
        } else {
            document.getElementById("b_image").src = "button_off.svg";
            chrome.tabs.reload();
        }
        clickCount++;
        var randomNumber = Math.floor(Math.random() * 10) + 1;
        document.getElementById('count').textContent = randomNumber;
    }


    chrome.runtime.onMessage.addListener(function (message) {
        if (message.action === 'sendMatchCounter') {
            document.getElementById('count').textContent = message.matchCounter;
        }
    });
});


function displayMatchingURLs() {
    chrome.tabs.query({ active: true, currentWindow: true }, function (tabs) {
        var currentURL = tabs[0].url;

        // Malicious scan API endpoint
        var maliciousScanAPI = "http://localhost:1100/scanweb";

        // Fetch data from the API
        fetch(maliciousScanAPI, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ url: currentURL }),
        })
        .then(response => response.json())
        .then(result => {
            // Log the entire result to the console
            console.log(result);

            // Reference to the output div in popup.html
            var outputDiv = document.getElementById("output");
            // Clear previous content
            outputDiv.innerHTML = "";
            // Display result in the output div
            var resultMessage = document.createElement('p');

            // Check if the website is marked as a scam
            if (result.category) {
                resultMessage.textContent = "Risk Assesment : " + result.category;
                // Add additional logic or UI changes for scam websites if needed
            } 
            outputDiv.appendChild(resultMessage);

            var score = result.riskScore;
            console.log(score)
            if(score>=0 && score<=100){
                var SiteScore = 100 - score;
                var SiteScoreMessage = document.createElement('p');
                SiteScoreMessage.textContent= "Site Score : "+ SiteScore;
                outputDiv.appendChild(SiteScoreMessage);
            }
            
        })
        .catch(error => console.error("Error:", error));
    });
}

// Run the displayMatchingURLs function when the popup is opened
displayMatchingURLs();

// Add event listener for the scan and review button



